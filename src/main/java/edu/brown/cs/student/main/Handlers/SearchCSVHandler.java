package edu.brown.cs.student.main.Handlers;

import edu.brown.cs.student.main.DataSource.Broadband.CensusDataSource;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;

public class SearchCSVHandler implements Route {

    private final CensusDataSource source;
    public SearchCSVHandler(CensusDataSource source) {
        this.source = source;
    }

    @Override
    public Object handle(Request request, Response response) {
        return 1;
    }
}