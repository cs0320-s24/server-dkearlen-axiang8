package edu.brown.cs.student.main.Handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.CSVDataSource.CSVData;
import edu.brown.cs.student.main.CSVDataSource.CSVSource;

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

    CSVData data = new CSVData(source.getParsedData());
    responseMap.put("data", csvDataAdapter.toJson(data));
    return adapter.toJson(responseMap);
  }
}
