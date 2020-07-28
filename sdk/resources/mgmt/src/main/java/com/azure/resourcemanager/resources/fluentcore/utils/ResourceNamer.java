// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore.utils;


import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/**
 * The ResourceNamer to generate random name.
 */
public class ResourceNamer {
    private final String randName;
    private static final Random RANDOM = new Random();

    /**
     * Creates ResourceNamer.
     *
     * @param name the randName
     */
    public ResourceNamer(String name) {
        this.randName = name.toLowerCase(Locale.ROOT)
            + UUID.randomUUID().toString().replace("-", "").substring(0, 3).toLowerCase(Locale.ROOT);
    }

    /**
     * Gets a random name.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the max length for the random generated name
     * @return the random name
     */
    public String randomName(String prefix, int maxLen) {
        prefix = prefix.toLowerCase(Locale.ROOT);
        int minRandomnessLength = 5;
        if (maxLen <= minRandomnessLength) {
            return randomString(maxLen);
        }

        if (maxLen < prefix.length() + minRandomnessLength) {
            return randomString(maxLen);
        }

        String minRandomString = String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));

        if (maxLen <= prefix.length() + randName.length() + minRandomnessLength) {
            String str = prefix + minRandomString;
            return str + randomString((maxLen - str.length()) / 2);
        }

        String str = prefix + randName + minRandomString;
        return str + randomString((maxLen - str.length()) / 2);
    }

    /**
     * Creates a random UUID.
     *
     * @return the UUID string.
     */
    public String randomUuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * @return the current time offset.
     */
    public OffsetDateTime dateTimeNow() {
        return OffsetDateTime.now();
    }

    private String randomString(int length) {
        StringBuilder str = new StringBuilder();
        while (str.length() < length) {
            str.append(UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, Math.min(32, length)).toLowerCase(Locale.ROOT));
        }
        return str.toString();
    }
}
