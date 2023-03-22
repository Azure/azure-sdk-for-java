// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.util;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.util.IdentityUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class IdentityUtilTests {

    @Test
    public void testMultiTenantAuthenticationEnabled() {
        String currentTenant = "tenant";
        String newTenant = "tenant-new";
        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);
        IdentityClientOptions options = new IdentityClientOptions()
            .setAdditionallyAllowedTenants(Arrays.asList(newTenant));

        Assert.assertEquals(newTenant, IdentityUtil.resolveTenantId(currentTenant, trc, options));
    }

    @Test(expected = ClientAuthenticationException.class)
    public void testMultiTenantAuthenticationDisabled() {
        String currentTenant = "tenant";
        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId("newTenant");
        IdentityClientOptions options = new IdentityClientOptions();
        options.disableMultiTenantAuthentication();

        IdentityUtil.resolveTenantId(currentTenant, trc, options);
    }

    @Test
    public void testAdditionallyAllowedTenants() {
        String currentTenant = "tenant";
        String newTenant = "newTenant";
        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);
        IdentityClientOptions options = new IdentityClientOptions();
        options.setAdditionallyAllowedTenants(Arrays.asList(IdentityUtil.ALL_TENANTS));

        String resolvedTenant = IdentityUtil.resolveTenantId(currentTenant, trc, options);
        Assert.assertEquals(newTenant, resolvedTenant);
    }

    @Test
    public void testAdditionallyAllowedTenantsCaseInsensitive() {
        String currentTenant = "tenant";
        String newTenant = "newTenant";
        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);
        IdentityClientOptions options = new IdentityClientOptions();
        options.setAdditionallyAllowedTenants(Arrays.asList("newtenant"));

        String resolvedTenant = IdentityUtil.resolveTenantId(currentTenant, trc, options);
        Assert.assertEquals(newTenant, resolvedTenant);
    }

    @Test(expected = ClientAuthenticationException.class)
    public void testAlienTenantWithAdditionallyAllowedTenants() {
        String currentTenant = "tenant";
        String newTenant = "newTenant";
        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);
        IdentityClientOptions options = new IdentityClientOptions();
        options.setAdditionallyAllowedTenants(Arrays.asList("tenant"));

        IdentityUtil.resolveTenantId(currentTenant, trc, options);
    }

    @Test(expected = ClientAuthenticationException.class)
    public void testAlienTenantWithAdditionallyAllowedNotConfigured() {
        String currentTenant = "tenant";
        String newTenant = "newTenant";
        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);
        IdentityClientOptions options = new IdentityClientOptions();

        IdentityUtil.resolveTenantId(currentTenant, trc, options);
    }

    @Test
    public void testTenantWithAdditionalTenantsFromEnv() {
        String currentTenant = "tenant";
        String newTenant = "newTenant";
        String allowedTenants = "newTenant;oldTenant";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(IdentityUtil.AZURE_ADDITIONALLY_ALLOWED_TENANTS, allowedTenants));

        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);

        IdentityClientOptions options = new IdentityClientOptions()
            .setAdditionallyAllowedTenants(IdentityUtil.getAdditionalTenantsFromEnvironment(configuration));

        String resolvedTenant = IdentityUtil.resolveTenantId(currentTenant, trc, options);
        Assert.assertEquals(newTenant, resolvedTenant);

    }

    @Test
    public void testTenantWithWildCardAdditionalTenantsFromEnv() {
        String currentTenant = "tenant";
        String newTenant = "newTenant";
        String allowedTenants = "*;randomTenant";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(IdentityUtil.AZURE_ADDITIONALLY_ALLOWED_TENANTS, allowedTenants));

        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);

        IdentityClientOptions options = new IdentityClientOptions()
            .setAdditionallyAllowedTenants(IdentityUtil.getAdditionalTenantsFromEnvironment(configuration));

        String resolvedTenant = IdentityUtil.resolveTenantId(currentTenant, trc, options);
        Assert.assertEquals(newTenant, resolvedTenant);

    }

    @Test(expected = ClientAuthenticationException.class)
    public void testAlienTenantWithAdditionalTenantsFromEnv() {
        String currentTenant = "tenant";
        String newTenant = "newTenant";
        String allowedTenants = "randomTenant";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put(IdentityUtil.AZURE_ADDITIONALLY_ALLOWED_TENANTS, allowedTenants));
        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);

        IdentityClientOptions options = new IdentityClientOptions()
            .setAdditionallyAllowedTenants(IdentityUtil.getAdditionalTenantsFromEnvironment(configuration));
        IdentityUtil.resolveTenantId(currentTenant, trc, options);
    }

}

