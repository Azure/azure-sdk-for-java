/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.graphrbac;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class RoleDefinitionTests extends GraphRbacManagementTest {
    @Test
    public void canGetRoleByRoleName() throws Exception {
        RoleDefinition roleDefinition = graphRbacManager.roleDefinitions()
                .getByScopeAndRoleName("subscriptions/" + resourceManager.getSubscriptionId(), "Contributor");
        Assertions.assertNotNull(roleDefinition);
        Assertions.assertEquals("Contributor", roleDefinition.roleName());
    }

}
