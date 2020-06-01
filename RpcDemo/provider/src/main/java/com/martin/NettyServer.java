package com.martin;

import com.martin.entity.Request;
import com.martin.producer.NettyProducerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty 服务端
 **/
class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    /**
     * 服务端 boss 线程组
     **/
    private EventLoopGroup bossGroup;

    /**
     * 服务端 worker 线程组
     **/
    private EventLoopGroup workerGroup;

    private Channel channel;

    private static NettyServer nettyServer = new NettyServer();

    private NettyServer() {

    }

    static NettyServer singleton() {
        return nettyServer;
    }

    /**
     * Netty 启动
     **/
    void start(final int port, final String serializeType) {
        synchronized (NettyServer.class) {
            if (bossGroup != null || workerGroup != null) {
                return;
            }

            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // 连接数放入队列中，队列大小为 256
                    .option(ChannelOption.SO_BACKLOG, 256)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel socketChannel) {
                            // 注册编解码器
                            socketChannel.pipeline().addLast(new NettyCodecHandler(Request.class, serializeType));
                            // 服务端处理 handler，解析客户端的请求，反射调用获取结果，再返回
                            socketChannel.pipeline().addLast(new NettyProducerHandler());
                        }
                    });
            try {
                channel = serverBootstrap.bind(port).sync().channel();
            } catch (InterruptedException e) {
                logger.error("Netty 服务端启动失败: " + e);
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * 停止 Netty 服务
     */
    public void stop() {
        if (channel == null) {
            logger.info("Netty 服务端 Channel 已经停止");
            throw new RuntimeException("Netty Server Stoped");
        }
        // 关闭 bossGroup、workerGroup、channel
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
    }
}
