package com.martin;

import com.martin.api.HelloService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * 消费者发起调用
 */
@SpringBootApplication
public class ClientApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ClientApplication.class, args);
        try {
            // 从 Spring 上下文中获取代理对象 bean
            HelloService helloService = (HelloService) context.getBean("HelloService");
            for (int i = 0; i < 1000; i++) {
                // 使用动态代理，代理对象，调用反射方法
                System.out.println(helloService.sayHello("martin, " + i));
                Thread.sleep(2000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
