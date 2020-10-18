// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common;

import org.junit.Assert;

import java.util.Comparator;
import java.util.List;

public class TestUtils {

    public static <T> void assertEntitiesEquals(List<T> expect, List<T> actual) {
        Assert.assertEquals(actual.size(), expect.size());

        actual.sort(Comparator.comparing(T::toString));
        expect.sort(Comparator.comparing(T::toString));

        Assert.assertEquals(actual, expect);
    }
}
