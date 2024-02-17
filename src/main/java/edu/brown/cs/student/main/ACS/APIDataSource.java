package edu.brown.cs.student.main.ACS;

import edu.brown.cs.student.main.Exceptions.DataRetrievalException;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author devonkearleng
 * @version 1.0 - The APIDataSource Interface is an interface that has only one method, called
 *     getData.
 */
public interface APIDataSource {
  /**
   * @param state - State given by the user.
   * @param county - County given by the user.
   * @return - Returns an InternetAccessData record.
   */
  InternetAccessData getData(String state, String county)
      throws IOException, URISyntaxException, InterruptedException, DataRetrievalException;
}
