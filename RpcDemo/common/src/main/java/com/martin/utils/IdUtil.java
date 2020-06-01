package com.martin.utils;

import java.util.UUID;

/**
 * Id 生成类
 **/
public class IdUtil {

    public static String generateId() {
        return UUID.randomUUID().toString() + "-" + Thread.currentThread().getId();
    }
}
