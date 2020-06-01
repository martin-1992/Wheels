package com.martin.zookeeper;


import com.martin.entity.ProviderService;

import java.util.List;
import java.util.Map;


/**
 * 服务端注册中心，生产者接口定义
 **/
public interface IRegisterCenterForProvider {

    /**
     * 将生产者注册到注册中心
     **/
    void registerProvider(final List<ProviderService> providerServiceList);

    /**
     * 从注册中心获取生产者的服务实现列表
     **/
    Map<String, List<ProviderService>> getZkProviderServiceMap();
}
