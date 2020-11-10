// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization;

import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.authorization.models.RoleAssignment;
import com.azure.resourcemanager.authorization.models.ServicePrincipal;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class RoleAssignmentTests extends GraphRbacManagementTest {
    @Test
    public void canCRUDRoleAssignment() throws Exception {
        String roleAssignmentName = generateRandomUuid();
        String spName = generateRandomResourceName("sp", 20);

        ServicePrincipal sp =
            authorizationManager.servicePrincipals().define(spName).withNewApplication("http://" + spName).create();

        ResourceManagerUtils.sleep(Duration.ofSeconds(15));

        RoleAssignment roleAssignment =
            authorizationManager
                .roleAssignments()
                .define(roleAssignmentName)
                .forServicePrincipal(sp)
                .withBuiltInRole(BuiltInRole.CONTRIBUTOR)
                .withSubscriptionScope(resourceManager.subscriptionId())
                .create();

        Assertions.assertNotNull(roleAssignment);
    }
}
