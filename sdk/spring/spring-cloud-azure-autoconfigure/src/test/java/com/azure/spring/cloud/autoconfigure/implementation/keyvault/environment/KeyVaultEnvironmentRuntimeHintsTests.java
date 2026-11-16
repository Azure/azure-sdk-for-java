// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment;

import com.azure.identity.AzureIdentityEnvVars;
import com.azure.json.JsonReader;
import com.azure.json.JsonWriter;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultPropertySourceProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.secrets.properties.AzureKeyVaultSecretProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.authentication.TokenCredentialConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;

import static org.assertj.core.api.Assertions.assertThat;

class KeyVaultEnvironmentRuntimeHintsTests {

    @ParameterizedTest
    @ValueSource(strings = {
        "com.azure.security.keyvault.secrets.implementation.models.SecretItem",
        "com.azure.security.keyvault.secrets.implementation.models.SecretBundle"
    })
    void registersHintsForSecretResponseSerialization(String typeName)
        throws ClassNotFoundException, NoSuchMethodException {
        Class<?> type = Class.forName(typeName);
        RuntimeHints hints = new RuntimeHints();
        new KeyVaultEnvironmentRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(type.getDeclaredMethod("fromJson", JsonReader.class))).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(type.getDeclaredMethod("toJson", JsonWriter.class))).accepts(hints);
    }

    @Test
    void registersHintsForIdentityEnvironmentVariables() throws NoSuchMethodException {
        RuntimeHints hints = new RuntimeHints();
        new KeyVaultEnvironmentRuntimeHints().registerHints(hints, getClass().getClassLoader());

        assertThat(RuntimeHintsPredicates.reflection()
            .onConstructorInvocation(AzureIdentityEnvVars.class.getDeclaredConstructor()))
            .accepts(hints);
    }

    @Test
    void doesNotRegisterHintsWithoutKeyVaultClient() {
        RuntimeHints hints = new RuntimeHints();
        new KeyVaultEnvironmentRuntimeHints().registerHints(hints,
            new FilteredClassLoader("com.azure.security.keyvault.secrets"));

        assertThat(hints.reflection().typeHints()).isEmpty();
    }

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
            .onMethodInvocation(TokenCredentialConfigurationProperties.class, "setClientId")).accepts(hints);
    }
}
