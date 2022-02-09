// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.web;

/**
 * Test constants which can be shared across different test classes
 */
public final class TestConstants {

    // Store specific configuration
    public static final String CONFIG_ENABLED_PROP = "spring.cloud.azure.appconfiguration.enabled";
    public static final String CONN_STRING_PROP = "spring.cloud.azure.appconfiguration.stores[0].connection-string";
    public static final String STORE_ENDPOINT_PROP = "spring.cloud.azure.appconfiguration.stores[0].endpoint";
    public static final String STORE = "fake.test";
    public static final String TEST_CONN_STRING = "Endpoint=https://" + STORE
        + ".azconfig.io;Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==";
    public static final String TEST_STORE_NAME = "store1";
    public static final String TRIGGER_KEY = "trigger_key";
    public static final String TRIGGER_LABEL = "trigger_label";
    public static final String VALIDATION_URL = "\"https://rp-eastus2.eventgrid.azure.net:553/eventsubscriptions/estest/validate?id=512d38b6-c7b8-40c8-89fe-f46f9e9622b6&t=2018-04-26T20:30:54.4538837Z&apiVersion=2018-05-01-preview&token=1A1A1A1A\"";
    public static final String TOPIC =
        "\"/subscriptions/75d5ab06/resourceGroups/rg/providers/Microsoft.AppConfiguration/configurationstores/"
            + STORE + "\"";

    private TestConstants() {
    }

}
