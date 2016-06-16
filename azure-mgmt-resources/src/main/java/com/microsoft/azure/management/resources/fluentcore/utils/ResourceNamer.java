package com.microsoft.azure.management.resources.fluentcore.utils;

import java.util.UUID;

/**
 * The ResourceNamer to generate random name.
 */
public class ResourceNamer {
    private final String randName;
    private final long modulus = 100000L;
    private final int moduloLen = 5;

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
        if (prefix.length() >= maxLen) {
            throw new IllegalArgumentException("prefix length (" + prefix.length() + ") cannot be more than " + maxLen);
        }

        long milli = System.currentTimeMillis();
        if (maxLen <= moduloLen) {
            return String.valueOf(milli % maxLen);
        }

        long modulo = (milli % this.modulus);
        // Try prefix and randName together
        if (prefix.length() + this.moduloLen + this.randName.length() <= maxLen) {
            return prefix + modulo + this.randName;
        }

        // We cannot use prefix and randName try prefix
        String name = prefix + modulo;
        if (name.length() + 4 <= maxLen) {
            return name + UUID.randomUUID()
                    .toString()
                    .replace("-", "").substring(0, 3);
        } else if (name.length() <= maxLen) {
            return name;
        }

        // We cannot use prefix, use complete random string
        name = UUID.randomUUID()
                .toString()
                .replace("-", "");
        if (name.length() <= maxLen) {
            return name;
        }

        return name.substring(0, maxLen - moduloLen - 1) + (modulo);
    }
}
