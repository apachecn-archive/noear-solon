package org.noear.solon.cache.redisson;

import org.noear.solon.Solon;
import org.noear.solon.Utils;
import org.noear.solon.data.cache.CacheService;
import org.redisson.api.RedissonClient;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @author noear
 * @since 1.7
 */
public class RedissonCacheService implements CacheService {
    protected String _cacheKeyHead;
    protected int _defaultSeconds;
    protected final RedissonClient redissonClient;

    public RedissonCacheService(Properties prop){
        this(prop, prop.getProperty("keyHeader"), 0);
    }

    public RedissonCacheService(Properties prop, String keyHeader, int defSeconds) {
        String defSeconds_str = prop.getProperty("defSeconds");

        if (defSeconds == 0) {
            if (Utils.isNotEmpty(defSeconds_str)) {
                defSeconds = Integer.parseInt(defSeconds_str);
            }
        }

        if(Utils.isEmpty(keyHeader)){
            keyHeader = Solon.cfg().appName();
        }

        _cacheKeyHead = keyHeader;
        _defaultSeconds = defSeconds;

        if (_defaultSeconds < 1) {
            _defaultSeconds = 30;
        }

        if (Utils.isEmpty(_cacheKeyHead)) {
            _cacheKeyHead = Solon.cfg().appName();
        }

        redissonClient = RedissonBuilder.build(prop);
    }

    @Override
    public void store(String key, Object obj, int seconds) {
        redissonClient.getBucket(key).set(obj,seconds, TimeUnit.SECONDS);
    }

    @Override
    public Object get(String key) {
        return redissonClient.getBucket(key).get();
    }

    @Override
    public void remove(String key) {
        redissonClient.getBucket(key).delete();
    }
}
