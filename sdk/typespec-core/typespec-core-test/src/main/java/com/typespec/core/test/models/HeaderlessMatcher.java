// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.models;

/**
 * Adjusts the "match" operation to ignore header differences when matching a request
 * */
public class HeaderlessMatcher extends TestProxyRequestMatcher {
    /**
     * Creates an instance of HeaderlessMatcher
     */
    public HeaderlessMatcher() {
        super(TestProxyRequestMatcherType.HEADERLESS);
    }
}
