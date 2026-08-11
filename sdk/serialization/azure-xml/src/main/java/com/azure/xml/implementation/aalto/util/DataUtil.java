// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.util;

import java.util.Arrays;

public final class DataUtil {
    private DataUtil() {
    }

    /*
    ////////////////////////////////////////////////////////////
    // Methods for common operations on std data structs
    ////////////////////////////////////////////////////////////
    */

    public static int[] growArrayBy(int[] arr, int more) {
        if (arr == null) {
            return new int[more];
        }
        return Arrays.copyOf(arr, arr.length + more);
    }

    public static char[] growArrayBy(char[] arr, int more) {
        if (arr == null) {
            return new char[more];
        }
        return Arrays.copyOf(arr, arr.length + more);
    }

    public static <T> T[] growAnyArrayBy(T[] arr, int more) {
        if (arr == null) {
            throw new IllegalArgumentException("Null array");
        }
        return Arrays.copyOf(arr, arr.length + more);
    }
}
