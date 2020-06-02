### RPC 框架结构

- **common，** 包含要调用的接口、实体类、工具类；
- **consumer，** 消费者，包含动态代理、反射调用、Netty Channel 对象池、注册消费者信息到 ZooKeeper；
- **handler，** Netty 的编解码 handler、消费者的 handler、生产者的 handler；
- **load-balance，** 负载均衡，选择一个消费者发起 RPC 调用；
- **provider，** 实现要调用接口的方法，将接口信息注册到 ZooKeeper；
- **serialization，** 序列化方法，RPC 调用是网络传输，所以需要进行序列化变为二进制数据；
- **zookeeper，** 为生产者的和消费者的接口信息创建节点路径，提供生产者接口列表到本地缓存；

### RPC 调用流程

![avatar](photo_1.png)

### 动态代理
　　在 common 模块中包含服务提供方的接口，通过注解 @RpcInterface 自动为该接口生成一个代理类。<br />
　　Spring AOP 提供统一拦截，在接口方法被调用时拦截，由创建的代理对象，调用反射方法，在反射方法中封装一套远程调用逻辑。

- 在 ConsumerBootStrap#afterPropertiesSet() 方法中，会扫描标注了 @RpcInterface 的类，为其创建代理对象，并注册到 Spring 容器中；

```java
    // ... 
    Set<Class<?>> typesAnnotatedWith = reflections.getTypesAnnotatedWith(RpcInterface.class);
    for (Class<?> clazz : typesAnnotatedWith) {
        // 调用反射，为标注了 @RpcInterface 的接口创建代理对象
        ConsumerProxy consumerProxy = ConsumerProxy.generate(clazz, timeout, loadBalanceStrategy);
        // 将代理对象注册到 Spring 容器中
        beanFactory.registerSingleton(clazz.getSimpleName(), consumerProxy.getProxy(clazz));
```

- 消费者启动类 ClientApplication，是 从 Spring 上下文中获取是该接口的代理对象 bean，而不是该接口；
```java
HelloService helloService = (HelloService) context.getBean("HelloService");
```

- 调用代理对象的 ConsumerProxy#invoke() 方法，该方法会发起远程 RPC 调用。
    1. 使用 JDK 默认的 InvocationHandler 能完成代理功能，即 ConsumerProxy 继承了 InvocationHandler 接口，实现 invoke() 方法；
    2. InvocationHandler 接口的生成代理类，已在扫描标注了 @RpcInterface 的类时创建，并注册到 Spring 容器中。

```java
    for (int i = 0; i < 1000; i++) {
        // 使用动态代理，代理对象，调用反射方法
        System.out.println(helloService.sayHello("martin, " + i));
        Thread.sleep(2000);
    }
```





