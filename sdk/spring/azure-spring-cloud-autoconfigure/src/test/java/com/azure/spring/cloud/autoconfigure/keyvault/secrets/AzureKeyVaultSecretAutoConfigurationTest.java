// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.keyvault.secrets;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AzureKeyVaultSecretAutoConfigurationTest {

    private static final String KEY_VAULT_URL = "https:/%s.vault.azure.net/";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureKeyVaultSecretAutoConfiguration.class));

    @Test
    void withoutSecretClientBuilderShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(SecretClientBuilder.class))
            .withPropertyValues("spring.cloud.azure.keyvault.secret.vault-url=" + String.format(KEY_VAULT_URL, "mykv"))
            .run(context -> assertThat(context).doesNotHaveBean(AzureKeyVaultSecretAutoConfiguration.class));
    }

    @Test
    void disableKeyVaultSecretShouldNotConfigure() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.keyvault.secret.enabled=false",
                "spring.cloud.azure.keyvault.secret.vault-url=" + String.format(KEY_VAULT_URL, "mykv")
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureKeyVaultSecretAutoConfiguration.class));
    }

    @Test
    void withoutVaultUrlShouldNotConfigure() {
        this.contextRunner
            .run(context -> assertThat(context).doesNotHaveBean(AzureKeyVaultSecretAutoConfiguration.class));
    }

    @Test
    void withVaultUrlShouldConfigure() {
        this.contextRunner
            .withPropertyValues("spring.cloud.azure.keyvault.secret.vault-url=" + String.format(KEY_VAULT_URL, "mykv"))
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureKeyVaultSecretAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureKeyVaultSecretProperties.class);
                assertThat(context).hasSingleBean(SecretClient.class);
                assertThat(context).hasSingleBean(SecretAsyncClient.class);
                assertThat(context).hasSingleBean(SecretClientBuilder.class);
                assertThat(context).hasSingleBean(SecretClientBuilderFactory.class);
            });
    }

}
