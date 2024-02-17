package edu.brown.cs.student;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.CSVDataSource.CSVSource;
import edu.brown.cs.student.main.Creators.CreatorFromString;
import edu.brown.cs.student.main.Handlers.LoadCSVHandler;
import edu.brown.cs.student.main.Handlers.SearchCSVHandler;
import edu.brown.cs.student.main.Handlers.ViewCSVHandler;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import okio.Buffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spark.Spark;

public class TestSearch {
  private final JsonAdapter<Map<String, Object>> adapter;
  Moshi moshi = new Moshi.Builder().build();

  public TestSearch() {
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
  public void testSearchCSVWithUnsuccessfulLoad() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest(
            "loadcsv?filepath=/Users/austinxiang/Desktop/brown/cs32/server-dkearlen-axiang8/RI_Income.csv");
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection1 = tryRequest("searchcsv?target=69");
    assertEquals(200, clientConnection1.getResponseCode());
    SearchCSVHandler.CSVFailureResponse response =
        moshi
            .adapter(SearchCSVHandler.CSVFailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection1.getInputStream()));
    assertEquals("error", response.response_type());
    assertEquals("No CSV loaded.", response.responseMap().get("error_type"));
  }

  @Test
  public void testSearchCSVWithHeaders() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest(
            "loadcsv?filepath=/Users/austinxiang/Desktop/brown/cs32/server-dkearlen-axiang8/data/RI_Income.csv");
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection1 = tryRequest("searchcsv?target=69&headers=true");
    assertEquals(200, clientConnection1.getResponseCode());
    SearchCSVHandler.CSVSuccessResponse response =
        moshi
            .adapter(SearchCSVHandler.CSVSuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection1.getInputStream()));
    assertEquals(
        "{type=search_success, target=69, headers=false, search_results={\"data\":[[\"Barrington\",\"130,455.00\",\"154,441.00\",\"69,917.00\"],[\"Cranston\",\"77,145.00\",\"95,763.00\",\"38,269.00\"],[\"West Warwick\",\"62,649.00\",\"80,699.00\",\"36,148.00\"]]}}",
        response.responseMap().toString());
  }

  @Test
  public void testSearchCSVWithoutHeaders() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest(
            "loadcsv?filepath=/Users/austinxiang/Desktop/brown/cs32/server-dkearlen-axiang8/data/RI_Income_No_Headers.csv");
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection1 = tryRequest("searchcsv?target=33&headers=false");
    assertEquals(200, clientConnection1.getResponseCode());
    SearchCSVHandler.CSVSuccessResponse response =
        moshi
            .adapter(SearchCSVHandler.CSVSuccessResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection1.getInputStream()));
    assertEquals(
        "{type=search_success, target=33, headers=false, search_results={\"data\":[[\"Central Falls\",\"40,235.00\",\"42,633.00\",\"17,962.00\"],[\"East Greenwich\",\"133,373.00\",\"173,775.00\",\"71,096.00\"],[\"Warwick\",\"77,110.00\",\"97,033.00\",\"41,476.00\"]]}}",
        response.responseMap().toString());
  }

  @Test
  public void testSearchCSVWithoutTarget() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest(
            "loadcsv?filepath=/Users/austinxiang/Desktop/brown/cs32/server-dkearlen-axiang8/data/RI_Income_No_Headers.csv");
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection1 = tryRequest("searchcsv");
    assertEquals(200, clientConnection1.getResponseCode());
    SearchCSVHandler.CSVFailureResponse response =
        moshi
            .adapter(SearchCSVHandler.CSVFailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection1.getInputStream()));
    assertEquals("error", response.response_type());
    assertEquals("Need to specify target to search for.", response.responseMap().get("error_type"));
  }

  @Test
  public void testSearchCSVWithInvalidStringIndex() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest(
            "loadcsv?filepath=/Users/austinxiang/Desktop/brown/cs32/server-dkearlen-axiang8/data/RI_Income.csv");
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection1 =
        tryRequest("searchcsv?target=33&headers=true&indexType=string&index=notvalidindex");
    assertEquals(200, clientConnection1.getResponseCode());
    SearchCSVHandler.CSVFailureResponse response =
        moshi
            .adapter(SearchCSVHandler.CSVFailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection1.getInputStream()));
    assertEquals("error", response.response_type());
    assertEquals(
        "Index not found. Columns available: City/Town, Median Household Income, Median Family Income, Per Capita Income",
        response.responseMap().get("error_type"));
  }

  @Test
  public void testSearchCSVWithIndexNoType() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest(
            "loadcsv?filepath=/Users/austinxiang/Desktop/brown/cs32/server-dkearlen-axiang8/data/RI_Income.csv");
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection1 =
        tryRequest("searchcsv?target=33&headers=true&index=hello");
    assertEquals(200, clientConnection1.getResponseCode());
    SearchCSVHandler.CSVFailureResponse response =
        moshi
            .adapter(SearchCSVHandler.CSVFailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection1.getInputStream()));
    assertEquals("error", response.response_type());
    assertEquals(
        "Must declare index type, either 'string' or 'int', along with index: 1",
        response.responseMap().get("error_type"));
  }

  @Test
  public void testSearchCSVWithInvalidIntIndex() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest(
            "loadcsv?filepath=/Users/austinxiang/Desktop/brown/cs32/server-dkearlen-axiang8/data/RI_Income.csv");
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection1 =
        tryRequest("searchcsv?target=33&headers=true&indexType=int&index=-1");
    assertEquals(200, clientConnection1.getResponseCode());
    SearchCSVHandler.CSVFailureResponse response =
        moshi
            .adapter(SearchCSVHandler.CSVFailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection1.getInputStream()));
    assertEquals("error", response.response_type());
    assertEquals(
        "Index must be greater than 0, less than 4", response.responseMap().get("error_type"));
  }

  @Test
  public void testSearchCSVWithStringDeclaredAsInt() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest(
            "loadcsv?filepath=/Users/austinxiang/Desktop/brown/cs32/server-dkearlen-axiang8/data/RI_Income.csv");
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection1 =
        tryRequest("searchcsv?target=33&headers=true&indexType=int&index=hello");
    assertEquals(200, clientConnection1.getResponseCode());
    SearchCSVHandler.CSVFailureResponse response =
        moshi
            .adapter(SearchCSVHandler.CSVFailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection1.getInputStream()));
    assertEquals("error", response.response_type());
    assertEquals("Index is not a valid int", response.responseMap().get("error_type"));
  }

  @Test
  public void testSearchCSVWithInvalidIndexType() throws IOException {
    HttpURLConnection clientConnection =
        tryRequest(
            "loadcsv?filepath=/Users/austinxiang/Desktop/brown/cs32/server-dkearlen-axiang8/data/RI_Income.csv");
    assertEquals(200, clientConnection.getResponseCode());
    HttpURLConnection clientConnection1 =
        tryRequest("searchcsv?target=33&headers=true&indexType=boolean&index=hello");
    assertEquals(200, clientConnection1.getResponseCode());
    SearchCSVHandler.CSVFailureResponse response =
        moshi
            .adapter(SearchCSVHandler.CSVFailureResponse.class)
            .fromJson(new Buffer().readFrom(clientConnection1.getInputStream()));
    assertEquals("error", response.response_type());
    assertEquals(
        "Invalid index type. Must be either 'int' or 'string'",
        response.responseMap().get("error_type"));
  }
}
