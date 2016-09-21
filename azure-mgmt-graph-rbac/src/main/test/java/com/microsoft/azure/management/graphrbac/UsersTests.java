/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class UsersTests extends GraphRbacManagementTestBase {
    private static final String RG_NAME = "javacsmrg350";
    private static final String APP_NAME = "app-javacsm350";

    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Test
    public void canCRUDUser() throws Exception {
        //LIST
        List<User> userList = graphRbacManager.users().list();
        Assert.assertNotNull(userList);
        User user = graphRbacManager.users().define("newuser")
                .withDisplayName("Test User 309")
                .withPassword("Pa$$w0rd")
                .withMailNickname(null)
                .create();
        Assert.assertNotNull(user);
        Assert.assertEquals("Test User 309", user.displayName());
    }

}
