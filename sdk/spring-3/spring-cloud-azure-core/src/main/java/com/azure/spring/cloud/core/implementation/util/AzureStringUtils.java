// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.util;

import org.springframework.util.StringUtils;

/**
 * Azure general string utility class.
 */
public final class AzureStringUtils {

    private AzureStringUtils() {
    }

    /**
     * Ensure a non-empty string end with the given suffix. If the string is empty
     * or already ends with the given suffix, return the string. If not, return a
     * new string suffixed with given suffix.
     * @param str input string
     * @param suffix suffix
     * @return result string ending with the specified suffix if not empty
     */
    public static String ensureEndsWithSuffix(String str, String suffix) {
        if (StringUtils.hasText(str) && !str.endsWith(suffix)) {
            return str + suffix;
        } else {
            return str;
        }
    }

}
