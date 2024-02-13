package edu.brown.cs.student.main;

import edu.brown.cs.student.main.Creators.CreatorFromString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Searcher {
  private boolean hasHeaders;
  private List<List<String>> data;
  CreatorFromString creatorFromString;

  private boolean matchFound = false;

  public Searcher(List<List<String>> data, CreatorFromString creatorFromString, boolean hasHeaders) {
    this.data = data;
    this.creatorFromString = creatorFromString;
    this.hasHeaders = hasHeaders;
  }

  public List<List<String>> search(String value) {
    this.matchFound = false;
    List<List<String>> returnList = new ArrayList<>();
    for (List<String> objectToSearch : this.data) {
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
      this.data.remove(0);
    }
    if (columnIdentifier > this.creatorFromString.getHeaderRow().size()) {
      throw new IllegalArgumentException();
    }
    return findMatchWithIndex(value, returnList, columnIdentifier);
  }

  public List<List<String>> search(String value, String columnIdentifier) {
    this.matchFound = false;
    List<List<String>> returnList = new ArrayList<>();
    if (hasHeaders) {
      this.data.remove(0);
    }
    int index = 0;
    if (this.creatorFromString.getHeaderRow().contains(columnIdentifier)) {
      index = this.creatorFromString.getHeaderRow().indexOf(columnIdentifier);
    } else {
      throw new IllegalArgumentException();
    }
    return findMatchWithIndex(value, returnList, index);
  }

  private List<List<String>> findMatchWithIndex(
          String value, List<List<String>> returnList, int index) {
    for (List<String> object : this.data) {
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