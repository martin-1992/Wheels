package com.martin.serializer.Impl;

import com.alibaba.fastjson.JSON;
import com.martin.serializer.ISerializer;

/**
 * @ClassName FastJsonSeralizer
 * @Description TODO
 * @Author chenjiahao
 * @Date 2020/4/20 16:48
 * @Version 1.0
 **/
public class FastJsonSeralizer implements ISerializer {

    @Override
    public <T> byte[] serialize(T objcet) {
        if (objcet == null) {
            return new byte[0];
        }

        try {
            return JSON.toJSONString(objcet).getBytes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0 || clazz == null) {
            return null;
        }

        try {
            return (T) JSON.parseObject(bytes, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
