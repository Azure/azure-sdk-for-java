// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.accesscontrol;

import com.azure.analytics.synapse.accesscontrol.models.ErrorContractException;
import com.azure.analytics.synapse.accesscontrol.models.RoleAssignmentDetails;
import com.azure.analytics.synapse.accesscontrol.models.RoleAssignmentOptions;
import com.azure.analytics.synapse.accesscontrol.models.SynapseRole;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AccessControlClientTest extends AccessControlClientTestBase {

    private AccessControlClient client;

    @Override
    protected void beforeTest() {
        client = clientSetup(httpPipeline -> getAccessControlClientBuilder(httpPipeline).buildClient());
    }

    /**
     * Tests that role assignments can be listed in the Synapse workspace.
     */
    @Test
    public void getRoleDefinitions() {
        for (SynapseRole expectedRole : client.getRoleDefinitions()) {
            SynapseRole actualRole = client.getRoleDefinitionById(expectedRole.getId());
            validateRoleDefinitions(expectedRole, actualRole);
        }
    }

    @Test
    public void getRoleDefinitionsWithResponse() {
        for (SynapseRole expectedRole : client.getRoleDefinitions(null)) {
            SynapseRole actualRole = client.getRoleDefinitionByIdWithResponse(expectedRole.getId(), null).getValue();
            validateRoleDefinitions(expectedRole, actualRole);
        }
    }

    @Test
    public void getRoleAssignment() {
        List<RoleAssignmentDetails> roleAssignments = client.getRoleAssignments();
        for (RoleAssignmentDetails expectedRoleAssignment: roleAssignments) {
            RoleAssignmentDetails actualRoleAssignment = client.getRoleAssignmentById(expectedRoleAssignment.getId());
            validateRoleAssignments(expectedRoleAssignment, actualRoleAssignment);
        }
    }

    @Test
    public void getRoleAssignmentWithResponse() {
        List<RoleAssignmentDetails> roleAssignments = client.getRoleAssignmentsWithResponse(null, null, null, null).getValue();
        for (RoleAssignmentDetails expectedRoleAssignment: roleAssignments) {
            RoleAssignmentDetails actualRoleAssignment = client.getRoleAssignmentByIdWithResponse(expectedRoleAssignment.getId(), null).getValue();
            validateRoleAssignments(expectedRoleAssignment, actualRoleAssignment);
        }
    }

    @Test
    public void createAndDeleteRoleAssignment() {
        String sqlAdminId = "7af0c69a-a548-47d6-aea3-d00e69bd83aa";
        String principalId = "1f104909-2748-49b1-b082-f83466d88086";

        // Create role assignment.
        RoleAssignmentOptions createRoleAssignmentOptions = new RoleAssignmentOptions()
            .setRoleId(sqlAdminId)
            .setPrincipalId(principalId);
        RoleAssignmentDetails actualRoleAssignment = client.createRoleAssignment(createRoleAssignmentOptions);

        // Verify the role assignment exists.
        validateRoleAssignments(createRoleAssignmentOptions, actualRoleAssignment);

        // Remove the role assignment.
        client.deleteRoleAssignmentById(actualRoleAssignment.getId());

        // Verify the role assignment doesn't exist.
        assertFalse(client.getRoleAssignments().stream()
            .anyMatch(ra -> ra.getPrincipalId().equalsIgnoreCase(principalId)));
    }

    @Test
    public void createAndDeleteRoleAssignmentWithResponse() {
        String sqlAdminId = "7af0c69a-a548-47d6-aea3-d00e69bd83aa";
        String principalId = "1f104909-2748-49b1-b082-f83466d88086";

        // Create role assignment.
        RoleAssignmentOptions createRoleAssignmentOptions = new RoleAssignmentOptions()
            .setRoleId(sqlAdminId)
            .setPrincipalId(principalId);
        RoleAssignmentDetails actualRoleAssignment = client.createRoleAssignmentWithResponse(createRoleAssignmentOptions, null).getValue();

        // Verify the role assignment exists.
        validateRoleAssignments(createRoleAssignmentOptions, actualRoleAssignment);

        // Remove the role assignment.
        client.deleteRoleAssignmentByIdWithResponse(actualRoleAssignment.getId(), null);

        // Verify the role assignment doesn't exist.
        assertFalse(client.getRoleAssignmentsWithResponse(null, null, null,null).getValue().stream()
            .anyMatch(ra -> ra.getPrincipalId().equalsIgnoreCase(principalId)));
    }

    @Test
    public void getCallerRoleAssignments() {
        List<String> actualRoles = client.getCallerRoleAssignments();
        assertTrue(actualRoles != null && actualRoles.size() > 0);
    }

    @Test
    public void getCallerRoleAssignmentsWithResponse() {
        List<String> actualRoles = client.getCallerRoleAssignmentsWithResponse(null).getValue();
        assertTrue(actualRoles != null && actualRoles.size() > 0);
    }

    @Test
    public void setRoleAssignmentEmptyRoleId() {
        String sqlAdminId = "";
        String principalId = "1f104909-2748-49b1-b082-f83466d88086";

        // Create role assignment.
        RoleAssignmentOptions createRoleAssignmentOptions = new RoleAssignmentOptions()
            .setRoleId(sqlAdminId)
            .setPrincipalId(principalId);
        assertRestException(() -> client.createRoleAssignment(createRoleAssignmentOptions), ErrorContractException.class, HttpURLConnection.HTTP_BAD_REQUEST);
    }

    @Test
    public void setNullRoleAssignment() {
        assertRunnableThrowsException(() -> client.createRoleAssignment(null), ErrorContractException.class);
    }

    /**
     * Tests that an attempt to get a non-existing role assignment throws an error.
     */
    @Test
    public void getRoleAssignmentNotFound() {
        assertRestException(() -> client.getRoleAssignmentById("non-existing"),  ErrorContractException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * Tests that an attempt to remove a non-existing role assignment throws an error.
     */
    @Test
    public void deleteRoleAssignmentNotFound() {
        assertRestException(() -> client.getRoleAssignmentById("non-existing"),  ErrorContractException.class, HttpURLConnection.HTTP_NOT_FOUND);
    }
}
