// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.keyvault.common.AzureKeyVaultProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;
import java.util.Arrays;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static org.assertj.core.api.Assertions.assertThat;

class AzureKeyVaultConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
        .withConfiguration(AutoConfigurations.of(AzureKeyVaultConfiguration.class));

    @Test
    void configurationPropertiesShouldBind() {
        this.contextRunner
            .withPropertyValues(
                "spring.cloud.azure.client.application-id=fake-application-id",
                "spring.cloud.azure.client.http.headers[0].name=header-name",
                "spring.cloud.azure.client.http.headers[0].values=a,b,c",
                "spring.cloud.azure.client.http.connect-timeout=1m",
                "spring.cloud.azure.client.http.read-timeout=2m",
                "spring.cloud.azure.client.http.response-timeout=3m",
                "spring.cloud.azure.client.http.write-timeout=4m",
                "spring.cloud.azure.client.http.connection-idle-timeout=5m",
                "spring.cloud.azure.client.http.maximum-connection-pool-size=6",
                "spring.cloud.azure.keyvault.client.headers[0].name=keyvault-header-name",
                "spring.cloud.azure.keyvault.client.headers[0].values=a,b,c,d",
                "spring.cloud.azure.keyvault.client.read-timeout=4m",
                "spring.cloud.azure.keyvault.client.response-timeout=5m",
                "spring.cloud.azure.keyvault.client.write-timeout=5m",
                "spring.cloud.azure.credential.client-id=fake-client-id",
                "spring.cloud.azure.credential.client-secret=fake-client-secret",
                "spring.cloud.azure.credential.client-certificate-path=fake-cert-path",
                "spring.cloud.azure.credential.client-certificate-password=fake-cert-password",
                "spring.cloud.azure.credential.username=fake-username",
                "spring.cloud.azure.credential.password=fake-password",
                "spring.cloud.azure.credential.managed-identity-enabled=true",
                "spring.cloud.azure.keyvault.credential.username=keyvault-fake-username",
                "spring.cloud.azure.keyvault.credential.password=keyvault-fake-password",
                "spring.cloud.azure.keyvault.credential.managed-identity-enabled=false",
                "spring.cloud.azure.proxy.type=https",
                "spring.cloud.azure.proxy.hostname=proxy-host",
                "spring.cloud.azure.proxy.port=8888",
                "spring.cloud.azure.proxy.username=x-user",
                "spring.cloud.azure.proxy.password=x-password",
                "spring.cloud.azure.proxy.http.non-proxy-hosts=127.0.0.1",
                "spring.cloud.azure.keyvault.proxy.non-proxy-hosts=127.0.0.2",
                "spring.cloud.azure.keyvault.proxy.username=keyvault-x-user",
                "spring.cloud.azure.keyvault.proxy.password=keyvault-x-password",
                "spring.cloud.azure.retry.exponential.max-retries=1",
                "spring.cloud.azure.retry.exponential.base-delay=20s",
                "spring.cloud.azure.retry.exponential.max-delay=30s",
                "spring.cloud.azure.retry.fixed.max-retries=4",
                "spring.cloud.azure.retry.fixed.delay=50s",
                "spring.cloud.azure.retry.mode=fixed",
                "spring.cloud.azure.keyvault.retry.fixed.max-retries=8",
                "spring.cloud.azure.keyvault.retry.fixed.delay=60s",
                "spring.cloud.azure.profile.tenant-id=fake-tenant-id",
                "spring.cloud.azure.profile.subscription-id=fake-sub-id",
                "spring.cloud.azure.profile.cloud-type=azure_china",
                "spring.cloud.azure.keyvault.profile.tenant-id=keyvault-fake-tenant-id",
                "spring.cloud.azure.keyvault.profile.subscription-id=keyvault-fake-sub-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureKeyVaultProperties.class);
                AzureKeyVaultProperties azureKeyVaultProperties = context.getBean(AzureKeyVaultProperties.class);
                assertThat(azureKeyVaultProperties.getClient().getApplicationId()).isEqualTo("fake-application-id");
                assertThat(azureKeyVaultProperties.getClient().getHeaders().get(0).getName()).isEqualTo("keyvault-header-name");
                assertThat(azureKeyVaultProperties.getClient().getHeaders().get(0).getValues()).isEqualTo(Arrays.asList("a", "b", "c", "d"));
                assertThat(azureKeyVaultProperties.getClient().getConnectTimeout()).isEqualTo(Duration.ofMinutes(1));
                assertThat(azureKeyVaultProperties.getClient().getReadTimeout()).isEqualTo(Duration.ofMinutes(4));
                assertThat(azureKeyVaultProperties.getClient().getResponseTimeout()).isEqualTo(Duration.ofMinutes(5));
                assertThat(azureKeyVaultProperties.getClient().getWriteTimeout()).isEqualTo(Duration.ofMinutes(5));
                assertThat(azureKeyVaultProperties.getClient().getConnectionIdleTimeout()).isEqualTo(Duration.ofMinutes(5));
                assertThat(azureKeyVaultProperties.getClient().getMaximumConnectionPoolSize()).isEqualTo(6);

                assertThat(azureKeyVaultProperties.getCredential().getClientId()).isEqualTo("fake-client-id");
                assertThat(azureKeyVaultProperties.getCredential().getClientSecret()).isEqualTo("fake-client-secret");
                assertThat(azureKeyVaultProperties.getCredential().getClientCertificatePath()).isEqualTo("fake-cert-path");
                assertThat(azureKeyVaultProperties.getCredential().getClientCertificatePassword()).isEqualTo("fake-cert-password");
                assertThat(azureKeyVaultProperties.getCredential().getUsername()).isEqualTo("keyvault-fake-username");
                assertThat(azureKeyVaultProperties.getCredential().getPassword()).isEqualTo("keyvault-fake-password");
                assertThat(azureKeyVaultProperties.getCredential().isManagedIdentityEnabled()).isFalse();

                assertThat(azureKeyVaultProperties.getProxy().getType()).isEqualTo("https");
                assertThat(azureKeyVaultProperties.getProxy().getHostname()).isEqualTo("proxy-host");
                assertThat(azureKeyVaultProperties.getProxy().getPort()).isEqualTo(8888);
                assertThat(azureKeyVaultProperties.getProxy().getUsername()).isEqualTo("keyvault-x-user");
                assertThat(azureKeyVaultProperties.getProxy().getPassword()).isEqualTo("keyvault-x-password");
                assertThat(azureKeyVaultProperties.getProxy().getNonProxyHosts()).isEqualTo("127.0.0.2");

                assertThat(azureKeyVaultProperties.getRetry().getExponential().getMaxRetries()).isEqualTo(1);
                assertThat(azureKeyVaultProperties.getRetry().getExponential().getBaseDelay()).isEqualTo(Duration.ofSeconds(20));
                assertThat(azureKeyVaultProperties.getRetry().getExponential().getMaxDelay()).isEqualTo(Duration.ofSeconds(30));
                assertThat(azureKeyVaultProperties.getRetry().getFixed().getMaxRetries()).isEqualTo(8);
                assertThat(azureKeyVaultProperties.getRetry().getFixed().getDelay()).isEqualTo(Duration.ofSeconds(60));
                assertThat(azureKeyVaultProperties.getRetry().getMode()).isEqualTo(RetryOptionsProvider.RetryMode.FIXED);

                assertThat(azureKeyVaultProperties.getProfile().getTenantId()).isEqualTo("keyvault-fake-tenant-id");
                assertThat(azureKeyVaultProperties.getProfile().getSubscriptionId()).isEqualTo("keyvault-fake-sub-id");
                assertThat(azureKeyVaultProperties.getProfile().getCloudType()).isEqualTo(AZURE_CHINA);
                assertThat(azureKeyVaultProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint()).isEqualTo(
                    AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint());
            });
    }

}
