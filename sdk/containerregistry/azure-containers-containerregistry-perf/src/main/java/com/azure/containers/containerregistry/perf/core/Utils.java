// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.perf.core;

import com.azure.core.util.Configuration;

/**
 * Utilities for the Container Registry performance tests.
 */
public class Utils {
    static final Configuration CONFIGURATION = Configuration.getGlobalConfiguration();
    static final String PROPERTY_CONTAINERREGISTRY_SUBSCRIPTION_ID = "CONTAINERREGISTRY_SUBSCRIPTION_ID";
    static final String PROPERTY_CONTAINERREGISTRY_ENDPOINT = "CONTAINERREGISTRY_ENDPOINT";
    static final String PROPERTY_CONTAINERREGISTRY_NAME = "CONTAINERREGISTRY_REGISTRY_NAME";
    static final String PROPERTY_CONTAINERREGISTRY_RESOURCE_GROUP = "CONTAINERREGISTRY_RESOURCE_GROUP";
    static final String REGISTRY_URI = "registry.hub.docker.com";

    /**
     * Well-known repository name.
     */
    public static final String REPOSITORY_NAME = "library/node";

    /**
     * Well-known tag name.
     */
    public static final String TEST_PERF_TAG1_NAME = "test-perf-tag1";

    /**
     * Well-known tag name.
     */
    public static final String TEST_PERF_TAG2_NAME = "test-perf-tag2";

    /**
     * Well-known tag name.
     */
    public static final String TEST_PERF_TAG3_NAME = "test-perf-tag3";

    /**
     * Well-known tag name.
     */
    public static final String TEST_PERF_TAG4_NAME = "test-perf-tag4";

    /**
     * Well-known tag name.
     */
    public static final String TEST_PERF_TAG5_NAME = "test-perf-tag5";
}
