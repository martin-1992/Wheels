package com.martin;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class main {

    public static void main(String[] args) throws InterruptedException {
        // qps 设置为 5，一秒钟生成 5 个令牌，处理 5 个请求
        double permitsPerSecond = 5;
        // 令牌桶中最大保存令牌数为，permitsPerSecond * maxBurstSeconds
        double maxBurstSeconds = 2;
        // 设置令牌桶中的秒表起始时间，设置为 5 秒前，当线程调用时，会有 5 秒的时间来产生的令牌
        long lastRequestMicros = System.currentTimeMillis() - (5 * 1000 * 1000);

        RateLimiter rateLimiter = RateLimiter.create(permitsPerSecond, maxBurstSeconds,
                lastRequestMicros);
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        int nTasks = 15;
        CountDownLatch countDownLatch = new CountDownLatch(nTasks);

        long start = System.currentTimeMillis();
        // 失败计数
        AtomicInteger failCount = new AtomicInteger();
        // 成功计数
        AtomicInteger successCount = new AtomicInteger();

        for (int i = 0; i < nTasks; i++) {
            executorService.submit(() -> {
                if (!rateLimiter.tryAcquire(1)) {
                    System.out.printf("获取令牌失败次数: %d\n", failCount.addAndGet(1));
                } else {
                    System.out.printf("成功获取令牌次数: %d\n", successCount.addAndGet(1));
                }
                try {
                    // 每个任务耗时 1000 微秒
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
                countDownLatch.countDown();
            });
        }
        executorService.shutdown();
        countDownLatch.await();
        System.out.println("耗时: " + (System.currentTimeMillis() - start));
    }
}
