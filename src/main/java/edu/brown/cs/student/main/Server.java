package edu.brown.cs.student.main;

import static spark.Spark.after;


import edu.brown.cs.student.main.Handlers.BroadbandHandler;

import edu.brown.cs.student.main.DataSource.Broadband.ACSAPIBroadbandSource;
import edu.brown.cs.student.main.DataSource.Broadband.CensusDataSource;

import edu.brown.cs.student.main.Handlers.LoadCSVHandler;
import edu.brown.cs.student.main.Handlers.SearchCSVHandler;
import edu.brown.cs.student.main.Handlers.ViewCSVHandler;
import spark.Spark;

/**
 * Top-level class for this demo. Contains the main() method which starts Spark and runs the various
 * handlers (2).
 *
 * <p>Notice that the OrderHandler takes in a state (menu) that can be shared if we extended the
 * restaurant They need to share state (a menu). This would be a great opportunity to use dependency
 * injection. If we needed more endpoints, more functionality classes, etc. we could make sure they
 * all had the same shared state.
 */
public class Server {


  // TODO 0: Read through this class and determine the shape of this project...
  // What are the endpoints that we can access... What happens if you go to them?

  private final CensusDataSource source;
  static final int port = 3030;

  public Server(CensusDataSource dataSource) {
    source = dataSource;
    Spark.port(port);

    after(
        (request, response) -> {
          response.header("Access-Control-Allow-Origin", "*");
          response.header("Access-Control-Allow-Methods", "*");
        });

    LoadCSVHandler load = new LoadCSVHandler(this.source);
    Spark.get("loadcsv", load);

    // TODO: Ensure that passing this.source to SearchCSVHandler and ViewCSVHandler is necessary.
    Spark.get("searchcsv", new SearchCSVHandler(this.source));
    Spark.get("viewcsv", new ViewCSVHandler(this.source));
    Spark.get("broadband", new BroadbandHandler());

    Spark.init();
    Spark.awaitInitialization();
  }

  public static void main(String[] args) {

    /*
       Setting CORS headers to allow cross-origin requests from the client; this is necessary for the client to
       be able to make requests to the server.

       By setting the Access-Control-Allow-Origin header to "*", we allow requests from any origin.
       This is not a good idea in real-world applications, since it opens up your server to cross-origin requests
       from any website. Instead, you should set this header to the origin of your client, or a list of origins
       that you trust.

       By setting the Access-Control-Allow-Methods header to "*", we allow requests with any HTTP method.
       Again, it's generally better to be more specific here and only allow the methods you need, but for
       this demo we'll allow all methods.

       We recommend you learn more about CORS with these resources:
           - https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS
           - https://portswigger.net/web-security/cors
    */

    // Setting up the handler for the GET /order and /activity endpoints

    // Notice this link alone leads to a 404... Why is that?
    Server server = new Server(new ACSAPIBroadbandSource());
    System.out.println("Server started at http://localhost:" + port);
  }
}
