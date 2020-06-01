package com.martin;

import com.martin.Impl.PollingBalance;
import com.martin.Impl.RandomBalance;
import com.martin.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据负载均衡算法，
 **/
public class loadBalanceMap {

    private static Logger logger = LoggerFactory.getLogger(loadBalanceMap.class);

    private static final Map<String, ILoadBalance> map = new ConcurrentHashMap<>();

    static {
        map.put(loadBalanceEnum.Polling.getBalanceStrategy(), new PollingBalance());
        map.put(loadBalanceEnum.Random.getBalanceStrategy(), new RandomBalance());
    }

    /**
     * 获取负载均衡策略算法
     **/
    public static ILoadBalance queryBalanceStrategy(String balanceStrategy) {
        if (StringUtils.isEmpty(balanceStrategy)) {
            return null;
        }
        return map.get(balanceStrategy);
    }
}
