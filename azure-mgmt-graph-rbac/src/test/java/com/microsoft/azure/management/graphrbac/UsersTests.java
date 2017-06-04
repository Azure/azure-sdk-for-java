/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class UsersTests extends GraphRbacManagementTest {
    private static final String RG_NAME = "javacsmrg350";
    private static final String APP_NAME = "app-javacsm350";

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
}
