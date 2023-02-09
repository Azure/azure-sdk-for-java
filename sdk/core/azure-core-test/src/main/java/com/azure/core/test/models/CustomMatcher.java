// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

/**
 * This matcher exposes the default matcher in a customizable way.
 * Currently, this includes ignoring/excluding headers, comparing request bodies and ignoring query params or query params ordering.
 */
public class CustomMatcher extends TestProxyMatcher {
    private String excludedHeaders;
    private String ignoredHeaders;
    private boolean ignoreQueryOrdering;
    private String ignoredQueryParameters;

    /**
     * Creates an instance of TestProxyMatcher
     * @param testProxyMatcherType teh type of matcher
     */
    public CustomMatcher() {
        super(TestProxyMatcherType.CUSTOM);
    }
    public String getExcludedHeaders() {
        return excludedHeaders;
    }

    public CustomMatcher setExcludedHeaders(String excludedHeaders) {
        this.excludedHeaders = excludedHeaders;
        return this;
    }

    public String getIgnoredHeaders() {
        return ignoredHeaders;
    }

    public CustomMatcher setIgnoredHeaders(String ignoredHeaders) {
        this.ignoredHeaders = ignoredHeaders;
        return this;
    }

    public boolean isIgnoreQueryOrdering() {
        return ignoreQueryOrdering;
    }

    public CustomMatcher setIgnoreQueryOrdering(boolean ignoreQueryOrdering) {
        this.ignoreQueryOrdering = ignoreQueryOrdering;
        return this;
    }

    public String getIgnoredQueryParameters() {
        return ignoredQueryParameters;
    }

    public CustomMatcher setIgnoredQueryParameters(String ignoredQueryParameters) {
        this.ignoredQueryParameters = ignoredQueryParameters;
        return this;
    }
}
