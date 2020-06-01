package com.martin.consumer;

import com.martin.entity.Request;
import com.martin.entity.Response;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;


/**
 * 消费者处理 handler，用于发送请求包、接收回复包
 **/
public class ConsumerHandler extends ChannelDuplexHandler {

    /**
     * 客户端发送 request 数据到服务端 Netty，将 request 对应的 ID 和 future 存入 map 中
     **/
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Request) {
            Request request = (Request) msg;
            // 发送请求数据时，先将该 requestId 放入 map 中，并初始化一个 future
            ResponseHolder.initResponse(request.getRequestId());
        }
        // 传到下个 handler
        super.write(ctx, msg, promise);
    }

    /**
     * 客户端读取服务端发送的数据包 response，将结果存到 map 中的 future
     **/
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Response) {
            Response response = (Response) msg;
            // 读取回复时，将结果存入 future 中
            ResponseHolder.putResponse(response);
        }
        // 传到下个 handler
        super.channelRead(ctx, msg);
    }
}
