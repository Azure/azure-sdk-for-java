// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.test.utils;

import com.azure.core.http.rest.PagedIterable;

import java.util.Iterator;

/**
 * Common utility functions for the tests.
 */
public class TestUtilities {
    /**
     * Wrapper on the ResourceManagerUtils.InternalRuntimeContext.sleep, in case of record mode will not sleep, otherwise sleep.
     *
     * @param milliseconds time in milliseconds for which to sleep.
     * @param isRecordMode the value indicates whether it is record mode.
     */
    public static void sleep(int milliseconds, boolean isRecordMode) {
        if (isRecordMode) {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Return the size of Iterable collection.
     *
     * @param iterable the Iterable collection.
     * @param <T> the type of the resource
     * @return the size of the collection.
     */
    public static <T> int getSize(Iterable<T> iterable) {
        int res = 0;
        Iterator<T> iterator = iterable.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            ++res;
        }
        return res;
    }

    /**
     * Return whether the Iterable collection is empty.
     *
     * @param iterable the Iterable collection.
     * @param <T> the type of the resource
     * @return if the collection is empty.
     */
    public static <T> boolean isEmpty(PagedIterable<T> iterable) {
        return !iterable.iterator().hasNext();
    }
}
