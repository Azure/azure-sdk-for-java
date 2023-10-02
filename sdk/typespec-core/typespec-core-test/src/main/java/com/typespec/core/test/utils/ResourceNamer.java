// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.utils;

import com.typespec.core.util.CoreUtils;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A random string generator used in tests.
 */
public class ResourceNamer {
    private static final Locale LOCALE = Locale.US;

    private final String randName;

    /**
     * Creates a ResourceNameGenerator that prefixes its strings with the name.
     *
     * @param name The prefix for generated strings.
     */
    public ResourceNamer(String name) {
        this.randName = name.toLowerCase(LOCALE)
            + CoreUtils.randomUuid().toString().replace("-", "").substring(0, 3).toLowerCase(LOCALE);
    }

    /**
     * Gets a random name.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the max length for the random generated name
     * @return the random name
     */
    public String randomName(String prefix, int maxLen) {
        prefix = prefix.toLowerCase(LOCALE);
        int minRandomnessLength = 5;
        if (maxLen <= minRandomnessLength) {
            return randomString(maxLen);
        }

        if (maxLen < prefix.length() + minRandomnessLength) {
            return randomString(maxLen);
        }

        String minRandomString = String.format("%05d", Math.abs(ThreadLocalRandom.current().nextInt() % 100000));

        if (maxLen <= prefix.length() + randName.length() + minRandomnessLength) {
            String str = prefix + minRandomString;
            return str + randomString((maxLen - str.length()) / 2);
        }

        String str = prefix + randName + minRandomString;
        return str + randomString((maxLen - str.length()) / 2);
    }

    /**
     * Creates a random UUID.
     * @return the UUID string.
     */
    public String randomUuid() {
        return CoreUtils.randomUuid().toString();
    }

    private String randomString(int length) {
        StringBuilder str = new StringBuilder();
        while (str.length() < length) {
            str.append(CoreUtils.randomUuid()
                .toString()
                .replace("-", "")
                .substring(0, Math.min(32, length)).toLowerCase(LOCALE));
        }
        return str.toString();
    }
}
