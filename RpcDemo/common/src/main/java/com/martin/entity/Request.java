package com.martin.entity;

import java.io.Serializable;
import java.util.Arrays;

/**
 * 请求类，照抄的，需要改
 **/
public class Request implements Serializable {

    private static final long serialVersionUID = 3245L;

    /**
     * UUID，唯一标识一次返回值
     **/
    private String requestId;

    /**
     * 服务提供者信息
     **/
    private ProviderService providerService;

    /**
     * 调用的方法名称
     **/
    private String invokedMethodName;

    /**
     * 传递的参数
     **/
    private Object[] args;

    /**
     * 消费端应用名
     **/
    private String appName;

    /**
     * 消费请求超时时长
     **/
    private long invokeTimeout;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public ProviderService getProviderService() {
        return providerService;
    }

    public void setProviderService(ProviderService providerService) {
        this.providerService = providerService;
    }

    public String getInvokedMethodName() {
        return invokedMethodName;
    }

    public void setInvokedMethodName(String invokedMethodName) {
        this.invokedMethodName = invokedMethodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public long getInvokeTimeout() {
        return invokeTimeout;
    }

    public void setInvokeTimeout(long invokeTimeout) {
        this.invokeTimeout = invokeTimeout;
    }

    @Override
    public String toString() {
        return "Request{" +
                "requestId='" + requestId + '\'' +
                ", providerService=" + providerService +
                ", invokedMethodName='" + invokedMethodName + '\'' +
                ", args=" + Arrays.toString(args) +
                ", appName='" + appName + '\'' +
                ", invokeTimeout=" + invokeTimeout +
                '}';
    }
}