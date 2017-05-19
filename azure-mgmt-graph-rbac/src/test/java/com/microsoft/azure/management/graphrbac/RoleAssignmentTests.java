/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class RoleAssignmentTests extends GraphRbacManagementTestBase {
    @BeforeClass
    public static void setup() throws Exception {
        createClients();
    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Test
    @Ignore("Need a specific subscription")
    public void canCRUDRoleAssignment() throws Exception {
        RoleAssignment roleAssignment = graphRbacManager.roleAssignments()
                .define("myassignment")
                .forServicePrincipal("anotherapp12")
                .withBuiltInRole(BuiltInRole.CONTRIBUTOR)
                .withSubscriptionScope("ec0aa5f7-9e78-40c9-85cd-535c6305b380")
                .create();

        Assert.assertNotNull(roleAssignment);
    }

}
