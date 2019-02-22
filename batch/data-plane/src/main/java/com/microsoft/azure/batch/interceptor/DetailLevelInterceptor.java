// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.batch.interceptor;

import com.microsoft.azure.batch.DetailLevel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Interceptor which contains a function used to apply the {@link DetailLevel}.
 * If there are multiple instances of this then the last set wins.
 */
public class DetailLevelInterceptor extends RequestInterceptor {

    private final DetailLevel detailLevel;

    /**
     * Initializes a new {@link DetailLevelInterceptor} for applying a {@link DetailLevel} object to a request.
     *
     * @param detailLevel The {@link DetailLevel} object.
     */
    public DetailLevelInterceptor(final DetailLevel detailLevel) {
        this.detailLevel = detailLevel;
        this.withHandler(new BatchRequestInterceptHandler() {
            @Override
            public void modify(Object request) {
                if (detailLevel != null) {
                    Class<?> c = request.getClass();
                    try {
                        Method selectMethod = c.getMethod("withSelect", String.class);
                        if (selectMethod != null) {
                            selectMethod.invoke(request, detailLevel.selectClause());
                        }
                    }
                    catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                        // Ignore exception
                    }

                    try {
                        Method filterMethod = c.getMethod("withFilter", String.class);
                        if (filterMethod != null) {
                            filterMethod.invoke(request, detailLevel.filterClause());
                        }
                    }
                    catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                        // Ignore exception
                    }

                    try {
                        Method expandMethod = c.getMethod("withExpand", String.class);
                        if (expandMethod != null) {
                            expandMethod.invoke(request, detailLevel.expandClause());
                        }
                    }
                    catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                        // Ignore exception
                    }
                }
            }
        });
    }

    /**
     * Gets the detail level applied by this {@link DetailLevelInterceptor} instance.
     *
     * @return The detail level applied.
     */
    public DetailLevel detailLevel() {
        return this.detailLevel;
    }
}
