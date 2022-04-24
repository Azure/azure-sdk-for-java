// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.properties.utils;

import org.springframework.util.StringUtils;

/**
 * Utility class for property conditions.
 */
public final class PropertyConditionUtil {

    private PropertyConditionUtil() {
    }

    /**
     * Ensure the prefix format is valid.
     * @param prefixAttr the input prefix value
     * @return the result valid prefix
     */
    public static String getValidPrefix(String prefixAttr) {
        prefixAttr = prefixAttr.trim();
        if (StringUtils.hasText(prefixAttr) && !prefixAttr.endsWith(".")) {
            return prefixAttr + ".";
        } else {
            return prefixAttr;
        }
    }
}
