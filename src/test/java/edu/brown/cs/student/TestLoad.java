package edu.brown.cs.student;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.ACS.ACSAPIDataSource;
import edu.brown.cs.student.main.ACS.CachingACSAPI;
import edu.brown.cs.student.main.CSVDataSource.CSVSource;
import edu.brown.cs.student.main.Creators.CreatorFromString;
import edu.brown.cs.student.main.Handlers.BroadbandHandler;
import edu.brown.cs.student.main.Handlers.LoadCSVHandler;
import edu.brown.cs.student.main.Handlers.SearchCSVHandler;
import edu.brown.cs.student.main.Handlers.ViewCSVHandler;
import okio.Buffer;
import okio.BufferedSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLoad {

    private final JsonAdapter<Map<String,Object>> adapter;
    Moshi moshi = new Moshi.Builder().build();
    public TestLoad() {
        Type type = Types.newParameterizedType(Map.class, String.class, Object.class);
        adapter = moshi.adapter(type);
    }

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
    public void testLoadCleanData() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=/Users/austinxiang/Desktop/brown/cs32/server-dkearlen-axiang8/data/RI_Income.csv");
        assertEquals(200, clientConnection.getResponseCode());
        LoadCSVHandler.CSVSuccessResponse response = moshi.adapter(LoadCSVHandler.CSVSuccessResponse.class).fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
        assertEquals("success", response.response_type());
    }
    @Test
    public void testLoadWrongDirectory() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=/Users/austinxiang/Desktop/brown/cs32/server-dkearlen-axiang8/RI_Income.csv");
        assertEquals(200, clientConnection.getResponseCode());
        LoadCSVHandler.CSVFailureResponse response = moshi.adapter(LoadCSVHandler.CSVFailureResponse.class).fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
        assertEquals("error", response.response_type());
        assertEquals("File is not in data directory.", response.responseMap().get("error_type"));
    }
    @Test
    public void testEmptyDirectory() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=");
        assertEquals(200, clientConnection.getResponseCode());
        LoadCSVHandler.CSVFailureResponse response = moshi.adapter(LoadCSVHandler.CSVFailureResponse.class).fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
        assertEquals("error", response.response_type());
        assertEquals("Empty file path.", response.responseMap().get("error_type"));
    }
    @Test
    public void testInvalidFile() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=/Users/austinxiang/Desktop/brown/cs32/server-dkearlen-axiang8/data/CA_Income.csv");
        assertEquals(200, clientConnection.getResponseCode());
        LoadCSVHandler.CSVFailureResponse response = moshi.adapter(LoadCSVHandler.CSVFailureResponse.class).fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
        assertEquals("error", response.response_type());
        assertEquals("File not found.", response.responseMap().get("error_type"));
    }
    @Test
    public void testMalformedFile() throws IOException {
        HttpURLConnection clientConnection = tryRequest("loadcsv?filepath=/Users/austinxiang/Desktop/brown/cs32/server-dkearlen-axiang8/data/malformed_signs.csv");
        assertEquals(200, clientConnection.getResponseCode());
        LoadCSVHandler.CSVFailureResponse response = moshi.adapter(LoadCSVHandler.CSVFailureResponse.class).fromJson(new Buffer().readFrom(clientConnection.getInputStream()));
        assertEquals("error", response.response_type());
        assertEquals("Cannot input malformed CSV.", response.responseMap().get("error_type"));
    }

}
