package edu.brown.cs.student.main.Exceptions;


import javax.xml.crypto.Data;

public class DataRetrievalException extends Exception{

  public DataRetrievalException(){
    super();
  }
  public DataRetrievalException(String message) {
    super(message);
  }
}
