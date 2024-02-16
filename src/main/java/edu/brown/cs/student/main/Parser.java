package edu.brown.cs.student.main;

import edu.brown.cs.student.main.Creators.CreatorFromString;
import edu.brown.cs.student.main.Exceptions.FactoryFailureException;
import edu.brown.cs.student.main.Exceptions.MalformedCSVException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Parser {

  private BufferedReader reader;
  private static CreatorFromString creator;
  private static final Pattern regexSplitCSVRow =
      Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");

  public Parser(Reader data, CreatorFromString cr) {
    this.reader = new BufferedReader(data);
    creator = cr;
  }
  /* TODO: I'm cool with using your parser. I just copied my files to add some structure since I think the document said
   *   we should be adding all of this. Just clone your MalformedCSVException if we're using this parser. */

  public List<List<String>> parseCSV() throws IOException, MalformedCSVException {
    List<List<String>> list = new ArrayList<>();
    try {
      int numCols = -1;
      String line;
      while ((line = reader.readLine()) != null) {
        List<String> row = parseLine(line);

        if (numCols == -1) {
          numCols = row.size();
        } else if (row.size() != numCols) {
          throw new MalformedCSVException();
        }

        List<String> object = creator.create(row);
        list.add(object);
      }
    } catch (IOException e) {
      System.err.println("Error reading file.");
    } catch (FactoryFailureException e) {
      System.err.println("Error creating object from row.");
    }
    return list;
  }

  private List<String> parseLine(String line) {
    // Use the provided regular expression to split CSV rows
    String[] values = regexSplitCSVRow.split(line);
    List<String> row = new ArrayList<>();
    for (String value : values) {
      row.add((postprocess(value.trim())));
    }
    return row;
  }

  private static String postprocess(String arg) {
    return arg
        // Remove extra spaces at beginning and end of the line
        .trim()
        // Remove a beginning quote, if present
        .replaceAll("^\"", "")
        // Remove an ending quote, if present
        .replaceAll("\"$", "")
        // Replace double-double-quotes with double-quotes
        .replaceAll("\"\"", "\"");
  }
}
