/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public class ClientRequestIdInterceptor extends RequestInterceptor {
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
