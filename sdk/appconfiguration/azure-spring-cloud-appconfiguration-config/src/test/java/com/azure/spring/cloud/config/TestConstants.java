// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config;

/**
 * Test constants which can be shared across different test classes
 */
public final class TestConstants {

    // Store specific configuration
    public static final String TEST_STORE_NAME = "store1";
    public static final String CONN_STRING_PROP = "spring.cloud.azure.appconfiguration.stores[0].connection-string";
    public static final String CONN_STRING_PROP_NEW =
        "spring.cloud.azure.appconfiguration.stores[1].connection-string";
    public static final String STORE_ENDPOINT_PROP = "spring.cloud.azure.appconfiguration.stores[0].endpoint";
    public static final String KEY_PROP = "spring.cloud.azure.appconfiguration.stores[0].selects[0].key-filter";
    public static final String LABEL_PROP = "spring.cloud.azure.appconfiguration.stores[0].selects[0].label-filter";
    public static final String WATCHED_KEY_PROP = "spring.cloud.azure.appconfiguration.stores[0].watched-key";
    public static final String CLIENT_ID = "spring.cloud.azure.appconfiguration.managed-identity.client-id";
    public static final String DEFAULT_CONTEXT_PROP = "spring.cloud.azure.appconfiguration.default-context";
    public static final String CONFIG_ENABLED_PROP = "spring.cloud.azure.appconfiguration.enabled";
    public static final String REFRESH_INTERVAL_PROP = "spring.cloud.azure.appconfiguration.refresh-interval";
    public static final String FAIL_FAST_PROP = "spring.cloud.azure.appconfiguration.failFast";
    public static final String KEY_VAULT_STORE_1_CONNECTION =
        "spring.cloud.azure.appconfiguration.key_vault_stores[0].connection_url";
    public static final String KEY_VAULT_STORE_1_CLIENT =
        "spring.cloud.azure.appconfiguration.key_vault_stores[0].client_id";
    public static final String KEY_VAULT_STORE_1_DOMAIN =
        "spring.cloud.azure.appconfiguration.key_vault_stores[0].domain";
    public static final String KEY_VAULT_STORE_1_SECRET =
        "spring.cloud.azure.appconfiguration.key_vault_stores[0].secret";
    public static final String TEST_CONN_STRING =
        "Endpoint=https://fake.test.config.io;Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==";
    public static final String TEST_CONN_STRING_2 =
        "Endpoint=https://fake2.test.config.io;Id=fake-conn-id;Secret=ZmFrZS1jb25uLXNlY3JldA==";
    public static final String LABEL_PARAM = "label";
    public static final String TEST_ENDPOINT = "https://fake.test.config.io";
    public static final String TEST_KV_API = TEST_ENDPOINT + "/kv?key=fake-key*&label=fake-label";
    public static final String TEST_ID = "fake-conn-id";
    public static final String TEST_ACCESS_TOKEN = "fake_token";
    // from
    // fake-conn-secret
    public static final String TEST_DEFAULT_CONTEXT = "/application/";
    public static final String TEST_CONTEXT = "/foo/";
    public static final String TEST_KEY_1 = "test_key_1";
    public static final String TEST_VALUE_1 = "test_value_1";
    public static final String TEST_LABEL_1 = "test_label_1";
    public static final String TEST_KEY_2 = "test_key_2";
    public static final String TEST_VALUE_2 = "test_value_2";
    public static final String TEST_LABEL_2 = "test_label_2";
    public static final String TEST_KEY_3 = "test_key_3";
    public static final String TEST_VALUE_3 = "test_value_3";
    public static final String TEST_LABEL_3 = "test_label_3";
    public static final String TEST_WATCH_KEY = "my.watched.key";
    public static final String TEST_WATCH_KEY_PATTERN = "my.watched.key.*";
    public static final String TEST_ETAG = "fake-etag";
    public static final String TEST_KEY_VAULT_1 = "test_key_vault_1";
    public static final String TEST_URI_VAULT_1 = "https://test.key.vault.com/my_secret_url";
    public static final String TEST_LABEL_VAULT_1 = "test_lable_vault_1";
    public static final String FEATURE_KEY = "feature-management.featureManagement";
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
    public static final String FEATURE_LABEL = "";
    public static final String LIST_KEY_1 = "test.list[0].key";
    public static final String LIST_KEY_2 = "test.list[1].key";
    public static final String TEST_SLASH_KEY = "test/slash/key";
    public static final String TEST_SLASH_VALUE = "prop value for slashed key name";
    public static final String TEST_SUBSCRIPTION_1 = "fake-subscription-id-1";
    public static final String TEST_SUBSCRIPTION_2 = "fake-subscription-id-2";
    public static final String TEST_SUBSCRIPTION_3 = "fake-subscription-id-3";
    public static final String TEST_RESOURCE_GROUP_1 = "fake-resource-group-1";
    public static final String TEST_RESOURCE_GROUP_2 = "fake-resource-group-2";
    public static final String TEST_RESOURCE_GROUP_3 = "fake-resource-group-3";
    public static final String TEST_CONFIG_TYPE = "Microsoft.AppConfiguration/configurationStores";
    public static final String TEST_NON_CONFIG_TYPE = "Incorrect.Resource.Type.1";
    public static final String TEST_STORE_NAME_1 = "fake-config-store-1";
    public static final String TEST_STORE_NAME_2 = "fake-config-store-2";

    private TestConstants() {
    }
}
