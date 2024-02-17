package edu.brown.cs.student.main.Handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.CSVDataSource.CSVData;
import edu.brown.cs.student.main.CSVDataSource.CSVSource;
import edu.brown.cs.student.main.Creators.CreatorFromString;
import edu.brown.cs.student.main.Searcher;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class SearchCSVHandler implements Route {

  private final CSVSource source;
  private static CreatorFromString creator;

  public SearchCSVHandler(CSVSource source, CreatorFromString c) {
    this.source = source;
    creator = c;
  }

  @Override
  public Object handle(Request request, Response response) throws IOException {
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    JsonAdapter<CSVData> csvDataAdapter = moshi.adapter(CSVData.class);
    LinkedHashMap<String, Object> responseMap = new LinkedHashMap<>();
    List<List<String>> data;

    try {
      data = source.getParsedData();
      String target = request.queryParams("target");
      boolean hasHeaders =
          Boolean.parseBoolean(request.queryParams("headers")); // default false if not specified
      String indexType = request.queryParams("indexType");
      String index = request.queryParams("index");
      Searcher searcher = new Searcher(data, creator, hasHeaders);

      if (target == null) {
        responseMap.put("type", "error");
        responseMap.put("error_type", "Need to specify target to search for.");
        return new SearchCSVHandler.CSVFailureResponse(responseMap).serialize();
      }
      List<List<String>> searchResults;
      if (indexType == null && index == null) {
        searchResults = searcher.search(target);
      } else if (indexType == null) {
        responseMap.put("type", "error");
        responseMap.put(
            "error_type",
            "Must declare index type, either 'string' or 'int', along with index: " + index);
        return new SearchCSVHandler.CSVFailureResponse(responseMap).serialize();
      } else if (indexType.equalsIgnoreCase("string")) {
        if (creator.getHeaderRow().contains(index)) {
          searchResults = searcher.search(target, index);
        } else {
          responseMap.put("type", "error");
          StringBuilder columnsAvailable = new StringBuilder();
          for (String column : creator.getHeaderRow()) {
            columnsAvailable.append(column).append(", ");
          }
          columnsAvailable.setLength(columnsAvailable.length() - 2);
          responseMap.put("type", "error");
          responseMap.put("error_type", "Index not found. Columns available: " + columnsAvailable);
          return new SearchCSVHandler.CSVFailureResponse(responseMap).serialize();
        }
      } else if (indexType.equalsIgnoreCase("int")) {
        try {
          searchResults = searcher.search(target, Integer.parseInt(index));
        } catch (NumberFormatException e) {
          responseMap.put("type", "error");
          responseMap.put("error_type", "Index is not a valid int");
          responseMap.put("index", index);
          responseMap.put("target", target);
          responseMap.put("headers", hasHeaders);
          responseMap.put("indexType", indexType);
          return new SearchCSVHandler.CSVFailureResponse(responseMap).serialize();
        } catch (IllegalArgumentException e) {
          responseMap.put("type", "error");
          responseMap.put(
              "error_type",
              "Index must be greater than 0, less than " + creator.getHeaderRow().size());
          responseMap.put("index", index);
          responseMap.put("target", target);
          responseMap.put("headers", hasHeaders);
          responseMap.put("indexType", indexType);
          return new SearchCSVHandler.CSVFailureResponse(responseMap).serialize();
        }
      } else {
        responseMap.put("type", "error");
        responseMap.put("error_type", "Invalid index type. Must be either 'int' or 'string'");
        responseMap.put("index", index);
        responseMap.put("target", target);
        responseMap.put("headers", hasHeaders);
        responseMap.put("indexType", indexType);
        return new SearchCSVHandler.CSVFailureResponse(responseMap).serialize();
      }
      responseMap.put("type", "search_success");
      responseMap.put("target", target);
      responseMap.put("headers", hasHeaders);
      responseMap.put("index", index);
      responseMap.put("indexType", indexType);
      responseMap.put("search_results", csvDataAdapter.toJson(new CSVData(searchResults)));
    } catch (IOException e) {
      responseMap.put("type", "error");
      responseMap.put("error_type", "No CSV loaded.");
      return new SearchCSVHandler.CSVFailureResponse(responseMap).serialize();
    }
    return new SearchCSVHandler.CSVSuccessResponse(responseMap).serialize();
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
        JsonAdapter<SearchCSVHandler.CSVSuccessResponse> adapter =
            moshi.adapter(SearchCSVHandler.CSVSuccessResponse.class);
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
        JsonAdapter<SearchCSVHandler.CSVFailureResponse> adapter =
            moshi.adapter(SearchCSVHandler.CSVFailureResponse.class);
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
