package com.martin.serializer.Impl;

import com.martin.serializer.ISerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @ClassName DefaultJavaSerializer
 * @Description TODO
 * @Author chenjiahao
 * @Date 2020/5/13 10:12
 * @Version 1.0
 **/
public class DefaultJavaSerializer implements ISerializer {


    public <T> byte[] serialize(T obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            // 将 object 序列化到 byteArrayOutputStream
            objectOutputStream.writeObject(obj);
            // 写完，就关闭
            objectOutputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // 返回序列化后的 byteArrayOutputStream
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 反序列化，将二进制字节转为对象
     **/
    public <T> T deserialize(byte[] data, Class<T> clazz) {
        // 将数据放到 byteArrayInputStream
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            // 读取对象
            return (T) objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
