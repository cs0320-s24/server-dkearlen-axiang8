> **GETTING STARTED:** You must start from some combination of the CSV Sprint code that you and your partner ended up with. Please move your code directly into this repository so that the `pom.xml`, `/src` folder, etc, are all at this base directory.

> **IMPORTANT NOTE**: In order to run the server, run `mvn package` in your terminal then `./run` (using Git Bash for Windows users). This will be the same as the first Sprint. Take notice when transferring this run sprint to your Sprint 2 implementation that the path of your Server class matches the path specified in the run script. Currently, it is set to execute Server at `edu/brown/cs/student/main/server/Server`. Running through terminal will save a lot of computer resources (IntelliJ is pretty intensive!) in future sprints.

# Project Details
Server, Team Members: Devon Kear-Leng (dkearlen) and Austin Xiang (axiang8). About 20 hours total.
GitHub Repo Link: https://github.com/cs0320-s24/server-dkearlen-axiang8

# Design Choices
Server is a project that has 3 main components to it: load, search, and view endpoints, an API endpoint, and a cache/proxy class that works with a class that work directly with the endpoint.
The load, search, and view endpoints are contained on their own, while the API endpoint works with the cache/proxy in order
to filter data properly. The APIDataSource also is an interface, such that the proxy class can wrap the data from the class that directly communicates with the API.

In Server.java, the load, search, and view endpoints, as well as the API endpoint, are initialized. The load, search, and view endpoints all take in the same object, a CSVSource, as a parameter.
This way, the data stored by load can be used by search and view. We also used records to store the data that we wanted to return.  In CSVSource, there is a method called retrieveandParse() that 
utilizes the Parser class and is used by LoadCSV, and the getParsedData() method is used by SearchCSV and ViewCSV that gets what had previously been loaded, returning an unmodifiable list for 
defensive programming purposes. 

# Errors/Bugs
N/A

# Tests
For broadband, we had three testing methods, one that tested normal input, specifically that it was non-case sensitive, another that tested malformed input,
and that it would return a Json with the errors listed properly, and a final method that tested the cache, specifically that even after some time,
the time returned would be the same, indicating that the data was given from the cache and not the API.

For LoadCSV, we tested load with clean data. We also tested load with data not contained in the "data" directory. We tested load with no file path given, as
well as a file that isn't found in the specified directory. Additionally, we tested for attempts to load malformed CSV. All of these return their own specific error
except for the clean data.

Since most of the error handling is handled in Load, for ViewCSV, we only needed two tests. One for viewing successfully loaded data, and one for viewing
unsuccessfully loaded data.

The testing for SearchCSV is quite extensive. We tested for searching with an unsuccessful load, searching with and without headers, searching without a target,
searching with an invalid string index, searching when the index type isn't specified, with an invalid integer index, with a string index declared as an integer,
and with an invalid index type, such as boolean.

# How to
Test for broadband are located in TestBroadband, and the program can be started by clicking the green arrow in Server. Tests for LoadCSVHandler are in TestLoad, for
SearchCSVHandler are in TestSearch, and for ViewCSVHandler in TestView.

