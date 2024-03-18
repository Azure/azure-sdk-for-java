// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.util.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}
