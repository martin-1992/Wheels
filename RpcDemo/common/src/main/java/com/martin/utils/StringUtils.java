package com.martin.utils;

/**
 * @ClassName StringUtils
 * @Description TODO
 * @Author chenjiahao
 * @Date 2020/4/20 17:33
 * @Version 1.0
 **/
public class StringUtils {

    public static boolean isEmpty(String string) {
        if (string == null || string.trim().length() == 0) {
            return true;
        }
        return false;
    }
}
