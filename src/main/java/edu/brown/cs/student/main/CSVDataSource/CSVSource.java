package edu.brown.cs.student.main.CSVDataSource;

import edu.brown.cs.student.main.Creators.CreatorFromString;
import edu.brown.cs.student.main.Exceptions.MalformedCSVException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class CSVSource {

  public CSVSource() {}

  private static List<List<String>> parsedData;

  public List<List<String>> retrieveAndParse(String filePath, CreatorFromString creator)
      throws IOException, MalformedCSVException {
    FileReader reader = new FileReader(filePath);
    Parser parser = new Parser(reader, creator);
    parsedData = parser.parseCSV();
    return parsedData;
  }

  public List<List<String>> getParsedData() throws IOException {
    if (parsedData == null) {
      throw new IOException();
    }
    return Collections.unmodifiableList(parsedData);
  }
  // potentially overload this method that can use API and have state code and county code as
  // parameters
}
