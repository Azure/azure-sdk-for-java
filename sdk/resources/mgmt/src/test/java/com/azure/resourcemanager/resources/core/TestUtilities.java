// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.core;

import com.azure.core.http.rest.PagedIterable;

/**
 * Common utility functions for the tests.
 */
public class TestUtilities {
    /**
     * Wrapper on the SdkContext.sleep, in case of record mode will not sleep, otherwise sleep.
     *
     * @param milliseconds time in milliseconds for which to sleep.
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

    public static synchronized <T> int getSize(Iterable<T> iterable) {
        int res = 0;

        for (T t : iterable) {
            res++;
        }
        return res;
    }

    public static synchronized <T> boolean isEmpty(PagedIterable<T> iterable) {
        return !iterable.iterator().hasNext();
    }
}
