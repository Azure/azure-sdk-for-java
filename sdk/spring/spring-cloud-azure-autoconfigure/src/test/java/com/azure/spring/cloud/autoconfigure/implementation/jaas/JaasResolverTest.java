// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.jaas;

import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class JaasResolverTest {

    @Test
    void testResolveJaasWithInvalidLoginModule() {
        Jaas jaas = JaasResolver.resolve("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required");
        assertNull(jaas.getLoginModule());
        assertNull(jaas.getControlFlag());
        assertThat(jaas.getOptions().size()).isEqualTo(0);

        jaas = JaasResolver.resolve("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModulerequired;");
        assertNull(jaas.getLoginModule());
        assertNull(jaas.getControlFlag());
        assertThat(jaas.getOptions().size()).isEqualTo(0);

        jaas = JaasResolver.resolve(null);
        assertNull(jaas.getLoginModule());
        assertNull(jaas.getControlFlag());
        assertThat(jaas.getOptions().size()).isEqualTo(0);
    }

    @Test
    void testResolveJaasWithoutAzureProperties() {
        Jaas jaas = JaasResolver.resolve("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
        assertThat(jaas.getLoginModule()).isEqualTo("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule");
        assertThat(jaas.getOptions().size()).isEqualTo(0);
        assertThat(jaas.getControlFlag()).isEqualTo("required");
    }

    @Test
    void testResolveJaasWithAzureProperties() {
        Jaas jaas = JaasResolver.resolve("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required "
            + "azure.credential.managed-identity-enabled=\"true\" azure.credential.client-id=\"test\" azure.profile.cloud-type=\"azure\";");
        assertThat(jaas.getOptions().size()).isEqualTo(3);
        assertThat(jaas.getOptions().get("azure.credential.managed-identity-enabled")).isEqualTo("true");
        assertThat(jaas.getOptions().get("azure.credential.client-id")).isEqualTo("test");
        assertThat(jaas.getOptions().get("azure.profile.cloud-type")).isEqualTo("azure");
        assertThat(jaas.getControlFlag()).isEqualTo("required");
        assertThat(jaas.getLoginModule()).isEqualTo(OAuthBearerLoginModule.class.getName());
    }
}
