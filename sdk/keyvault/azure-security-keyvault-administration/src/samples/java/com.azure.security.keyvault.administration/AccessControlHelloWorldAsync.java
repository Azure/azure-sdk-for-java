// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * This sample demonstrates how to create, get, list and delete role assignments asynchronously in the Key Vault.
 */
public class AccessControlHelloWorldAsync {
    /**
     * Authenticates with the key vault and shows how to create, get, list and delete role assignments asynchronously.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when an invalid key vault URL is passed.
     */
    public static void main(String[] args) throws InterruptedException {
        /* Instantiate a KeyVaultAccessControlAsyncClient that will be used to call the service. Notice that the client
        is using default Azure credentials. To make default credentials work, ensure that environment variables
        'AZURE_CLIENT_ID', 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.

        To get started, you'll need a URI to an Azure Key Vault. See the README (https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-administration/README.md)
        for links and instructions. */
        KeyVaultAccessControlAsyncClient accessControlAsyncClient = new KeyVaultAccessControlClientBuilder()
            .vaultUrl("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

        /* In order to assign a role to a service principal, we'll have to know which role definitions are available.
        Let's get all of them. */
        List<KeyVaultRoleDefinition> roleDefinitions = new ArrayList<>();

        accessControlAsyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL)
            .doOnNext((roleDefinition) -> {
                roleDefinitions.add(roleDefinition);
                System.out.printf("Retrieved role definition with name: %s %n", roleDefinition.getName());
            })
            .blockLast();

        // Before assigning any new roles, let's get all the current role assignments.
        accessControlAsyncClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL)
            .doOnNext((roleAssignment ->
                System.out.printf("Retrieved role assignment with name: %s %n", roleAssignment.getName())))
            .blockLast();

        /* Now let's assign a role to a service principal. To do this we'll need a role definition ID and a service
        principal object ID. A role definition ID can be obtained from the 'id' property of one of the role definitions
        returned from listRoleAssignments().

        See the README (https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-administration/README.md#authenticate-the-client)
        for links and instructions on how to generate a new service principal and obtain it's object ID. You can also
        get the object ID for your currently signed in account by running the following Azure CLI command:
        az ad signed-in-user show --query objectId */
        String servicePrincipalId = "<service-principal-id>";
        KeyVaultRoleDefinition roleDefinition = roleDefinitions.get(0);
        KeyVaultRoleAssignment createdRoleAssignment =
            accessControlAsyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinition.getId(),
                servicePrincipalId).block();

        /* To get an existing role assignment, we'll need the 'name' property from an existing assignment. Let's use the
        createdAssignment from the previous example. */
        assert createdRoleAssignment != null;

        accessControlAsyncClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, createdRoleAssignment.getName())
            .doOnSuccess((retrievedRoleAssignment) ->
                System.out.printf("Retrieved role assignment with name: %s %n", retrievedRoleAssignment.getName()))
            .block();

        /* To remove a role assignment from a service principal, the role assignment must be deleted. Let's delete the
        createdAssignment from the previous example. */
        accessControlAsyncClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, createdRoleAssignment.getName())
            .doOnSuccess((deletedRoleAssignment) ->
                System.out.printf("Retrieved role assignment with name: %s %n", deletedRoleAssignment.getName()))
            .block();

        /* NOTE: block() and blockLast() will block until the above operations are completed. This is strongly
        discouraged for use in production as it eliminates the benefits of asynchronous IO. It is used here to ensure
        the sample runs to completion. */
    }
}
