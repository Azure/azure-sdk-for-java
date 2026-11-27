// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation;

import com.azure.identity.ClientSecretCredential;
import com.azure.spring.cloud.appconfiguration.config.ConfigurationClientCustomizer;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.AppConfigurationProperties;
import com.azure.spring.cloud.appconfiguration.config.implementation.properties.ConfigStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.bootstrap.DefaultBootstrapContext;
import org.springframework.boot.context.config.ConfigDataLocationResolverContext;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static com.azure.spring.cloud.appconfiguration.config.implementation.TestConstants.TEST_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureAppConfigurationBootstrapRegistrarTest {

    @Test
    void usesServiceCredentialsWithoutGlobalSettings() {
        Binder binder = new Binder(new MapConfigurationPropertySource(Map.of(
            "spring.cloud.azure.appconfiguration.credential.client-id", "test-client-id",
            "spring.cloud.azure.appconfiguration.credential.client-secret", "test-client-secret",
            "spring.cloud.azure.appconfiguration.profile.tenant-id", "test-tenant-id")));

        DefaultBootstrapContext bootstrapContext = new DefaultBootstrapContext();
        bootstrapContext.register(ConfigurationClientCustomizer.class, context -> (builder, endpoint) ->
            assertInstanceOf(ClientSecretCredential.class, ReflectionTestUtils.getField(builder, "tokenCredential")));
        ConfigDataLocationResolverContext context = mock(ConfigDataLocationResolverContext.class);
        when(context.getBootstrapContext()).thenReturn(bootstrapContext);

        ConfigStore store = new ConfigStore();
        store.setEndpoint(TEST_ENDPOINT);
        store.validateAndInit();
        AppConfigurationProperties properties = new AppConfigurationProperties();
        properties.setStores(List.of(store));

        AzureAppConfigurationBootstrapRegistrar.register(context, binder, properties, null);
        assertEquals(1, bootstrapContext.get(AppConfigurationReplicaClientFactory.class)
            .getConnections().get(TEST_ENDPOINT).getAvailableClients().size());
    }
}
