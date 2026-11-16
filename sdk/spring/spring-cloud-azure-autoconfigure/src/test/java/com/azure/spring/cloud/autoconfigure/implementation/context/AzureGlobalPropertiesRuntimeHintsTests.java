// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.context;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.authentication.TokenCredentialConfigurationProperties;
import com.azure.spring.cloud.autoconfigure.implementation.properties.core.profile.AzureProfileConfigurationProperties;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class AzureGlobalPropertiesRuntimeHintsTests {

    @Test
    void registersHintsForGlobalPropertyBinding() throws NoSuchMethodException {
        assertGlobalBindingHints(getClass().getClassLoader());
    }

    @Test
    void registersHintsWithoutKeyVaultClient() throws IOException, NoSuchMethodException {
        try (FilteredClassLoader classLoader = new FilteredClassLoader("com.azure.security.keyvault.secrets")) {
            assertGlobalBindingHints(classLoader);
        }
    }

    private void assertGlobalBindingHints(ClassLoader classLoader) throws NoSuchMethodException {
        RuntimeHints hints = new RuntimeHints();
        SpringFactoriesLoader.forResourceLocation("META-INF/spring/aot.factories", classLoader)
            .load(RuntimeHintsRegistrar.class)
            .forEach(registrar -> registrar.registerHints(hints, classLoader));

        assertThat(RuntimeHintsPredicates.reflection()
            .onConstructorInvocation(AzureGlobalProperties.class.getDeclaredConstructor())).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(AzureGlobalProperties.class, "getCredential")).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(TokenCredentialConfigurationProperties.class, "setClientId")).accepts(hints);
        assertThat(RuntimeHintsPredicates.reflection()
            .onMethodInvocation(AzureProfileConfigurationProperties.class, "setTenantId")).accepts(hints);
    }
}
