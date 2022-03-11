// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.util;

import com.azure.core.management.AzureEnvironment;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.core.properties.AzureProperties;
import com.azure.spring.cloud.core.properties.authentication.TokenCredentialProperties;
import com.azure.spring.cloud.core.properties.client.ClientProperties;
import com.azure.spring.cloud.core.properties.profile.AzureProfileProperties;
import com.azure.spring.cloud.core.properties.proxy.ProxyProperties;
import com.azure.spring.cloud.core.properties.retry.RetryProperties;
import com.azure.spring.cloud.core.provider.RetryOptionsProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.AZURE_CHINA;
import static com.azure.spring.cloud.core.provider.AzureProfileOptionsProvider.CloudType.OTHER;


/**
 *
 */
class AzurePropertiesUtilsTests {

    @Test
    void testCopyPropertiesToNewObjectShouldEqual() {
        AzurePropertiesA source = new AzurePropertiesA();
        source.client.setApplicationId("application-id-A");
        source.profile.setCloudType(AZURE_CHINA);
        source.profile.setTenantId("tenant-id-A");
        source.proxy.setHostname("hostname-A");
        source.retry.getExponential().setMaxRetries(3);
        source.retry.getExponential().setMaxDelay(Duration.ofSeconds(4));
        source.credential.setClientId("client-id-A");

        final AzurePropertiesB target = new AzurePropertiesB();
        AzurePropertiesUtils.copyAzureCommonProperties(source, target);

        Assertions.assertEquals("application-id-A", target.client.getApplicationId());
        Assertions.assertEquals(AZURE_CHINA, target.profile.getCloudType());
        Assertions.assertEquals("tenant-id-A", target.profile.getTenantId());
        Assertions.assertEquals("hostname-A", target.proxy.getHostname());
        Assertions.assertEquals(3, target.retry.getExponential().getMaxRetries());
        Assertions.assertEquals(Duration.ofSeconds(4), target.retry.getExponential().getMaxDelay());
        Assertions.assertEquals("client-id-A", target.credential.getClientId());
        Assertions.assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            target.profile.getEnvironment().getActiveDirectoryEndpoint());
    }

    @Test
    void testCopyPropertiesToObjectWithSameFieldsSetShouldOverride() {
        AzurePropertiesA source = new AzurePropertiesA();
        source.client.setApplicationId("application-id-A");
        source.profile.setCloudType(AZURE_CHINA);
        source.profile.setTenantId("tenant-id-A");
        source.proxy.setHostname("hostname-A");
        source.retry.getExponential().setMaxRetries(3);
        source.retry.getExponential().setMaxDelay(Duration.ofSeconds(4));
        source.credential.setClientId("client-id-A");

        AzurePropertiesB target = new AzurePropertiesB();
        target.client.setApplicationId("application-id-B");
        target.profile.setCloudType(AZURE);
        target.profile.setTenantId("tenant-id-B");
        target.proxy.setHostname("hostname-B");
        target.retry.getExponential().setMaxRetries(13);
        target.retry.getExponential().setMaxDelay(Duration.ofSeconds(14));
        target.credential.setClientId("client-id-B");

        Assertions.assertEquals("application-id-B", target.client.getApplicationId());
        Assertions.assertEquals(AZURE, target.profile.getCloudType());
        Assertions.assertEquals("tenant-id-B", target.profile.getTenantId());
        Assertions.assertEquals("hostname-B", target.proxy.getHostname());
        Assertions.assertEquals(13, target.retry.getExponential().getMaxRetries());
        Assertions.assertEquals(Duration.ofSeconds(14), target.retry.getExponential().getMaxDelay());
        Assertions.assertEquals("client-id-B", target.credential.getClientId());
        Assertions.assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            target.profile.getEnvironment().getActiveDirectoryEndpoint());

        AzurePropertiesUtils.copyAzureCommonProperties(source, target);

        Assertions.assertEquals("application-id-A", target.client.getApplicationId());
        Assertions.assertEquals(AZURE_CHINA, target.profile.getCloudType());
        Assertions.assertEquals("tenant-id-A", target.profile.getTenantId());
        Assertions.assertEquals("hostname-A", target.proxy.getHostname());
        Assertions.assertEquals(3, target.retry.getExponential().getMaxRetries());
        Assertions.assertEquals(Duration.ofSeconds(4), target.retry.getExponential().getMaxDelay());
        Assertions.assertEquals("client-id-A", target.credential.getClientId());
        Assertions.assertEquals(AzureEnvironment.AZURE_CHINA.getActiveDirectoryEndpoint(),
            target.profile.getEnvironment().getActiveDirectoryEndpoint());
    }

    @Test
    void testCopyPropertiesToObjectWithDifferentFieldsSetShouldOverrideWithNull() {
        AzurePropertiesA source = new AzurePropertiesA();
        source.credential.setClientId("client-id-A");

        AzurePropertiesB target = new AzurePropertiesB();
        target.credential.setClientSecret("client-secret-B");

        AzurePropertiesUtils.copyAzureCommonProperties(source, target);

        // target properties should be the same as source
        Assertions.assertEquals("client-id-A", target.credential.getClientId());
        Assertions.assertNull(target.credential.getClientSecret());
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

        Assertions.assertEquals(AZURE, source.getProfile().getCloudType());
        Assertions.assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            source.profile.getEnvironment().getActiveDirectoryEndpoint());
        Assertions.assertEquals("client-secret-B", target.credential.getClientSecret());
        Assertions.assertEquals(Duration.ofSeconds(2), target.retry.getExponential().getMaxDelay());

        AzurePropertiesUtils.copyAzureCommonPropertiesIgnoreNull(source, target);

        // target properties should be merged properties from source + target
        Assertions.assertEquals("client-id-A", target.credential.getClientId());
        Assertions.assertEquals("client-secret-B", target.credential.getClientSecret());
        Assertions.assertEquals(Duration.ofSeconds(2), target.retry.getExponential().getMaxDelay());
        Assertions.assertEquals(AZURE, source.getProfile().getCloudType());
        Assertions.assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            target.profile.getEnvironment().getActiveDirectoryEndpoint());


        // source properties should not be updated
        Assertions.assertNull(source.credential.getClientSecret());
        Assertions.assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            source.profile.getEnvironment().getActiveDirectoryEndpoint());
    }

    @Test
    void testCopyPropertiesSourceNotChanged() {
        AzurePropertiesA source = new AzurePropertiesA();
        source.credential.setClientId("client-id-A");

        AzurePropertiesB target = new AzurePropertiesB();

        AzurePropertiesUtils.copyAzureCommonProperties(source, target);

        Assertions.assertEquals("client-id-A", target.credential.getClientId());

        // Update target will not affect source
        target.retry.getExponential().setBaseDelay(Duration.ofSeconds(2));
        target.profile.setCloudType(OTHER);
        target.profile.getEnvironment().setActiveDirectoryEndpoint("abc");

        Assertions.assertNull(source.retry.getExponential().getBaseDelay());
        Assertions.assertEquals(AzureEnvironment.AZURE.getActiveDirectoryEndpoint(),
            source.profile.getEnvironment().getActiveDirectoryEndpoint());
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
