// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.models;

/**
 * A matcher is applied during a playback session. The default matcher matches a request on headers, URI, and the body on playback recorded data.
 */
public class TestProxyRequestMatcher {
    private final TestProxyRequestMatcherType testProxyMatcherType;

    /**
     * Creates an instance of TestProxyMatcher
     * @param testProxyMatcherType the type of matcher
     */
    public TestProxyRequestMatcher(TestProxyRequestMatcherType testProxyMatcherType) {
        this.testProxyMatcherType = testProxyMatcherType;
    }

    /**
     * Get the type of proxy matcher
     * @return the type of proxy matcher
     */
    public TestProxyRequestMatcherType getType() {
        return testProxyMatcherType;
    }

    /**
     * The possible types for Matcher.
     */
    public enum TestProxyRequestMatcherType {

        /**
         * Adjusts the "match" operation to EXCLUDE the body when matching a request to a recording's entries
         */
        BODILESS("BodilessMatcher"),

        /**
         * Exposes the default matcher to be customized, using setting properties of compareBodies, excludedHeaders, ignoredHeaders, ignoreQueryOrdering, ignoredQueryParameters
         */
        CUSTOM("CustomDefaultMatcher"),

        /**
         * Adjusts the "match" operation to ignore header differences when matching a request
         */
        HEADERLESS("HeaderlessMatcher");

        private final String name;

        TestProxyRequestMatcherType(String name) {
            this.name = name;
        }

        /**
         * Gets the name value of the enum.
         * @return the name value of the enum.
         */
        public String getName() {
            return name;
        }
    }
}
