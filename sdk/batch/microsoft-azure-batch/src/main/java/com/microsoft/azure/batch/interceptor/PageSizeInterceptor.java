// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Interceptor which contains a function used to set the maximum page size of a request.
 * If there are multiple instances of this then the last set wins.
 */
public class PageSizeInterceptor extends RequestInterceptor {

    private final int maxResults;

    /**
     * Initializes a new {@link PageSizeInterceptor} for setting maximum page size of a request.
     *
     * @param pageSize The maximum number of items to return in a response.
     */
    public PageSizeInterceptor(int pageSize) {
        this.maxResults = pageSize;
        this.withHandler(new BatchRequestInterceptHandler() {
            @Override
            public void modify(Object request) {
                Class<?> c = request.getClass();
                try {
                    Method maxResultsMethod = c.getMethod("withMaxResults", Integer.class);
                    maxResultsMethod.invoke(request, maxResults);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                    // Ignore exception
                }
            }
        });
    }

    /**
     * Gets the maximum number of items applied by this {@link PageSizeInterceptor} instance.
     *
     * @return The maximum number of items to return in a response.
     */
    public int maxResults() {
        return this.maxResults;
    }
}
