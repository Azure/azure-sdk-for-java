// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.accesscontrol;

import com.azure.analytics.synapse.accesscontrol.models.RoleAssignmentDetails;
import com.azure.analytics.synapse.accesscontrol.models.RoleAssignmentOptions;
import com.azure.analytics.synapse.accesscontrol.models.SynapseRole;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.List;
import java.util.UUID;

/**
 * Sample demonstrates how to set, get, update and delete a role assignment.
 */
public class HelloWorld {

    /**
     * Authenticates with the Synapse workspace and shows how to set, get, update and delete a role assignment in the workspace.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid workspace endpoint is passed.
     */
    public static void main(String[] args) throws  IllegalArgumentException {
        // Instantiate a access control client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        AccessControlClient client = new AccessControlClientBuilder()
            .endpoint("https://{YOUR_WORKSPACE_NAME}.dev.azuresynapse.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Get the role definition of workspace admin.
        SynapseRole role = client.getRoleDefinitions().stream()
            .filter(r -> r.getName().equalsIgnoreCase("Workspace Admin"))
            .findAny()
            .get();

        // Add a role assignment
        String principalId = UUID.randomUUID().toString();
        RoleAssignmentOptions request = new RoleAssignmentOptions();
        request.setRoleId(role.getId());
        request.setPrincipalId(principalId);
        RoleAssignmentDetails roleAssignmentCreated = client.createRoleAssignment(request);

        List<RoleAssignmentDetails> allRoleAssignments = client.getRoleAssignments();
        for (RoleAssignmentDetails roleAssignment : allRoleAssignments) {
            System.out.println(roleAssignment.getId());
        }

        // Get the role assignment
        RoleAssignmentDetails roleAssignment = client.getRoleAssignmentById(roleAssignmentCreated.getId());
        System.out.printf("Role %s is assigned to %s. Role assignment id: %s\n",
            role.getName(),
            roleAssignment.getPrincipalId(),
            roleAssignment.getId());

        // Delete the role assignment
        client.deleteRoleAssignmentById(roleAssignment.getId());

        try {
            RoleAssignmentDetails deletedRoleAssignment = client.getRoleAssignmentById(roleAssignmentCreated.getId());
        } catch (ResourceNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
}
