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

  @Override
  public Object handle(Request request, Response response) throws Exception {
    // Instantialize a Moshi object to transfer a .json file into a Java object
    Moshi moshi = new Moshi.Builder().build();

    Map<String, Object> stateCodes = new HashMap<>();

  }

  private String sendCountyRequest() throws URISyntaxException, IOException, InterruptedException {
    // Build a request to this BoredAPI. Try out this link in your browser, what do you see?
    // TODO 1: Looking at the documentation, how can we add to the URI to query based
    // on participant number?
    HttpRequest buildCencusApiRequest =
        HttpRequest.newBuilder()
            .uri(new URI("http://www.boredapi.com/api/activity/"))
            .GET()
            .build();

    // Send that API request then store the response in this variable. Note the generic type.
    HttpResponse<String> sentBoredApiResponse =
        HttpClient.newBuilder()
            .build()
            .send(buildCencusApiRequest, HttpResponse.BodyHandlers.ofString());

    // What's the difference between these two lines? Why do we return the body? What is useful from
    // the raw response (hint: how can we use the status of response)?
    System.out.println(sentBoredApiResponse);
    System.out.println(sentBoredApiResponse.body());

    return sentBoredApiResponse.body();
  }
}
