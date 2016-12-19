/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Interceptor which contains a function used to generate a client request ID.
 * If there are multiple instances of this then the last set wins.
 */
public class ClientRequestIdInterceptor extends RequestInterceptor {
    /**
     * Initializes a new {@link ClientRequestIdInterceptor} for use in setting the client request ID of a request.
     */
    public ClientRequestIdInterceptor() {
        this.withHandler(new BatchRequestInterceptHandler() {
            @Override
            public void modify(Object request) {
                Class<?> c = request.getClass();

                try {
                    Method clientRequestIdMethod = c.getMethod("withClientRequestId", new Class[]{String.class});
                    if (clientRequestIdMethod != null) {
                        String clientRequestId = UUID.randomUUID().toString();
                        clientRequestIdMethod.invoke(request, clientRequestId);
                    }

                    Method returnClientRequestIdMethod = c.getMethod("withReturnClientRequestId", new Class[]{Boolean.class});
                    if (returnClientRequestIdMethod != null) {
                        returnClientRequestIdMethod.invoke(request, true);
                    }
                }
                catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                }

            }
        });
    }
}
