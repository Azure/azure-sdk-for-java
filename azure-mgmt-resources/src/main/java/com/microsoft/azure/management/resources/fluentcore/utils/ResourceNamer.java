/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.utils;

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
        this.randName = name.toLowerCase() + UUID.randomUUID().toString().replace("-", "").substring(0, 3).toLowerCase();
    }

    /**
     * Gets a random name.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the max length for the random generated name
     * @return the random name
     */
    public String randomName(String prefix, int maxLen) {
        prefix = prefix.toLowerCase();
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

    private String randomString(int length) {
        String str = "";
        while (str.length() < length) {
            str += UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, Math.min(32, length)).toLowerCase();
        }
        return str;
    }
}
