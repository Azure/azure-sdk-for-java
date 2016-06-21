package com.microsoft.azure.management.resources.fluentcore.utils;

import java.util.UUID;

/**
 * The ResourceNamer to generate random name.
 */
public class ResourceNamer {
    private final String randName;

    /**
     * Creates ResourceNamer.
     *
     * @param name the randName
     */
    public ResourceNamer(String name) {
        this.randName = name + UUID.randomUUID().toString().replace("-", "").substring(0, 3);
    }

    /**
     * Gets a random randName.
     *
     * @param prefix the prefix to be used if possible
     * @param maxLen the max length for the randName
     * @return the random name
     */
    public String randomName(String prefix, int maxLen) {
        int minRandomnessLength = 5;
        if (maxLen <= minRandomnessLength) {
            return randomString(maxLen);
        }

        if (maxLen <= prefix.length() + minRandomnessLength) {
            return randomString(maxLen);
        }

        String minRandomString = String.valueOf(System.currentTimeMillis() % 100000L);
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
                    .substring(0, Math.min(32, length));
        }
        return str;
    }
}
