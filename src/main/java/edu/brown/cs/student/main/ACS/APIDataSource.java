package edu.brown.cs.student.main.ACS;

import edu.brown.cs.student.main.Exceptions.DataRetrievalException;
import java.io.IOException;
import java.net.URISyntaxException;

public interface APIDataSource {
  // TODO: Ensure that this exception is fixed.
  InternetAccessData getData(String state, String county)
      throws IOException, URISyntaxException, InterruptedException, DataRetrievalException;

}
