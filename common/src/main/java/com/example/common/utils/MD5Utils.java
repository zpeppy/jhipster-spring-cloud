package com.example.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * MD5 工具类
 *
 * @author peppy
 */
public final class MD5Utils {

    private static final String MD5 = "MD5";

    private static final int RADIX = 16;

    private static final int HEX_FIFTEEN = 0XF;

    private MD5Utils() {

    }

    public static String md5(Object[] args) throws NoSuchAlgorithmException {
        if (Objects.isNull(args)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            sb.append(arg);
        }
        MessageDigest messageDigest = MessageDigest.getInstance(MD5);
        byte[] bytes = messageDigest.digest(sb.toString().getBytes(StandardCharsets.UTF_8));

        StringBuilder md5Sb = new StringBuilder();
        for (byte bt : bytes) {
            md5Sb.append(Character.forDigit((bt >> 4) & HEX_FIFTEEN, RADIX));
            md5Sb.append(Character.forDigit(bt & HEX_FIFTEEN, RADIX));
        }
        return md5Sb.toString();
    }

}
