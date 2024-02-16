package edu.brown.cs.student;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.squareup.moshi.Moshi;
import edu.brown.cs.student.main.ACS.ACSAPIDataSource;
import edu.brown.cs.student.main.ACS.CachingACSAPI;
import edu.brown.cs.student.main.Handlers.BroadbandHandler;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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
  private static HttpURLConnection tryRequest(String apiCall) throws IOException {
    // Configure the connection (but don't actually send the request yet)
    URL requestURL = new URL("http://localhost:" + Spark.port() + "/" + apiCall);
    HttpURLConnection clientConnection = (HttpURLConnection) requestURL.openConnection();

    // The default method is "GET", which is what we're using here.
    // If we were using "POST", we'd need to say so.
    clientConnection.setRequestMethod("GET");

    clientConnection.connect();
    return clientConnection;
  }

  @Test
  public void testBroadband() throws IOException {
    HttpURLConnection clientConnection = tryRequest("broadband");
    // Get an OK response (the *connection* worked, the *API* provides an error response)
    assertEquals(200, clientConnection.getResponseCode());
    // Build a new Moshi to run tests on.
    Moshi moshi = new Moshi.Builder().build();

    BroadbandHandler.APISuccessResponse response = moshi.adapter(BroadbandHandler.APISuccessResponse.class).fromJson(new Buffer().readFrom(clientConnection.getInputStream()));

  }
}
