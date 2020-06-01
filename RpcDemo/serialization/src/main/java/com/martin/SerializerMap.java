package com.martin;

import com.martin.serializer.ISerializer;
import com.martin.serializer.Impl.FastJsonSeralizer;
import com.martin.utils.StringUtils;



import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 根据序列化类型，获取对应的序列化算法
 **/
public class SerializerMap {

    public static final Map<String, ISerializer> map = new ConcurrentHashMap<>();

    static {
        // 可扩充序列化算法，继承 ISeralizer 接口
        map.put(SerializeEnum.FastJsonSeralizer.getSerializeType(), new FastJsonSeralizer());
    }

    /**
     * 序列化
     **/
    public static <T> byte[] serialize(T object, String serializeType) {
        if (StringUtils.isEmpty(serializeType)) {
            throw new RuntimeException("序列化类型为空");
        }

        try {
            return map.get(serializeType).serialize(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 反序列化
     **/
    public static <T> Object deserialize(byte[] bytes, Class<T> clazz, String serializeType) {
        if (StringUtils.isEmpty(serializeType)) {
            throw new RuntimeException("序列化类型为空");
        }

        try {
            return map.get(serializeType).deserialize(bytes, clazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
