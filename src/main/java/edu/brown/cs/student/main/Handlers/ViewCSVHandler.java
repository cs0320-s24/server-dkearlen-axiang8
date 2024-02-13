package edu.brown.cs.student.main.Handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.DataSource.Broadband.BroadbandData;
import edu.brown.cs.student.main.DataSource.Broadband.CensusDataSource;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import spark.Request;
import spark.Response;
import spark.Route;

public class ViewCSVHandler implements Route {
  private final CensusDataSource source;

  public ViewCSVHandler(CensusDataSource source) {
    this.source = source;
  }

  @Override
  public Object handle(Request request, Response response) {
    Moshi moshi = new Moshi.Builder().build();
    // Replies will be Maps from String to Object. This isn't ideal; see reflection...
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    JsonAdapter<BroadbandData> broadbandDataAdapter = moshi.adapter(BroadbandData.class);
    Map<String, Object> responseMap = new HashMap<>();

    BroadbandData data = new BroadbandData(source.getParsedData());
    responseMap.put("broadband percentages", broadbandDataAdapter.toJson(data));
    return adapter.toJson(responseMap);
  }
}
