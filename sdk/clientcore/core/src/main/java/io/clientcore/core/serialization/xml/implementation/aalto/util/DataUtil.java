// Original file from https://github.com/FasterXML/aalto-xml under Apache-2.0 license.
package io.clientcore.core.serialization.xml.implementation.aalto.util;

import io.clientcore.core.serialization.xml.implementation.aalto.in.NsBinding;

public final class DataUtil {
    private DataUtil() {
    }

    /*
    ////////////////////////////////////////////////////////////
    // Methods for common operations on std data structs
    ////////////////////////////////////////////////////////////
    */

    public static int[] growArrayBy(int[] arr, int more) {
        int[] old = arr;
        int len = arr.length;
        arr = new int[len + more];
        System.arraycopy(old, 0, arr, 0, len);
        return arr;
    }

    public static char[] growArrayBy(char[] arr, int more) {
        char[] old = arr;
        int len = arr.length;
        arr = new char[len + more];
        System.arraycopy(old, 0, arr, 0, len);
        return arr;
    }

    public static Object growAnyArrayBy(NsBinding[] arr, int more) {
        NsBinding[] old = arr;
        int len = arr.length;
        arr = new NsBinding[len + more];
        System.arraycopy(old, 0, arr, 0, len);
        return arr;
    }
}
