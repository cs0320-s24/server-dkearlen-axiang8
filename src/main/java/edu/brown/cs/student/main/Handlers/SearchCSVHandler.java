package edu.brown.cs.student.main.Handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.Creators.CreatorFromString;
import edu.brown.cs.student.main.DataSource.Broadband.BroadbandData;
import edu.brown.cs.student.main.DataSource.Broadband.CensusDataSource;
import edu.brown.cs.student.main.Searcher;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class SearchCSVHandler implements Route {

  private final CensusDataSource source;
  private static CreatorFromString creator;

  public SearchCSVHandler(CensusDataSource source, CreatorFromString c) {
    this.source = source;
    creator = c;
  }

  @Override
  public Object handle(Request request, Response response) {
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    JsonAdapter<BroadbandData> broadbandDataAdapter = moshi.adapter(BroadbandData.class);
    LinkedHashMap<String, Object> responseMap = new LinkedHashMap<>();

    List<List<String>> data = source.getParsedData();

    String target = request.queryParams("target");
    boolean hasHeaders = Boolean.parseBoolean(request.queryParams("headers"));
    String indexType = request.queryParams("indexType");
    String index = request.queryParams("index");
    Searcher searcher = new Searcher(data, creator, hasHeaders);
    System.out.println("target: " + target);
    System.out.println("headers: " + hasHeaders);
    System.out.println("indexType: " + indexType);
    System.out.println("index: " + index);

    List<List<String>> searchResults;
    if (indexType == null && index == null) {
      searchResults = searcher.search(target);
    }
    else if (indexType == null) {
      responseMap.put("type", "error");
      responseMap.put("error_type", "Must declare index type, either 'string' or 'int', along with index: " + target);
      return adapter.toJson(responseMap);
    }
    else if (indexType.equalsIgnoreCase("string")) {
      if (creator.getHeaderRow().contains(index)) {
        searchResults = searcher.search(target, index);
      }
      else {
        responseMap.put("type", "error");
        StringBuilder columnsAvailable = new StringBuilder();
        for (String column : creator.getHeaderRow()) {
          columnsAvailable.append(column).append(", ");
        }
        columnsAvailable.setLength(columnsAvailable.length() - 2);
        responseMap.put("type", "error");
        responseMap.put("error_type", "Index not found. Columns available: " + columnsAvailable);
        return adapter.toJson(responseMap);
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
        return adapter.toJson(responseMap);
      }
      catch (IllegalArgumentException e) {
        responseMap.put("type", "error");
        responseMap.put("error_type", "Index must be greater than 0, less than " + creator.getHeaderRow().size());
        responseMap.put("index", index);
        responseMap.put("target", target);
        responseMap.put("headers", hasHeaders);
        responseMap.put("indexType", indexType);
        return adapter.toJson(responseMap);
      }
    } else {
      responseMap.put("type", "error");
      responseMap.put("error_type", "Invalid index type. Must be either 'int' or 'string'");
      responseMap.put("index", index);
      responseMap.put("target", target);
      responseMap.put("headers", hasHeaders);
      responseMap.put("indexType", indexType);
      return adapter.toJson(responseMap);
    }
    responseMap.put("type", "success");
    responseMap.put("target", target);
    responseMap.put("headers", hasHeaders);
    responseMap.put("index", index);
    responseMap.put("indexType", indexType);
    responseMap.put(
        "search_results", broadbandDataAdapter.toJson(new BroadbandData(searchResults)));
    return adapter.toJson(responseMap);
  }
}
