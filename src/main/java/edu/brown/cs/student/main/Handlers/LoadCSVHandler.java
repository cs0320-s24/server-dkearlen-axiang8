package edu.brown.cs.student.main.Handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.DataSource.Broadband.BroadbandData;
import edu.brown.cs.student.main.DataSource.Broadband.CensusDataSource;
import edu.brown.cs.student.main.DataSource.DataSourceException;
import edu.brown.cs.student.main.Exceptions.MalformedCSVException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class LoadCSVHandler implements Route {
  private final CensusDataSource source;

  public LoadCSVHandler(CensusDataSource source) {
    this.source = source;
  }

  @Override
  public Object handle(Request request, Response response) {
    System.out.println("in handle of Load");
    Moshi moshi = new Moshi.Builder().build();
    // Replies will be Maps from String to Object. This isn't ideal; see reflection...
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    JsonAdapter<BroadbandData> broadbandDataAdapter = moshi.adapter(BroadbandData.class);
    Map<String, Object> responseMap = new HashMap<>();
    File file;
    String filePath = request.queryParams("filepath");
    if (filePath.isEmpty()) {
      responseMap.put("filePath", filePath);
      responseMap.put("type", "error");
      responseMap.put("error_type", "empty_file_path");
      return adapter.toJson(responseMap);
    } else {
      file = new File(filePath);
    }

    // Ensure that the file exists and is a file (not a directory)
    if (!file.exists() || !file.isFile()) {
      responseMap.put("filePath", filePath);
      responseMap.put("type", "error");
      responseMap.put("error_type", "file_not_found");
      return adapter.toJson(responseMap);
    }

    String dataDirectory = "data";
    String fileAbsolutePath = file.getAbsolutePath();

    // Ensure that the file is within the "data" directory
    if (!fileAbsolutePath.contains(File.separator + dataDirectory)) {
      responseMap.put("filePath", filePath);
      responseMap.put("type", "error");
      responseMap.put("error_type", "file_not_in_data_directory");
      return adapter.toJson(responseMap);
    }
    try {
      BroadbandData data = new BroadbandData(source.getBroadbandData(filePath));
      // Building responses *IS* the job of this class:
      responseMap.put("type", "load_success");
      // responseMap.put("broadband percentages", broadbandDataAdapter.toJson(data));
      return adapter.toJson(responseMap);
    } catch (IllegalArgumentException e) {
      responseMap.put("filepath", filePath);
      responseMap.put("type", "error");
      responseMap.put("error_type", "invalid_file");
      responseMap.put("details", e.getMessage());
      return adapter.toJson(responseMap);
    } catch (FileNotFoundException e) {
      responseMap.put("filepath", filePath);
      responseMap.put("type", "error");
      responseMap.put("error_type", "file_not_found");
      responseMap.put("details", e.getMessage());
      return adapter.toJson(responseMap);
    } catch (MalformedCSVException e) {
      responseMap.put("filepath", filePath);
      responseMap.put("type", "error");
      responseMap.put("error_type", "malformed_csv");
      responseMap.put("details", e.getMessage());
      return adapter.toJson(responseMap);
    } catch (IOException e) {
      responseMap.put("filepath", filePath);
      responseMap.put("type", "error");
      responseMap.put("error_type", "unreadable_file");
      responseMap.put("details", e.getMessage());
      return adapter.toJson(responseMap);
    } catch (DataSourceException e) {
      responseMap.put("filepath", filePath);
      responseMap.put("type", "error");
      responseMap.put("error_type", "data_source_exception");
      responseMap.put("details", e.getMessage());
      return adapter.toJson(responseMap);
    }
  }

  public record loadFileFailedResponse(String response_type) {
    public loadFileFailedResponse() {
      this("error_bad_request");
    }

    /**
     * @return this response, serialized as Json
     */
    String serialize() {
      Moshi moshi = new Moshi.Builder().build();
      return moshi.adapter(loadFileFailedResponse.class).toJson(this);
    }
  }

  public record loadFileSuccessResponse(String responseString, Map<String, Object> responseMap) {
    public loadFileSuccessResponse(Map<String, Object> responseMap) {
      this("success", responseMap);
    }

    String serialize() {
      try {
        // Initialize Moshi which takes in this class and returns it as JSON!
        Moshi moshi = new Moshi.Builder().build();
        JsonAdapter<loadFileSuccessResponse> adapter = moshi.adapter(loadFileSuccessResponse.class);
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
