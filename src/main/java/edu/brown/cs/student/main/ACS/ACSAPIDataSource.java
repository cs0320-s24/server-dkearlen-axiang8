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

/**
 * @author devonkearleng
 * @version 1.0 - ACSAPIDataSource is the direct point of contact with the ACS API. It processes and
 *     fetches any data needed by the user, throwing errors in the process if any errors occur.
 */
public class ACSAPIDataSource implements APIDataSource {
  private Map<String, String> stateMap;

  public ACSAPIDataSource() {
    // Instantiate the state to state code HashMap
    this.stateMap = new HashMap<>();
  }
  /**
   * @param state - State given by the user
   * @param county - County given by the user
   * @return - Returns an InternetAccessData, a record that holds all of the relevant data.
   */
  @Override
  public InternetAccessData getData(String state, String county)
      throws IOException, URISyntaxException, InterruptedException, DataRetrievalException {
    return produceData(state, county);
  }
  /**
   * @param state - State given by the user
   * @param county - County given by the user
   * @return - Returns an InternetAccessData, a record that holds all of the relevant data.
   */
  private InternetAccessData produceData(String state, String county)
      throws IOException, URISyntaxException, InterruptedException, DataRetrievalException {
    // Call for the stateMap to be filled if empty.
    if (this.stateMap.isEmpty()) {
      this.getStateCodes();
    }
    // get the county code from the state code and the county given (lowercase to remove
    // case-sensitivity)
    String stateCode = this.stateMap.get(state.toLowerCase());
    String countyCode = getCountyCode(stateCode, county.toLowerCase(), state.toLowerCase());
    String percentage = getPercentageData(stateCode, countyCode);
    LocalDate currentDate = LocalDate.now();
    LocalTime currentTime = LocalTime.now();
    return new InternetAccessData(state, county, currentDate, currentTime, percentage);
  }

  /**
   * getStateCodes() is a method that will fill the stateMap HashMap if it is not filled (basically
   * on first search)
   */
  private void getStateCodes() throws URISyntaxException, IOException, InterruptedException {
    // Create an instance of a request. This is where we get the link to where we want our JSON
    // file data.
    HttpRequest buildStateCodeApiRequest =
        HttpRequest.newBuilder()
            .uri(new URI("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*"))
            .GET()
            .build();
    // Send that API request then store the response in this variable.
    HttpResponse<String> sentStateAPIRequest =
        HttpClient.newBuilder()
            .build()
            .send(buildStateCodeApiRequest, HttpResponse.BodyHandlers.ofString());
    // Save the API response as a String
    String codes = sentStateAPIRequest.body();
    Moshi moshi = new Moshi.Builder().build();
    // Create a JsonAdapter, and turn the Json file into a HashMap
    Type types = Types.newParameterizedType(List.class, List.class, String.class);
    JsonAdapter<List<List<String>>> adaptedCodes = moshi.adapter(types);
    List<List<String>> codesMatrix = adaptedCodes.fromJson(codes);
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

  /**
   * @param stateCode - The stateCode given by the stateMap for the state given by the user
   * @param state - The state given by the user
   * @param county - The county given by the user
   * @return - Returns a county code for the given county.
   */
  private String getCountyCode(String stateCode, String county, String state)
      throws URISyntaxException, IOException, InterruptedException, DataRetrievalException {
    // If the stateCode is null, a DataRetrievalException should be thrown, as the data cannot be
    // found for given input.
    if (stateCode == null) {
      throw new DataRetrievalException();
    }
    // Create a request at the URL where the API is located.
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
    return sentCountyAPIRequest.body();
  }

  /**
   * @param stateCode - stateCode for the API given by the stateMap HashMap for the given state.
   * @param countyCode - countyCode for the API given by getCountyCode.
   * @return - A string representing the percentage that has access to internet.
   */
  private String getPercentageData(String stateCode, String countyCode)
      throws URISyntaxException, IOException, InterruptedException, DataRetrievalException {
    // Checks if the stateCode or the countyCode is null. If so, throw a DataRetrievalException.
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
    // If percentage is null, we should throw a DataRetrievalException, as nothing coul dbe found.
    if (percentage == null) {
      throw new DataRetrievalException();
    }
    // If the percentage is a infeasible number, we should throw a URISyntaxException, since the API
    // gave bad data
    // This message means nothing, as it is handled in BroadbandHandler.
    if (Double.parseDouble(percentage) < 0 || Double.parseDouble(percentage) > 100) {
      throw new URISyntaxException("bad data", "bad data");
    }
    return percentage;
  }
}
