/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.utils;

/**
 * A wrapper class for thread sleep.
 */
public class DelayProvider {
    /**
     * Puts current thread on sleep for passed milliseconds.
     * @param milliseconds time to sleep for
     */
    public void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
        }
    }
}
