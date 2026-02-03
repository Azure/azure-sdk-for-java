// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;

/**
 * Test for CosmosClientCacheConfig
 */
public class CosmosClientCacheConfigTest {

    @Test(groups = "unit")
    void shouldBeEqualWhenAllValuesAreSame() {
        CosmosAadAuthConfig authConfig =
            new CosmosAadAuthConfig(
                "endpoint",
                "tenantId",
                "clientId",
                "clientSecret",
                CosmosAzureEnvironment.AZURE);

        CosmosClientCacheConfig config1 = new CosmosClientCacheConfig(
            "https://test.documents.azure.com",
            authConfig,
            "testApp",
            true,
            Arrays.asList("region1", "region2"),
            "context1"
        );

        CosmosClientCacheConfig config2 = new CosmosClientCacheConfig(
            "https://test.documents.azure.com",
            authConfig,
            "testApp",
            true,
            Arrays.asList("region1", "region2"),
            "context1"
        );

        assertThat(config1)
            .isEqualTo(config2)
            .hasSameHashCodeAs(config2);
    }

    @Test(groups = "unit")
    void shouldNotBeEqualWhenValuesAreDifferent() {
        CosmosAadAuthConfig authConfig1 =
            new CosmosAadAuthConfig(
                "endpoint",
                "tenantId1",
                "clientId",
                "clientSecret",
                CosmosAzureEnvironment.AZURE);
        CosmosAadAuthConfig authConfig2 =
            new CosmosAadAuthConfig(
                "endpoint",
                "tenantId2",
                "clientId",
                "clientSecret",
                CosmosAzureEnvironment.AZURE);

        CosmosClientCacheConfig config1 = new CosmosClientCacheConfig(
            "https://test1.documents.azure.com",
            authConfig1,
            "testApp1",
            true,
            Arrays.asList("region1", "region2"),
            "context1"
        );

        CosmosClientCacheConfig config2 = new CosmosClientCacheConfig(
            "https://test2.documents.azure.com",
            authConfig2,
            "testApp2",
            false,
            Collections.singletonList("region1"),
            "context2"
        );

        assertThat(config1)
            .isNotEqualTo(config2)
            .doesNotHaveSameHashCodeAs(config2);
    }

    @Test(groups = "unit")
    void shouldSerializeToStringCorrectly() {
        CosmosAadAuthConfig authConfig =
            new CosmosAadAuthConfig(
                "endpoint",
                "tenantId",
                "clientId",
                "clientSecret",
                CosmosAzureEnvironment.AZURE);

        CosmosClientCacheConfig config = new CosmosClientCacheConfig(
            "https://test.documents.azure.com",
            authConfig,
            "testApp",
            true,
            Arrays.asList("region1", "region2"),
            "context1"
        );

        String expected = "https://test.documents.azure.com|" + authConfig.toString() +
            "|testApp|true|region1,region2|context1";

        assertThat(config.toString())
            .isEqualTo(expected)
            .contains("https://test.documents.azure.com")
            .contains("testApp")
            .contains("region1,region2")
            .contains("context1");
    }

    @Test(groups = "unit")
    void shouldHandleNullValuesCorrectly() {
        CosmosAadAuthConfig authConfig =
            new CosmosAadAuthConfig(
                "endpoint",
                "tenantId",
                "clientId",
                "clientSecret",
                CosmosAzureEnvironment.AZURE);

        CosmosClientCacheConfig config = new CosmosClientCacheConfig(
            "https://test.documents.azure.com",
            authConfig,
            null,
            true,
            null,
            null
        );

        String expected = "https://test.documents.azure.com|" + authConfig.toString() +
            "||true||";

        assertThat(config.toString())
            .isEqualTo(expected)
            .contains("https://test.documents.azure.com")
            .contains("||true||");
    }

    @Test(groups = "unit")
    void shouldNotBeEqualToNull() {
        CosmosAadAuthConfig authConfig =
            new CosmosAadAuthConfig(
                "endpoint",
                "tenantId",
                "clientId",
                "clientSecret",
                CosmosAzureEnvironment.AZURE);

        CosmosClientCacheConfig config = new CosmosClientCacheConfig(
            "https://test.documents.azure.com",
            authConfig,
            "testApp",
            true,
            Arrays.asList("region1", "region2"),
            "context1"
        );

        assertThat(config).isNotNull();
    }

    @Test(groups = "unit")
    void shouldNotBeEqualToDifferentClass() {
        CosmosAadAuthConfig authConfig =
            new CosmosAadAuthConfig(
                "endpoint",
                "tenantId",
                "clientId",
                "clientSecret",
                CosmosAzureEnvironment.AZURE);

        CosmosClientCacheConfig config = new CosmosClientCacheConfig(
            "https://test.documents.azure.com",
            authConfig,
            "testApp",
            true,
            Arrays.asList("region1", "region2"),
            "context1"
        );

        assertThat(config).isNotEqualTo(new Object());
    }
}
