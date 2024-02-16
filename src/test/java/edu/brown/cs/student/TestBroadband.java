package edu.brown.cs.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.ACS.ACSAPIDataSource;
import edu.brown.cs.student.main.ACS.CachingACSAPI;
import edu.brown.cs.student.main.Handlers.BroadbandHandler;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.Map;
import okio.Buffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

public class TestBroadband {

  @BeforeAll
  public static void setup_before_everything() {
    // Set the Spark port number. This can only be done once, and has to
    // happen before any route maps are added. Hence using @BeforeClass.
    // Setting port 0 will cause Spark to use an arbitrary available port.
    Spark.port(0);
    // Don't try to remember it. Spark won't actually give Spark.port() back
    // until route mapping has started. Just get the port number later. We're using
    // a random _free_ port to remove the chances that something is already using a
    // specific port on the system used for testing.

    // Remove the logging spam during tests
    //   This is surprisingly difficult. (Notes to self omitted to avoid complicating things.)

    // SLF4J doesn't let us change the logging level directly (which makes sense,
    //   given that different logging frameworks have different level labels etc.)
    // Changing the JDK *ROOT* logger's level (not global) will block messages
    //   (assuming using JDK, not Log4J)
    Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
  }

  @BeforeEach
  public void setup() {
    // Restart the entire Spark server for every test!
    Spark.get("broadband", new BroadbandHandler(new CachingACSAPI(new ACSAPIDataSource(), 10, 2)));
    Spark.init();
    Spark.awaitInitialization(); // don't continue until the server is listening
  }

  @AfterEach
  public void teardown() {
    // Gracefully stop Spark listening on the broadband endpoint after each test
    Spark.unmap("broadband");
    Spark.awaitStop(); // don't proceed until the server is stopped
  }

  /**
   * Helper to start a connection to a specific API endpoint/params
   *
   * @param apiCall the call string, including endpoint (NOTE: this would be better if it had more
   *     structure!)
   * @return the connection for the given URL, just after connecting
   * @throws IOException if the connection fails for some reason
   */
  private static HttpURLConnection tryRequest(String apiCall, String state, String county) throws IOException {
    // Configure the connection (but don't actually send the request yet)
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall + "?state=" + state + "&county=" + county);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();
    // The default method is "GET", which is what we're using here.
    // If we were using "POST", we'd need to say so.
    clientConnection.setRequestMethod("GET");

    clientConnection.connect();
    return clientConnection;
  }
  /**
   * testBroadbandCleanData tests the API on clean input and that it gives back data properly.
   * */
  @Test
  public void testBroadbandCleanData() throws IOException {
    HttpURLConnection clientConnection = tryRequest("broadband", "texas", "comal%20county");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());
    // Build a new Moshi to run tests on.
    Moshi moshi = new Moshi.Builder().build();
    BroadbandHandler.APISuccessResponse response = moshi.adapter(BroadbandHandler.APISuccessResponse.class).fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    // These tests ensure that the response map works with the API as intended.
    assertEquals(response.response_type(), "success");
    assertEquals(response.responseMap().get("State"), "texas");
    assertEquals(response.responseMap().get("County"), "comal county");
    assertEquals(response.responseMap().get("Percentage with Access to Internet"), "92.6");
    // These tests ensure that the response map gives date as intended (time is too sensitive to test)
    LocalDate date = LocalDate.now();
    assertEquals(response.responseMap().get("Date"), date.toString());
    clientConnection.disconnect();
    // Create another client but with all capital letters to show non case sensitivity
    HttpURLConnection clientConnection2 = tryRequest("broadband", "TEXAS", "COMAL%20COUNTY");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection2.getResponseCode());
    // Build a new Moshi to run tests on.
    Moshi moshi2 = new Moshi.Builder().build();
    BroadbandHandler.APISuccessResponse response2 = moshi2.adapter(BroadbandHandler.APISuccessResponse.class).fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));
    assertEquals(response.responseMap().get("Percentage with Access to Internet"), response2.responseMap().get("Percentage with Access to Internet"));
    assertEquals(response.response_type(), response2.response_type());
  }
  /**
   * testBroadbandMalformedInput() tests that errors are given in Json form correctly.
   * */
  @Test
  public void testBroadbandMalformedInput() throws IOException {
    // This first set tests the output when the user input is something nonsensicle.
    HttpURLConnection clientConnection = tryRequest("broadband", "cows", "cows");
    assertEquals(200, clientConnection.getResponseCode());
    // Build a new Moshi to run tests on.
    Moshi moshi = new Moshi.Builder().build();
    BroadbandHandler.APIFailureResponse response = moshi.adapter(BroadbandHandler.APIFailureResponse.class).fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    // If an error has been thrown and given, the normal categories of output should not be present
    assertEquals(response.response_type(), "error");
    assertNull(response.responseMap().get("State"));
    assertNull(response.responseMap().get("County"));
    assertNull(response.responseMap().get("Date"));
    assertNull(response.responseMap().get("Time"));
    assertNull(response.responseMap().get("Percentage with Access to Internet"));
    // Instead new categories corresponding to an error should be present instead.
    assertNotNull(response.responseMap().get("error_message"));
    assertNotNull(response.responseMap().get("error_type"));
    assertEquals(response.responseMap().get("error_message"), "DataRetrievalException: Given state and county returned no results!");
    assertEquals(response.responseMap().get("error_type"), "error_datasource");
    clientConnection.disconnect();
    // This second set tests the output when the API data given back is something nonsensicle.
    HttpURLConnection clientConnection2 = tryRequest("broadband", "California", "San%20Benito%20County");
    assertEquals(200, clientConnection2.getResponseCode());
    // Build a new Moshi to run tests on.
    Moshi moshi2 = new Moshi.Builder().build();
    BroadbandHandler.APIFailureResponse response2 = moshi2.adapter(BroadbandHandler.APIFailureResponse.class).fromJson(new Buffer().readFrom(clientConnection2.getInputStream()));
    assertEquals(response2.response_type(), "error");
    assertEquals(response2.responseMap().get("error_message"), "URISyntaxException: State or County codes given were incorrect/API gave an error!");
    assertEquals(response2.responseMap().get("error_type"), "error_bad_json");
    clientConnection2.disconnect();
  }

  @Test
  public void testCache() throws IOException, InterruptedException {
    HttpURLConnection clientConnection = tryRequest("broadband", "Texas", "comal%20county");
    assertEquals(200, clientConnection.getResponseCode());
    // Build a new Moshi to run tests on.
    Moshi moshi = new Moshi.Builder().build();
    BroadbandHandler.APISuccessResponse response = moshi.adapter(BroadbandHandler.APISuccessResponse.class).fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
    clientConnection.disconnect();
    // Sleep for 5 seconds, a margin of time long enough to create a difference in time if a cache was not present.
    Thread.sleep(5000);
    HttpURLConnection clientConnectionAgain = tryRequest("broadband", "Texas", "comal%20county");
    assertEquals(200, clientConnectionAgain.getResponseCode());
    // Build another new Moshi
    Moshi moshiAgain = new Moshi.Builder().build();
    BroadbandHandler.APISuccessResponse responseCached = moshiAgain.adapter(BroadbandHandler.APISuccessResponse.class).fromJson(new Buffer().readFrom(clientConnectionAgain.getInputStream()));
    // If the cache works properly, getting two responses 5 seconds away from one another should yield the same time.
    assertEquals(response.responseMap().get("Time"), responseCached.responseMap().get("Time"));
  }
}
