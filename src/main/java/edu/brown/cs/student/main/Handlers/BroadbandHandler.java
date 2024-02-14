package edu.brown.cs.student.main.Handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
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
import java.util.Set;
import spark.Request;
import spark.Response;
import spark.Route;

public class BroadbandHandler implements Route {

  private Map<String, String> stateMap;

  @Override
  public Object handle(Request request, Response response) throws Exception {
    // Put the arguments into a set of Strings.
    Set<String> params = request.queryParams();
    String state = request.queryParams("state");
    String county = request.queryParams("county");
    this.stateMap = new HashMap<>();
    // transfer the state codes into a hashmap.
    getStateCodes();
    // get the county code from the state code and the county given (lowercase to remove
    // case-sensitivity)
    String stateCode = this.stateMap.get(state.toLowerCase());
    String countyCode = getCountyCode(stateCode, county.toLowerCase(), state.toLowerCase());
    String percentage = getData(stateCode, countyCode);
    LocalDate currentDate = LocalDate.now();
    LocalTime currentTime = LocalTime.now();
    // Turn the LocalDate and LocalTime objects into a string to make it easier on the adapter.
    String date = currentDate.toString();
    String time = currentTime.toString();
    Map<String, Object> responseMap = new HashMap<>();
    // TODO: Think about putting this in a helper method.
    // Use Moshi to turn the Map into a Json object to display on the web page.
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    responseMap.put("Date", date);
    responseMap.put("Time", time);
    responseMap.put("County", county);
    responseMap.put("Percentage with Access to Internet", percentage);
    responseMap.put("State", state);
    // Return the response map in Json form to display on the server.
    return adapter.toJson(responseMap);
  }

  /**
   * The getStateCodes method is a helper method that turns the stateCodes variable into a map of
   * states and their corresponding codes.
   */
  // TODO: Make sure all these errors are handled correctly.
  private void getStateCodes() throws URISyntaxException, IOException, InterruptedException {
    try {
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
      // TODO: Think about changing the deserialization process into a object from a record.
      List<List<String>> codesMatrix = adaptedCodes.fromJson(codes);
      // Create a boolean to skip over the first iteration (the first iteration is [Names, State],
      // thus since State is not an integer it must be skipped.
      boolean firstIteration = true;
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
    } catch (Exception e) {
      // TODO: Put something better in the catch block. Also check if the try-catch in handle is
      // necessary.
      System.err.println("Something went wong");
      e.printStackTrace();
    }
  }

  private String getCountyCode(String stateCode, String county, String state)
      throws URISyntaxException, IOException, InterruptedException {
    // Create a request at the URL where the API is located.
    if (stateCode == null) {
      // TODO: Return something better here, maybe an error?
      return "";
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
    // TODO: Figure out something to return if no entry matched. Maybe an error like in the gearup code? Also read the google doc for info as well
    return sentCountyAPIRequest.body();
  }

  private String getData(String stateCode, String countyCode)
      throws URISyntaxException, IOException, InterruptedException {
    // TODO: Return something better here, as this would be an error.
    if (stateCode == null || countyCode == null) {
      return "";
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
    List<String> data = codesMatrix.get(1);
    String percentage = data.get(1);
    // TODO: Also do something here if percentage is null. Read through the google doc to figure out what error to throw.
    return percentage;
  }
}
