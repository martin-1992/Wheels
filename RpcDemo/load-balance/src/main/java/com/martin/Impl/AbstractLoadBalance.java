package com.martin.Impl;

import com.martin.entity.ProviderService;
import com.martin.ILoadBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @ClassName AbstractBalance
 * @Description TODO
 * @Author chenjiahao
 * @Date 2020/4/21 10:07
 * @Version 1.0
 **/
public abstract class AbstractLoadBalance implements ILoadBalance {

    private static final Logger logger = LoggerFactory.getLogger(AbstractLoadBalance.class);

    @Override
    public ProviderService select(List<ProviderService> providerServiceList) {
        if (providerServiceList == null || providerServiceList.isEmpty()) {
            logger.info("生产服务实现列表为空，无法进行负载均衡策略");
            return null;
        }

        // 只有一个返回
        if (providerServiceList.size() == 1) {
            return providerServiceList.get(0);
        }

        return doSelect(providerServiceList);
    }

    protected abstract ProviderService doSelect(List<ProviderService> providerServiceList);
}
