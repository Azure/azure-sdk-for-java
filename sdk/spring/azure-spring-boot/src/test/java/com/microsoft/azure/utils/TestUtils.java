// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.utils;

public final class TestUtils {

    private TestUtils() {
    }

    /**
     *
     * @param propName property name
     * @param propValue value of property
     * @return property name and value pair. e.g., prop.name=prop.value
     */
    public static String propPair(String propName, String propValue) {
        return  propName + "=" + propValue;
    }
}
