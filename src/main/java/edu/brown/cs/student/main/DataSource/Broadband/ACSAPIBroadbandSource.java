package edu.brown.cs.student.main.DataSource.Broadband;

import edu.brown.cs.student.main.DataSource.DataSourceException;
import edu.brown.cs.student.main.Exceptions.MalformedCSVException;
import java.io.IOException;
import java.util.List;

public class ACSAPIBroadbandSource implements CensusDataSource {

  @Override
  public List<List<String>> getBroadbandData(String filePath)
      throws DataSourceException, IllegalArgumentException, IOException, MalformedCSVException {
    return null;
  }

  @Override
  public List<List<String>> getParsedData() {
    return null;
  }
}
