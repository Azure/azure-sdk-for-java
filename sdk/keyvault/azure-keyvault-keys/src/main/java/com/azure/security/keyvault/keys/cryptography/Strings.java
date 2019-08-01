// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

class Strings {

    /**
     * Determines whether the parameter string is either null or empty.
     * 
     * @param arg The string to be checked.
     * @return true if the string is null or empty.
     */
    public static boolean isNullOrEmpty(String arg) {

        if (arg == null || arg.length() == 0) {
            return true;
        }

        return false;
    }

    /**
     * Determines whether the parameter string is null, empty or whitespace.
     * 
     * @param arg The string to be checked.
     * @return true if the string is null, empty or whitespace.
     */
    public static boolean isNullOrWhiteSpace(String arg) {

        if (Strings.isNullOrEmpty(arg) || arg.trim().isEmpty()) {
            return true;
        }

        return false;
    }
}
