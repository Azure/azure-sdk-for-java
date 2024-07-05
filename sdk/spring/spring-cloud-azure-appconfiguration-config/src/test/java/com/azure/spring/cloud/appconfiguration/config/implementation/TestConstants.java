// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

/**
 * Test constants which can be shared across different test classes
 */
public final class TestConstants {

    // Store specific configuration
    public static final String TEST_STORE_NAME = "store1";

    public static final String CONN_STRING_PROP = "spring.cloud.azure.appconfiguration.stores[0].connection-string";

    public static final String CONN_STRING_PROP_NEW = "spring.cloud.azure.appconfiguration.stores[1].connection-string";

    public static final String STORE_ENDPOINT_PROP = "spring.cloud.azure.appconfiguration.stores[0].endpoint";

    public static final String KEY_PROP = "spring.cloud.azure.appconfiguration.stores[0].selects[0].key-filter";

    public static final String LABEL_PROP = "spring.cloud.azure.appconfiguration.stores[0].selects[0].label-filter";

    public static final String REFRESH_INTERVAL_PROP = "spring.cloud.azure.appconfiguration.refresh-interval";

    public static final String FAIL_FAST_PROP = "spring.cloud.azure.appconfiguration.failFast";

    public static final String TEST_CONN_STRING = "Endpoint=https://fake.test.config.io;Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==";

    public static final String TEST_CONN_STRING_2 = "Endpoint=https://fake2.test.config.io;Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==";

    public static final String TEST_CONN_STRING_GEO = "Endpoint=https://fake.test.geo.config.io;Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==";

    public static final String TEST_ENDPOINT = "https://fake.test.config.io";

    public static final String TEST_ENDPOINT_GEO = "https://fake.test.geo.config.io";

    // from
    // fake-conn-secret
    public static final String TEST_KEY_1 = "test_key_1";

    public static final String TEST_VALUE_1 = "test_value_1";

    public static final String TEST_LABEL_1 = "test_label_1";

    public static final String TEST_KEY_2 = "test_key_2";

    public static final String TEST_VALUE_2 = "test_value_2";

    public static final String TEST_LABEL_2 = "test_label_2";

    public static final String TEST_KEY_3 = "test_key_3";

    public static final String TEST_VALUE_3 = "test_value_3";

    public static final String TEST_LABEL_3 = "test_label_3";

    public static final String TEST_KEY_VAULT_1 = "test_key_vault_1";

    public static final String TEST_URI_VAULT_1 = "https://test.key.vault.com/my_secret_url";
    
    public static final String TEST_URI_VAULT_2 = "not a valid uri";

    public static final String TEST_LABEL_VAULT_1 = "test_label_vault_1";

    public static final String FEATURE_VALUE = "{\"id\":\"Alpha\",\"description\":\"\",\"enabled\":true,"
        + "\"conditions\":{\"client_filters\":[{\"Name\":\"TestFilter\"}]}}";

    public static final String FEATURE_BOOLEAN_VALUE = "{\"id\":\"Beta\",\"description\":\"\",\"enabled\":true,"
        + "\"conditions\":{\"client_filters\":[]}}";

    public static final String FEATURE_VALUE_PARAMETERS = "{\"id\":\"Alpha\",\"description\":\"\",\"enabled\":true,"
        + "\"conditions\":{\"client_filters\":[{\"Name\":\"TestFilter\",\"Parameters\":{\"key\":\"value\"}}]}}";

    public static final String FEATURE_VALUE_TARGETING = "{\"id\":\"target\",\"description\":\"\",\"enabled\":true,"
        + "\"conditions\":{\"client_filters\":[{\"Name\":\"targetingFilter\",\"Parameters\":{\"Users\":[\"Jeff\","
        + "\"Alicia\"],\"Groups\":[{\"name\":\"Ring0\",\"rolloutPercentage\":100},{\"name\":\"Ring1\","
        + "\"rolloutPercentage\":100}],\"DefaultRolloutPercentage\":50}}]}}]}}";

    public static final String FEATURE_VALUE_TELEMETRY = "{\"id\":\"Delta\",\"description\":\"\",\"enabled\":true,"
        + "\"conditions\":{\"client_filters\":[{\"Name\":\"TestFilter\",\"Parameters\":{\"key\":\"value\"}}]},"
        + "\"telemetry\":{\"enabled\":true,\"metadata\":{\"key\":\"value\"}}}";

    public static final String FEATURE_VALUE_ALL = "{ \"id\": \"AndTest\", \"description\": \"\",\"enabled\": true,\"conditions\": {\"requirement_type\": \"All\",\"client_filters\": [{\"Name\": \"percentageFilter\",\"Parameters\": {\"Value\": 50}},{\"Name\": \"percentageFilter\",\"Parameters\": {\"Value\": 50}}]}}";

    public static final String FEATURE_LABEL = "";

    public static final String TEST_SLASH_KEY = "slash/key";

    public static final String TEST_SLASH_VALUE = "prop value for slashed key name";

    public static final String TEST_STORE_NAME_1 = "fake-config-store-1";

    public static final String TEST_STORE_NAME_2 = "fake-config-store-2";

    public static final String TEST_E_TAG = "4f6dd610dd5e4deebc7fbaef685fb903";

    private TestConstants() {
    }
}
