package edu.brown.cs.student.main.Handlers;

import com.squareup.moshi.Moshi;
import java.io.IOException;
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

  private Map<String, Object> stateCodes;

  @Override
  public Object handle(Request request, Response response) throws Exception {
    // Instantialize a Moshi object to transfer a .json file into a Java object
    Moshi moshi = new Moshi.Builder().build();

    this.stateCodes = new HashMap<>();
    try{
      String stateCodes = getStateCodes();

    } catch (Exception e){
      stateCodes.put("result", "exception");
    }
    // TODO: return something of value here
    return 1;
  }


  private String getStateCodes() throws URISyntaxException, IOException, InterruptedException {
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

    return stateCodes.body();
  }
}
