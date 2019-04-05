package com.vitor.tcc_projeto.utils;

import org.apache.commons.lang.RandomStringUtils;

import java.util.Random;

public final class Geradores {
    public static String GenerateRandomString(int length){
        return RandomStringUtils.random(length, true, true).toUpperCase();
    }

    public static int GenerateRandomPort(){
        Random rand = new Random();
        int  n = rand.nextInt(65535) + 1024;
        return n;
    }
}
