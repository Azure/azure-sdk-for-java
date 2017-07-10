/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import org.junit.Assert;
import org.junit.Test;

public class GroupsTests extends GraphRbacManagementTest {
    private static final String RG_NAME = "javacsmrg350";
    private static final String APP_NAME = "app-javacsm350";

    @Test
    public void canCRUDGroup() throws Exception {
        String name = SdkContext.randomResourceName("group", 16);
        ActiveDirectoryGroup user = graphRbacManager.groups().define(name)
                .withEmailAlias(name)
                .create();

        Assert.assertNotNull(user);
        Assert.assertNotNull(user.id());
    }
}
