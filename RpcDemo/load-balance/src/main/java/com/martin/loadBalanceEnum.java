package com.martin;

/**
 * @ClassName loadBalancingEnum
 * @Description TODO
 * @Author chenjiahao
 * @Date 2020/4/20 17:41
 * @Version 1.0
 **/
public enum loadBalanceEnum {

    // 轮询
    Polling("Polling"),
    // 随机
    Random("Random");

    private String balanceStrategy;

    private loadBalanceEnum(String balanceStrategy) {
        this.balanceStrategy = balanceStrategy;
    }

    public String getBalanceStrategy() {
        return balanceStrategy;
    }

    public void setBalanceStrategy(String balanceStrategy) {
        this.balanceStrategy = balanceStrategy;
    }
}
