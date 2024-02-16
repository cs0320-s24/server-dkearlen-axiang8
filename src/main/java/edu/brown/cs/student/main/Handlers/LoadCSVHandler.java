package edu.brown.cs.student.main.Handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.Creators.CreatorFromString;
import edu.brown.cs.student.main.DataSource.Broadband.BroadbandData;
import edu.brown.cs.student.main.DataSource.Broadband.CSVSource;
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
  private final CSVSource source;
  private static CreatorFromString creator;

  public LoadCSVHandler(CSVSource source, CreatorFromString c) {
    this.source = source;
    creator = c;
  }

  @Override
  public Object handle(Request request, Response response) {
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
      responseMap.put("error_type", "Empty file path.");
      return adapter.toJson(responseMap);
    } else {
      file = new File(filePath);
    }

    // Ensure that the file exists and is a file (not a directory)
    if (!file.exists() || !file.isFile()) {
      responseMap.put("filePath", filePath);
      responseMap.put("type", "error");
      responseMap.put("error_type", "File not found.");
      return adapter.toJson(responseMap);
    }

    String dataDirectory = "data";
    String fileAbsolutePath = file.getAbsolutePath();

    // Ensure that the file is within the "data" directory
    if (!fileAbsolutePath.contains(File.separator + dataDirectory)) {
      responseMap.put("filePath", filePath);
      responseMap.put("type", "error");
      responseMap.put("error_type", "File is not in data directory.");
      return adapter.toJson(responseMap);
    }
    try {
      BroadbandData data = new BroadbandData(source.retrieveAndParse(filePath, creator));
      // Building responses *IS* the job of this class:
      responseMap.put("type", "load_success");
      // responseMap.put("broadband percentages", broadbandDataAdapter.toJson(data));
      return adapter.toJson(responseMap);
    } catch (IllegalArgumentException e) {
      responseMap.put("filepath", filePath);
      responseMap.put("type", "error");
      responseMap.put("error_type", "Invalid File.");
      responseMap.put("details", e.getMessage());
      return adapter.toJson(responseMap);
    } catch (FileNotFoundException e) {
      responseMap.put("filepath", filePath);
      responseMap.put("type", "error");
      responseMap.put("error_type", "File not found.");
      responseMap.put("details", e.getMessage());
      return adapter.toJson(responseMap);
    } catch (MalformedCSVException e) {
      responseMap.put("filepath", filePath);
      responseMap.put("type", "error");
      responseMap.put("error_type", "Cannot input malformed CSV.");
      responseMap.put("details", e.getMessage());
      return adapter.toJson(responseMap);
    } catch (IOException e) {
      responseMap.put("filepath", filePath);
      responseMap.put("type", "error");
      responseMap.put("error_type", "Unreadable file.");
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
