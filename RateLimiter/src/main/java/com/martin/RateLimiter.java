package com.martin;


/**
 * 固定速度的令牌桶算法，为简单起见，没有预支付令牌（应对暴涨流量）
 */
public class RateLimiter {

    private volatile Object lock;

    /**
     * 令牌桶中保存的最大秒数，可计算出对应的最大令牌数
     **/
    private final double maxBurstSeconds;

    /**
     * 每秒生成多少个令牌
     **/
    private final double permitsPerSecond;

    /**
     * 上次请求的时间
     **/
    private long lastRequestMicros;

    /**
     * 令牌桶中存储的令牌数
     **/
    private double storedPermits;

    /**
     * 令牌桶中存储的最大令牌数，
     **/
    private double maxPermits;

    /**
     * 每隔多少微秒生成一个令牌
     **/
    private double stableIntervalMicros;

    /**
     * 秒转换成微秒
     **/
    private final int micros = 1000000;

    private Object mutex() {
        // 双重检查锁
        Object mutex = lock;
        if (mutex == null) {
            synchronized (this) {
                if (mutex == null) {
                    mutex = lock = new Object();
                }
            }
        }
        return mutex;
    }

    /**
     * 创建一个令牌桶算法
     **/
    public static RateLimiter create(double permitsPerSecond, double maxBurstSeconds, long lastRequestMicros) {
        return new RateLimiter(permitsPerSecond, maxBurstSeconds, lastRequestMicros);
    }

    private RateLimiter (double permitsPerSecond, double maxBurstSeconds, long lastRequestMicros) {
        checkArgument(permitsPerSecond, "每秒生成的令牌数需大于 0");
        checkArgument(maxBurstSeconds, "令牌桶中的最大秒数需大于 0");
        this.maxBurstSeconds = maxBurstSeconds;
        this.permitsPerSecond = permitsPerSecond;
        // 转换成微秒，计算得出每隔多少微秒生成一个令牌
        stableIntervalMicros = micros / permitsPerSecond;
        maxPermits = permitsPerSecond * maxBurstSeconds;
        this.lastRequestMicros = lastRequestMicros;
    }

    private void checkArgument(double argument, String error) {
        if (argument <= 0) {
            throw new RuntimeException(error);
        }
    }

    public boolean tryAcquire(double requirePermits) {
        checkArgument(requirePermits, "获取的令牌数需大于 0");
        synchronized (mutex()) {
            // 生成令牌
            generatePermits(System.currentTimeMillis());
            // 令牌桶中的令牌数不足，返回 false，请求令牌失败
            if (storedPermits - requirePermits < 0) {
                System.out.printf("令牌数不足，请求的令牌: %f，令牌桶中存在的令牌: %f\n",
                        requirePermits, storedPermits);
                return false;
            }
            storedPermits -= requirePermits;
            return true;
        }
    }

    /**
     * 生成令牌桶中的令牌，惰性算法，调用时才计算生成多少个令牌
     **/
    private void generatePermits(long nowMicros) {
        // 生成的令牌数 = (当前时间 - 上次请求时间) * 每隔多少微秒生成一个令牌
        double newPermits = (nowMicros - lastRequestMicros) / stableIntervalMicros;
        // 新生成的令牌，最大不能超过 maxPermits
        storedPermits = Math.min(maxPermits, storedPermits + newPermits);
        System.out.printf("新生成的令牌: %f，存储的令牌: %f \n", newPermits, storedPermits);
        // 重新设置上次请求时间，用于计算下次请求间隔要生成多少个新令牌
        lastRequestMicros = nowMicros;
    }
}
