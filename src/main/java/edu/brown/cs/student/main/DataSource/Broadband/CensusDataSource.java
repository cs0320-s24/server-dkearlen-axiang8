package edu.brown.cs.student.main.DataSource.Broadband;

import edu.brown.cs.student.main.DataSource.DataSourceException;
import edu.brown.cs.student.main.Exceptions.MalformedCSVException;
import java.io.IOException;
import java.util.List;

public interface CensusDataSource {

  List<List<String>> getBroadbandData(String filePath)
      throws DataSourceException, IllegalArgumentException, IOException, MalformedCSVException;
  // TODO: Create a defensive copy for when you call this getter method.
  List<List<String>> getParsedData();
}
