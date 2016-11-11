/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

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
     * Initializes a new {@link DetailLevelInterceptor} for applying DetailLevel in a request.
     *
     * @param detailLevel the DetailLevel
     */
    public DetailLevelInterceptor(final DetailLevel detailLevel) {
        this.detailLevel = detailLevel;
        this.withHandler(new BatchRequestInterceptHandler() {
            @Override
            public void modify(Object request) {
                if (detailLevel != null) {
                    Class<?> c = request.getClass();
                    try {
                        Method selectMethod = c.getMethod("withSelect", new Class[]{String.class});
                        if (selectMethod != null) {
                            selectMethod.invoke(request, detailLevel.selectClause());
                        }
                    }
                    catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                    }

                    try {
                        Method filterMethod = c.getMethod("withFilter", new Class[]{String.class});
                        if (filterMethod != null) {
                            filterMethod.invoke(request, detailLevel.filterClause());
                        }
                    }
                    catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                    }

                    try {
                        Method expandMethod = c.getMethod("withExpand", new Class[]{String.class});
                        if (expandMethod != null) {
                            expandMethod.invoke(request, detailLevel.expandClause());
                        }
                    }
                    catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                    }
                }
            }
        });
    }
}
