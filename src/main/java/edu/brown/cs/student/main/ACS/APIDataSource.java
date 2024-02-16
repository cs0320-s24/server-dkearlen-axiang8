package edu.brown.cs.student.main.ACS;

public interface APIDataSource {
  // TODO: Ensure that this exception is fixed.
  InternetAccessData getData(String state, String county) throws Exception;

}
