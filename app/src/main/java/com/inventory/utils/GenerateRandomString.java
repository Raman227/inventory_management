package com.inventory.utils;

import java.util.Random;

public class GenerateRandomString {

    @SuppressWarnings("SpellCheckingInspection")
    private static final String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZqwertyuioplkjhgfdsazxcvbnm";
    private static final Random RANDOM = new Random();

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
        }
        return sb.toString();
    }
}
