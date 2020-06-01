package com.martin;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

/**
 * 编解码处理 Handler，协议格式为数据体长度 + 数据体内容
 **/
public class NettyCodecHandler extends ByteToMessageCodec<Object> {

    /**
     * 解码对象 class
     **/
    private Class<?> genericClass;

    /**
     * 编解码对象所使用的序列化类型
     **/
    private String serializeType;

    public NettyCodecHandler(Class<?> genericClass, String serializeType) {
        this.genericClass = genericClass;
        this.serializeType = serializeType;
    }

    @Override
    public void encode(ChannelHandlerContext ctx, Object in, ByteBuf out) throws Exception {
        // 将对象序列化为字节数组
        byte[] data = SerializerMap.serialize(in, serializeType);
        // 将字节数组（消息体）的长度作为消息头写入，解决半包、粘包问题
        out.writeInt(data.length);
        // 写入序列化后得到的字节数组
        out.writeBytes(data);
    }

    /**
     * 编码，字节长度 + 字节内容，解码时根据字节长度获取对应的字节内容
     **/
    @Override
    public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 获取消息头所标识的消息体字节数组长度
        if (in.readableBytes() < 4) {
            // 可读字节数不足，直接返回
            return;
        }
        // 标记可读位
        in.markReaderIndex();
        // 编码为字节长度 + 字节内容，所以解码时先获取字节长度，再根据字节长度获取字节内容，可解决半包、粘包问题
        int dataLength = in.readInt();
        if (dataLength < 0) {
            ctx.close();
        }
        // 若当前可获取到的字节数小于实际长度，则直接返回，直到当前可获取的字节数等于实际长度
        if (in.readableBytes() < dataLength) {
            // 重置可读位置
            in.resetReaderIndex();
            return;
        }
        // 读取完整的消息体字节数组，根据字节长度读取字节内容
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        // 将字节数组反序列为 Java 对象 (SerializerEngine 参考序列化与反序列化章节)
        out.add(SerializerMap.deserialize(data, genericClass, serializeType));
    }
}
