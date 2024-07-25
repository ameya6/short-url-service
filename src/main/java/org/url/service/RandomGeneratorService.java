package org.url.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service

public class RandomGeneratorService {

    @Autowired
    private Random random;

    private final static String PROTOCOL = "http://";

    public String getRandomURL() {
        return  PROTOCOL + getRandomAlphaString(getRandomInt(1, 10)) + "."
                + getRandomAlphaString(getRandomInt(10, 30)) + "."
                + getRandomAlphaString(getRandomInt(10, 20)) + ".com/"
                + getRandomAlphaString(getRandomInt(3, 10));
    }

    private String getRandomAlphaString(int length) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < length; i++) {

            char str1 = (char) getRandomInt(65, 90); // UPPER_CASE
            char str2 = (char) getRandomInt(48, 57); // NUMBERS
            char str3 = (char) getRandomInt(97, 122); // lower_case

            b.append(str1).append(str2).append(str3);
        }

        return b.toString();
    }

    public int getRandomInt(int bound) {
        return random.nextInt(bound);
    }

    public int getRandomInt(int origin, int bound) {
        return random.nextInt(origin, bound);
    }

    public long getRandomLong(int bound) {
        return random.nextLong(bound);
    }

    public long getRandomLong(int origin, int bound) {
        return random.nextLong(origin, bound);
    }
}
