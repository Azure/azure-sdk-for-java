// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public class JavaStreamUtils {

    private static <T> String safeToString(T t) {
        return t != null ? t.toString() : "null";
    }

    public static <T> String info(Collection<T> collection) {
        return collection == null ? "null collection" :
            "collection size: " + collection.size();
    }

    public static <T> String info(T[] collection) {
        return collection == null ? "null collection" :
            "collection size: " + collection.length;
    }

    public static <T> String toString(Collection<T> collection, String delimiter) {
        return collection == null ? "null collection" :
            collection.isEmpty() ? "empty collection" :
                collection.stream()
                          .map(t -> safeToString(t))
                          .collect(Collectors.joining(delimiter));
    }

    public static <T> String toString(T[] array, String delimiter) {
        return array == null ? "null array" :
            toString(Arrays.asList(array), delimiter);
    }

}
