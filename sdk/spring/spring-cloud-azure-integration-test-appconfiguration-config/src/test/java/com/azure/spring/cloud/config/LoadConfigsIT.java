// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.data.appconfiguration.models.SecretReferenceConfigurationSetting;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.feature.management.FeatureManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

@SpringBootTest(classes = AppConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
    "spring.config.import=azureAppConfiguration",
    "spring.cloud.azure.appconfiguration.stores[0].endpoint=${STORE_NAME}",
    "spring.cloud.azure.appconfiguration.stores[0].feature-flags.enabled=true"
})
class LoadConfigsIT {

    @Autowired
    private MessageProperties properties;

    @Autowired
    private Environment environment;

    @Autowired
    private FeatureManager featureManager;

    @Test
    void loadsRemoteProperties() {
        assertEquals("Test", properties.getMessage());
        assertEquals(properties.getMessage(), environment.getProperty("config.message"));
    }

    @Test
    void resolvesKeyVaultReference() {
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        String endpoint = environment.getRequiredProperty("spring.cloud.azure.appconfiguration.stores[0].endpoint");
        SecretReferenceConfigurationSetting reference = assertInstanceOf(SecretReferenceConfigurationSetting.class,
            new ConfigurationClientBuilder().endpoint(endpoint).credential(credential).buildClient()
                .getConfigurationSetting("/application/config.secret", null));
        URI secretId = URI.create(reference.getSecretId());
        String[] segments = secretId.getPath().split("/");
        String expected = new SecretClientBuilder()
            .vaultUrl(secretId.getScheme() + "://" + secretId.getAuthority()).credential(credential).buildClient()
            .getSecret(segments[2], segments.length > 3 ? segments[3] : null).getValue();

        assertTrue(expected != null && expected.equals(properties.getSecret()),
            "The bound secret must match the referenced Key Vault secret.");
    }

    @Test
    void evaluatesRemoteFeatureFlag() {
        assertTrue(featureManager.isEnabled("Alpha"));
    }
}
