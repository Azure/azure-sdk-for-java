// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.util;

/**
 * Functionality for supporting exposing of component {@code Version}s.
 * Also contains other misc methods that have no other place to live in.
 *<p>
 * Note that this class can be used in two roles: first, as a static
 * utility class for loading purposes, and second, as a singleton
 * loader of per-module version information.
 *<p>
 * Note that method for accessing version information changed between versions
 * 2.1 and 2.2; earlier code used file named "VERSION.txt"; but this has serious
 * performance issues on some platforms (Android), so a replacement system
 * was implemented to use class generation and dynamic class loading.
 *<p>
 * Note that functionality for reading "VERSION.txt" was removed completely
 * from Jackson 2.6.
 */
public class VersionUtil {
    /*
    /**********************************************************************
    /* Orphan utility methods
    /**********************************************************************
     */

    public static <T> T throwInternalReturnAny() {
        throw new RuntimeException("Internal error: this code path should never get executed");
    }
}
