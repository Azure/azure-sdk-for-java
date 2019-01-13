// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Sets Batch service request timeouts.
 */
public class ServerTimeoutInterceptor extends RequestInterceptor {

    private final int serverTimeout;

    /**
     * Initializes a new {@link ServerTimeoutInterceptor} for setting the service timeout interval for a request issued to the Batch service.
     *
     * @param timeout The service timeout interval, in seconds.
     */
    public ServerTimeoutInterceptor(int timeout) {
        this.serverTimeout = timeout;
        this.withHandler(new BatchRequestInterceptHandler() {
            @Override
            public void modify(Object request) {
                Class<?> c = request.getClass();
                try {
                    Method timeoutMethod = c.getMethod("withTimeout", Integer.class);
                    if (timeoutMethod != null) {
                        timeoutMethod.invoke(request, serverTimeout);
                    }
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                    // Ignore exception
                }
            }
        });
    }

    /**
     * Gets the service timeout interval applied by this {@link ServerTimeoutInterceptor} instance.
     *
     * @return The service timeout interval, in seconds.
     */
    public int serverTimeout() {
        return this.serverTimeout;
    }
}
