package com.martin.entity;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Netty 异步调用返回结果的包装类
 **/
public class ResponseWrapper {

    /**
     * 存储返回结果的阻塞队列
     **/
    private BlockingQueue<Response> responseBlockingQueue = new ArrayBlockingQueue<>(1);

    /**
     * 结果返回时间
     **/
    private long responseTime;

    /**
     * 计算该返回结果是否已经过期
     **/
    public boolean isExpire() {
        // 结果弹出
        Response response = responseBlockingQueue.peek();
        if (response == null) {
            return false;
        }

        // 获取该结果的指定超时超时间
        long timeout = response.getConsumeTimeout();
        // 计算是否超时
        if ((System.currentTimeMillis() - responseTime) > timeout) {
            return true;
        }
        return false;
    }

    public BlockingQueue<Response> getResponseBlockingQueue() {
        return responseBlockingQueue;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }
}
