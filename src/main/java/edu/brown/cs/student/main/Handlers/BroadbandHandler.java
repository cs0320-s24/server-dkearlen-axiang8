package edu.brown.cs.student.main.Handlers;

import com.google.common.util.concurrent.UncheckedExecutionException;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.ACS.APIDataSource;
import edu.brown.cs.student.main.ACS.InternetAccessData;
import edu.brown.cs.student.main.Exceptions.DataRetrievalException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * @author devonkearleng
 * @version 1.0 - BroadbandHandler is a handler that helps access and retrieve data from the ACS
 *     API.
 */
public class BroadbandHandler implements Route {

  private final APIDataSource dataSource;
  private Map<String, Object> responseMap;

  // This is the constructor for BroadbandHandler
  public BroadbandHandler(APIDataSource dataSource) {
    this.dataSource = dataSource;
  }
  /**
   * @param request - The request that a user inputs when using the server and specifying the
   *     Broadband handler
   * @param response - The response given by the server.
   * @return - Returns a map of String,Object with messages pertaining to the info requested or an
   *     error being formed.
   */
  @Override
  public Object handle(Request request, Response response) {
    // Use Moshi to turn the Map into a Json object to display on the web page.
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    this.responseMap = new HashMap<>();
    // Put the arguments into a set of Strings.
    String state = request.queryParams("state");
    String county = request.queryParams("county");
    try {
      if (checkParams(state, county) == -1) {
        return new APIFailureResponse(this.responseMap).serialize();
      }
      InternetAccessData data = this.dataSource.getData(state, county);
      this.responseMap.put("Date", data.date().toString());
      this.responseMap.put("Time", data.time().toString());
      this.responseMap.put("County", data.county());
      this.responseMap.put("Percentage with Access to Internet", data.percentage());
      this.responseMap.put("State", data.state());
      // Return the response map in Json form to display on the server.
      return new APISuccessResponse(this.responseMap).serialize();
      // Every exception should be caught, and then processed as to which exception it was, and
      // acted on accordingly.
    } catch (UncheckedExecutionException
        | IOException
        | URISyntaxException
        | InterruptedException
        | DataRetrievalException e) {
      Throwable cause = e.getCause();
      // This if statement will run in the case of a DataRetrievalException.
      if (cause instanceof DataRetrievalException) {
        this.responseMap.put("error_type", "error_datasource");
        this.responseMap.put(
            "error_message", "DataRetrievalException: Given state and county returned no results!");
        return new APIFailureResponse(this.responseMap).serialize();
      }
      // This if statement will run in the case of a URISyntaxException.
      if (cause instanceof URISyntaxException) {
        this.responseMap.put("error_type", "error_bad_json");
        this.responseMap.put(
            "error_message",
            "URISyntaxException: State or County codes given were incorrect/API gave an error!");
        return new APIFailureResponse(this.responseMap).serialize();
      }
      // This if statement will run in the case of a InterruptedException.
      if (cause instanceof InterruptedException) {
        this.responseMap.put("error_type", "error_datasource");
        this.responseMap.put(
            "error_message", "InterruptedException: HTTPResponse was interrupted at some point!");
        return new APIFailureResponse(this.responseMap).serialize();
      }
      // This if statement will run in the case of a IOException.
      if (cause instanceof IOException) {
        this.responseMap.put("error_type", "error_datasource");
        this.responseMap.put("error_message", "IOException: ");
        return new APIFailureResponse(this.responseMap).serialize();
      }
      return new APIFailureResponse(this.responseMap).serialize();
    }
  }

  /**
   * @param response_type - Type of response given by BroadbandHandler
   * @param responseMap - responseMap given by data or errors.
   */
  public record APISuccessResponse(String response_type, Map<String, Object> responseMap) {
    public APISuccessResponse(Map<String, Object> responseMap) {
      this("success", responseMap);
    }
    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      try {
        // Initialize Moshi which takes in this class and returns it as JSON!
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<APISuccessResponse> adapter = moshi.adapter(APISuccessResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        // For debugging purposes, show in the console _why_ this fails
        // Otherwise we'll just get an error 500 from the API in integration
        // testing.
        e.printStackTrace();
        throw e;
      }
    }
  }

  /**
   * @param response_type - Type of response given by BroadbandHandler
   * @param responseMap - responseMap given by data or errors.
   */
  public record APIFailureResponse(String response_type, Map<String, Object> responseMap) {
    public APIFailureResponse(Map<String, Object> responseMap) {
      this("error", responseMap);
    }
    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      try {
        // Initialize Moshi which takes in this class and returns it as JSON!
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<APIFailureResponse> adapter = moshi.adapter(APIFailureResponse.class);
        return adapter.toJson(this);
      } catch (Exception e) {
        // For debugging purposes, show in the console _why_ this fails
        // Otherwise we'll just get an error 500 from the API in integration
        // testing.
        e.printStackTrace();
        throw e;
      }
    }
  }

  /**
   * @param state - the state given by the request
   * @param county - the county given by the request
   * @return - Returns an Integer specifying if there was an error. Returns a -1 on error and a 0 on
   *     non-error.
   */
  private Integer checkParams(String state, String county) {
    if ((state == null || state.equals("")) && (county == null || county.equals(""))) {
      this.responseMap.put("result", "error");
      this.responseMap.put("error_type", "error_bad_request");
      this.responseMap.put("error_message", "Both state and county were not given!");
      return -1;
    } else if (state == null || state.equals("")) {
      this.responseMap.put("result", "error");
      responseMap.put("error_type", "error_bad_request");
      responseMap.put("error_message", "State was not given!");
      return -1;
    } else if (county == null || county.equals("")) {
      this.responseMap.put("result", "error");
      this.responseMap.put("error_type", "error_bad_request");
      this.responseMap.put("error_message", "County was not given!");
      return -1;
    }
    return 0;
  }
}
