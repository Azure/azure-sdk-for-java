// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

public class ServiceBusExceptionTestHelper {
    /**
     * Get the error source out of the ServiceBusException (used in tests where we're not in the
     * same package, since errorSource is an internal field)
     */
    public static ServiceBusErrorSource getInternalErrorSource(ServiceBusException exc) {
        return exc.getErrorSource();
    }
}

