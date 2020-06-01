package com.martin;

/**
 * 序列化类型，这里只写一个，可扩充
 **/
public enum SerializeEnum {

    DefaultJavaSerializer("DefaultJavaSerializer"),
    FastJsonSeralizer("FastJsonSeralizer");

    private String serializeType;

    private SerializeEnum(String serializeType) {
        this.serializeType = serializeType;
    }

    public String getSerializeType() {
        return serializeType;
    }
}
