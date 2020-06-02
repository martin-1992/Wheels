package com.martin.consumer;

import com.martin.NettyCodecHandler;
import com.martin.config.ZooKeeperProperty;
import com.martin.entity.ProviderService;
import com.martin.entity.Response;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Netty 的 channel 连接池，包含初始化 Channel 连接池、创建 Channel、获取 channel、回收 Channel
 **/
class NettyChannelPool {

    private static final Logger logger = LoggerFactory.getLogger(NettyChannelPool.class);

    /**
     * key 为服务提供者地址，value 为 Netty Channel 的阻塞队列
     **/
    private static final Map<InetSocketAddress, ArrayBlockingQueue<Channel>> channelPoolMap = new ConcurrentHashMap<>();

    /**
     * 服务提供者列表
     **/
    private static final List<ProviderService> providerServiceList = new ArrayList<>();

    /**
     * 每个接口的连接数
     **/
    private static final int channelConnectCount = ZooKeeperProperty.getNettyChannelConnectCount();

    /**
     * 单例模式
     **/
    private static final NettyChannelPool nettyChannelPool = new NettyChannelPool();

    private NettyChannelPool() {

    }

    static NettyChannelPool nettyChannelPoolInstance() {
        return nettyChannelPool;
    }

    /**
     * 为每个服务机器(ip:port) 创建一定数量的 Channel
     *
     * 1. 将服务提供者信息存入一个列表中；
     * 2. Netty 连接池初始化，遍历该列表，为每个不同 IP 地址机器创建固定数量的 Channel，放入阻塞队列中；
     * 3. 在将阻塞队列放入一个哈希表 channelPoolMap，通过 key（IP 地址）获取阻塞队列。
     *
     **/
    void init(Map<String, List<ProviderService>> providerMap) {
        // 将服务提供者信息存入 providerServiceList
        for (List<ProviderService> list : providerMap.values()) {
            if (list == null || list.isEmpty()) {
                continue;
            }
            providerServiceList.addAll(list);
        }

        // 获取每个生产服务的 IP 和端口，为不同的 IP 地址建立固定数量的 Channel，
        // 放入阻塞队列中，即连接池初始化
        for (ProviderService providerService : providerServiceList) {
            // 将生产者 IP 和端口包装成 InetSocketAddress 类
            InetSocketAddress socketAddress = new InetSocketAddress(providerService.getServiceIP(),
                    providerService.getServicePort());

            ArrayBlockingQueue<Channel> channelArrayBlockingQueue = channelPoolMap.get(socketAddress);
            if (channelArrayBlockingQueue == null) {
                channelArrayBlockingQueue = new ArrayBlockingQueue<>(channelConnectCount);
                // 为该 IP 地址创建 channelConnectSize 个 channel
                for (int i = 0; i < channelConnectCount; i++) {
                    channelArrayBlockingQueue.add(registerChannel(socketAddress));
                }
            }
            // 阻塞队列添加了一定数量（channelConnectSize）的 channel
            channelPoolMap.put(socketAddress, channelArrayBlockingQueue);
        }
    }

    /**
     * 获取 Channel
     **/
    Channel getChannel(InetSocketAddress socketAddress, long timeout, TimeUnit timeUnit) {
        ArrayBlockingQueue<Channel> channelArrayBlockingQueue = channelPoolMap.get(socketAddress);
        if (channelArrayBlockingQueue == null || channelArrayBlockingQueue.isEmpty()) {
            // 队列中没有可用 Channel，注册一个新的 Channel
            return registerChannel(socketAddress);
        }

        try {
            return channelArrayBlockingQueue.poll(timeout, timeUnit);
        } catch (InterruptedException e) {
            logger.debug("Netty 连接池获取 channel 失败，e: " + e.getCause());
        }
        return null;
    }

    /**
     * 回收 channel，放回到阻塞队列中
     **/
    void release(InetSocketAddress socketAddress, Channel channel) {
        ArrayBlockingQueue<Channel> arrayBlockingQueue = channelPoolMap.get(socketAddress);
        if (arrayBlockingQueue == null || arrayBlockingQueue.isEmpty()) {
            logger.debug("socketAddress 地址为空，无法回收");
            return;
        }

        // 回收之前先检查 channel 是否可用，不可用的话，重新注册一个，放入阻塞队列
        if (channel == null || !channel.isActive() || !channel.isOpen() || !channel.isWritable()) {
            if (channel != null) {
                channel.deregister().syncUninterruptibly().awaitUninterruptibly();
                channel.closeFuture().syncUninterruptibly().awaitUninterruptibly();
            }
            logger.debug("回收的 channe 为空，重新注册一个放入阻塞队列中");
            channel = registerChannel(socketAddress);
        }
        arrayBlockingQueue.add(channel);
    }

    /**
     * 为服务提供者地址 socketAddress 注册新的 Channel
     **/
    Channel registerChannel(InetSocketAddress socketAddress) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.remoteAddress(socketAddress);

        // 引导类设置
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel socketChannel) throws Exception {
                        // 注册 Netty 编解码器
                        socketChannel.pipeline().addLast(new NettyCodecHandler(Response.class, ZooKeeperProperty.getSerializeType()));
                        // 注册客户端业务逻辑处理 handler
//                        socketChannel.pipeline().addLast(new NettyConsumerHandler());
                        socketChannel.pipeline().addLast(new ConsumerHandler());
                    }
                });

        return connect(bootstrap);
    }

    private Channel connect(Bootstrap bootstrap) {
        try {
            // 异步连接
            ChannelFuture channelFuture = bootstrap.connect().sync();
            final Channel newChannel = channelFuture.channel();
            // 信号量为 1，只允许一个线程访问
            final CountDownLatch countDownLatch = new CountDownLatch(1);

            final List<Boolean> isSuccess = new ArrayList<>(1);
            // 监听 Channel 是否创建成功，使用信号量，保证 Channel 创建成功后，才返回新建的 Channel
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    // 如果 Channel 建立成功，保存建立成功的标志
                    if (channelFuture.isSuccess()) {
                        isSuccess.add(true);
                    } else {
                        // Channel 建立失败，保存建立失败的标记
                        logger.debug("channel 建立失败，e: {}", channelFuture.cause().getCause());
                        isSuccess.add(false);
                    }
                    // 减一后为 0
                    countDownLatch.countDown();
                }
            });

            // 线程进入等待状态，当线程 connectedLatch 为 0 后，开始运行
            countDownLatch.await();
            // 如果 Channel 建立成功，即 isSuccess.get(0) 为 true，返回新建的 Channel
            if (isSuccess.get(0)) {
                return newChannel;
            }
        } catch (InterruptedException e) {
            logger.debug("连接失败，e: {}", e.getCause());
        }
        return null;
    }
}
