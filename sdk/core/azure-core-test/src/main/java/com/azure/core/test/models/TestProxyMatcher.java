// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

/**
 * Keeps track of different sanitizers that redact the sensitive information when recording
 */
public class TestProxyMatcher {
    private TestProxyMatcherType testProxyMatcherType;
    private String excludedHeaders;
    private String ignoredHeaders;
    private boolean ignoreQueryOrdering;
    private String ignoredQueryParameters;

    /**
     * Get the type of proxy sanitizer
     * @return the type of proxy sanitizer
     */
    public TestProxyMatcherType getType() {
        return testProxyMatcherType;
    }

    public TestProxyMatcherType getTestProxyMatcherType() {
        return testProxyMatcherType;
    }

    public TestProxyMatcher setTestProxyMatcherType(TestProxyMatcherType testProxyMatcherType) {
        this.testProxyMatcherType = testProxyMatcherType;
        return this;
    }

    public String getExcludedHeaders() {
        return excludedHeaders;
    }

    public TestProxyMatcher setExcludedHeaders(String excludedHeaders) {
        this.excludedHeaders = excludedHeaders;
        return this;
    }

    public String getIgnoredHeaders() {
        return ignoredHeaders;
    }

    public TestProxyMatcher setIgnoredHeaders(String ignoredHeaders) {
        this.ignoredHeaders = ignoredHeaders;
        return this;
    }

    public boolean isIgnoreQueryOrdering() {
        return ignoreQueryOrdering;
    }

    public TestProxyMatcher setIgnoreQueryOrdering(boolean ignoreQueryOrdering) {
        this.ignoreQueryOrdering = ignoreQueryOrdering;
        return this;
    }

    public String getIgnoredQueryParameters() {
        return ignoredQueryParameters;
    }

    public TestProxyMatcher setIgnoredQueryParameters(String ignoredQueryParameters) {
        this.ignoredQueryParameters = ignoredQueryParameters;
        return this;
    }
}
