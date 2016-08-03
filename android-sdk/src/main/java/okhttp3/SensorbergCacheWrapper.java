package okhttp3;

import okhttp3.internal.InternalCache;

/**
 * Created by falkorichter on 02/08/16.
 */
public final class SensorbergCacheWrapper {
    private final Cache cache;


    public SensorbergCacheWrapper(Cache cache) {
        this.cache = cache;
    }

    public InternalCache getInternalCache(){
        return cache.internalCache;
    }
}
