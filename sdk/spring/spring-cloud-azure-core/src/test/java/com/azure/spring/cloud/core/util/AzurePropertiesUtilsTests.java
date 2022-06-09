// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.util;

import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.core.implementation.properties.AzureAmqpSdkProperties;
import com.azure.spring.cloud.core.implementation.properties.AzureHttpSdkProperties;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.client.ClientProperties;
import com.azure.spring.cloud.core.properties.client.HeaderProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;
import com.azure.spring.cloud.core.properties.proxy.ProxyProperties;
import com.azure.spring.cloud.core.properties.retry.RetryProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.OTHER;
import static com.azure.spring.cloud.core.provider.RetryOptionsProvider.RetryMode.EXPONENTIAL;
import static com.azure.spring.cloud.core.provider.RetryOptionsProvider.RetryMode.FIXED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class AzurePropertiesUtilsTests {

    @Test
    void testCopyPropertiesToNewObjectShouldEqual() {
        AzurePropertiesA source = new AzurePropertiesA();
        source.client.setApplicationId("client-application-id-A");
        source.profile.setCloudType(AZURE_CHINA);
        source.profile.setTenantId("profile-tenant-id-A");
        source.profile.setSubscriptionId("profile-sub-id-A");
        source.profile.getEnvironment().setActiveDirectoryEndpoint("aad-endpoint-A");
        source.proxy.setType("proxy-type-A");
        source.proxy.setHostname("proxy-hostname-A");
        source.proxy.setPort(1234);
        source.proxy.setUsername("proxy-username-A");
        source.proxy.setPassword("proxy-password-A");
        source.retry.setMode(FIXED);
        source.retry.getExponential().setMaxRetries(3);
        source.retry.getExponential().setBaseDelay(Duration.ofSeconds(4));
        source.retry.getExponential().setMaxDelay(Duration.ofSeconds(5));
        source.retry.getFixed().setDelay(Duration.ofSeconds(6));
        source.retry.getFixed().setMaxRetries(7);
        source.credential.setClientId("credential-client-id-A");
        source.credential.setClientSecret("credential-client-secret-A");
        source.credential.setClientCertificatePath("credential-client-cert-path-A");
        source.credential.setClientCertificatePassword("credential-client-cert-password-A");
        source.credential.setUsername("credential-username-A");
        source.credential.setPassword("credential-password-A");
        source.credential.setManagedIdentityEnabled(true);

        final AzurePropertiesB target = new AzurePropertiesB();
        AzurePropertiesUtils.copyAzureCommonProperties(source, target);

        assertEquals("client-application-id-A", target.client.getApplicationId());
        assertEquals(AZURE_CHINA, target.profile.getCloudType());
        assertEquals("profile-tenant-id-A", target.profile.getTenantId());
        assertEquals("profile-sub-id-A", target.profile.getSubscriptionId());
        assertEquals("aad-endpoint-A", target.profile.getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryGraphApiVersion(), target.profile.getEnvironment().getActiveDirectoryGraphApiVersion());
        assertEquals("proxy-type-A", target.proxy.getType());
        assertEquals("proxy-hostname-A", target.proxy.getHostname());
        assertEquals(1234, target.proxy.getPort());
        assertEquals("proxy-username-A", target.proxy.getUsername());
        assertEquals("proxy-password-A", target.proxy.getPassword());
        assertEquals(FIXED, target.retry.getMode());
        assertEquals(3, target.retry.getExponential().getMaxRetries());
        assertEquals(Duration.ofSeconds(4), target.retry.getExponential().getBaseDelay());
        assertEquals(Duration.ofSeconds(5), target.retry.getExponential().getMaxDelay());
        assertEquals(Duration.ofSeconds(6), target.retry.getFixed().getDelay());
        assertEquals(7, target.retry.getFixed().getMaxRetries());
        assertEquals("credential-client-id-A", target.credential.getClientId());
        assertEquals("credential-client-secret-A", target.credential.getClientSecret());
        assertEquals("credential-client-cert-path-A", target.credential.getClientCertificatePath());
        assertEquals("credential-client-cert-password-A", target.credential.getClientCertificatePassword());
        assertEquals("credential-username-A", target.credential.getUsername());
        assertEquals("credential-password-A", target.credential.getPassword());

    }

    @Test
    void testCopyPropertiesToObjectWithSameFieldsSetShouldOverrideWithNull() {
        AzurePropertiesB target = new AzurePropertiesB();
        target.client.setApplicationId("client-application-id-B");
        target.profile.setCloudType(AZURE);
        target.profile.setTenantId("profile-tenant-id-B");
        target.profile.setSubscriptionId("profile-sub-id-B");
        target.profile.getEnvironment().setActiveDirectoryEndpoint("aad-endpoint-B");
        target.proxy.setType("proxy-type-B");
        target.proxy.setHostname("proxy-hostname-B");
        target.proxy.setPort(1234);
        target.proxy.setUsername("proxy-username-B");
        target.proxy.setPassword("proxy-password-B");
        target.retry.setMode(FIXED);
        target.retry.getExponential().setMaxRetries(3);
        target.retry.getExponential().setBaseDelay(Duration.ofSeconds(4));
        target.retry.getExponential().setMaxDelay(Duration.ofSeconds(5));
        target.retry.getFixed().setDelay(Duration.ofSeconds(6));
        target.retry.getFixed().setMaxRetries(7);
        target.credential.setClientId("credential-client-id-B");
        target.credential.setClientSecret("credential-client-secret-B");
        target.credential.setClientCertificatePath("credential-client-cert-path-B");
        target.credential.setClientCertificatePassword("credential-client-cert-password-B");
        target.credential.setUsername("credential-username-B");
        target.credential.setPassword("credential-password-B");
        target.credential.setManagedIdentityEnabled(true);

        // assert properties have been set correctly to target
        assertEquals("client-application-id-B", target.client.getApplicationId());
        assertEquals(AZURE, target.profile.getCloudType());
        assertEquals("profile-tenant-id-B", target.profile.getTenantId());
        assertEquals("profile-sub-id-B", target.profile.getSubscriptionId());
        assertEquals("aad-endpoint-B", target.profile.getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryGraphApiVersion(), target.profile.getEnvironment().getActiveDirectoryGraphApiVersion());
        assertEquals("proxy-type-B", target.proxy.getType());
        assertEquals("proxy-hostname-B", target.proxy.getHostname());
        assertEquals(1234, target.proxy.getPort());
        assertEquals("proxy-username-B", target.proxy.getUsername());
        assertEquals("proxy-password-B", target.proxy.getPassword());
        assertEquals(FIXED, target.retry.getMode());
        assertEquals(3, target.retry.getExponential().getMaxRetries());
        assertEquals(Duration.ofSeconds(4), target.retry.getExponential().getBaseDelay());
        assertEquals(Duration.ofSeconds(5), target.retry.getExponential().getMaxDelay());
        assertEquals(Duration.ofSeconds(6), target.retry.getFixed().getDelay());
        assertEquals(7, target.retry.getFixed().getMaxRetries());
        assertEquals("credential-client-id-B", target.credential.getClientId());
        assertEquals("credential-client-secret-B", target.credential.getClientSecret());
        assertEquals("credential-client-cert-path-B", target.credential.getClientCertificatePath());
        assertEquals("credential-client-cert-password-B", target.credential.getClientCertificatePassword());
        assertEquals("credential-username-B", target.credential.getUsername());
        assertEquals("credential-password-B", target.credential.getPassword());

        AzurePropertiesA source = new AzurePropertiesA();
        source.client.setApplicationId("client-application-id-A");
        source.profile.setCloudType(AZURE_CHINA);
        source.profile.setTenantId("profile-tenant-id-A");
        source.proxy.setHostname("proxy-hostname-A");
        source.retry.getExponential().setMaxRetries(13);
        source.retry.getExponential().setMaxDelay(Duration.ofSeconds(14));
        source.credential.setClientId("credential-client-id-A");

        AzurePropertiesUtils.copyAzureCommonProperties(source, target);

        // assert properties have been set correctly to target after copy
        assertEquals("client-application-id-A", target.client.getApplicationId());
        assertEquals(AZURE_CHINA, target.profile.getCloudType());
        assertEquals("profile-tenant-id-A", target.profile.getTenantId());
        assertNull(target.profile.getSubscriptionId());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(), target.profile.getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryGraphApiVersion(), target.profile.getEnvironment().getActiveDirectoryGraphApiVersion());
        assertNull(target.proxy.getType());
        assertEquals("proxy-hostname-A", target.proxy.getHostname());
        assertNull(target.proxy.getPort());
        assertNull(target.proxy.getUsername());
        assertNull(target.proxy.getPassword());
        assertEquals(EXPONENTIAL, target.retry.getMode());
        assertEquals(13, target.retry.getExponential().getMaxRetries());
        assertNull(target.retry.getExponential().getBaseDelay());
        assertEquals(Duration.ofSeconds(14), target.retry.getExponential().getMaxDelay());
        assertNull(target.retry.getFixed().getDelay());
        assertNull(target.retry.getFixed().getMaxRetries());
        assertEquals("credential-client-id-A", target.credential.getClientId());
        assertNull(target.credential.getClientSecret());
        assertNull(target.credential.getClientCertificatePath());
        assertNull(target.credential.getClientCertificatePassword());
        assertNull(target.credential.getUsername());
        assertNull(target.credential.getPassword());
    }

    @Test
    void testCopyPropertiesIgnoresNullToObjectWithSameFieldsSetShouldOverrideWithoutNull() {
        AzurePropertiesB target = new AzurePropertiesB();
        target.client.setApplicationId("client-application-id-B");
        target.profile.setCloudType(AZURE);
        target.profile.setTenantId("profile-tenant-id-B");
        target.profile.setSubscriptionId("profile-sub-id-B");
        target.profile.getEnvironment().setActiveDirectoryEndpoint("aad-endpoint-B");
        target.proxy.setType("proxy-type-B");
        target.proxy.setHostname("proxy-hostname-B");
        target.proxy.setPort(1234);
        target.proxy.setUsername("proxy-username-B");
        target.proxy.setPassword("proxy-password-B");
        target.retry.setMode(FIXED);
        target.retry.getExponential().setMaxRetries(3);
        target.retry.getExponential().setBaseDelay(Duration.ofSeconds(4));
        target.retry.getExponential().setMaxDelay(Duration.ofSeconds(5));
        target.retry.getFixed().setDelay(Duration.ofSeconds(6));
        target.retry.getFixed().setMaxRetries(7);
        target.credential.setClientId("credential-client-id-B");
        target.credential.setClientSecret("credential-client-secret-B");
        target.credential.setClientCertificatePath("credential-client-cert-path-B");
        target.credential.setClientCertificatePassword("credential-client-cert-password-B");
        target.credential.setUsername("credential-username-B");
        target.credential.setPassword("credential-password-B");
        target.credential.setManagedIdentityEnabled(true);

        // assert properties have been set correctly to target
        assertEquals("client-application-id-B", target.client.getApplicationId());
        assertEquals(AZURE, target.profile.getCloudType());
        assertEquals("profile-tenant-id-B", target.profile.getTenantId());
        assertEquals("profile-sub-id-B", target.profile.getSubscriptionId());
        assertEquals("aad-endpoint-B", target.profile.getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryGraphApiVersion(), target.profile.getEnvironment().getActiveDirectoryGraphApiVersion());
        assertEquals("proxy-type-B", target.proxy.getType());
        assertEquals("proxy-hostname-B", target.proxy.getHostname());
        assertEquals(1234, target.proxy.getPort());
        assertEquals("proxy-username-B", target.proxy.getUsername());
        assertEquals("proxy-password-B", target.proxy.getPassword());
        assertEquals(FIXED, target.retry.getMode());
        assertEquals(3, target.retry.getExponential().getMaxRetries());
        assertEquals(Duration.ofSeconds(4), target.retry.getExponential().getBaseDelay());
        assertEquals(Duration.ofSeconds(5), target.retry.getExponential().getMaxDelay());
        assertEquals(Duration.ofSeconds(6), target.retry.getFixed().getDelay());
        assertEquals(7, target.retry.getFixed().getMaxRetries());
        assertEquals("credential-client-id-B", target.credential.getClientId());
        assertEquals("credential-client-secret-B", target.credential.getClientSecret());
        assertEquals("credential-client-cert-path-B", target.credential.getClientCertificatePath());
        assertEquals("credential-client-cert-password-B", target.credential.getClientCertificatePassword());
        assertEquals("credential-username-B", target.credential.getUsername());
        assertEquals("credential-password-B", target.credential.getPassword());

        AzurePropertiesA source = new AzurePropertiesA();
        source.client.setApplicationId("client-application-id-A");
        source.profile.setCloudType(AZURE_CHINA);
        source.profile.setTenantId("profile-tenant-id-A");
        source.profile.getEnvironment().setActiveDirectoryEndpoint("aad-endpoint-A");
        source.proxy.setHostname("proxy-hostname-A");
        source.retry.getExponential().setMaxRetries(13);
        source.retry.getFixed().setMaxRetries(17);
        source.credential.setClientId("credential-client-id-A");

        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(source, target);

        // assert properties have been set correctly to target after copy
        assertEquals("client-application-id-A", target.client.getApplicationId());
        assertEquals(AZURE_CHINA, target.profile.getCloudType());
        assertEquals("profile-tenant-id-A", target.profile.getTenantId());
        assertEquals("profile-sub-id-B", target.profile.getSubscriptionId());
        assertEquals("aad-endpoint-A", target.profile.getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryGraphApiVersion(), target.profile.getEnvironment().getActiveDirectoryGraphApiVersion());
        assertEquals("proxy-type-B", target.proxy.getType());
        assertEquals("proxy-hostname-A", target.proxy.getHostname());
        assertEquals(1234, target.proxy.getPort());
        assertEquals("proxy-username-B", target.proxy.getUsername());
        assertEquals("proxy-password-B", target.proxy.getPassword());
        assertEquals(EXPONENTIAL, target.retry.getMode());
        assertEquals(13, target.retry.getExponential().getMaxRetries());
        assertEquals(Duration.ofSeconds(4), target.retry.getExponential().getBaseDelay());
        assertEquals(Duration.ofSeconds(5), target.retry.getExponential().getMaxDelay());
        assertEquals(Duration.ofSeconds(6), target.retry.getFixed().getDelay());
        assertEquals(17, target.retry.getFixed().getMaxRetries());
        assertEquals("credential-client-id-A", target.credential.getClientId());
        assertEquals("credential-client-secret-B", target.credential.getClientSecret());
        assertEquals("credential-client-cert-path-B", target.credential.getClientCertificatePath());
        assertEquals("credential-client-cert-password-B", target.credential.getClientCertificatePassword());
        assertEquals("credential-username-B", target.credential.getUsername());
        assertEquals("credential-password-B", target.credential.getPassword());
    }

    @Test
    void testMergePropertiesObjectWithSameFieldsSetShouldTakeLater() {
        AzurePropertiesB defaultProperties = new AzurePropertiesB();
        defaultProperties.client.setApplicationId("client-application-id-B");
        defaultProperties.profile.setCloudType(AZURE);
        defaultProperties.profile.setTenantId("profile-tenant-id-B");
        defaultProperties.profile.setSubscriptionId("profile-sub-id-B");
        defaultProperties.profile.getEnvironment().setActiveDirectoryEndpoint("aad-endpoint-B");
        defaultProperties.proxy.setType("proxy-type-B");
        defaultProperties.proxy.setHostname("proxy-hostname-B");
        defaultProperties.proxy.setPort(1234);
        defaultProperties.proxy.setUsername("proxy-username-B");
        defaultProperties.proxy.setPassword("proxy-password-B");
        defaultProperties.retry.setMode(FIXED);
        defaultProperties.retry.getExponential().setMaxRetries(3);
        defaultProperties.retry.getExponential().setBaseDelay(Duration.ofSeconds(4));
        defaultProperties.retry.getExponential().setMaxDelay(Duration.ofSeconds(5));
        defaultProperties.retry.getFixed().setDelay(Duration.ofSeconds(6));
        defaultProperties.retry.getFixed().setMaxRetries(7);
        defaultProperties.credential.setClientId("credential-client-id-B");
        defaultProperties.credential.setClientSecret("credential-client-secret-B");
        defaultProperties.credential.setClientCertificatePath("credential-client-cert-path-B");
        defaultProperties.credential.setClientCertificatePassword("credential-client-cert-password-B");
        defaultProperties.credential.setUsername("credential-username-B");
        defaultProperties.credential.setPassword("credential-password-B");
        defaultProperties.credential.setManagedIdentityEnabled(true);

        // assert properties have been set correctly to defaultProperties
        assertEquals("client-application-id-B", defaultProperties.client.getApplicationId());
        assertEquals(AZURE, defaultProperties.profile.getCloudType());
        assertEquals("profile-tenant-id-B", defaultProperties.profile.getTenantId());
        assertEquals("profile-sub-id-B", defaultProperties.profile.getSubscriptionId());
        assertEquals("aad-endpoint-B", defaultProperties.profile.getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryGraphApiVersion(), defaultProperties.profile.getEnvironment().getActiveDirectoryGraphApiVersion());
        assertEquals("proxy-type-B", defaultProperties.proxy.getType());
        assertEquals("proxy-hostname-B", defaultProperties.proxy.getHostname());
        assertEquals(1234, defaultProperties.proxy.getPort());
        assertEquals("proxy-username-B", defaultProperties.proxy.getUsername());
        assertEquals("proxy-password-B", defaultProperties.proxy.getPassword());
        assertEquals(FIXED, defaultProperties.retry.getMode());
        assertEquals(3, defaultProperties.retry.getExponential().getMaxRetries());
        assertEquals(Duration.ofSeconds(4), defaultProperties.retry.getExponential().getBaseDelay());
        assertEquals(Duration.ofSeconds(5), defaultProperties.retry.getExponential().getMaxDelay());
        assertEquals(Duration.ofSeconds(6), defaultProperties.retry.getFixed().getDelay());
        assertEquals(7, defaultProperties.retry.getFixed().getMaxRetries());
        assertEquals("credential-client-id-B", defaultProperties.credential.getClientId());
        assertEquals("credential-client-secret-B", defaultProperties.credential.getClientSecret());
        assertEquals("credential-client-cert-path-B", defaultProperties.credential.getClientCertificatePath());
        assertEquals("credential-client-cert-password-B", defaultProperties.credential.getClientCertificatePassword());
        assertEquals("credential-username-B", defaultProperties.credential.getUsername());
        assertEquals("credential-password-B", defaultProperties.credential.getPassword());

        AzurePropertiesA propertiesToOverride = new AzurePropertiesA();
        propertiesToOverride.client.setApplicationId("client-application-id-A");
        propertiesToOverride.profile.setCloudType(AZURE_CHINA);
        propertiesToOverride.profile.setTenantId("profile-tenant-id-A");
        propertiesToOverride.profile.getEnvironment().setActiveDirectoryEndpoint("aad-endpoint-A");
        propertiesToOverride.proxy.setHostname("proxy-hostname-A");
        propertiesToOverride.retry.getExponential().setMaxRetries(13);
        propertiesToOverride.retry.getFixed().setMaxRetries(17);
        propertiesToOverride.credential.setClientId("credential-client-id-A");


        AzurePropertiesB result = new AzurePropertiesB();
        AzurePropertiesUtils.mergeAzureCommonProperties(defaultProperties, propertiesToOverride, result);

        // assert properties have been set correctly to result after copy
        assertEquals("client-application-id-A", result.client.getApplicationId());
        assertEquals(AZURE_CHINA, result.profile.getCloudType());
        assertEquals("profile-tenant-id-A", result.profile.getTenantId());
        assertEquals("profile-sub-id-B", result.profile.getSubscriptionId());
        assertEquals("aad-endpoint-A", result.profile.getEnvironment().getActiveDirectoryEndpoint());
        assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryGraphApiVersion(), result.profile.getEnvironment().getActiveDirectoryGraphApiVersion());
        assertEquals("proxy-type-B", result.proxy.getType());
        assertEquals("proxy-hostname-A", result.proxy.getHostname());
        assertEquals(1234, result.proxy.getPort());
        assertEquals("proxy-username-B", result.proxy.getUsername());
        assertEquals("proxy-password-B", result.proxy.getPassword());
        assertEquals(EXPONENTIAL, result.retry.getMode());
        assertEquals(13, result.retry.getExponential().getMaxRetries());
        assertEquals(Duration.ofSeconds(4), result.retry.getExponential().getBaseDelay());
        assertEquals(Duration.ofSeconds(5), result.retry.getExponential().getMaxDelay());
        assertEquals(Duration.ofSeconds(6), result.retry.getFixed().getDelay());
        assertEquals(17, result.retry.getFixed().getMaxRetries());
        assertEquals("credential-client-id-A", result.credential.getClientId());
        assertEquals("credential-client-secret-B", result.credential.getClientSecret());
        assertEquals("credential-client-cert-path-B", result.credential.getClientCertificatePath());
        assertEquals("credential-client-cert-password-B", result.credential.getClientCertificatePassword());
        assertEquals("credential-username-B", result.credential.getUsername());
        assertEquals("credential-password-B", result.credential.getPassword());
    }

    @Test
    void testCopyPropertiesIgnoreNullToObjectWithDifferentFieldsSetShouldMerge() {
        AzurePropertiesA source = new AzurePropertiesA();
        source.credential.setClientId("client-id-A");

        AzurePropertiesB target = new AzurePropertiesB();
        target.credential.setClientSecret("client-secret-B");
        target.retry.getExponential().setMaxDelay(Duration.ofSeconds(2));
        target.profile.setCloudType(OTHER);
        target.profile.getEnvironment().setActiveDirectoryEndpoint("abc");

        assertEquals(AZURE, source.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            source.profile.getEnvironment().getActiveDirectoryEndpoint());
        assertEquals("client-secret-B", target.credential.getClientSecret());
        assertEquals(Duration.ofSeconds(2), target.retry.getExponential().getMaxDelay());

        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(source, target);

        // target properties should be merged properties from source + target
        assertEquals("client-id-A", target.credential.getClientId());
        assertEquals("client-secret-B", target.credential.getClientSecret());
        assertEquals(Duration.ofSeconds(2), target.retry.getExponential().getMaxDelay());
        assertEquals(AZURE, source.getProfile().getCloudType());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            target.profile.getEnvironment().getActiveDirectoryEndpoint());


        // source properties should not be updated
        assertNull(source.credential.getClientSecret());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            source.profile.getEnvironment().getActiveDirectoryEndpoint());
    }

    @Test
    void testCopyPropertiesSourceNotChanged() {
        AzurePropertiesA source = new AzurePropertiesA();
        source.credential.setClientId("client-id-A");

        AzurePropertiesB target = new AzurePropertiesB();

        AzurePropertiesUtils.copyAzureCommonProperties(source, target);

        assertEquals("client-id-A", target.credential.getClientId());

        // Update target will not affect source
        target.retry.getExponential().setBaseDelay(Duration.ofSeconds(2));
        target.profile.setCloudType(OTHER);
        target.profile.getEnvironment().setActiveDirectoryEndpoint("abc");

        assertNull(source.retry.getExponential().getBaseDelay());
        assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            source.profile.getEnvironment().getActiveDirectoryEndpoint());
    }

    @Test
    void testCopyPropertiesHttpClientPropertiesShouldBeCopied() {
        HeaderProperties headerProperties = new HeaderProperties();
        headerProperties.setName("header-1");
        headerProperties.setValues(Arrays.asList("value-1", "value-2"));
        AzureHttpClientProperties source = new AzureHttpClientProperties();
        source.getClient().setWriteTimeout(Duration.ofSeconds(3));
        source.getClient().setResponseTimeout(Duration.ofSeconds(4));
        source.getClient().setReadTimeout(Duration.ofSeconds(4));
        source.getClient().setConnectTimeout(Duration.ofSeconds(2));
        source.getClient().setMaximumConnectionPoolSize(5);
        source.getClient().setConnectionIdleTimeout(Duration.ofSeconds(5));
        source.getClient().setApplicationId("global-application-id");
        source.getClient().getLogging().setLevel(HttpLogDetailLevel.BODY_AND_HEADERS);
        source.getClient().getLogging().getAllowedHeaderNames().add("header-name1");
        source.getClient().getHeaders().add(headerProperties);
        AzureHttpClientProperties target = new AzureHttpClientProperties();
        target.getClient().setConnectTimeout(Duration.ofSeconds(3));
        target.getClient().setMaximumConnectionPoolSize(5);
        target.getClient().setConnectionIdleTimeout(Duration.ofSeconds(5));
        target.getClient().setApplicationId("target-global-application-id");
        target.getClient().getLogging().setLevel(HttpLogDetailLevel.HEADERS);
        target.getClient().getLogging().getAllowedHeaderNames().add("header-name2");
        HeaderProperties targetHeaderProperties = new HeaderProperties();
        targetHeaderProperties.setName("header-2");
        targetHeaderProperties.setValues(Arrays.asList("value-1", "value-2"));
        target.getClient().getHeaders().add(targetHeaderProperties);
        AzurePropertiesUtils.copyAzureCommonProperties(source, target);
        assertEquals(Duration.ofSeconds(3), target.getClient().getWriteTimeout());
        assertEquals(Duration.ofSeconds(4), target.getClient().getResponseTimeout());
        assertEquals(Duration.ofSeconds(4), target.getClient().getReadTimeout());
        assertEquals(Duration.ofSeconds(2), target.getClient().getConnectTimeout());
        assertEquals(Duration.ofSeconds(5), target.getClient().getConnectionIdleTimeout());
        assertEquals(5, target.getClient().getMaximumConnectionPoolSize());
        assertEquals("global-application-id", target.getClient().getApplicationId());
        assertEquals(HttpLogDetailLevel.BODY_AND_HEADERS, target.getClient().getLogging().getLevel());
        Set<String> allowedHeaderNames = new HashSet<>();
        allowedHeaderNames.add("header-name1");
        allowedHeaderNames.add("header-name2");
        assertEquals(allowedHeaderNames, target.getClient().getLogging().getAllowedHeaderNames());
        assertEquals(2, target.getClient().getHeaders().size());
    }

    @Test
    void testCopyPropertiesIgnoreNullHttpClientPropertiesShouldBeCopied() {
        AzureHttpClientProperties source = new AzureHttpClientProperties();
        source.getClient().setReadTimeout(Duration.ofSeconds(4));
        source.getClient().setConnectTimeout(Duration.ofSeconds(2));
        source.getClient().setMaximumConnectionPoolSize(5);
        source.getClient().getLogging().getAllowedHeaderNames().add("header-name1");
        AzureHttpClientProperties target = new AzureHttpClientProperties();
        target.getClient().setWriteTimeout(Duration.ofSeconds(4));
        target.getClient().setResponseTimeout(Duration.ofSeconds(5));
        target.getClient().setReadTimeout(Duration.ofSeconds(5));
        target.getClient().setConnectTimeout(Duration.ofSeconds(3));
        target.getClient().setMaximumConnectionPoolSize(5);
        target.getClient().setConnectionIdleTimeout(Duration.ofSeconds(5));
        target.getClient().setApplicationId("target-global-application-id");
        target.getClient().getLogging().setLevel(HttpLogDetailLevel.HEADERS);
        target.getClient().getLogging().getAllowedHeaderNames().add("header-name2");
        HeaderProperties headerProperties = new HeaderProperties();
        headerProperties.setName("header-2");
        headerProperties.setValues(Arrays.asList("value-1", "value-2"));
        target.getClient().getHeaders().add(headerProperties);
        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(source, target);
        assertEquals(Duration.ofSeconds(4), target.getClient().getWriteTimeout());
        assertEquals(Duration.ofSeconds(5), target.getClient().getResponseTimeout());
        assertEquals(Duration.ofSeconds(4), target.getClient().getReadTimeout());
        assertEquals(Duration.ofSeconds(2), target.getClient().getConnectTimeout());
        assertEquals(Duration.ofSeconds(5), target.getClient().getConnectionIdleTimeout());
        assertEquals(5, target.getClient().getMaximumConnectionPoolSize());
        assertEquals("target-global-application-id", target.getClient().getApplicationId());
        assertEquals(HttpLogDetailLevel.HEADERS, target.getClient().getLogging().getLevel());
        Set<String> allowedHeaderNames = new HashSet<>();
        allowedHeaderNames.add("header-name1");
        allowedHeaderNames.add("header-name2");
        assertEquals(allowedHeaderNames, target.getClient().getLogging().getAllowedHeaderNames());
        assertEquals(1, target.getClient().getHeaders().size());
    }

    static class AzureHttpClientProperties extends AzureHttpSdkProperties {

    }

    @Test
    void testCopyPropertiesAmqpClientPropertiesShouldBeCopied() {
        AzureAmqpClientProperties source = new AzureAmqpClientProperties();
        source.getClient().setApplicationId("global-application-id");
        source.getClient().setTransportType(AmqpTransportType.AMQP);
        AzureAmqpClientProperties target = new AzureAmqpClientProperties();
        target.getClient().setApplicationId("target-global-application-id");
        target.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);
        AzurePropertiesUtils.copyAzureCommonProperties(source, target);
        assertEquals("global-application-id", target.getClient().getApplicationId());
        assertEquals(AmqpTransportType.AMQP, target.getClient().getTransportType());
    }

    @Test
    void testCopyPropertiesIgnoreNullAmqpClientPropertiesShouldBeCopied() {
        AzureAmqpClientProperties source = new AzureAmqpClientProperties();
        source.getClient().setTransportType(AmqpTransportType.AMQP);
        AzureAmqpClientProperties target = new AzureAmqpClientProperties();
        target.getClient().setApplicationId("target-global-application-id");
        target.getClient().setTransportType(AmqpTransportType.AMQP_WEB_SOCKETS);
        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(source, target);
        assertEquals("target-global-application-id", target.getClient().getApplicationId());
        assertEquals(AmqpTransportType.AMQP, target.getClient().getTransportType());
    }


    static class AzureAmqpClientProperties extends AzureAmqpSdkProperties {

    }

    static class AzurePropertiesA implements AzureProperties, RetryOptionsProvider {

        private final ClientProperties client = new ClientProperties();
        private final ProxyProperties proxy = new ProxyProperties();
        private final RetryProperties retry = new RetryProperties();
        private final TokenCredentialProperties credential = new TokenCredentialProperties();
        private final AzureProfileProperties profile = new AzureProfileProperties();

        @Override
        public ClientProperties getClient() {
            return client;
        }

        @Override
        public ProxyProperties getProxy() {
            return proxy;
        }

        @Override
        public RetryProperties getRetry() {
            return retry;
        }

        @Override
        public TokenCredentialProperties getCredential() {
            return credential;
        }

        @Override
        public AzureProfileProperties getProfile() {
            return profile;
        }
    }

    static class AzurePropertiesB implements AzureProperties, RetryOptionsProvider {

        private final ClientProperties client = new ClientProperties();
        private final ProxyProperties proxy = new ProxyProperties();
        private final RetryProperties retry = new RetryProperties();
        private final TokenCredentialProperties credential = new TokenCredentialProperties();
        private final AzureProfileProperties profile = new AzureProfileProperties();

        @Override
        public ClientProperties getClient() {
            return client;
        }

        @Override
        public ProxyProperties getProxy() {
            return proxy;
        }

        @Override
        public RetryProperties getRetry() {
            return retry;
        }

        @Override
        public TokenCredentialProperties getCredential() {
            return credential;
        }

        @Override
        public AzureProfileProperties getProfile() {
            return profile;
        }

    }

}
