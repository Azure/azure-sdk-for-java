// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.perf.core;

import com.azure.core.util.Configuration;

public class Utils {
    static final Configuration CONFIGURATION = Configuration.getGlobalConfiguration();
    static final String PROPERTY_CONTAINERREGISTRY_SUBSCRIPTION_ID = "CONTAINERREGISTRY_SUBSCRIPTION_ID";
    static final String PROPERTY_CONTAINERREGISTRY_ENDPOINT = "CONTAINERREGISTRY_ENDPOINT";
    static final String PROPERTY_CONTAINERREGISTRY_NAME = "CONTAINERREGISTRY_REGISTRY_NAME";
    static final String PROPERTY_CONTAINERREGISTRY_RESOURCE_GROUP = "CONTAINERREGISTRY_RESOURCE_GROUP";
    static final String REGISTRY_URI = "registry.hub.docker.com";
    public static final String REPOSITORY_NAME = "library/node";
    public static final String TEST_PERF_TAG1_NAME = "test-perf-tag1";
    public static final String TEST_PERF_TAG2_NAME = "test-perf-tag2";
    public static final String TEST_PERF_TAG3_NAME = "test-perf-tag3";
    public static final String TEST_PERF_TAG4_NAME = "test-perf-tag4";
    public static final String TEST_PERF_TAG5_NAME = "test-perf-tag5";
}
