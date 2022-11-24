// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka.jaas;

import com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class JaasResolverTest {

    @Test
    void testResolveJaasWithoutAzureProperties() {
        Jaas jaas = JaasResolver.resolve("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
        assertThat(jaas.getOptions().size()).isEqualTo(1);
        assertThat(jaas.getOptions().get(AzureKafkaConfigurationUtils.AZURE_CONFIGURED_JAAS_OPTIONS_KEY)).isEqualTo(AzureKafkaConfigurationUtils.AZURE_CONFIGURED_JAAS_OPTIONS_VALUE);
        assertThat(jaas.getControlFlag()).isEqualTo("required");
        assertThat(jaas.getLoginModule()).isEqualTo(OAuthBearerLoginModule.class.getName());

    }

    @Test
    void testResolveJaasWithAzureProperties() {
        Jaas jaas = JaasResolver.resolve("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required "
            + "azure.credential.managed-identity-enabled=\"true\" azure.credential.client-id=\"test\" azure.profile.cloud-type=\"azure\";");
        assertThat(jaas.getOptions().size()).isEqualTo(4);
        assertThat(jaas.getOptions().get(AzureKafkaConfigurationUtils.AZURE_CONFIGURED_JAAS_OPTIONS_KEY)).isEqualTo(AzureKafkaConfigurationUtils.AZURE_CONFIGURED_JAAS_OPTIONS_VALUE);
        assertThat(jaas.getOptions().get("azure.credential.managed-identity-enabled")).isEqualTo("true");
        assertThat(jaas.getOptions().get("azure.credential.client-id")).isEqualTo("test");
        assertThat(jaas.getOptions().get("azure.profile.cloud-type")).isEqualTo("azure");
        assertThat(jaas.getControlFlag()).isEqualTo("required");
        assertThat(jaas.getLoginModule()).isEqualTo(OAuthBearerLoginModule.class.getName());

    }
}
