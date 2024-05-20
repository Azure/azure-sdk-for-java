// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

public class IdentityClientOptionsTest {

    @Test
    public void testDefaultAuthorityHost() {
        IdentityClientOptions identityClientOptions = new IdentityClientOptions();

        Configuration configuration = Configuration.getGlobalConfiguration();

        String expected = configuration.get(Configuration.PROPERTY_AZURE_AUTHORITY_HOST,
            AzureAuthorityHosts.AZURE_PUBLIC_CLOUD);

        Assertions.assertEquals(expected, identityClientOptions.getAuthorityHost());
    }

    @Test
    public void testEnvAuthorityHost() {
        String envAuthorityHost = "https://envauthority.com/";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("AZURE_AUTHORITY_HOST", envAuthorityHost));

        IdentityClientOptions identityClientOptions = new IdentityClientOptions().setConfiguration(configuration);
        Assertions.assertEquals(envAuthorityHost, identityClientOptions.getAuthorityHost());
    }

    @Test
    public void testCustomAuthorityHost() {
        String authorityHost = "https://custom.com/";
        IdentityClientOptions identityClientOptions = new IdentityClientOptions();
        identityClientOptions.setAuthorityHost(authorityHost);
        Assertions.assertEquals(authorityHost, identityClientOptions.getAuthorityHost());
    }

    @Test
    public void testDisableAuthorityValidationAndInstanceDiscovery() {
        IdentityClientOptions identityClientOptions = new IdentityClientOptions();
        identityClientOptions.disableInstanceDiscovery();
        Assertions.assertFalse(identityClientOptions.isInstanceDiscoveryEnabled());
    }

    @Test
    public void testCloneDoesNotEnableThingsItShouldNot() {
        IdentityClientOptions identityClientOptions = new IdentityClientOptions();

        IdentityClientOptions clonedOptions = identityClientOptions.clone();
        Assertions.assertFalse(clonedOptions.isBrokerEnabled());
        Assertions.assertFalse(clonedOptions.isUnsafeSupportLoggingEnabled());
    }

    @Test
    public void testIMDSRetry() {
        IdentityClientOptions identityClientOptions = new IdentityClientOptions();
        IdentityClient idClient = new IdentityClientBuilder().identityClientOptions(identityClientOptions).build();
        int retry = 1;
        Queue<Integer> expectedEntries = new LinkedList<>();
        expectedEntries.addAll(Arrays.asList(800, 1600, 3200, 6400, 12800));


        while (retry < identityClientOptions.getMaxRetry()) {
            int timeout = idClient.getRetryTimeoutInMs(retry);
            if (expectedEntries.contains(timeout)) {
                expectedEntries.remove(timeout);
            } else {
                Assertions.fail("Unexpected timeout: " + timeout);
            }
            retry++;
        }
    }
}
