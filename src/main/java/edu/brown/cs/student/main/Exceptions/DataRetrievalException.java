package edu.brown.cs.student.main.Exceptions;

public class DataRetrievalException extends Exception {

  public DataRetrievalException() {
    super();
  }

  public DataRetrievalException(String message) {
    super(message);
  }

  public static class DataSourceException extends Exception {
    private final Throwable cause;

    public DataSourceException(String message) {
      super(message); // Exception message
      this.cause = null;
    }

    public DataSourceException(String message, Throwable cause) {
      super(message); // Exception message
      this.cause = cause;
    }

    /**
     * Returns the Throwable provided (if any) as the root cause of this exception. We don't make a
     * defensive copy here because we don't anticipate mutation of the Throwable to be any issue,
     * and because this is mostly implemented for debugging support.
     *
     * @return the root cause Throwable
     */
    public Throwable getCause() {
      return this.cause;
    }
  }
}
