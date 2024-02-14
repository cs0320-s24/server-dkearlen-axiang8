package edu.brown.cs.student.main.DataSource.Broadband;

import edu.brown.cs.student.main.Creators.CreatorFromString;
import edu.brown.cs.student.main.Exceptions.MalformedCSVException;
import edu.brown.cs.student.main.Parser;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class CSVSource implements CensusDataSource {

  private static List<List<String>> parsedData;

  @Override
  public List<List<String>> getBroadbandData(String filePath)
      throws IOException, MalformedCSVException {
    System.out.println("successful call to CSV Source getBroadband data");
    FileReader reader = new FileReader(filePath);
    CreatorFromString creator = new CreatorFromString();
    Parser parser = new Parser(reader, creator);
    parsedData = parser.parseCSV();
    return parsedData;
  }

  public List<List<String>> getParsedData() {
    System.out.println("successful call to get Parsed Data");
    return parsedData;
  }
  // potentially overload this method that can use API and have state code and county code as
  // parameters
}
