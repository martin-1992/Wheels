package com.martin;

import com.martin.entity.ProviderService;

import java.util.List;

/**
 * 负载均衡算法接口
 **/
public interface ILoadBalance {

    /**
     * 从生产者提供的服务列表中，选取一个
     **/
    ProviderService select(List<ProviderService> providerServiceList);
}
