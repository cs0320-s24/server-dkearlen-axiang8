package edu.brown.cs.student.main.ACS;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import edu.brown.cs.student.main.Exceptions.DataRetrievalException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class CachingACSAPI implements APIDataSource {
  private final APIDataSource wrappedSource;
  private final LoadingCache<String, InternetAccessData> cache;
  public CachingACSAPI(APIDataSource dataSource, Integer maxSize, Integer minutesLength){
    // Set the wrappedSource to the dataSource provided.
    this.wrappedSource = dataSource;
    this.cache = CacheBuilder.newBuilder()
        // How many entries maximum in the cache?
        .maximumSize(maxSize)
        // How long should entries remain in the cache?
        .expireAfterWrite(minutesLength, TimeUnit.MINUTES)
        // Keep statistical info around for profiling purposes
        .recordStats()
        .build(
            // Strategy pattern: how should the cache behave when
            // it's asked for something it doesn't have?
            new CacheLoader<>() {
              @Override
              public InternetAccessData load(String key)
                  throws IOException, URISyntaxException, InterruptedException, DataRetrievalException {
                // If this isn't yet present in the cache, load it. We will split the key since
                // we concatenated the strings earlier.
                String [] args = key.split(":");
                // Return the wrappedSource's getData function.
                return wrappedSource.getData(args[0], args[1]);
              }
            });
  }

  @Override
  public InternetAccessData getData(String state, String county) {
    // Concatenate the strings since load takes only one string.
    String compositeKey = state + ":" + county;
    // Return the cache result if there is a result that matches previous results.
    return this.cache.getUnchecked(compositeKey);
  }
}
