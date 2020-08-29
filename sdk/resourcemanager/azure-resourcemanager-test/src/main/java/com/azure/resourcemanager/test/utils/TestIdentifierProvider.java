// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.test.utils;

import com.azure.core.management.provider.IdentifierProvider;
import com.azure.core.test.utils.TestResourceNamer;

/**
 * Class helps generate unique identifier.
 */
public class TestIdentifierProvider implements IdentifierProvider {

    private final TestResourceNamer testResourceNamer;

    /**
     * Constructor of TestIdentifierProvider
     *
     * @param testResourceNamer the test resource namer
     */
    public TestIdentifierProvider(TestResourceNamer testResourceNamer) {
        this.testResourceNamer = testResourceNamer;
    }

    @Override
    public String randomName(String prefix, int maxLen) {
        return testResourceNamer.randomName(prefix, maxLen);
    }

    @Override
    public String randomUuid() {
        return testResourceNamer.randomUuid();
    }
}
