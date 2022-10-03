// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

/**
 * Adding an unchecked {@link InterruptedException}.
 */
public class UncheckedInterruptedException extends RuntimeException {
    public UncheckedInterruptedException(Throwable error) {
        super("Unable to fetch batch.", error);
    }
}
