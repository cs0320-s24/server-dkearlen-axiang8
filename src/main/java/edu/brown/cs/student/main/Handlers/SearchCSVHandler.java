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
import java.util.List;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class SearchCSVHandler implements Route {

  private final CensusDataSource source;

  public SearchCSVHandler(CensusDataSource source) {
    this.source = source;
  }

  @Override
  public Object handle(Request request, Response response) {
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    JsonAdapter<BroadbandData> broadbandDataAdapter = moshi.adapter(BroadbandData.class);
    Map<String, Object> responseMap = new HashMap<>();

    List<List<String>> data = source.getParsedData();
    CreatorFromString creator = new CreatorFromString();
    String target = request.queryParams("target");
    boolean hasHeaders = Boolean.parseBoolean(request.queryParams("headers"));
    String index = request.queryParams("index");
    Searcher searcher = new Searcher(data, creator, hasHeaders);
    data = searcher.search(target);
    responseMap.put("target", target);
    responseMap.put("headers", hasHeaders);
    responseMap.put("index", index);
    responseMap.put(
        "filtered_search_results", broadbandDataAdapter.toJson(new BroadbandData(data)));
    return adapter.toJson(responseMap);
  }
}
