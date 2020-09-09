// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TestUtils {
    public static <T> List<T> toList(Iterable<T> iterable) {
        if (iterable != null) {
            final List<T> list = new ArrayList<>();
            iterable.forEach(list::add);
            return list;
        }
        return null;
    }

    public static <T> void testIndexingPolicyPathsEquals(List<T> policyPaths,
                                                         String[] pathsExpected) {
        if (policyPaths == null) {
            throw new IllegalStateException("policyPaths should not be null");
        } else if (pathsExpected == null) {
            throw new IllegalStateException("pathsExpected should not be null");
        }

        final Iterator<T> pathIterator = policyPaths.iterator();

        Assert.isTrue(pathsExpected.length == policyPaths.size(), "unmatched size of policy paths");

        for (final String path: pathsExpected) {
            Assert.isTrue(pathIterator.hasNext(), "policy path iterator should have next");
            final T includedPath = pathIterator.next();
            Assert.isTrue(includedPath.toString().equals(path), "unmatched policy path");
        }
    }
}

