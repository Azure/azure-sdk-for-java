// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

/**
 * Adjusts the "match" operation to EXCLUDE the body when matching a request to a recording's entries
 */
public class BodilessMatcher extends TestProxyRequestMatcher {

    /**
     * Creates an instance of BodilessMatcher
     */
    public BodilessMatcher() {
        super(TestProxyRequestMatcherType.BODILESS);
    }
}
