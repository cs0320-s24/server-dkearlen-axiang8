package edu.brown.cs.student.main.Handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import edu.brown.cs.student.main.ACS.ACSAPIDataSource;
import edu.brown.cs.student.main.ACS.APIDataSource;
import edu.brown.cs.student.main.ACS.InternetAccessData;
import edu.brown.cs.student.main.Exceptions.DataRetrievalException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.crypto.Data;
import spark.Request;
import spark.Response;
import spark.Route;

public class BroadbandHandler implements Route {

  private final APIDataSource dataSource;
  private Map<String, Object> responseMap;


  public BroadbandHandler(APIDataSource dataSource){
    this.dataSource = dataSource;
  }
  @Override
  public Object handle(Request request, Response response){
    // Use Moshi to turn the Map into a Json object to display on the web page.
    Moshi moshi = new Moshi.Builder().build();
    Type mapStringObject = Types.newParameterizedType(Map.class, String.class, Object.class);
    JsonAdapter<Map<String, Object>> adapter = moshi.adapter(mapStringObject);
    this.responseMap = new HashMap<>();
    // Put the arguments into a set of Strings.
    String state = request.queryParams("state");
    String county = request.queryParams("county");
    try {
      if (checkParams(state, county) == -1){
        return adapter.toJson(responseMap);
      }
      // TODO: Throw better Errors in ACSAPIDataSource and handle them here (Since responseMap is here).
      InternetAccessData data = this.dataSource.getData(state, county);
      this.responseMap.put("Date", data.date().toString());
      this.responseMap.put("Time", data.time().toString());
      this.responseMap.put("County", data.county());
      this.responseMap.put("Percentage with Access to Internet", data.percentage());
      this.responseMap.put("State", data.state());
      // Return the response map in Json form to display on the server.
      return adapter.toJson(responseMap);
    } catch (IOException e){
      this.responseMap.put("error_type", "error_datasource");
      this.responseMap.put("error_message", "IOException: ");
      return adapter.toJson(this.responseMap);
    } catch (URISyntaxException e){
      this.responseMap.put("error_type", "error_datasource");
      this.responseMap.put("error_message", "URISyntaxException: State or County codes given were incorrect/API gave an error!");
      return adapter.toJson(this.responseMap);
    } catch (InterruptedException e){
      this.responseMap.put("error_type", "error_datasource");
      this.responseMap.put("error_message", "InterruptedException: HTTPResponse was interrupted at some point!");
      return adapter.toJson(this.responseMap);
    } catch (DataRetrievalException e){
      this.responseMap.put("error_type", "error_datasource");
      this.responseMap.put("error_message", "DataRetrievalException: Given state and county returned no results!");
      return adapter.toJson(this.responseMap);
    }
  }

  /**
   * @param state - the state given by the request
   * @param county - the county given by the request
   * @return - Returns an Integer specifying if there was an error. Returns a -1 on error
   * and a 0 on non-error.
   * */
  private Integer checkParams(String state, String county){
    if (state == null && county == null){
      responseMap.put("error_type", "error_bad_request");
      responseMap.put("error_message", "Both state and county were not given!");
      return -1;
    } else if (state == null){
      responseMap.put("error_type", "error_bad_request");
      responseMap.put("error_message", "State was not given!");
      return -1;
    } else if (county == null){
      responseMap.put("error_type", "error_bad_request");
      responseMap.put("error_message", "County was not given!");
      return -1;
    }
    return 0;
  }
}
