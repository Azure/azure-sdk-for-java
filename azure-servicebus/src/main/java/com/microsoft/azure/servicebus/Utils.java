// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

final class Utils {

    static <T> T completeFuture(CompletableFuture<T> future) throws InterruptedException, ServiceBusException {
        try {
            return future.get();
        } catch (InterruptedException ie) {
            // Rare instance
            throw ie;
        } catch (ExecutionException ee) {
            Throwable cause = ee.getCause();
            if (cause instanceof ServiceBusException) {
                throw (ServiceBusException) cause;
            } else {
                throw new ServiceBusException(true, cause);
            }
        }
    }

    static void assertNonNull(String argumentName, Object argument) {
        if (argument == null)
            throw new IllegalArgumentException("Argument '" + argumentName + "' is null.");
    }
}
