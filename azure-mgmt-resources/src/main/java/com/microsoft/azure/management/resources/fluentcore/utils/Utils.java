/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.utils;

/**
 * Defines a few utilities.
 */
public final class Utils {
    /**
     * Converts an object Boolean to a primitive boolean.
     *
     * @param value the <tt>Boolean</tt> value
     * @return <tt>false</tt> if the given Boolean value is null or false else <tt>true</tt>
     */
    public static boolean toPrimitiveBoolean(Boolean value) {
        if (value == null) {
            return false;
        }
        return value;
    }

    /**
     * Creates an Odata filter string that can be used for filtering list results by tags.
     *
     * @param tagName the name of the tag. If not provided, all resources will be returned.
     * @param tagValue the value of the tag. If not provided, only tag name will be filtered.
     * @return the Odata filter to pass into list methods
     */
    public static String createOdataFilterForTags(String tagName, String tagValue) {
        if (tagName == null) {
            return null;
        } else if (tagValue == null) {
            return String.format("tagname eq '%s'", tagName);
        } else {
            return String.format("tagname eq '%s' and tagvalue eq '%s'", tagName, tagValue);
        }
    }

    private Utils() {
    }
}
