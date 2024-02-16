package edu.brown.cs.student.main.Handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.ACS.ACSAPIDataSource;
import edu.brown.cs.student.main.ACS.APIDataSource;
import edu.brown.cs.student.main.ACS.InternetAccessData;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import spark.Request;
import spark.Response;
import spark.Route;

public class BroadbandHandler implements Route {

  private final APIDataSource dataSource;

  public BroadbandHandler(APIDataSource dataSource){
    this.dataSource = dataSource;
  }
  @Override
  public Object handle(Request request, Response response) throws Exception {
    // Put the arguments into a set of Strings.
    Set<String> params = request.queryParams();
    String state = request.queryParams("state");
    String county = request.queryParams("county");
    Map<String, Object> responseMap = new HashMap<>();
    // TODO: Throw better Errors in ACSAPIDataSource and handle them here (Since responseMap is here).
    // Use Moshi to turn the Map into a Json object to display on the web page.
    // TODO: Change getting the data straight from the ACSAPIDataSource to the Caching proxy class
    InternetAccessData data = this.dataSource.getData(state, county);
    System.out.println(data);
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    responseMap.put("Date", data.date().toString());
    responseMap.put("Time", data.time().toString());
    responseMap.put("County", data.county());
    responseMap.put("Percentage with Access to Internet", data.percentage());
    responseMap.put("State", data.state());
    // Return the response map in Json form to display on the server.
    return adapter.toJson(responseMap);
  }
}
