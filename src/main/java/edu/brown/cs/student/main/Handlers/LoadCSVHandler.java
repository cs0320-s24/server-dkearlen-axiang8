package edu.brown.cs.student.main.Handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.Creators.CreatorFromString;
import edu.brown.cs.student.main.CSVDataSource.CSVData;
import edu.brown.cs.student.main.CSVDataSource.CSVSource;
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
    JsonAdapter<CSVData> csvDataAdapter = moshi.adapter(CSVData.class);
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
      CSVData data = new CSVData(source.retrieveAndParse(filePath, creator));
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
}
