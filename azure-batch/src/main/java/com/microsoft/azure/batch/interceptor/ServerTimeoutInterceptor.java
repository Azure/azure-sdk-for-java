/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServerTimeoutInterceptor extends RequestInterceptor {

    private final int serverTimeout;

    public ServerTimeoutInterceptor(int timeout) {
        this.serverTimeout = timeout;
        this.withHandler(new BatchRequestInterceptHandler() {
            @Override
            public void modify(Object request) {
                Class<?> c = request.getClass();
                try {
                    Method timeoutMethod = c.getMethod("withTimeout", new Class[]{Integer.class});
                    if (timeoutMethod != null) {
                        timeoutMethod.invoke(request, serverTimeout);
                    }
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                }
            }
        });
    }
}
