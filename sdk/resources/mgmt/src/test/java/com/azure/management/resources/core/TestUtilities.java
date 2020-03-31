/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.core;

import com.azure.core.http.rest.PagedIterable;

import java.util.Iterator;

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

    public synchronized static <T> int getSize(Iterable<T> iterable) {
        int res = 0;

        for (T t : iterable) {
            res++;
        }
        return res;
    }

    public synchronized static <T> boolean isEmpty(PagedIterable<T> iterable) {
        return !iterable.iterator().hasNext();
    }
}
