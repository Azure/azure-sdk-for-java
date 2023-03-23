// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

public class IdentityClientOptionsTest {

    @Test
    public void testDefaultAuthorityHost() {
        IdentityClientOptions identityClientOptions = new IdentityClientOptions();
        Assert.assertEquals(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD, identityClientOptions.getAuthorityHost());
    }

    @Test
    public void testEnvAuthorityHost() {
        String envAuthorityHost = "https://envauthority.com/";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("AZURE_AUTHORITY_HOST", envAuthorityHost));

        IdentityClientOptions identityClientOptions = new IdentityClientOptions().setConfiguration(configuration);
        Assert.assertEquals(envAuthorityHost, identityClientOptions.getAuthorityHost());
    }

    @Test
    public void testCustomAuthorityHost() {
        String authorityHost = "https://custom.com/";
        IdentityClientOptions identityClientOptions = new IdentityClientOptions();
        identityClientOptions.setAuthorityHost(authorityHost);
        Assert.assertEquals(authorityHost, identityClientOptions.getAuthorityHost());
    }

    @Test
    public void testDisableInstanceDiscovery() {
        IdentityClientOptions identityClientOptions = new IdentityClientOptions();
        identityClientOptions.disableInstanceDisovery();
        Assert.assertFalse(identityClientOptions.getInstanceDiscovery());
    }
}
