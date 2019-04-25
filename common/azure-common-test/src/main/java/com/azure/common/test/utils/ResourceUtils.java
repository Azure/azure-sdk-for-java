// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.test.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for Azure resource IDs.
 */
public final class ResourceUtils {
    private ResourceUtils() { }

    /**
     * Extract information from a resource ID string with the resource type
     * as the identifier.
     *
     * @param id the resource ID
     * @param identifier the identifier to match, e.g. "resourceGroups", "storageAccounts"
     * @return the information extracted from the identifier
     */
    public static String extractFromResourceId(String id, String identifier) {
        if (id == null || identifier == null) {
            return id;
        }
        Pattern pattern = Pattern.compile(identifier + "/[-\\w._]+");
        Matcher matcher = pattern.matcher(id);
        if (matcher.find()) {
            return matcher.group().split("/")[1];
        } else {
            return null;
        }
    }
}
