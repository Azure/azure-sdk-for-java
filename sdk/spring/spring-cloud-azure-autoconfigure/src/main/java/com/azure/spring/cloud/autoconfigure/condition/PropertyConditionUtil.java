// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.condition;

import org.springframework.util.StringUtils;

/**
 * Utility class for property conditions.
 */
public class PropertyConditionUtil {

    private PropertyConditionUtil() {
    }

    /**
     * Ensure the prefix format is valid.
     * @param prefixAttr the input prefix value
     * @return the result valid prefix
     */
    static String getValidPrefix(String prefixAttr) {
        prefixAttr = prefixAttr.trim();
        if (StringUtils.hasText(prefixAttr) && !prefixAttr.endsWith(".")) {
            return prefixAttr + ".";
        } else {
            return prefixAttr;
        }
    }
}
