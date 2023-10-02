// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.models;

import java.util.List;

/**
 * This matcher exposes the default matcher in a customizable way.
 * Currently, this includes ignoring/excluding headers, comparing request bodies and ignoring query params or query params ordering.
 */
public class CustomMatcher extends TestProxyRequestMatcher {
    private List<String> excludedHeaders;

    private List<String> headersKeyOnlyMatch;
    private boolean queryOrderingIgnored;
    private List<String> ignoredQueryParameters;

    private boolean comparingBodies;

    /**
     * Creates an instance of CustomMatcher
     */
    public CustomMatcher() {
        super(TestProxyRequestMatcherType.CUSTOM);
    }

    /**
     * Gets the list of headers that should be excluded during matching.
     * The presence of these headers will not be taken into account while matching.
     *
     * @return the excluded headers list
     */
    public List<String> getExcludedHeaders() {
        return excludedHeaders;
    }

    /**
     * Sets the list of headers that should be excluded during matching.
     * The presence of these headers will not be taken into account while matching.
     *
     * @param excludedHeaders the list of excluded headers
     * @return The updated {@link CustomMatcher} object.
     */
    public CustomMatcher setExcludedHeaders(List<String> excludedHeaders) {
        this.excludedHeaders = excludedHeaders;
        return this;
    }


    /**
     * Gets the list of headers that should be ignored during matching.
     * The header values won't be matched, but the presence of these headers will be taken into account while matching.
     *
     * @return the ignored headers list
     */
    public List<String> getHeadersKeyOnlyMatch() {
        return headersKeyOnlyMatch;
    }


    /**
     * Sets the list of headers that should be ignored during matching.
     * The header values won't be matched, but the presence of these headers will be taken into account while matching.
     *
     * @param headersKeyOnlyMatch the ignored headers list
     * @return The updated {@link CustomMatcher} object.
     */
    public CustomMatcher setHeadersKeyOnlyMatch(List<String> headersKeyOnlyMatch) {
        this.headersKeyOnlyMatch = headersKeyOnlyMatch;
        return this;
    }

    /**
     * Get the boolean value to sort query params alphabetically before comparing URIs when matching
     * @return the boolean
     */
    public boolean isQueryOrderingIgnored() {
        return queryOrderingIgnored;
    }

    /**
     * Sets query ordering to a boolean value to sort query params alphabetically before comparing URIs when matching.
     *
     * @param queryOrderingIgnored to ignore query ordering boolean value
     * @return The updated {@link CustomMatcher} object.
     */
    public CustomMatcher setQueryOrderingIgnored(boolean queryOrderingIgnored) {
        this.queryOrderingIgnored = queryOrderingIgnored;
        return this;
    }

    /**
     * Gets the list of query parameters that should be ignored during matching.
     * The parameter values won't be matched, but the presence of these parameters will be taken into account.
     *
     * @return the ignored query parameters
     */
    public List<String> getIgnoredQueryParameters() {
        return ignoredQueryParameters;
    }

    /**
     * Sets the list of query parameters that should be ignored during matching.
     * The parameter values won't be matched, but the presence of these parameters will be taken into account.
     *
     * @param ignoredQueryParameters the ignored query parameters
     * @return The updated {@link CustomMatcher} object.
     */
    public CustomMatcher setIgnoredQueryParameters(List<String> ignoredQueryParameters) {
        this.ignoredQueryParameters = ignoredQueryParameters;
        return this;
    }

    /**
     * Get the comparing bodies boolean.
     * True to enable body matching (default behavior), or false to disable body matching.
     *
     * @return the boolean
     */
    public boolean isComparingBodies() {
        return comparingBodies;
    }

    /**
     * Sets true to enable body matching (default behavior), or false to disable body matching.
     *
     * @param comparingBodies the compare bodies
     * @return The updated {@link CustomMatcher} object.
     */
    public CustomMatcher setComparingBodies(boolean comparingBodies) {
        this.comparingBodies = comparingBodies;
        return this;
    }
}
