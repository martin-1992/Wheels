package com.martin.entity;

//import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * 服务提供者信息
 **/
public class ProviderService implements Serializable {

    private static final long serialVersionUID = 123L;

    /**
     * 服务接口
     **/
    private Class<?> serviceInterface;

    /**
     * 服务接口的实现，注册中心不需要知道，所以该值不会被序列化
     **/
    private transient Object serviceImpl;

    //    @JsonIgnore
    private transient Method serviceMethod;

    /**
     * 服务 IP
     **/
    private String serviceIP;

    /**
     * 服务端口
     **/
    private int servicePort;

    /**
     * 超时时间
     **/
    private long timeout;

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    public Object getServiceImpl() {
        return serviceImpl;
    }

    public void setServiceImpl(Object serviceImpl) {
        this.serviceImpl = serviceImpl;
    }

    public String getServiceIP() {
        return serviceIP;
    }

    public void setServiceIP(String serviceIP) {
        this.serviceIP = serviceIP;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public Method getServiceMethod() {
        return serviceMethod;
    }

    public void setServiceMethod(Method serviceMethod) {
        this.serviceMethod = serviceMethod;
    }
}
