package com.sensorberg.sdk.location;

import java.util.Random;

public class GeoHashSource {

    private static final char[] base32 = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f',
            'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

    private Random random;

    public GeoHashSource() {
        random = new Random();
    }

    public String randomFence() {
        String hash = "";
        for (int i = 0; i < 8; i++) {
            hash += base32[random.nextInt(base32.length)];
        }
        hash += "000100";
        return hash;
    }
}
