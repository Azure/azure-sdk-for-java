// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.stress.util;

/**
 * Utility class to check Java version.
 */
public class JavaVersionUtil {

    /**
     * Check if Java version is 21 or later.
     * @return true if Java version is 21 or later, false otherwise.
     */
    public static boolean isJava21OrLater() {
        String version = System.getProperty("java.version");
        return version.startsWith("21") || version.compareTo("21") > 0;
    }

}
