package kr.hhplus.be.server.common.lock;

import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.List;

public class LockFactory {
    public static RLock create(RedissonClient client, DistributedLock.LockType type, List<String> keys){
        return switch(type){
            case SINGLE -> client.getLock(keys.get(0));
            case MULTI -> {
                RLock[] locks = keys.stream()
                        .map(k -> client.getLock(k))
                        .toArray(RLock[]::new);

                yield new RedissonMultiLock(locks);
            }
        };
    }
}
