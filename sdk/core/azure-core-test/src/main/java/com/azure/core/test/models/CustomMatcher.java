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

    private boolean compareBodies;

    /**
     * Creates an instance of CustomMatcher
     */
    public CustomMatcher() {
        super(TestProxyMatcherType.CUSTOM);
    }

    /**
     * Gets the A comma separated list of headers that should be excluded during matching. For example,
     * "Authorization, Content-Length",.
     *
     * @return the excluded headers
     */
    public String getExcludedHeaders() {
        return excludedHeaders;
    }

    /**
     * Sets the comma separated list of headers that should be excluded during matching. For example,
     * "Authorization, Content-Length",.
     *
     * @param excludedHeaders the excluded headers
     * @return The updated {@link CustomMatcher} object.
     */
    public CustomMatcher setExcludedHeaders(String excludedHeaders) {
        this.excludedHeaders = excludedHeaders;
        return this;
    }


    /**
     * Gets the comma separated list of headers that should be ignored during matching.
     * The header values won't be matched, but the presence of these headers will be taken into account while matching.
     *
     * @return the ignored headers
     */
    public String getIgnoredHeaders() {
        return ignoredHeaders;
    }


    /**
     * Sets ignored headers. A comma separated list of headers that should be ignored during matching.
     * The header values won't be matched, but the presence of these headers will be taken into account while matching.
     *
     * @param ignoredHeaders the ignored headers
     * @return The updated {@link CustomMatcher} object.
     */
    public CustomMatcher setIgnoredHeaders(String ignoredHeaders) {
        this.ignoredHeaders = ignoredHeaders;
        return this;
    }

    /**
     * Get the boolean value to sort query params alphabetically before comparing URIs when matching
     * @return the boolean
     */
    public boolean isIgnoreQueryOrdering() {
        return ignoreQueryOrdering;
    }

    /**
     * Sets query ordering to a boolean value to sort query params alphabetically before comparing URIs when matching.
     *
     * @param ignoreQueryOrdering the ignore query ordering boolean value
     * @return The updated {@link CustomMatcher} object.
     */
    public CustomMatcher setIgnoreQueryOrdering(boolean ignoreQueryOrdering) {
        this.ignoreQueryOrdering = ignoreQueryOrdering;
        return this;
    }

    /**
     * Gets the comma separated list of query parameters that should be ignored during matching.
     * The parameter values won't be matched, but the presence of these parameters will be taken into account.
     *
     * @return the ignored query parameters
     */
    public String getIgnoredQueryParameters() {
        return ignoredQueryParameters;
    }

    /**
     * Sets the comma separated list of query parameters that should be ignored during matching.
     * The parameter values won't be matched, but the presence of these parameters will be taken into account.
     *
     * @param ignoredQueryParameters the ignored query parameters
     * @return The updated {@link CustomMatcher} object.
     */
    public CustomMatcher setIgnoredQueryParameters(String ignoredQueryParameters) {
        this.ignoredQueryParameters = ignoredQueryParameters;
        return this;
    }

    /**
     * Get the compare bodies boolean.
     * True to enable body matching (default behavior), or false to disable body matching.
     *
     * @return the boolean
     */
    public boolean isCompareBodies() {
        return compareBodies;
    }

    /**
     * Sets true to enable body matching (default behavior), or false to disable body matching.
     *
     * @param compareBodies the compare bodies
     * @return The updated {@link CustomMatcher} object.
     */
    public CustomMatcher setCompareBodies(boolean compareBodies) {
        this.compareBodies = compareBodies;
        return this;
    }
}
