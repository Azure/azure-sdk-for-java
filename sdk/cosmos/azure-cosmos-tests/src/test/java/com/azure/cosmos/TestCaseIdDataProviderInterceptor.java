// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import org.testng.IDataProviderInterceptor;
import org.testng.IDataProviderMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public final class TestCaseIdDataProviderInterceptor implements IDataProviderInterceptor {
    private static final String TEST_CASE_FILTER_PROPERTY = "COSMOS.TEST_CASE_FILTER";

    @Override
    public Iterator<Object[]> intercept(
        Iterator<Object[]> original,
        IDataProviderMethod dataProviderMethod,
        ITestNGMethod method,
        ITestContext context) {

        String filter = System.getProperty(TEST_CASE_FILTER_PROPERTY);
        if (filter == null || filter.trim().isEmpty()) {
            return original;
        }

        if (!PerPartitionCircuitBreakerE2ETests.class.equals(method.getRealClass())) {
            return original;
        }

        String normalizedFilter = filter.trim().toLowerCase(Locale.ROOT);
        List<Object[]> rows = new ArrayList<>();
        List<Object[]> matches = new ArrayList<>();
        boolean hasTestCaseIds = false;
        while (original.hasNext()) {
            Object[] parameters = original.next();
            rows.add(parameters);
            if (parameters.length > 0
                && parameters[0] instanceof String) {
                hasTestCaseIds = true;
                if (((String) parameters[0]).toLowerCase(Locale.ROOT).contains(normalizedFilter)) {
                    matches.add(parameters);
                }
            }
        }

        if (!hasTestCaseIds) {
            return rows.iterator();
        }

        if (matches.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                "No data-provider rows for %s matched %s=%s",
                method.getQualifiedName(),
                TEST_CASE_FILTER_PROPERTY,
                filter));
        }

        return matches.iterator();
    }
}
