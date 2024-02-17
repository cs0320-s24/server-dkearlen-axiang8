package edu.brown.cs.student.main.ACS;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import edu.brown.cs.student.main.Exceptions.DataRetrievalException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

/**
 * @author devonkearleng
 * @version 1.0 - CachingACSAPI is a class that helps cache the inputs given by the API. If a recent
 *     search is found in the cache, it will return from the cache instead of making another input.
 */
public class CachingACSAPI implements APIDataSource {
  private final APIDataSource wrappedSource;
  private final LoadingCache<String, InternetAccessData> cache;
  /**
   * @param dataSource - The instance of the APIDataSource interface to wrap.
   * @param maxSize - The max amount of entries in the cache you want to hold at any given moment.
   * @param minutesLength - The max amount of minutes any entry should be held in the cache.
   */
  public CachingACSAPI(APIDataSource dataSource, Integer maxSize, Integer minutesLength) {
    // Set the wrappedSource to the dataSource provided.
    this.wrappedSource = dataSource;
    this.cache =
        CacheBuilder.newBuilder()
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
                      throws IOException, URISyntaxException, InterruptedException,
                          DataRetrievalException {
                    try {
                      // If this isn't yet present in the cache, load it. We will split the key
                      // since
                      // we concatenated the strings earlier.
                      String[] args = key.split(":");
                      // Return the wrappedSource's getData function if none is found in the cache.
                      return wrappedSource.getData(args[0], args[1]);
                      // All these throws will be thrown as a UncheckedExecutionException to be
                      // handled in BroadbandHandler
                    } catch (IOException e) {
                      throw new IOException();
                    } catch (URISyntaxException e) {
                      throw new URISyntaxException("catch", "bad input");
                    } catch (InterruptedException e) {
                      throw new InterruptedException();
                    } catch (DataRetrievalException e) {
                      throw new DataRetrievalException();
                    }
                  }
                });
  }

  /**
   * @param state - State given by the user
   * @param county - County given by the user
   * @return - Returns an InternetAccessData record to be extracted from in BroadbandHandler for its
   *     data.
   */
  @Override
  public InternetAccessData getData(String state, String county) {
    // Concatenate the strings since load takes only one string.
    String compositeKey = state + ":" + county;
    // Return the cache result if there is a result that matches previous results.
    return this.cache.getUnchecked(compositeKey);
  }
}
