package com.martin.Impl;

import com.martin.api.HelloService;

/**
 * 服务实现，生产者提供服务接口的实现，创建 HelloServiceImpl 实现类
 **/
public class HelloServiceImpl implements HelloService {

    @Override
    public String sayHello(String somebody) {
        return "hi, " + somebody;
    }

}
