package edu.brown.cs.student.main.Handlers;
// TODO: See if you can merge this with any of Austin's existing classes or not.
public class CensusData {
  private String name;
  private Integer code;

  public String returnName() {
    return new String(name);
  }

  public Integer returnCode() {
    return Integer.valueOf(code);
  }
}
