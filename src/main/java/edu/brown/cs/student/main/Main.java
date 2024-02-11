package edu.brown.cs.student.main;

import java.io.*;
import java.util.List;
import java.util.Scanner;

public final class Main {

    public static void main(String[] args) {
        CreatorString creatorString = new CreatorString();
        boolean exitRequested = false;

        while (!exitRequested) {
            try {
                Scanner scanner = new Scanner(System.in);
                String filePath = validateAndCreateFileReader(scanner);

                if (filePath.equalsIgnoreCase("exit")) {
                    exitRequested = true;
                    continue;
                }
                boolean hasHeaders = getHeadersInput(scanner);

                CSVParser<List<String>> parser = new CSVParser<>(new FileReader(filePath), creatorString);
                List<List<String>> list = parser.parseCSV();
                CSVSearch<Object> csvSearch = new CSVSearch<>(list, creatorString, hasHeaders);

                boolean continueSearching = true;

                while (continueSearching) {
                    System.out.println(
                            "Enter string to search for (case-insensitive). If the row contains the string, it will be printed.");
                    String value = scanner.nextLine();

                    System.out.println("Insert a column identifier. Type 'string', 'int', or 'none'.");
                    String dataType = scanner.nextLine().toLowerCase();

                    switch (dataType) {
                        case "exit":
                            exitRequested = true;
                            continueSearching = false;
                            break;
                        case "string":
                            System.out.println("Enter a string column identifier:");
                            String stringColumnIdentifier = scanner.nextLine();
                            csvSearch.search(value, stringColumnIdentifier);
                            break;
                        case "int":
                            System.out.println("Enter an integer column identifier:");
                            if (scanner.hasNextInt()) {
                                int intColumnIdentifier = scanner.nextInt();
                                csvSearch.search(value, intColumnIdentifier);
                            } else {
                                System.out.println("Invalid input. Expected an integer.");
                                scanner.nextLine(); // Consume the invalid input
                            }
                            break;
                        case "none":
                            csvSearch.search(value);
                            break;
                        default:
                            System.out.println("Invalid data type. Expected 'string', 'int', or 'none'.");
                            break;
                    }

                    // Check user input for continuation
                    if (continueSearching) {
                        System.out.println(
                                "Type 'exit' if done. To restart search in a new file, type 'r'. To continue searching in the same file, type 'c'.");
                        String userInput = scanner.nextLine();

                        if (userInput.equalsIgnoreCase("exit")) {
                            continueSearching = false;
                        } else if (userInput.equalsIgnoreCase("r")) {
                            break; // Exit the inner loop to restart the whole process
                        } else if (userInput.equalsIgnoreCase("c")) {
                            continue;
                        } else {
                            continueSearching = false;
                        }
                    }
                }

            } catch (FileNotFoundException e) {
                System.err.println("File not found. Please provide a valid file path.");
            } catch (MalformedCSVException e) {
                System.err.println("Malformed CSV file. Please check the file format.");
            } catch (IOException e) {
                System.err.println("Error reading file.");
            } catch (FactoryFailureException e) {
                System.err.println("Error creating object from row.");
            }
        }

        System.out.println("Exiting the program.");
    }

    private static boolean getHeadersInput(Scanner scanner) {
        boolean validBool = false;
        boolean hasHeaders = false;

        while (!validBool) {
            System.out.println("Does the file have headers? Type 'true' or 'false'.");
            String headersInput = scanner.nextLine().toLowerCase();

            if (headersInput.equals("true") || headersInput.equals("false")) {
                hasHeaders = Boolean.parseBoolean(headersInput);
                validBool = true;
            } else {
                System.out.println("Invalid boolean.");
            }
        }
        return hasHeaders;
    }

    private static String validateAndCreateFileReader(Scanner scanner)
            throws IllegalArgumentException, IOException {
        boolean successfulInput = false;
        String filePath = null;

        while (!successfulInput) {
            System.out.println("Enter file directory. Type 'exit' to quit.");
            filePath = scanner.nextLine();

            if (filePath.equalsIgnoreCase("exit")) {
                System.out.println("Exiting the program.");
                System.exit(0); // Terminate the program
            } else if (filePath.isEmpty()) {
                throw new IllegalArgumentException("File path cannot be null or empty");
            } else {
                // Create a File object to represent the input path
                File file = new File(filePath);

                // Ensure that the file exists and is a file (not a directory)
                if (!file.exists() || !file.isFile()) {
                    throw new IllegalArgumentException("Invalid file path: " + filePath);
                }

                String dataDirectory = "data";
                String fileAbsolutePath = file.getAbsolutePath();

                // Ensure that the file is within the "data" directory
                if (!fileAbsolutePath.contains(File.separator + dataDirectory)) {
                    throw new IllegalArgumentException(
                            "File must be within the " + dataDirectory + " directory");
                } else {
                    successfulInput = true;
                }
            }
        }
        return filePath;
    }
}