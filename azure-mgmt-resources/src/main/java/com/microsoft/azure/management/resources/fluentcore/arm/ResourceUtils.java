/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for Azure resource IDs.
 */
public final class ResourceUtils {
    private ResourceUtils() { }

    /**
     * Extract resource group from a resource ID string.
     * @param id the resource ID string
     * @return the resource group name
     */
    public static String groupFromResourceId(String id) {
        return extractFromResourceId(id, "resourceGroups");
    }

    /**
     * Extract resource provider from a resource ID string.
     * @param id the resource ID string
     * @return the resource group name
     */
    public static String resourceProviderFromResourceId(String id) {
        return extractFromResourceId(id, "providers");
    }

    /**
     * Extract resource type from a resource ID string.
     * @param id the resource ID string
     * @return the resource type
     */
    public static String resourceTypeFromResourceId(String id) {
        String[] splits = id.split("/");
        return splits[splits.length - 2];
    }

    /**
     * Extract parent resource path from a resource ID string.
     * @param id the resource ID string
     * @return the parent resource path
     */
    public static String parentResourcePathFromResourceId(String id) {
        String parent = id.replace("/" + resourceTypeFromResourceId(id) + "/" + nameFromResourceId(id), "");
        return extractFromResourceId(parent, resourceProviderFromResourceId(parent));
    }

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
        Pattern pattern = Pattern.compile(identifier + "/[-\\w\\._]+");
        Matcher matcher = pattern.matcher(id);
        if (matcher.find()) {
            return matcher.group().split("/")[1];
        } else {
            return null;
        }
    }

    /**
     * Extract name of the resource from a resource ID.
     * @param id the resource ID
     * @return the name of the resource
     */
    public static String nameFromResourceId(String id) {
        String[] splits = id.split("/");
        return splits[splits.length - 1];
    }
}
