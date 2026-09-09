package com.nnp.keycloak.utils;

public class LogUtils {
    public LogUtils() {}
    public static String sanitizeForLog(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("\r", "\\r") .replace("\n", "\\n");
    }
}
