package edu.brown.cs.student.main.ACS;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.Exceptions.DataRetrievalException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ACSAPIDataSource implements APIDataSource{
  private Map<String, String> stateMap;

  @Override
  public InternetAccessData getData(String state, String county)
      throws IOException, URISyntaxException, InterruptedException, DataRetrievalException {
    return produceData(state, county);
  }

  private InternetAccessData produceData(String state, String county)
      throws IOException, URISyntaxException, InterruptedException, DataRetrievalException {
    // Instantiate the state to state code HashMap and call for it to be filled.
    this.stateMap = new HashMap<>();
    this.getStateCodes();
    // get the county code from the state code and the county given (lowercase to remove
    // case-sensitivity)
    String stateCode = this.stateMap.get(state.toLowerCase());
    String countyCode = getCountyCode(stateCode, county.toLowerCase(), state.toLowerCase());
    String percentage = getPercentageData(stateCode, countyCode);
    LocalDate currentDate = LocalDate.now();
    LocalTime currentTime = LocalTime.now();
    return new InternetAccessData(state, county, currentDate, currentTime, percentage);
  }

  private void getStateCodes() throws URISyntaxException, IOException, InterruptedException {
    // Create an instance of a request. This is where we get the link to where we want our JSON
    // file data.
    HttpRequest buildStateCodeApiRequest =
        HttpRequest.newBuilder()
            .uri(new URI("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*"))
            .GET()
            .build();
    // TODO: urisyntaxexception
    // Send that API request then store the response in this variable.
    HttpResponse<String> sentStateAPIRequest =
        HttpClient.newBuilder()
            .build()
            .send(buildStateCodeApiRequest, HttpResponse.BodyHandlers.ofString());
    // TODO: interrupted exception
    // Save the API response as a String
    String codes = sentStateAPIRequest.body();
    Moshi moshi = new Moshi.Builder().build();
    // Create a JsonAdapter, and turn the Json file into a HashMap
    Type types = Types.newParameterizedType(List.class, List.class, String.class);
    JsonAdapter<List<List<String>>> adaptedCodes = moshi.adapter(types);
    List<List<String>> codesMatrix = adaptedCodes.fromJson(codes);
    // TODO: ioexception
    // Create a boolean to skip over the first iteration (the first iteration is [Names, State],
    // thus since State is not an integer it must be skipped.
    boolean firstIteration = true;
    // assert that the codeMatrix is not null (should never happen)
    assert codesMatrix != null;
    if (!codesMatrix.isEmpty()) {
      for (List<String> entries : codesMatrix) {
        if (firstIteration) {
          firstIteration = false;
          continue;
        }
        // Enter the entries into the stateCodes HashMap. Use toLowerCase() to make the entries
        // case-insensitive.
        this.stateMap.put(entries.get(0).toLowerCase(), entries.get(1));
      }
    }
  }

  private String getCountyCode(String stateCode, String county, String state)
      throws URISyntaxException, IOException, InterruptedException, DataRetrievalException {
    // Create a request at the URL where the API is located.
    if (stateCode == null) {
      throw new DataRetrievalException();
    }
    HttpRequest buildCensusAPIRequest =
        HttpRequest.newBuilder()
            .uri(
                new URI(
                    "https://api.census.gov/data/2021/acs/acs1/subject/"
                        + "variables?get=NAME,S2802_C03_022E&for=county:*&in=state:"
                        + stateCode))
            .GET()
            .build();

    // Send that API request then store the response in this variable.
    HttpResponse<String> sentCountyAPIRequest =
        HttpClient.newBuilder()
            .build()
            .send(buildCensusAPIRequest, HttpResponse.BodyHandlers.ofString());

    // Save the API response as a string.
    String countyCodes = sentCountyAPIRequest.body();
    Moshi moshi = new Moshi.Builder().build();
    // Create a JsonAdapter, and turn the Json file into a HashMap
    Type types = Types.newParameterizedType(List.class, List.class, String.class);
    JsonAdapter<List<List<String>>> adaptedCodes = moshi.adapter(types);
    List<List<String>> codesMatrix = adaptedCodes.fromJson(countyCodes);
    // check first if the List<List<String>> from the Json is empty
    assert codesMatrix != null;
    if (!codesMatrix.isEmpty()) {
      // for every entry, if the county name given is equal to an entry, then we will return that
      // entry.
      for (List<String> entries : codesMatrix) {
        String countyName = entries.get(0).toLowerCase();
        if (countyName.equals(county + ", " + state)) {
          return entries.get(3);
        }
      }
    }
    // TODO: Figure out something to return if no entry matched. Maybe an error like in the gearup
    // code? Also read the google doc for info as well
    return sentCountyAPIRequest.body();
  }

  private String getPercentageData(String stateCode, String countyCode)
      throws URISyntaxException, IOException, InterruptedException, DataRetrievalException {
    // TODO: Return something better here, as this would be an error.
    if (stateCode == null || countyCode == null) {
      throw new DataRetrievalException();
    }
    HttpRequest buildDataAPIRequest =
        HttpRequest.newBuilder()
            .uri(
                new URI(
                    "https://api.census.gov/data/2021/acs/acs1/subject/variables?get=NAME,S2802_C03_022E&for=county:"
                        + countyCode
                        + "&in=state:"
                        + stateCode))
            .GET()
            .build();

    // Send that API request then store the response in this variable.
    HttpResponse<String> sentDataAPIRequest =
        HttpClient.newBuilder()
            .build()
            .send(buildDataAPIRequest, HttpResponse.BodyHandlers.ofString());

    // Save the API response as a string.
    String dataCodes = sentDataAPIRequest.body();
    Moshi moshi = new Moshi.Builder().build();
    // Create a JsonAdapter, and turn the Json file into a HashMap
    Type types = Types.newParameterizedType(List.class, List.class, String.class);
    JsonAdapter<List<List<String>>> adaptedCodes = moshi.adapter(types);
    List<List<String>> codesMatrix = adaptedCodes.fromJson(dataCodes);
    assert codesMatrix != null;
    List<String> data = codesMatrix.get(1);
    String percentage = data.get(1);
    // TODO: Check if the percentage is something crazy as well
    if (percentage == null) {
      throw new DataRetrievalException();
    }
    return percentage;
  }

}
