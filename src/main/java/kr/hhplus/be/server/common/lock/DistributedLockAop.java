package kr.hhplus.be.server.common.lock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;

@RequiredArgsConstructor
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class DistributedLockAop {

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;

    @Around("@annotation(kr.hhplus.be.server.common.lock.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        List<String> keys = LockKeyGenerator.generateKeys(distributedLock.keys(), signature.getParameterNames(), joinPoint.getArgs())
                .stream()
                .toList();

        log.info("keys:  " + keys);
        RLock rLock = LockFactory.create(redissonClient, distributedLock.type(), keys);

        try {
            boolean locked = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!locked) {
                return false;
            }

            log.info("락 획득");
            return aopForTransaction.proceed(joinPoint);
        } catch (InterruptedException e) {
            throw new InterruptedException();
        } finally {
            try {
                rLock.unlock();
            } catch (IllegalMonitorStateException e) {
                log.error("IllegalMonitorStateException on unlock. key={}, msg={}", keys, e.toString());
            } finally {
                log.info("락 해제");
            }
        }
    }
}
