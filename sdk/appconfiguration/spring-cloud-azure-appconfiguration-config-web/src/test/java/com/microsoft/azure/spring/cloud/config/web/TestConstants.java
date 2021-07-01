// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config.web;

/**
 * Test constants which can be shared across different test classes
 */
public class TestConstants {
    private TestConstants() {
    }

    // Store specific configuration
    public static final String CONFIG_ENABLED_PROP = "spring.cloud.azure.appconfiguration.enabled";
    public static final String CONN_STRING_PROP = "spring.cloud.azure.appconfiguration.stores[0].connection-string";
    public static final String STORE_ENDPOINT_PROP = "spring.cloud.azure.appconfiguration.stores[0].endpoint";
    public static final String TEST_CONN_STRING =
            "Endpoint=https://fake.test.config.io;Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==";
    public static final String TEST_STORE_NAME = "store1";
}
