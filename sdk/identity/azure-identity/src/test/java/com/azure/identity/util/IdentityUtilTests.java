// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.util;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.identity.implementation.IdentityClientOptions;
import com.azure.identity.implementation.util.IdentityUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;

@RunWith(PowerMockRunner.class)
public class IdentityUtilTests {

    public void testMultiTenantAuthenticationEnabled() throws Exception {
        String currentTenant = "tenant";
        String newTenant = "tenant-new";
        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId(newTenant);
        IdentityClientOptions options = new IdentityClientOptions();
        options.setAllowMultiTenantAuthentication(true);

        Assert.assertEquals(newTenant, IdentityUtil.resolveTenantId(currentTenant, trc, options));
    }

    @Test(expected = ClientAuthenticationException.class)
    public void testMultiTenantAuthenticationDisabled() throws Exception {
        String currentTenant = "tenant";
        String newTenant = "tenant-new";
        TokenRequestContext trc = new TokenRequestContext()
            .setScopes(Arrays.asList("http://vault.azure.net/.default"))
            .setTenantId("newTenant");
        IdentityClientOptions options = new IdentityClientOptions();
        options.setAllowMultiTenantAuthentication(false);

        IdentityUtil.resolveTenantId(currentTenant, trc, options);
    }

}

