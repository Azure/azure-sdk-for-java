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
import java.util.List;
import java.util.stream.Collectors;

public class RoleAssignmentTests extends GraphRbacManagementTest {
    @Test
    public void canCRUDRoleAssignment() throws Exception {
        String roleAssignmentName = generateRandomUuid();
        String spName = generateRandomResourceName("sp", 20);

        ServicePrincipal sp =
            authorizationManager.servicePrincipals().define(spName).withNewApplication("http://" + spName).create();

        try {
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

            List<RoleAssignment> roleAssignments = authorizationManager.roleAssignments()
                .listByServicePrincipal(sp.id()).stream().collect(Collectors.toList());

            Assertions.assertEquals(1, roleAssignments.size());
            RoleAssignment roleAssignment1 = roleAssignments.iterator().next();
            Assertions.assertEquals(roleAssignment.id(), roleAssignment1.id());
            Assertions.assertEquals(roleAssignment.scope(), roleAssignment1.scope());
            Assertions.assertEquals(roleAssignment.roleDefinitionId(), roleAssignment1.roleDefinitionId());
            Assertions.assertEquals(roleAssignment.principalId(), roleAssignment1.principalId());
        } finally {
            authorizationManager.servicePrincipals().deleteById(sp.id());
            authorizationManager
                .applications()
                .deleteById(authorizationManager.applications().getByName(sp.applicationId()).id());
        }
    }
}
