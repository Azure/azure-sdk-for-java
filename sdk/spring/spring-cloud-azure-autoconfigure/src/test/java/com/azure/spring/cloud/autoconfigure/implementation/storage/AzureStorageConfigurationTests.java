// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.storage;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.implementation.storage.common.AzureStorageProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;
import java.util.Arrays;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static org.assertj.core.api.Assertions.assertThat;

class AzureStorageConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(AzureStorageConfiguration.class));

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
                "spring.cloud.azure.storage.client.headers[0].name=storage-header-name",
                "spring.cloud.azure.storage.client.headers[0].values=a,b,c,d",
                "spring.cloud.azure.storage.client.read-timeout=4m",
                "spring.cloud.azure.storage.client.response-timeout=5m",
                "spring.cloud.azure.storage.client.write-timeout=5m",
                "spring.cloud.azure.credential.client-id=fake-client-id",
                "spring.cloud.azure.credential.client-secret=fake-client-secret",
                "spring.cloud.azure.credential.client-certificate-path=fake-cert-path",
                "spring.cloud.azure.credential.client-certificate-password=fake-cert-password",
                "spring.cloud.azure.credential.username=fake-username",
                "spring.cloud.azure.credential.password=fake-password",
                "spring.cloud.azure.credential.managed-identity-enabled=true",
                "spring.cloud.azure.storage.credential.username=storage-fake-username",
                "spring.cloud.azure.storage.credential.password=storage-fake-password",
                "spring.cloud.azure.storage.credential.managed-identity-enabled=false",
                "spring.cloud.azure.proxy.type=https",
                "spring.cloud.azure.proxy.hostname=proxy-host",
                "spring.cloud.azure.proxy.port=8888",
                "spring.cloud.azure.proxy.username=x-user",
                "spring.cloud.azure.proxy.password=x-password",
                "spring.cloud.azure.proxy.http.non-proxy-hosts=127.0.0.1",
                "spring.cloud.azure.storage.proxy.non-proxy-hosts=127.0.0.2",
                "spring.cloud.azure.storage.proxy.username=storage-x-user",
                "spring.cloud.azure.storage.proxy.password=storage-x-password",
                "spring.cloud.azure.retry.exponential.max-retries=1",
                "spring.cloud.azure.retry.exponential.base-delay=20s",
                "spring.cloud.azure.retry.exponential.max-delay=30s",
                "spring.cloud.azure.retry.fixed.max-retries=4",
                "spring.cloud.azure.retry.fixed.delay=50s",
                "spring.cloud.azure.retry.mode=fixed",
                "spring.cloud.azure.storage.retry.fixed.max-retries=8",
                "spring.cloud.azure.storage.retry.fixed.delay=60s",
                "spring.cloud.azure.profile.tenant-id=fake-tenant-id",
                "spring.cloud.azure.profile.subscription-id=fake-sub-id",
                "spring.cloud.azure.profile.cloud-type=azure_china",
                "spring.cloud.azure.storage.profile.tenant-id=storage-fake-tenant-id",
                "spring.cloud.azure.storage.profile.subscription-id=storage-fake-sub-id"
            )
            .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
            .run(context -> {
                assertThat(context).hasSingleBean(AzureStorageProperties.class);
                AzureStorageProperties azureStorageProperties = context.getBean(AzureStorageProperties.class);
                assertThat(azureStorageProperties.getClient().getApplicationId()).isEqualTo("fake-application-id");
                assertThat(azureStorageProperties.getClient().getHeaders().get(0).getName()).isEqualTo("storage-header-name");
                assertThat(azureStorageProperties.getClient().getHeaders().get(0).getValues()).isEqualTo(Arrays.asList("a", "b", "c", "d"));
                assertThat(azureStorageProperties.getClient().getConnectTimeout()).isEqualTo(Duration.ofMinutes(1));
                assertThat(azureStorageProperties.getClient().getReadTimeout()).isEqualTo(Duration.ofMinutes(4));
                assertThat(azureStorageProperties.getClient().getResponseTimeout()).isEqualTo(Duration.ofMinutes(5));
                assertThat(azureStorageProperties.getClient().getWriteTimeout()).isEqualTo(Duration.ofMinutes(5));
                assertThat(azureStorageProperties.getClient().getConnectionIdleTimeout()).isEqualTo(Duration.ofMinutes(5));
                assertThat(azureStorageProperties.getClient().getMaximumConnectionPoolSize()).isEqualTo(6);

                assertThat(azureStorageProperties.getCredential().getClientId()).isEqualTo("fake-client-id");
                assertThat(azureStorageProperties.getCredential().getClientSecret()).isEqualTo("fake-client-secret");
                assertThat(azureStorageProperties.getCredential().getClientCertificatePath()).isEqualTo("fake-cert-path");
                assertThat(azureStorageProperties.getCredential().getClientCertificatePassword()).isEqualTo("fake-cert-password");
                assertThat(azureStorageProperties.getCredential().getUsername()).isEqualTo("storage-fake-username");
                assertThat(azureStorageProperties.getCredential().getPassword()).isEqualTo("storage-fake-password");
                assertThat(azureStorageProperties.getCredential().isManagedIdentityEnabled()).isFalse();

                assertThat(azureStorageProperties.getProxy().getType()).isEqualTo("https");
                assertThat(azureStorageProperties.getProxy().getHostname()).isEqualTo("proxy-host");
                assertThat(azureStorageProperties.getProxy().getPort()).isEqualTo(8888);
                assertThat(azureStorageProperties.getProxy().getUsername()).isEqualTo("storage-x-user");
                assertThat(azureStorageProperties.getProxy().getPassword()).isEqualTo("storage-x-password");
                assertThat(azureStorageProperties.getProxy().getNonProxyHosts()).isEqualTo("127.0.0.2");

                assertThat(azureStorageProperties.getRetry().getExponential().getMaxRetries()).isEqualTo(1);
                assertThat(azureStorageProperties.getRetry().getExponential().getBaseDelay()).isEqualTo(Duration.ofSeconds(20));
                assertThat(azureStorageProperties.getRetry().getExponential().getMaxDelay()).isEqualTo(Duration.ofSeconds(30));
                assertThat(azureStorageProperties.getRetry().getFixed().getMaxRetries()).isEqualTo(8);
                assertThat(azureStorageProperties.getRetry().getFixed().getDelay()).isEqualTo(Duration.ofSeconds(60));
                assertThat(azureStorageProperties.getRetry().getMode()).isEqualTo(RetryOptionsProvider.RetryMode.FIXED);

                assertThat(azureStorageProperties.getProfile().getTenantId()).isEqualTo("storage-fake-tenant-id");
                assertThat(azureStorageProperties.getProfile().getSubscriptionId()).isEqualTo("storage-fake-sub-id");
                assertThat(azureStorageProperties.getProfile().getCloudType()).isEqualTo(AZURE_CHINA);
                assertThat(azureStorageProperties.getProfile().getEnvironment().getActiveDirectoryEndpoint()).isEqualTo(
                    AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint());
            });
    }

}
