// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.identity.AzureAuthorityHosts;
import org.junit.Assert;
import org.junit.Test;

import com.azure.core.util.Configuration;

public class IdentityClientOptionsTest {

    @Test
    public void testDefaultAuthorityHost() {
        IdentityClientOptions identityClientOptions = new IdentityClientOptions();
        Assert.assertEquals(AzureAuthorityHosts.AZURE_PUBLIC_CLOUD, identityClientOptions.getAuthorityHost());
    }

    @Test
    public void testEnvAuthorityHost() {
        Configuration configuration = Configuration.getGlobalConfiguration();

        try {
            String envAuthorityHost = "https://envauthority.com/";
            configuration.put("AZURE_AUTHORITY_HOST", envAuthorityHost);
            IdentityClientOptions identityClientOptions = new IdentityClientOptions();
            Assert.assertEquals(envAuthorityHost, identityClientOptions.getAuthorityHost());
        } finally {
            configuration.remove("AZURE_AUTHORITY_HOST");
        }
    }

    @Test
    public void testCustomAuthorityHost() {
        String authorityHost = "https://custom.com/";
        IdentityClientOptions identityClientOptions = new IdentityClientOptions();
        identityClientOptions.setAuthorityHost(authorityHost);
        Assert.assertEquals(authorityHost, identityClientOptions.getAuthorityHost());
    }
}
