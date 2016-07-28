/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PageSizeInterceptor extends RequestInterceptor {

    private final int maxResults;

    public PageSizeInterceptor(int pageSize) {
        this.maxResults = pageSize;
        this.withHandler(new BatchRequestInterceptHandler() {
            @Override
            public void modify(Object request) {
                Class<?> c = request.getClass();
                try {
                    Method maxResultsMethod = c.getMethod("setMaxResults", new Class[]{Integer.class});
                    if (maxResultsMethod != null) {
                        maxResultsMethod.invoke(request, maxResults);
                    }
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                }
            }
        });
    }
}
