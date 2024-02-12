package edu.brown.cs.student.main.Handlers;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;
import spark.Request;
import spark.Response;
import spark.Route;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LoadCSVHandler implements Route {
    public LoadCSVHandler() {

    }

    @Override
    public Object handle(Request request, Response response) {
        // If you are interested in how parameters are received, try commenting out and
        // printing these lines! Notice that requesting a specific parameter requires that parameter
        // to be fulfilled.
        // If you specify a queryParam, you can access it by appending ?parameterName=name to the
        // endpoint
        // ex. http://localhost:3232/activity?participants=num
        //     System.out.println(params);
        File file;
        String filePath = request.queryParams("filepath");
        //     System.out.println(participants);
        if (filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        } else {
            // Create a File object to represent the input path
            file = new File(filePath);
        }

        // Ensure that the file exists and is a file (not a directory)
        if (!file.exists() || !file.isFile()) {
            return new loadFileFailedResponse().serialize();
        }

        String dataDirectory = "data";
        String fileAbsolutePath = file.getAbsolutePath();

        // Ensure that the file is within the "data" directory
        if (!fileAbsolutePath.contains(File.separator + dataDirectory)) {
            throw new IllegalArgumentException(
                    "File must be within the " + dataDirectory + " directory");
        }

        boolean success = loadCSVFile(filePath);

        if (success) {
            // If the CSV file was loaded successfully, return a success response
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("result", "success");
            responseData.put("filepath", filePath);
            return responseData;
        } else {
            // If there was an error loading the CSV file, return an error response
            return createErrorResponse("error_datasource", "Failed to load CSV file");
        }
    }

    // Dummy method to simulate loading a CSV file
    private boolean loadCSVFile(String filePath) {
        //Parser parser = new Parser();
        return true;
    }

    public record loadFileFailedResponse(String response_type) {
        public loadFileFailedResponse() {
            this("error_bad_request");
        }

        /**
         * @return this response, serialized as Json
         */
        String serialize() {
            Moshi moshi = new Moshi.Builder().build();
            return moshi.adapter(loadFileFailedResponse.class).toJson(this);
        }
    }

    public record loadFileSuccessResponse(String responseString, Map<String, Object> responseMap){
        public loadFileSuccessResponse(Map<String, Object> responseMap){
            this("success", responseMap);
        }

        String serialize(){
            try {
                // Initialize Moshi which takes in this class and returns it as JSON!
                Moshi moshi = new Moshi.Builder().build();
                JsonAdapter<loadFileSuccessResponse> adapter = moshi.adapter(loadFileSuccessResponse.class);
                return adapter.toJson(this);
            } catch (Exception e) {
                // For debugging purposes, show in the console _why_ this fails
                // Otherwise we'll just get an error 500 from the API in integration
                // testing.
                e.printStackTrace();
                throw e;
            }
        }
    }


}