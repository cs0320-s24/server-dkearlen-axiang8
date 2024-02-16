package edu.brown.cs.student.main.ACS;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class CachingACSAPI implements APIDataSource {
  private final APIDataSource wrappedSource;
  private final LoadingCache<String, InternetAccessData> cache;
  public CachingACSAPI(APIDataSource dataSource, String state, String county){
    this.wrappedSource = dataSource;

    this.cache = CacheBuilder.newBuilder()
        // How many entries maximum in the cache?
        .maximumSize(10)
        // How long should entries remain in the cache?
        .expireAfterWrite(1, TimeUnit.MINUTES)
        // Keep statistical info around for profiling purposes
        .recordStats()
        .build(
            // Strategy pattern: how should the cache behave when
            // it's asked for something it doesn't have?
            new CacheLoader<>() {
              @Override
              public InternetAccessData load(String key) throws Exception {
                // If this isn't yet present in the cache, load it:
                return wrappedSource.getData(state, county);
              }
            });
  }

  @Override
  public InternetAccessData getData(String state, String county) throws Exception {
    // TODO: Return something here.
    return null;
  }
}
