package edu.brown.cs.student.main.Creators;

import edu.brown.cs.student.main.Exceptions.FactoryFailureException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CreatorFromString implements CreatorFromRow<List<String>> {
  private List<List<String>> rows;

  public CreatorFromString() {
    this.rows = new ArrayList<>();
  }
  /**
   * @param row - inputted by the CSVParser class
   * @return - returns a list of Strings, as declared in the implementation
   */
  @Override
  public List<String> create(List<String> row) throws FactoryFailureException {
    this.rows.add(row);
    return row;
  }

  /**
   * @param rowID - a rowID to get from the rows.
   * @return - returns a List<String> to use in CSVSearcher
   */
  public List<String> getRow(Integer rowID) {
    return Collections.unmodifiableList(this.rows.get(rowID));
  }

  /**
   * @return - returns a List<String> to use in CSVSearcher. Specifically the first row.
   */
  public List<String> getHeaderRow() {
    return Collections.unmodifiableList(rows.get(0));
  }
}
