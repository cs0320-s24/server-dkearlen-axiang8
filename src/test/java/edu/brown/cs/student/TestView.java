package edu.brown.cs.student;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.CSVDataSource.CSVSource;
import edu.brown.cs.student.main.Creators.CreatorFromString;
import edu.brown.cs.student.main.Handlers.LoadCSVHandler;
import edu.brown.cs.student.main.Handlers.SearchCSVHandler;
import edu.brown.cs.student.main.Handlers.ViewCSVHandler;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestView {
    Moshi moshi = new Moshi.Builder().build();
    Type type = Types.newParameterizedType(Map.class, String.class, Object.class);
    private final JsonAdapter<Map<String, Object>> adapter = moshi.adapter(type);

    CSVSource csvSource = new CSVSource();
    CreatorFromString creator = new CreatorFromString();

    @BeforeAll
    public static void setup_before_everything() {
        Spark.port(0);
        Logger.getLogger("").setLevel(Level.WARNING); // empty name = root logger
    }

    @BeforeEach
    public void setup() {
        Spark.get("loadcsv", new LoadCSVHandler(this.csvSource, creator));
        Spark.get("searchcsv", new SearchCSVHandler(this.csvSource, creator));
        Spark.get("viewcsv", new ViewCSVHandler(this.csvSource));
        Spark.init();
        Spark.awaitInitialization(); // don't continue until the server is listening
    }

    @AfterEach
    public void teardown() {
        // Gracefully stop Spark listening on the broadband endpoint after each test
        Spark.unmap("loadcsv");
        Spark.unmap("searchcsv");
        Spark.unmap("viewcsv");
        Spark.awaitStop(); // don't proceed until the server is stopped
    }

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
    public void testViewCSVWithSuccessfulLoad() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=/Users/austinxiang/Desktop/brown/cs32/server-dkearlen-axiang8/data/RI_Income.csv");
        assertEquals(200, clientConnection.getResponseCode());
        HttpURLConnection clientConnection1 = tryRequest("viewcsv");
        assertEquals(200, clientConnection1.getResponseCode());

        ViewCSVHandler.CSVSuccessResponse response = moshi.adapter(ViewCSVHandler.CSVSuccessResponse.class).fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
        assertEquals("success", response.response_type());
    }
    @Test
    public void testViewCSVWithUnsuccessfulLoad() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=/Users/austinxiang/Desktop/brown/cs32/server-dkearlen-axiang8/RI_Income.csv");
        assertEquals(200, clientConnection.getResponseCode());
        HttpURLConnection clientConnection1 = tryRequest("viewcsv");
        assertEquals(200, clientConnection1.getResponseCode());

        ViewCSVHandler.CSVFailureResponse response = moshi.adapter(ViewCSVHandler.CSVFailureResponse.class).fromJson(new Buffer().readFrom(clientConnection1.getInputStream()));
        assertEquals("error", response.response_type());
        assertEquals("No CSV loaded.", response.responseMap().get("error_type"));
    }
}

