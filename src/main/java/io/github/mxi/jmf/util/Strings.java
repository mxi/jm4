package io.github.mxi.jmf.util;

public class Strings {

    public static boolean isLikeChar(String str) {
        return null != str && 1 == str.length();
    }

    public static boolean isLegit(String str) {
        return null != str && 0 < str.length();
    }

    public static boolean isVapid(String str) {
        return null == str || 0 == str.length();
    }

    public static String nonVapidElse(String target, String fallback) {
        return isVapid(target) ? fallback : target;
    }
}
