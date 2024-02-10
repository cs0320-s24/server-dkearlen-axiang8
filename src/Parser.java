package edu.brown.cs.student.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CSVParser<T> {

    private BufferedReader reader;
    private CreatorFromRow<T> creator;
    private static final Pattern regexSplitCSVRow =
            Pattern.compile(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*(?![^\\\"]*\\\"))");

    public CSVParser(Reader data, CreatorFromRow<T> creator) {
        this.reader = new BufferedReader(data);
        this.creator = creator;
    }

    public List<T> parseCSV() throws IOException, MalformedCSVException {
        List<T> list = new ArrayList<T>();
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

                T object = creator.create(row);
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
