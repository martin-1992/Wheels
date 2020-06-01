package com.martin.Impl;

import com.martin.entity.ProviderService;

import java.util.List;
import java.util.Random;

/**
 * 负载均衡策略算法，随机选择一个服务
 **/
public class RandomBalance extends AbstractLoadBalance {

    @Override
    public ProviderService doSelect(List<ProviderService> providerServiceList) {
        Random random = new Random();
        return providerServiceList.get(random.nextInt(providerServiceList.size()-1));
    }
}
