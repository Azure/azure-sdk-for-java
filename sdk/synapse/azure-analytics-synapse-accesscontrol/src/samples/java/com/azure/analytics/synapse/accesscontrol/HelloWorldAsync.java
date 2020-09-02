// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.accesscontrol;

import com.azure.analytics.synapse.accesscontrol.models.RoleAssignmentDetails;
import com.azure.analytics.synapse.accesscontrol.models.RoleAssignmentOptions;
import com.azure.analytics.synapse.accesscontrol.models.SynapseRole;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.UUID;

/**
 * Sample demonstrates how to set, get, update and delete a key.
 */
public class HelloWorldAsync {

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
        AccessControlAsyncClient client = new AccessControlClientBuilder()
            .endpoint("https://{YOUR_WORKSPACE_NAME}.dev.azuresynapse.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        // Get the role definition of workspace admin.
        SynapseRole role = client.getRoleDefinitions().blockFirst();

        // Add a role assignment
        String principalId = UUID.randomUUID().toString();
        RoleAssignmentOptions request = new RoleAssignmentOptions();
        request.setRoleId(role.getId());
        request.setPrincipalId(principalId);
        client.createRoleAssignment(request)
            .subscribe(r -> System.out.printf("Role assignment created with id \"%s\"", r.getId()));

        client.getRoleAssignments()
            .subscribe(allRoleAssignments -> {
                for (RoleAssignmentDetails roleAssignment : allRoleAssignments) {
                    System.out.println(roleAssignment.getId());
                    client.getRoleAssignmentById(roleAssignment.getId())
                        .subscribe(ra ->
                            System.out.printf("Role %s is assigned to %s. Role assignment id: %s\n",
                                ra.getRoleId(),
                                ra.getPrincipalId(),
                                ra.getId()));
                }
            });

        // Delete the role assignment
        client.deleteRoleAssignmentById("roleAssignmentId").block();
    }
}
