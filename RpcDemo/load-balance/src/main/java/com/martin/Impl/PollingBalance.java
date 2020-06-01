package com.martin.Impl;

import com.martin.entity.ProviderService;

import java.util.List;

/**
 * 负载均衡策略算法，轮询选择一个服务
 **/
public class PollingBalance extends AbstractLoadBalance {

    private int index = 0;

    @Override
    public ProviderService doSelect(List<ProviderService> providerServiceList) {
        synchronized (PollingBalance.class) {
            if (index == Integer.MAX_VALUE) {
                index = 0;
            }

            return providerServiceList.get(index % providerServiceList.size());
        }
    }
}
