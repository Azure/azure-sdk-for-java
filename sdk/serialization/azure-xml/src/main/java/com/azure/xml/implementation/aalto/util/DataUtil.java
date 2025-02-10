// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package com.azure.xml.implementation.aalto.util;

import java.lang.reflect.Array;

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
        int[] old = arr;
        int len = arr.length;
        arr = new int[len + more];
        System.arraycopy(old, 0, arr, 0, len);
        return arr;
    }

    public static char[] growArrayBy(char[] arr, int more) {
        if (arr == null) {
            return new char[more];
        }
        char[] old = arr;
        int len = arr.length;
        arr = new char[len + more];
        System.arraycopy(old, 0, arr, 0, len);
        return arr;
    }

    public static Object growAnyArrayBy(Object arr, int more) {
        if (arr == null) {
            throw new IllegalArgumentException("Null array");
        }
        Object old = arr;
        int len = Array.getLength(arr);
        arr = Array.newInstance(arr.getClass().getComponentType(), len + more);
        System.arraycopy(old, 0, arr, 0, len);
        return arr;
    }
}
