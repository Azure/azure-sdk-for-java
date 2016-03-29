/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.interceptor;

import com.microsoft.azure.batch.DetailLevel;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DetailLevelInterceptor extends RequestInterceptor {

    private final DetailLevel detailLevel;

    public DetailLevelInterceptor(final DetailLevel detailLevel) {
        this.detailLevel = detailLevel;
        this.setHandler(new BatchRequestInterceptHandler() {
            @Override
            public void modify(Object request) {
                if (detailLevel != null) {
                    Class<?> c = request.getClass();
                    try {
                        Method selectMethod = c.getMethod("setSelect", new Class[]{String.class});
                        if (selectMethod != null) {
                            selectMethod.invoke(request, detailLevel.getSelectClause());
                        }
                    }
                    catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                    }

                    try {
                        Method filterMethod = c.getMethod("setFilter", new Class[]{String.class});
                        if (filterMethod != null) {
                            filterMethod.invoke(request, detailLevel.getFilterClause());
                        }
                    }
                    catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                    }

                    try {
                        Method expandMethod = c.getMethod("setExpand", new Class[]{String.class});
                        if (expandMethod != null) {
                            expandMethod.invoke(request, detailLevel.getExpandClause());
                        }
                    }
                    catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                    }
                }
            }
        });
    }
}
