package edu.brown.cs.student.main.Handlers;

import spark.Request;
import spark.Response;
import spark.Route;

public class ViewCSVHandler implements Route {

    public ViewCSVHandler(LoadCSVHandler load) {

    }
    @Override
    public Object handle(Request request, Response response) {
        return 1;
    }
}