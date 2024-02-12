package edu.brown.cs.student.main.Handlers;

import spark.Request;
import spark.Response;
import spark.Route;

public class SearchCSVHandler implements Route {

    public SearchCSVHandler(LoadCSVHandler load) {

    }

    @Override
    public Object handle(Request request, Response response) {
        return 1;
    }
}