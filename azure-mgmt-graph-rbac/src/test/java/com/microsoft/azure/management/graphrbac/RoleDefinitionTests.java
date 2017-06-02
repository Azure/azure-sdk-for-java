/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class RoleDefinitionTests extends GraphRbacManagementTest {
    @Test
    public void canGetRoleByRoleName() throws Exception {
        RoleDefinition roleDefinition = graphRbacManager.roleDefinitions()
                .getByScopeAndRoleName("subscriptions/" + resourceManager.subscriptionId(), "Contributor");
        Assert.assertNotNull(roleDefinition);
        Assert.assertEquals("Contributor", roleDefinition.roleName());
    }

}
