// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.extensions;

/**
 * String handlers.
 */
public class Strings {

    /**
     * Determines whether the parameter string is either null or empty.
     * @param arg the string to verify
     * @return true if the string is empty or null and false otherwise.
     */
    public static boolean isNullOrEmpty(String arg) {

        if (arg == null || arg.length() == 0) {
            return true;
        }

        return false;
    }

    /**
     * Determines whether the parameter string is null, empty or whitespace.
     * @param arg the string to verify
     * @return true if the string is empty, contains only whitespace or is null and false otherwise
     */
    public static boolean isNullOrWhiteSpace(String arg) {

        if (Strings.isNullOrEmpty(arg) || arg.trim().isEmpty()) {
            return true;
        }

        return false;
    }
}
