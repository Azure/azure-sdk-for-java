/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.resources.fluentcore.arm.CountryIsoCode;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class UsersTests extends GraphRbacManagementTest {
    @Test
    @Ignore("Need a specific domain")
    public void canGetUserByEmail() throws Exception {
        ActiveDirectoryUser user = graphRbacManager.users().getByName("admin@azuresdkteam.onmicrosoft.com");
        Assert.assertEquals("Admin", user.name());
    }

    @Test
    @Ignore("Need a specific domain")
    public void canGetUserByForeignEmail() throws Exception {
        ActiveDirectoryUser user = graphRbacManager.users().getByName("jianghlu@microsoft.com");
        Assert.assertEquals("Jianghao Lu", user.name());
    }

    @Test
    @Ignore("Need a specific domain")
    public void canGetUserByDisplayName() throws Exception {
        ActiveDirectoryUser user = graphRbacManager.users().getByName("Reader zero");
        Assert.assertEquals("Reader zero", user.name());
    }

    @Test
    public void canCreateUser() throws Exception {
        String name = SdkContext.randomResourceName("user", 16);
        ActiveDirectoryUser user = graphRbacManager.users().define("Automatic " + name)
                .withEmailAlias(name)
                .withPassword("StrongPass!123")
                .withPromptToChangePasswordOnLogin(true)
                .create();

        Assert.assertNotNull(user);
        Assert.assertNotNull(user.id());
    }

    @Test
    public void canUpdateUser() throws Exception {
        String name = SdkContext.randomResourceName("user", 16);
        ActiveDirectoryUser user = graphRbacManager.users().define("Test " + name)
                .withEmailAlias(name)
                .withPassword("StrongPass!123")
                .create();

        user = user.update()
                .withUsageLocation(CountryIsoCode.AUSTRALIA)
                .apply();

        Assert.assertEquals(CountryIsoCode.AUSTRALIA, user.usageLocation());
    }
}
