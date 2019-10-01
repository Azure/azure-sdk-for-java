// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import java.util.Collection;
import java.util.stream.Collectors;

public class JavaStreamUtils {

    private static <T> String safeToString(T t) {
        return t != null ? t.toString() : "null";
    }

    public static <T> String toString(Collection<T> collection, String delimiter) {
        return collection.stream()
                .map( t -> safeToString(t) )
                .collect(Collectors.joining(delimiter));
    }


}
