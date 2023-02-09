// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

/**
 * A matcher is applied during a playback session. The default matcher matches a request on headers, URI, and the body on playback recorded data.
 */
public class TestProxyMatcher {
    private final TestProxyMatcherType testProxyMatcherType;

    /**
     * Creates an instance of TestProxyMatcher
     * @param testProxyMatcherType teh type of matcher
     */
    public TestProxyMatcher(TestProxyMatcherType testProxyMatcherType) {
        this.testProxyMatcherType = testProxyMatcherType;
    }

    /**
     * Get the type of proxy sanitizer
     * @return the type of proxy sanitizer
     */
    public TestProxyMatcherType getType() {
        return testProxyMatcherType;
    }

    /**
     * The possible record sanitizer types.
     * Each sanitizer is optionally prefaced with the specific part of the request/response pair that it applies to.
     */
    public enum TestProxyMatcherType {

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

        TestProxyMatcherType(String name) {
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
