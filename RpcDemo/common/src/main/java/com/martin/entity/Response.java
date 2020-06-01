package com.martin.entity;

import java.io.Serializable;

/**
 * 请求回复类
 **/
public class Response implements Serializable {

    private static final long serialVersionUID = 512235L;

    /**
     * 请求回复的唯一 ID
     **/
    private String responseId;

    /**
     * 客户端指定的服务超时时间
     **/
    private long consumeTimeout;

    /**
     * 接口调用返回的结果对象
     **/
    private Object result;

    public String getResponseId() {
        return responseId;
    }

    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public long getConsumeTimeout() {
        return consumeTimeout;
    }

    public void setConsumeTimeout(long consumeTimeout) {
        this.consumeTimeout = consumeTimeout;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Response{" +
                "responseId='" + responseId + '\'' +
                ", consumeTimeout=" + consumeTimeout +
                ", result=" + result +
                '}';
    }
}
