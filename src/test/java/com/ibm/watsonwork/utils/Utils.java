package com.ibm.watsonwork.utils;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.SecureRandom;

import org.apache.commons.codec.digest.HmacUtils;

public class Utils {

    private final static int secretLength = 32;
    private final static int secretRadix = 10 + 26;

    public static String generateSecret() {
        int secretBitLength = (int) Math.floor(secretLength * Math.log(secretRadix) / Math.log(2));
        return new BigInteger(secretBitLength, new SecureRandom()).toString(secretRadix);
    }

    public static String prepareSHA256Hash(String key, byte[] payload) {
        return HmacUtils.hmacSha256Hex(key.getBytes(Charset.forName("UTF8")), payload);
    }
}
