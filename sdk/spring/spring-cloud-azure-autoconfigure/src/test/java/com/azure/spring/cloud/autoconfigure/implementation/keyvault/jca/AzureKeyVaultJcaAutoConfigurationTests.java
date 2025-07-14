// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca;

import com.azure.security.keyvault.jca.KeyVaultJcaProvider;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultJcaProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.jca.properties.AzureKeyVaultSslBundleProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AzureKeyVaultJcaAutoConfigurationTests {
    private static final String ENDPOINT = "https:/%s.vault.azure.net/";

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureKeyVaultJcaAutoConfiguration.class));

    @Test
    void noJcaProviderClass() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(KeyVaultJcaProvider.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureKeyVaultJcaAutoConfiguration.class));
    }

    @Test
    void noSslBundleClass() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(SslBundle.class))
            .run(context -> assertThat(context).doesNotHaveBean(AzureKeyVaultJcaAutoConfiguration.class));
    }

    @Test
    void disabledJca() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.keyvault.jca.enabled=false"
            )
            .run(context -> assertThat(context).doesNotHaveBean(AzureKeyVaultJcaAutoConfiguration.class));
    }

    @Test
    void keyVaultJca() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.keyvault.jca.vaults.kv1.endpoint=" + String.format(ENDPOINT, "test1"),
                "spring.cloud.azure.keyvault.jca.vaults.kv1.credential.client-id=client-id",
                "spring.cloud.azure.keyvault.jca.vaults.kv2.endpoint=" + String.format(ENDPOINT, "test2"),
                "spring.ssl.bundle.keyvault.testBundle1.truststore.certificate-paths.custom=classpath:keyvault/certificate-paths/custom",
                "spring.ssl.bundle.keyvault.testBundle2.truststore.keyvault-ref=kv2",
                "spring.ssl.bundle.keyvault.testBundle3.truststore.keyvault-ref=kv1",
                "spring.ssl.bundle.keyvault.testBundle3.keystore.keyvault-ref=kv2"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureKeyVaultJcaAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureKeyVaultJcaProperties.class);
                assertThat(context).hasSingleBean(AzureKeyVaultSslBundleProperties.class);
                assertThat(context).hasSingleBean(AzureKeyVaultSslBundleRegistrar.class);

                AzureKeyVaultJcaProperties jcaProperties = context.getBean(AzureKeyVaultJcaProperties.class);
                assertThat(jcaProperties.getVaults()).hasSize(2);
                assertThat(jcaProperties.getVaults().get("kv1").getEndpoint()).isEqualTo(String.format(ENDPOINT, "test1"));
                assertThat(jcaProperties.getVaults().get("kv1").getCredential().getClientId()).isEqualTo("client-id");
                assertThat(jcaProperties.getVaults().get("kv2").getEndpoint()).isEqualTo(String.format(ENDPOINT, "test2"));

                AzureKeyVaultSslBundleProperties sslBundlesProperties = context.getBean(AzureKeyVaultSslBundleProperties.class);
                assertThat(sslBundlesProperties.getKeyvault()).hasSize(3);
                assertThat(sslBundlesProperties.getKeyvault().get("testBundle1").getTruststore().getCertificatePaths().getCustom()).isEqualTo("classpath:keyvault/certificate-paths/custom");
                assertThat(sslBundlesProperties.getKeyvault().get("testBundle2").getTruststore().getKeyvaultRef()).isEqualTo("kv2");
                assertThat(sslBundlesProperties.getKeyvault().get("testBundle3").getTruststore().getKeyvaultRef()).isEqualTo("kv1");
                assertThat(sslBundlesProperties.getKeyvault().get("testBundle3").getKeystore().getKeyvaultRef()).isEqualTo("kv2");
            });
    }

    @Test
    void useSystemManagedIdentity() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.keyvault.jca.vaults.keyvault1.endpoint=" + String.format(ENDPOINT, "test1"),
                "spring.cloud.azure.keyvault.jca.vaults.kv1.credential.managed-identity-enabled=true",
                "spring.cloud.azure.keyvault.jca.vaults.kv1.profile.tenant-id=test-tenant-id",
                "spring.ssl.bundle.keyvault.tlsServerBundle.key.alias=server",
                "spring.ssl.bundle.keyvault.tlsServerBundle.keystore.keyvault-ref=kv1",
                "spring.ssl.bundle.keyvault.tlsClientBundle.truststore.keyvault-ref=kv1"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureKeyVaultJcaAutoConfiguration.class);
                assertThat(context).hasSingleBean(AzureKeyVaultJcaProperties.class);
                assertThat(context).hasSingleBean(AzureKeyVaultSslBundleProperties.class);
                assertThat(context).hasSingleBean(AzureKeyVaultSslBundleRegistrar.class);

                AzureKeyVaultJcaProperties jcaProperties = context.getBean(AzureKeyVaultJcaProperties.class);
                assertThat(jcaProperties.getVaults()).hasSize(2);
                assertThat(jcaProperties.getVaults().get("keyvault1").getEndpoint()).isEqualTo(String.format(ENDPOINT, "test1"));
                assertThat(jcaProperties.getVaults().get("kv1").getCredential().isManagedIdentityEnabled()).isTrue();

                AzureKeyVaultSslBundleProperties sslBundlesProperties = context.getBean(AzureKeyVaultSslBundleProperties.class);
                assertThat(sslBundlesProperties.getKeyvault()).hasSize(2);
                assertThat(sslBundlesProperties.getKeyvault().get("tlsServerBundle").getKeystore().getKeyvaultRef()).isEqualTo("kv1");
                assertThat(sslBundlesProperties.getKeyvault().get("tlsClientBundle").getTruststore().getKeyvaultRef()).isEqualTo("kv1");
            });
    }
}
