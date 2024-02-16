package edu.brown.cs.student.main;

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
    System.out.println("searching default");
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
    if (hasHeaders) {
      data.remove(0);
    }
    System.out.println("header row size " + creator.getHeaderRow().size());
    if (columnIdentifier > creator.getHeaderRow().size() || columnIdentifier < 0) {
      throw new IllegalArgumentException();
    }
    System.out.println("at bottom of function");
    return findMatchWithIndex(value, returnList, columnIdentifier);
  }

  public List<List<String>> search(String value, String columnIdentifier) {
    System.out.println("searching String colId. target: " + value + " colId: " + columnIdentifier);
    this.matchFound = false;
    List<List<String>> returnList = new ArrayList<>();
    if (hasHeaders) {
      System.out.println("in has headers");
      data.remove(0);
    }
    int index = 0;
    if (creator.getHeaderRow().contains(columnIdentifier)) {
      index = creator.getHeaderRow().indexOf(columnIdentifier);
    } else {
      System.out.println("throwing exception");
      throw new IllegalArgumentException();
    }
    System.out.println("at bottom of function");
    return findMatchWithIndex(value, returnList, index);
  }

  private List<List<String>> findMatchWithIndex(
      String value, List<List<String>> returnList, int index) {
    System.out.println("in findMatchWithIndex");
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
