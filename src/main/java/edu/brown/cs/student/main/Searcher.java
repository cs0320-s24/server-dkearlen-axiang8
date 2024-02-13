package edu.brown.cs.student.main;

import edu.brown.cs.student.main.Creators.CreatorFromString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author devonkearleng
 * @version 1.0
 *     <p>The CSVSearcher class is the class that performs many of the main duties for user story 1.
 *     This is where we can search the parsed CSV file for a string of the user's choosing.
 */
public class Searcher {
  private String target;
  private String columnID;
  private boolean headers;
  private CreatorFromString creatorFromString;
  private List<List<String>> data;

  public Searcher(List<List<String>> data, CreatorFromString creatorFromString) {
    this.creatorFromString = creatorFromString;
    this.data = data;
  }
  /**
   * @param target - target is a string given by the user in which the search through the parsed
   *     file will be performed for
   * @return - returns a list with all the rows where the target was found.
   */
  public List<Integer> search(String target) {
    this.target = target.toLowerCase();
    List<Integer> rowsFound = new ArrayList<>();
    int startIndex = this.startIndex();
    for (int i = startIndex; i < this.data.size(); i++) {
      int rowIndex = this.iterateRow(i);
      if (rowIndex != -1) {
        rowsFound.add(rowIndex);
      }
    }
    this.emptyList(rowsFound);
    return rowsFound;
  }
  /**
   * @param target - target is a string given by the user in which the search through the parsed
   *     file will be performed for
   * @param headers - headers is a boolean given by the user to allow the search to either ensure
   *     that the first line is searched through, or to skip it entirely
   * @return - returns a list with all the rows where the target was found.
   */
  public List<Integer> search(String target, boolean headers) {
    this.target = target.toLowerCase();
    this.headers = headers;
    List<Integer> rowsFound = new ArrayList<>();
    int startIndex = this.startIndex();
    for (int i = startIndex; i < this.data.size(); i++) {
      int rowIndex = this.iterateRow(i);
      if (rowIndex != -1) {
        rowsFound.add(rowIndex);
      }
    }
    this.emptyList(rowsFound);
    return rowsFound;
  }
  /**
   * @param target - target is a string given by the user in which the search through the parsed
   *     file will be performed for
   * @param headers - headers is a boolean given by the user to allow the search to either ensure
   *     that the first line is searched through, or to skip it entirely
   * @param columnID - columnID is a string that allows the user to only look through a certain
   *     column.
   * @return - returns a list with all the rows where the target was found.
   */
  public List<Integer> search(String target, boolean headers, String columnID) {
    this.target = target.toLowerCase();
    this.columnID = columnID.toLowerCase();
    this.headers = headers;
    int columnIndex = this.searchHeaders();
    List<Integer> rowsFound = new ArrayList<>();
    if (columnIndex == -1) {
      this.emptyList(rowsFound);
      return rowsFound;
    }
    int startIndex = this.startIndex();
    for (int i = startIndex; i < this.data.size(); i++) {
      int rowIndex = this.iterateColumn(i, columnIndex);
      if (rowIndex != -1) {
        rowsFound.add(rowIndex);
      }
    }
    this.emptyList(rowsFound);
    return rowsFound;
  }
  /**
   * @param target - target is a string given by the user in which the search through the parsed
   *     file will be performed for
   * @param headers - headers is a boolean given by the user to allow the search to either ensure
   *     that the first line is searched through, or to skip it entirely
   * @param columnID - columnID is an int that allows the user to only look through a certain
   *     column.
   * @return - returns a list with all the rows where the target was found.
   */
  public List<Integer> search(String target, boolean headers, int columnID) {
    this.target = target.toLowerCase();
    this.headers = headers;
    int columnIndex = columnID;
    List<Integer> rowsFound = new ArrayList<>();
    int startIndex = this.startIndex();
    for (int i = startIndex; i < this.data.size(); i++) {
      int rowIndex = this.iterateColumn(i, columnIndex);
      if (rowIndex != -1) {
        rowsFound.add(rowIndex);
      }
    }
    this.emptyList(rowsFound);
    return rowsFound;
  }
  /**
   * @param rowID - the index of the row we want to look at.
   * @return - returns a rowID if the target was found in a particular row
   */
  private int iterateRow(int rowID) {
    // take note of how you can use the generic (declared as List<String>) to get a string.
    for (String string : this.creatorFromString.getRow(rowID)) {
      // in this if statement make both the target and string lowercase
      if (this.target.equals(string.toLowerCase())) {
        System.out.println(this.creatorFromString.getRow(rowID));
        return rowID;
      }
    }
    return -1;
  }
  /**
   * @param rowID - the index of the row we want to look at.
   * @param columnIndex - the index of the column we want to look at.
   * @return - returns a rowID if the target was found in a particular row and column.
   */
  private int iterateColumn(int rowID, int columnIndex) {
    // only need to check for getting columnIndex since that's the only user input. rest of code
    // works fine.
    String string = this.creatorFromString.getRow(rowID).get(columnIndex);
    if (this.target.equals(string.toLowerCase())) {
      System.out.println(this.creatorFromString.getRow(rowID));
      return rowID;
    }
    return -1;
  }
  /**
   * @return - returns a columnIndex if the column specified is found. if not, -1 is returned to
   *     denote an error.
   */
  private int searchHeaders() {
    int columnIndex = 0;
    for (String string : this.creatorFromString.getHeaderRow()) {
      // make both the columnID and string lower
      if (this.columnID.equals(string.toLowerCase())) {
        return columnIndex;
      }
      columnIndex++;
    }
    return -1;
  }
  /**
   * startIndex is a helper function that is called in order to set the start index for the search
   * method.
   *
   * @return - returns an integer representing when the start index should be for the data.
   */
  private int startIndex() {
    if (!this.headers) {
      return 0;
    } else if (this.headers) {
      return 1;
    }
    // even if null is returned we should just start at 0.
    return 0;
  }
  /**
   * @param list - checks if the list of rows containing the target is empty. if so, prints a
   *     message saying such.
   */
  private void emptyList(List<Integer> list) {
    if (list.isEmpty()) {
      System.out.println("No rows with target found!");
    }
  }
}
