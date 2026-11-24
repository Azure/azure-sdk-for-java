// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * Utility class for validating configuration properties.
 */
public final class ValidationUtil {

    private ValidationUtil() {
        // Utility class, prevent instantiation
    }

    /**
     * Validates that tag filter entries follow the expected {@code tagName=tagValue} format.
     * Each entry must contain an equals sign, with non-empty tag name and value on either side.
     *
     * @param tagsFilter the list of tag filter expressions to validate
     * @throws IllegalArgumentException if any tag filter entry is invalid
     */
    public static void validateTagsFilter(List<String> tagsFilter) {
        if (tagsFilter == null) {
            return;
        }

        for (String tagFilter : tagsFilter) {
            Assert.isTrue(StringUtils.hasText(tagFilter),
                "Tag filter entries must not be null or empty");
            Assert.isTrue(tagFilter.contains("="),
                "Tag filter entries must be in tagName=tagValue format");
            String[] parts = tagFilter.split("=", 2);
            Assert.isTrue(StringUtils.hasText(parts[0]),
                "Tag name must not be empty in tag filter: " + tagFilter);
            Assert.isTrue(parts.length == 2 && StringUtils.hasText(parts[1]),
                "Tag value must not be empty in tag filter: " + tagFilter);
        }
    }
}
