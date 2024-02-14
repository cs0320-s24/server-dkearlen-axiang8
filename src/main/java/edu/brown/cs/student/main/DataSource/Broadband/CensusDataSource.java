package edu.brown.cs.student.main.DataSource.Broadband;

import edu.brown.cs.student.main.Creators.CreatorFromString;
import edu.brown.cs.student.main.Exceptions.MalformedCSVException;

import java.io.IOException;
import java.util.List;

public interface CensusDataSource {
  // TODO: Create a defensive copy for when you call this getter method.
  List<List<String>> retrieveAndParse(String filePath, CreatorFromString creator) throws IOException, MalformedCSVException;

  List<List<String>> getParsedData();
}
