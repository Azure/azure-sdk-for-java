// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.authorization;

import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.authorization.models.RoleDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Locale;

public class RoleDefinitionTests extends GraphRbacManagementTest {
    @Test
    public void canGetRoleByRoleName() throws Exception {
        RoleDefinition roleDefinition =
            authorizationManager
                .roleDefinitions()
                .getByScopeAndRoleName("subscriptions/" + resourceManager.subscriptionId(), "Contributor");
        Assertions.assertNotNull(roleDefinition);
        Assertions.assertEquals("Contributor", roleDefinition.roleName());
    }

    @Test
    @Disabled("Util to generate missing built-in role")
    public void generateMissingRole() {
        PagedIterable<RoleDefinition> roleDefinitions =
            authorizationManager.roleDefinitions().listByScope("subscriptions/" + resourceManager.subscriptionId());

        StringBuilder sb = new StringBuilder();

        roleDefinitions.stream()
            //.filter(r -> r.roleName().startsWith("Storage"))
            .forEach(r -> {
                String roleEnumName = r.roleName().toUpperCase(Locale.ROOT).replaceAll(" ", "_");

                sb.append("/** ").append(r.description()).append(". */").append(System.lineSeparator())
                    .append("public static final BuiltInRole ").append(roleEnumName).append(" =").append(System.lineSeparator())
                    .append("    BuiltInRole.fromString(\"").append(r.roleName()).append("\");").append(System.lineSeparator());
            });

        System.out.print(sb.toString());
    }
}
