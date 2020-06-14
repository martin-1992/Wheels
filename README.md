## Wheels

### [HashedWheelTimer](https://github.com/martin-1992/Wheels/tree/master/HashedWheelTimer)
　　简化版时间轮，参考了 Netty 的 HashedWheelTimer，原版是放到一个类。这里放到多个类，便于直观了解。<br />
　　没有使用位运算来获取索引，而是用 %，去除参数校验。

[!avatar](./HashedWheelTimer/photo_1.png)

### [RateLimiter](https://github.com/martin-1992/Wheels/tree/master/RateLimiter)
　　限流器。

### [RpcDemo](https://github.com/martin-1992/Wheels/tree/master/RpcDemo)
　　RPC 简易框架。
