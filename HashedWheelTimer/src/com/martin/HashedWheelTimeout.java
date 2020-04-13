package com.martin;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * 单个任务
 **/
public class HashedWheelTimeout {

    /**
     * 截止时间
     **/
    private long deadline;

    /**
     * 剩余轮数
     **/
    private long remainingRounds;

    /**
     * 要执行的任务
     **/
    private final TimerTask task;

    /**
     * 定时任务状态
     **/
    private volatile int state = ST_INIT;

    /**
     * 定时任务状态，初始化
     **/
    private static final int ST_INIT = 0;

    /**
     * 定时任务状态，过期
     **/
    private static final int ST_EXPIRED = 2;

    private static final AtomicIntegerFieldUpdater<HashedWheelTimeout> STATE_UPDATER =
            AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimeout.class, "state");

    HashedWheelTimeout(TimerTask task, long deadline) {
        this.task = task;
        this.deadline = deadline;
    }

    long getDeadline() {
        return deadline;
    }

    long getRemainingRounds() {
        return remainingRounds;
    }

    void setRemainingRounds(long remainingRounds) {
        this.remainingRounds = remainingRounds;
    }

    int getState() {
        return state;
    }

    boolean isExpired() {
        return getState() == ST_EXPIRED;
    }

    /**
     * 使用 CAS 设置定时任务状态为已过期，并调用 worker 线程执行定时任务
     **/
    void expire() {
        // 如果预期不是初始化，则不能执行任务
        if (!STATE_UPDATER.compareAndSet(this, ST_INIT, ST_EXPIRED)) {
            return;
        }

        try {
            task.run(this);
        } catch (Throwable t) {
            System.out.println(t);
        }
    }
}
