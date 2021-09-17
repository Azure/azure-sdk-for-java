// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.properties;

import com.azure.spring.core.properties.AzureProperties;
import com.azure.spring.core.properties.AzurePropertiesUtils;
import com.azure.spring.core.properties.client.ClientProperties;
import com.azure.spring.core.properties.credential.TokenCredentialProperties;
import com.azure.spring.core.properties.profile.AzureProfile;
import com.azure.spring.core.properties.proxy.ProxyProperties;
import com.azure.spring.core.properties.retry.RetryProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.azure.spring.core.properties.profile.AzureProfile.AzureEnvironment.AZURE;
import static com.azure.spring.core.properties.profile.AzureProfile.AzureEnvironment.AZURE_CHINA;

/**
 *
 */
class AzurePropertiesUtilsTest {

    @Test
    void testCopyPropertiesToNewObjectShouldEqual() {
        AzurePropertiesA source = new AzurePropertiesA();
        source.client.setApplicationId("application-id-A");
        source.profile.setCloud("AZURE_CHINA");
        source.profile.setTenantId("tenant-id-A");
        source.proxy.setHostname("hostname-A");
        source.retry.getBackoff().setDelay(Duration.ofSeconds(2));
        source.credential.setClientId("client-id-A");

        final AzurePropertiesB target = new AzurePropertiesB();
        AzurePropertiesUtils.copyAzureProperties(source, target);

        Assertions.assertEquals("application-id-A", target.client.getApplicationId());
        Assertions.assertEquals("AZURE_CHINA", target.profile.getCloud());
        Assertions.assertEquals("tenant-id-A", target.profile.getTenantId());
        Assertions.assertEquals("hostname-A", target.proxy.getHostname());
        Assertions.assertEquals(Duration.ofSeconds(2), target.retry.getBackoff().getDelay());
        Assertions.assertEquals("client-id-A", target.credential.getClientId());
        Assertions.assertEquals(AZURE_CHINA.getActiveDirectoryEndpoint(), target.profile.getEnvironment().getActiveDirectoryEndpoint());
    }

    @Test
    void testCopyPropertiesToObjectWithSameFieldsSetShouldOverride() {
        AzurePropertiesA source = new AzurePropertiesA();
        source.client.setApplicationId("application-id-A");
        source.profile.setCloud("AZURE_CHINA");
        source.profile.setTenantId("tenant-id-A");
        source.proxy.setHostname("hostname-A");
        source.retry.getBackoff().setDelay(Duration.ofSeconds(2));
        source.credential.setClientId("client-id-A");

        AzurePropertiesB target = new AzurePropertiesB();
        target.client.setApplicationId("application-id-B");
        target.profile.setCloud("AZURE");
        target.profile.setTenantId("tenant-id-B");
        target.proxy.setHostname("hostname-B");
        target.retry.getBackoff().setDelay(Duration.ofSeconds(4));
        target.credential.setClientId("client-id-B");

        Assertions.assertEquals("application-id-B", target.client.getApplicationId());
        Assertions.assertEquals("AZURE", target.profile.getCloud());
        Assertions.assertEquals("tenant-id-B", target.profile.getTenantId());
        Assertions.assertEquals("hostname-B", target.proxy.getHostname());
        Assertions.assertEquals(Duration.ofSeconds(4), target.retry.getBackoff().getDelay());
        Assertions.assertEquals("client-id-B", target.credential.getClientId());
        Assertions.assertEquals(AZURE.getActiveDirectoryEndpoint(), target.profile.getEnvironment().getActiveDirectoryEndpoint());

        AzurePropertiesUtils.copyAzureProperties(source, target);

        Assertions.assertEquals("application-id-A", target.client.getApplicationId());
        Assertions.assertEquals("AZURE_CHINA", target.profile.getCloud());
        Assertions.assertEquals("tenant-id-A", target.profile.getTenantId());
        Assertions.assertEquals("hostname-A", target.proxy.getHostname());
        Assertions.assertEquals(Duration.ofSeconds(2), target.retry.getBackoff().getDelay());
        Assertions.assertEquals("client-id-A", target.credential.getClientId());
        Assertions.assertEquals(AZURE_CHINA.getActiveDirectoryEndpoint(), target.profile.getEnvironment().getActiveDirectoryEndpoint());


    }

    @Test
    void testCopyPropertiesToObjectWithDifferentFieldsSetShouldOverrideWithNull() {
        AzurePropertiesA source = new AzurePropertiesA();
        source.credential.setClientId("client-id-A");

        AzurePropertiesB target = new AzurePropertiesB();
        target.credential.setClientSecret("client-secret-B");

        AzurePropertiesUtils.copyAzureProperties(source, target);

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
        target.retry.getBackoff().setMaxDelay(Duration.ofSeconds(2));
        target.profile.getEnvironment().setActiveDirectoryEndpoint("abc");

        Assertions.assertEquals(AZURE.getActiveDirectoryEndpoint(), source.profile.getEnvironment().getActiveDirectoryEndpoint());
        Assertions.assertEquals("client-secret-B", target.credential.getClientSecret());
        Assertions.assertEquals(Duration.ofSeconds(2), target.retry.getBackoff().getMaxDelay());

        AzurePropertiesUtils.copyAzurePropertiesIgnoreNull(source, target);

        // target properties should be merged properties from source + target
        Assertions.assertEquals("client-id-A", target.credential.getClientId());
        Assertions.assertEquals("client-secret-B", target.credential.getClientSecret());
        Assertions.assertEquals(Duration.ofSeconds(2), target.retry.getBackoff().getMaxDelay());
        Assertions.assertEquals("abc", target.profile.getEnvironment().getActiveDirectoryEndpoint());


        // source properties should not be updated
        Assertions.assertNull(source.credential.getClientSecret());
        Assertions.assertEquals(AZURE.getActiveDirectoryEndpoint(), source.profile.getEnvironment().getActiveDirectoryEndpoint());
    }

    @Test
    void testCopyPropertiesSourceNotChanged() {
        AzurePropertiesA source = new AzurePropertiesA();
        source.credential.setClientId("client-id-A");

        AzurePropertiesB target = new AzurePropertiesB();

        AzurePropertiesUtils.copyAzureProperties(source, target);

        Assertions.assertEquals("client-id-A", target.credential.getClientId());

        // Update target will not affect source
        target.retry.getBackoff().setDelay(Duration.ofSeconds(2));
        target.profile.getEnvironment().setActiveDirectoryEndpoint("abc");

        Assertions.assertNull(source.retry.getBackoff().getDelay());
        Assertions.assertEquals(AZURE.getActiveDirectoryEndpoint(), source.profile.getEnvironment().getActiveDirectoryEndpoint());
    }


    static class AzurePropertiesA implements AzureProperties {

        private final ClientProperties client = new ClientProperties();
        private final ProxyProperties proxy = new ProxyProperties();
        private final RetryProperties retry = new RetryProperties();
        private final TokenCredentialProperties credential = new TokenCredentialProperties();
        private final AzureProfile profile = new AzureProfile();

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
        public AzureProfile getProfile() {
            return profile;
        }
    }

    static class AzurePropertiesB implements AzureProperties {

        private final ClientProperties client = new ClientProperties();
        private final ProxyProperties proxy = new ProxyProperties();
        private final RetryProperties retry = new RetryProperties();
        private final TokenCredentialProperties credential = new TokenCredentialProperties();
        private final AzureProfile profile = new AzureProfile();

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
        public AzureProfile getProfile() {
            return profile;
        }

    }

}
