> **GETTING STARTED:** You must start from some combination of the CSV Sprint code that you and your partner ended up with. Please move your code directly into this repository so that the `pom.xml`, `/src` folder, etc, are all at this base directory.

> **IMPORTANT NOTE**: In order to run the server, run `mvn package` in your terminal then `./run` (using Git Bash for Windows users). This will be the same as the first Sprint. Take notice when transferring this run sprint to your Sprint 2 implementation that the path of your Server class matches the path specified in the run script. Currently, it is set to execute Server at `edu/brown/cs/student/main/server/Server`. Running through terminal will save a lot of computer resources (IntelliJ is pretty intensive!) in future sprints.

# Project Details
Server, Team Members: Devon Kear-Leng (dkearlen) and Austin Xiang (axiang8). About 15 hours total.
GitHub Repo Link: https://github.com/cs0320-s24/server-dkearlen-axiang8
# Design Choices
Server is a project that has 3 main components to it: load, search, and view endpoints, an API endpoint, and a cache/proxy class that works with a class that work directly with the endpoint.
The load, search, and view endpoints are contained on their own, while the API endpoint works with the cache/proxy in order
to filter data properly. The APIDataSource also is an interface, such that the proxy class can wrap the data from the class that directly communicates with the API.
# Errors/Bugs
N/A
# Tests
For broadband, we had three testing methods, one that tested normal input, specifically that it was non-case sensitive, another that tested malformed input,
and that it would return a Json with the errors listed properly, and a final method that tested the cache, specifically that even after some time,
the time returned would be the same, indicating that the data was given from the cache and not the API.
# How to
Test for broadband are located in TestBroadband, and the program can be started by clicking the green arrow in Server.

