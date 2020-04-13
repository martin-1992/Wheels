package com.martin;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;


/**
 * 时间轮
 */
public class HashedWheelTimer {

    /**
     * 时间轮
     **/
    private final HashedWheelBucket[] wheel;

    private Thread workerThread;

    /**
     * 待添加到时间轮中的定时任务队列
     **/
    private final Queue<HashedWheelTimeout> timeouts = new LinkedList<>();

    /**
     * 启动时间
     **/
    private volatile long startTime;

    private final Worker worker = new Worker();

    /**
     * 只允许一个线程运行，保证线程安全
     **/
    private CountDownLatch startTimeInitialized = new CountDownLatch(1);

    /**
     * 用于计算时间轮格子数的索引
     **/
    private final int mask;

    /**
     * 一个时间轮格子代表的时间大小
     **/
    private final long tickDuration;

    /**
     * 线程状态
     **/
    private static final AtomicIntegerFieldUpdater<HashedWheelTimer> WORKER_STATE_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimer.class, "workerState");

    private volatile int workerState;

    /**
     * 线程状态，初始化
     **/
    private static final int WORKER_STATE_INIT = 0;

    /**
     * 线程状态，已启动
     **/
    private static final int WORKER_STATE_STARTED = 1;

    /**
     * 线程状态，已关闭
     **/
    private static final int WORKER_STATE_SHUTDOWN = 2;

    private final int tasks = 10000;

    public HashedWheelTimer() {
        this(100, TimeUnit.MILLISECONDS, 512);
    }

    /**
     * @Description 时间轮的构造函数
     * @Param threadFactory: 线程工厂，创建 worker 线程
     * @Param tickDuration: 时间轮的基本时间跨度，即指针多久转一格
     * @Param timeUnit: tickDuration 的时间单位
     * @Param ticksPerWheel: 时间轮的格子数，即一圈有多少格
     * @return
     **/
    public HashedWheelTimer (long tickDuration, TimeUnit timeUnit,
                             int ticksPerWheel) {
        // 创建时间轮，用于存储定时任务的环形队列，底层用数组实现
        wheel = createWheel(ticksPerWheel);
        // 新线程，用于启动时间轮
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        workerThread = threadFactory.newThread(worker);
        this.tickDuration = timeUnit.toNanos(tickDuration);
        this.mask = wheel.length - 1;
    }

    /**
     * 创建时间轮，为 HashedWheelBucket 数组，Netty 中会保证输入的 ticksPerWheel
     * 为 2 的幂，这里简化了，所以不能使用位运算，而是用 % 来获取
     **/
    private static HashedWheelBucket[] createWheel(int ticksPerWheel) {
        // 创建 HashedWheelBucket 的数组，为时间轮
        HashedWheelBucket[] wheel = new HashedWheelBucket[ticksPerWheel];
        // 进行初始化
        for (int i = 0; i < wheel.length; i++) {
            wheel[i] = new HashedWheelBucket();
        }
        return wheel;
    }

    /**
     * 当有新任务添加时，会启动时间轮，即在 newTimeout() 中调用
     **/
    private void start() {
        // 获取时间轮的状态
        switch (WORKER_STATE_UPDATER.get(this)) {
            case WORKER_STATE_INIT:
                // 时间轮为初始化，则启动 worker 线程来启动时间轮，使用 CAS 来更新状态，保证线程安全
                if (WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_INIT, WORKER_STATE_STARTED)) {
                    // 启动时间轮时
                    workerThread.start();
                }
                break;
            case WORKER_STATE_STARTED:
                // 时间轮已启动，则跳过
                break;
            case WORKER_STATE_SHUTDOWN:
                // 时间轮已关闭，则抛出异常
                throw new IllegalStateException("时间轮已关闭");
            default:
                throw new Error("不合法的时间轮状态");
        }

        // 等待 worker 线程初始化时间轮的启动时间，在 run() 方法中的 startTime = System.nanoTime();
        // 防止 newTimeout 在计算任务的 deadline 时，startTime 为 0
        while (startTime == 0) {
            try {
                startTimeInitialized.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 线程停止，将未处理完的任务添加到 unprocessedTimeouts
     **/
    public Set<HashedWheelTimeout> stop() {
        if (!WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_STARTED, WORKER_STATE_SHUTDOWN)) {
            // 已停止，返回空集合
            return Collections.emptySet();
        }

        // 停止 worker 线程
        boolean interrupted = false;
        while (workerThread.isAlive()) {
            workerThread.interrupt();

            try {
                workerThread.join(100);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        return worker.unprocessedTimeouts;
    }

    /**
     * 将定时任务保证成 HashedWheelTimeout，添加到待处理的定时任务队列中
     **/
    public HashedWheelTimeout newTimeout(TimerTask task, long delay, TimeUnit unit) {
        // 启动时间轮
        start();
        // 计算定时任务的定时时间（相对的）
        long deadline = System.nanoTime() + unit.toNanos(delay) - startTime;
        System.out.println(startTime);
        System.out.println("newTimeout deadline: " + deadline);
        // 将定时任务包装成 HashedWheelTimeout 类，它是一个节点，能添加到 HashedWheelBucket 链表中
        HashedWheelTimeout timeout = new HashedWheelTimeout(task, deadline);
        // 添加到待处理的定时任务队列中
        timeouts.add(timeout);
        return timeout;
    }

    private final class Worker implements Runnable {
        /**
         * 还没执行的定时任务集合
         **/
        private final Set<HashedWheelTimeout> unprocessedTimeouts = new HashSet<HashedWheelTimeout>();

        private long tick = 0;

        @Override
        public void run() {
            // 设置启动时间
            startTime = System.nanoTime();
            System.out.println("run() startTime: " + startTime);
            // 唤醒阻塞在 start() 的线程
            startTimeInitialized.countDown();
            // 核心方法，线程为已启动，则执行时间格的任务
            while (WORKER_STATE_UPDATER.get(HashedWheelTimer.this) == WORKER_STATE_STARTED) {
                final long deadline = waitForNextTick();
                if (deadline > 0) {
                    // 获取对应的时间格
                    int index = (int) (tick % mask);
                    HashedWheelBucket bucket = wheel[index];
                    // 从任务队列中获取定时任务，根据定时时间计算格子索引，存入对应的格子中
                    transferTimeoutsToBuckets();
                    // 执行当前格子的定时任务
                    bucket.expireTimeouts(deadline);
                    // 执行下个格子的定时任务
                    tick++;
                }
            }

            // 当线程取消时（不等于 WORKER_STATE_STARTED），遍历时间轮，将每个格子中未过期
            // 和未取消的定时任务重新添加到待处理的任务队列 unprocessedTimeouts
            for (HashedWheelBucket bucket : wheel) {
                bucket.clearTimeouts(unprocessedTimeouts);
            }

            // 从定时任务队列中获取定时任务添加到待处理的任务队列 unprocessedTimeouts
            while (true) {
                if (timeouts.isEmpty()) {
                    break;
                }
                HashedWheelTimeout timeout = timeouts.poll();
                // 定时任务队列为空
                if (timeout == null) {
                    break;
                }
                unprocessedTimeouts.add(timeout);
            }
        }

        /**
         * 判断，如果还没到当前格子的执行时间，则先 sleep 到指定时间，在返回
         **/
        private long waitForNextTick() {
            long deadline = tickDuration * (tick + 1);

            while (true) {
                // 当前的相对时间，距离 startTime 的时间差
                final long currentTime = System.nanoTime() - startTime;
                // 如果 deadline 比 currentTime 大，表示当前时间还没到 deadline，没到时间不能处理任务
                // 获取当前时间（相对的），截止时间减去当前时间，表示还需要 sleep 的时间，除以 1000000 转为毫秒，这里
                // 加上 999999，为四舍五入，比如 deadline - currentTime=2000007，转为毫秒则为 2ms，但还缺 7 到达 deadline
                // 时间点，所以加上 999999，多睡一毫秒
                long sleepTimeMs = (deadline - currentTime + 999999) / 1000000;
                System.out.println("waitForNextTick sleepTimeMs: " + sleepTimeMs);
                // 已到定时任务的时间，不需要休眠
                if (sleepTimeMs <= 0) {
                    return currentTime;
                }

                // 还没到定时任务的时间，先 sleep
                try {
                    Thread.sleep(sleepTimeMs);
                } catch (InterruptedException e) {
                    // 当主线程关闭 worker 线程时，worker 线程检测到 WORKER_STATE_SHUTDOWN，则返回负数 Long.MIN_VALUE，
                    // 在 run() 中 while 循环不会继续调用
                    return Long.MIN_VALUE;
                }
            }
        }

        /**
         * 从任务队列中获取定时任务，根据定时时间计算格子索引，存入对应的格子中
         **/
        private void transferTimeoutsToBuckets() {
            for (int i = 0; i < tasks; i++) {
                // 从定时任务队列中获取定时任务
                HashedWheelTimeout timeout = timeouts.poll();
                // 定时任务队列为空
                if (timeout == null) {
                    return;
                }

                // 根据定时时间，计算需要经过多少个格子，比如定时时间为 700，一个格子的时间刻
                // 度 tickDuration 为 20，则需要 700 / 20 = 35，即经过 35 个格子
                long calculated = timeout.getDeadline() / tickDuration;
                // 计算需要绕多少轮，假设一个时间轮有 20 个格子，即 wheel.length = 20，tick 为
                // 当前的格子，假设为 3，则 （35 - 3） / 20，还需要绕一轮
                timeout.setRemainingRounds((calculated - tick) / wheel.length);
                // 如果该任务放在任务队列 timeouts 太久，过了执行时间，就放入当前的格子执行，
                // 比如，当前格子 tick 为 5，而 calculated 为 3，则将该任务 timeout 添加到 5
                // 的格子中进行
                final long ticks = Math.max(calculated, tick);
                // 获取该格子对应的任务
                HashedWheelBucket bucket = wheel[(int) ticks % mask];
                // 将该定时任务添加到格子中
                bucket.addTimeout(timeout);
            }
        }
    }
}

