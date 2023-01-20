// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.revapi.transforms;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.azure.tools.revapi.transforms.AzureSdkTreeFilterProvider.excludeClass;
import static com.azure.tools.revapi.transforms.AzureSdkTreeFilterProvider.excludePackage;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests exclusion logic for {@link AzureSdkTreeFilterProvider}.
 */
public class AzureSdkTreeFilterProviderTest {
    @ParameterizedTest
    @ValueSource(strings = {
        "com.azure.core.util.Configuration", "com.azure.cosmos.BridgeInternal", "com.azure.cosmos.CosmosBridgeInternal",
        "com.azure.cosmos.models.ModelBridgeInternal", "com.azure.cosmos.util.UtilBridgeInternal",
        "com.azure.spring.cloud.config.AppConfigurationBootstrapConfiguration",
        "com.azure.spring.cloud.config.AppConfigurationRefresh",
        "com.azure.spring.cloud.config.properties.AppConfigurationProviderProperties",
        "com.azure.spring.cloud.config.web.AppConfigurationEndpoint",
        "com.azure.spring.cloud.config.web.pushrefresh.AppConfigurationRefreshEvent"
    })
    public void classesThatShouldBeExcluded(String className) {
        assertTrue(excludeClass(className));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "com.azure.core.util.CoreUtils", "com.azure.cosmos.ConnectionMode", "com.azure.cosmos.CosmosClient",
        "com.azure.cosmos.models.ChangeFeedPolicy", "com.azure.cosmos.util.CosmosPagedFlux",
        "com.azure.spring.cloud.config.AppConfigurationConstants", "com.azure.spring.cloud.config.HostType",
        "com.azure.spring.cloud.config.properties.ConfigStore",
        "com.azure.spring.cloud.config.web.AppConfigurationWebAutoConfiguration",
        "com.azure.spring.cloud.config.web.pushrefresh.AppConfigurationRefreshEndpoint"
    })
    public void classesThatShouldNotBeExcluded(String className) {
        assertFalse(excludeClass(className));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "com.azure.data.cosmos", "com.azure.core.implementation", "com.azure.resourcemanager.fluent",
        "com.fasterxml.jackson", "com.google.gson", "com.microsoft.azure", "com.nimbusds", "io.micrometer", "io.netty",
        "io.vertx", "javax.jms", "javax.servlet", "kotlin", "okhttp3", "okio", "org.apache.avro", "org.apache.commons",
        "org.apache.qpid", "org.junit", "org.slf4j", "org.springframework", "reactor.core", "reactor.netty",
        "reactor.util"
    })
    public void packagesThatShouldBeExcluded(String packageName) {
        assertTrue(excludePackage(packageName));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "com.azure.cosmos", "com.azure.core.util", "com.azure.resourcemanager.models"
    })
    public void packagesThatShouldBeNotExcluded(String packageName) {
        assertFalse(excludePackage(packageName));
    }
}
