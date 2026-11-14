// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultPropertySourceProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.authentication.TokenCredentialConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.core.io.support.SpringFactoriesLoader;

import static org.assertj.core.api.Assertions.assertThat;

class KeyVaultEnvironmentRuntimeHintsTests {

    @Test
    void registersHintsForEnvironmentPropertyBinding() throws NoSuchMethodException {
        ClassLoader classLoader = getClass().getClassLoader();
        RuntimeHints hints = new RuntimeHints();
        SpringFactoriesLoader.forResourceLocation("META-INF/spring/aot.factories", classLoader)
            .load(RuntimeHintsRegistrar.class)
            .stream()
            .filter(KeyVaultEnvironmentRuntimeHints.class::isInstance)
            .forEach(registrar -> registrar.registerHints(hints, classLoader));

        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(AzureKeyVaultSecretProperties.class, "getPropertySources")).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onConstructorInvocation(AzureKeyVaultPropertySourceProperties.class.getDeclaredConstructor()))
            .accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(AzureKeyVaultPropertySourceProperties.class, "setEndpoint")).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(AzureGlobalProperties.class, "getCredential")).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(TokenCredentialConfigurationProperties.class, "setClientId")).accepts(hints);
    }
}
