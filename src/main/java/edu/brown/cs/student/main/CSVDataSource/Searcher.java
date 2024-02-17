package edu.brown.cs.student.main.CSVDataSource;

import edu.brown.cs.student.main.Creators.CreatorFromString;
import java.util.ArrayList;
import java.util.List;

public class Searcher {
  private boolean hasHeaders;
  private static List<List<String>> data;
  private static CreatorFromString creator;

  private boolean matchFound = false;

  public Searcher(List<List<String>> d, CreatorFromString creatorFromString, boolean hasHeaders) {
    data = d;
    creator = creatorFromString;
    this.hasHeaders = hasHeaders;
  }

  public List<List<String>> search(String value) {
    this.matchFound = false;
    List<List<String>> returnList = new ArrayList<>();
    for (List<String> objectToSearch : data) {
      for (String s : objectToSearch) {
        if (s.toLowerCase().contains(value.toLowerCase())) {
          System.out.println(objectToSearch);
          returnList.add(objectToSearch);
          this.matchFound = true;
          break;
        }
      }
    }
    if (!this.matchFound) {
      System.out.println("No match found.");
    }
    return returnList;
  }

  public List<List<String>> search(String value, int columnIdentifier) {
    this.matchFound = false;
    List<List<String>> returnList = new ArrayList<>();
    if (columnIdentifier > data.get(0).size() || columnIdentifier < 0) {
      throw new IllegalArgumentException();
    }
    if (hasHeaders) {
      System.out.println("removing headers");
      data.remove(0);
    }
    return findMatchWithIndex(value, returnList, columnIdentifier);
  }

  public List<List<String>> search(String value, String columnIdentifier) {
    this.matchFound = false;
    List<List<String>> returnList = new ArrayList<>();
    if (hasHeaders) {
      data.remove(0);
    }
    int index = 0;
    if (creator.getHeaderRow().contains(columnIdentifier)) {
      index = creator.getHeaderRow().indexOf(columnIdentifier);
    } else {
      throw new IllegalArgumentException();
    }
    return findMatchWithIndex(value, returnList, index);
  }

  private List<List<String>> findMatchWithIndex(
      String value, List<List<String>> returnList, int index) {
    System.out.println("in find match with index");
    for (List<String> object : data) {
      if (object.get(index).toLowerCase().contains((value.toLowerCase()))) {
        System.out.println(object);
        returnList.add(object);
        this.matchFound = true;
      }
    }
    if (!this.matchFound) {
      System.out.println("No match found.");
    }
    return returnList;
  }
}
