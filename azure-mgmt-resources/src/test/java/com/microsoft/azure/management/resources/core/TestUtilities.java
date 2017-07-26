/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.core;

/**
 * Common utility functions for the tests.
 */
public class TestUtilities {
    /**
     * Wrapper on the Thread.sleep, in case of record mode will not sleep, otherwise sleep.
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

}
