package edu.brown.cs.student.main.ServerAndHandlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.CSVDataSource.CSVData;
import edu.brown.cs.student.main.CSVDataSource.CSVSource;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class ViewCSVHandler implements Route {
  private final CSVSource source;

  public ViewCSVHandler(CSVSource source) {
    this.source = source;
  }

  @Override
  public Object handle(Request request, Response response) {
    Moshi moshi = new Moshi.Builder().build();
    // Replies will be Maps from String to Object. This isn't ideal; see reflection...
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    JsonAdapter<CSVData> csvDataAdapter = moshi.adapter(CSVData.class);
    Map<String, Object> responseMap = new HashMap<>();
    CSVData data;
    try {
      data = new CSVData(source.getParsedData());
      responseMap.put("type", "view_success");
      responseMap.put("data", csvDataAdapter.toJson(data));
      return new ViewCSVHandler.CSVSuccessResponse(responseMap).serialize();
    } catch (IOException e) {
      responseMap.put("type", "error");
      responseMap.put("error_type", "No CSV loaded.");
      return new ViewCSVHandler.CSVFailureResponse(responseMap).serialize();
    }
  }

  public record CSVSuccessResponse(String response_type, Map<String, Object> responseMap) {
    public CSVSuccessResponse(Map<String, Object> responseMap) {
      this("success", responseMap);
    }
    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      try {
        // Initialize Moshi which takes in this class and returns it as JSON!
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<ViewCSVHandler.CSVSuccessResponse> adapter =
            moshi.adapter(ViewCSVHandler.CSVSuccessResponse.class);
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

  public record CSVFailureResponse(String response_type, Map<String, Object> responseMap) {
    public CSVFailureResponse(Map<String, Object> responseMap) {
      this("error", responseMap);
    }
    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      try {
        // Initialize Moshi which takes in this class and returns it as JSON!
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<ViewCSVHandler.CSVFailureResponse> adapter =
            moshi.adapter(ViewCSVHandler.CSVFailureResponse.class);
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
}
