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
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class BroadbandHandler implements Route {

  // TODO: Ensure that it should return an Integer, and not something more general.
  private Map<String, Integer> stateCodes;

  @Override
  public Object handle(Request request, Response response) throws Exception {
    // Instantialize a Moshi object to transfer a .json file into a Java object
    Moshi moshi = new Moshi.Builder().build();

    this.stateCodes = new HashMap<>();
    // TODO: ensure that there should not be a try-catch block for this statement here
    // transfer the state codes into a hashmap.
    getStateCodes();
    // TODO: return something of value here
    return 1;
  }

  private void getStateCodes() throws URISyntaxException, IOException, InterruptedException {
    // create an instance of a request. This
    HttpRequest buildStateCodeApiRequest =
        HttpRequest.newBuilder()
            .uri(new URI("https://api.census.gov/data/2010/dec/sf1?get=NAME&for=state:*"))
            .GET()
            .build();

    // Send that API request then store the response in this variable. Note the generic type.
    HttpResponse<String> stateCodes =
        HttpClient.newBuilder()
            .build()
            .send(buildStateCodeApiRequest, HttpResponse.BodyHandlers.ofString());

    // Save the API file as a String
    String codes = stateCodes.body();
    try {
      Moshi moshi = new Moshi.Builder().build();
      // create a parameterized type in order to pass into the moshi adapter.
      Type mapCodes = Types.newParameterizedType(HashMap.class, String.class, Integer.class);
      // Instantialize a JsonAdapter, and turn the Json file into a HashMap
      JsonAdapter<Map<String, Integer>> adaptedCodes = moshi.adapter(mapCodes);
      this.stateCodes = adaptedCodes.fromJson(codes);
    } catch (Exception e) {
      // TODO: Put something better in the catch block. Also check if the try-catch in handle is
      // necessary.
      System.err.println("Something went wong");
      e.printStackTrace();
    }
  }

  private String sendRequest() throws URISyntaxException, IOException, InterruptedException {
    // Build a request to this BoredAPI. Try out this link in your browser, what do you see?
    // TODO 1: Looking at the documentation, how can we add to the URI to query based
    // on participant number?
    HttpRequest buildCensusAPIRequest =
        HttpRequest.newBuilder()
            .uri(new URI("http://www.boredapi.com/api/activity/"))
            .GET()
            .build();

    // Send that API request then store the response in this variable. Note the generic type.
    HttpResponse<String> sentCensusAPIRequests =
        HttpClient.newBuilder()
            .build()
            .send(buildCensusAPIRequest, HttpResponse.BodyHandlers.ofString());

    // What's the difference between these two lines? Why do we return the body? What is useful from
    // the raw response (hint: how can we use the status of response)?
    // System.out.println(sentBoredApiResponse);
    System.out.println(sentCensusAPIRequests.body());

    return sentCensusAPIRequests.body();
  }
}
