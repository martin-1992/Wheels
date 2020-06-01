package com.martin.zookeeper;

import com.martin.entity.ConsumerService;
import com.martin.entity.ProviderService;

import java.util.List;
import java.util.Map;

/**
 * 服务端注册中心，消费者接口定义
 **/
public interface IRegisterCenterForConsumer {

    /**
     * 将生产者信息缓存到本地，用于消费端从本地获取，在通过负载均衡发起请求调用
     **/
    void initProviderMap(String path);

    /**
     * 消费端获取服务提供者信息
     **/
    Map<String, List<ProviderService>> getProviderServiceMapForConsume();

    /**
     * 消费端将消费者信息注册到 zookeeper 对应的节点下
     **/
    void registerConsumer(final ConsumerService consumerService);
}
