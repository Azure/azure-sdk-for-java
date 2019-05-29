// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage.interceptor;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;

public class TestResourceNamer {
    private final InterceptorManager interceptorManager;
    private static final int MAX_BYTE_BOUND = 64;
    private static final int OFFSET = 1;
    private final String randName;
    private static final Random RANDOM = new Random();

    public TestResourceNamer(String name, InterceptorManager interceptorManager) {
        this.randName = name.toLowerCase() + UUID.randomUUID().toString().replace("-", "").substring(0, 3).toLowerCase();
        this.interceptorManager = interceptorManager;
    }

    /**
     * Gets a random name.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the max length for the random generated name
     * @return the random name
     */
    public String randomName(String prefix, int maxLen) {
        if (interceptorManager.isPlaybackMode()) {
            return interceptorManager.popVariable();
        }
        String randomName = generateRandomName(prefix, maxLen);
        interceptorManager.pushVariable(randomName);
        return randomName;
    }

    public String randomString() {
        if (interceptorManager.isPlaybackMode()) {
            return interceptorManager.popVariable();
        }
        String randomString = new String(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()));
        interceptorManager.pushVariable(randomString);
        return randomString;
    }

    /**
     * Gets a random byte.
     *
     * @return the random byte
     */
    public byte[] randomByte(int size) {
        if (interceptorManager.isPlaybackMode()) {
            return interceptorManager.popVariable().getBytes(StandardCharsets.UTF_8);
        }
        byte[] randomBytes = new byte[size];
        for (int i = 0; i < size; i++) {
            randomBytes[i] = (byte)((new Random()).nextInt(MAX_BYTE_BOUND) + OFFSET);
        }
        interceptorManager.pushVariable(new String(randomBytes, StandardCharsets.UTF_8));
        return randomBytes;
    }


    public OffsetDateTime getCurrentTime() {
        if (interceptorManager.isPlaybackMode()) {
            return OffsetDateTime.parse(interceptorManager.popVariable());
        }
        OffsetDateTime now = OffsetDateTime.now();

        interceptorManager.pushVariable(now.toString());

        return now;
    }

    private String generateRandomName(String prefix, int maxLen) {
        prefix = prefix.toLowerCase();
        int minRandomnessLength = 5;
        if (maxLen <= minRandomnessLength) {
            return randomString(maxLen);
        } else if (maxLen < prefix.length() + minRandomnessLength) {
            return randomString(maxLen);
        } else {
            String minRandomString = String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
            String str;
            if (maxLen <= prefix.length() + this.randName.length() + minRandomnessLength) {
                str = prefix + minRandomString;
                return str + this.randomString((maxLen - str.length()) / 2);
            } else {
                str = prefix + this.randName + minRandomString;
                return str + this.randomString((maxLen - str.length()) / 2);
            }
        }
    }

    private String randomString(int length) {
        String str;
        for(str = ""; str.length() < length; str = str + UUID.randomUUID().toString().replace("-", "").substring(0, Math.min(32, length)).toLowerCase()) {
        }

        return str;
    }
}
