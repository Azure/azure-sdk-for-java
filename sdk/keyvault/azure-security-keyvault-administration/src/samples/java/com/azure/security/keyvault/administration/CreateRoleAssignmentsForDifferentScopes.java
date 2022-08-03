// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;

/**
 * This sample demonstrates how to create role assignments in the key vault for different scopes.
 */
public class CreateRoleAssignmentsForDifferentScopes {
    /**
     * Authenticates with the key vault and shows how to create role assignments in the key vault for different scopes
     * synchronously. For examples of how to perform async operations, please refer to
     * {@link AccessControlHelloWorldAsync the async client samples}.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when an invalid key vault URL is passed.
     */
    public static void main(String[] args) {
        /* Instantiate a KeyVaultAccessControlClient that will be used to call the service. Notice that the client is
        using default Azure credentials. For more information on this and other types of credentials, see this document:
        https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable.

        To get started, you'll need a URL to an Azure Key Vault Managed HSM. See the README
        (https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-administration/README.md)
        for links and instructions. */
        KeyVaultAccessControlClient accessControlClient = new KeyVaultAccessControlClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        /* By default role assignments apply to the global scope. It is also possible to be more specific by applying an
        assignment to the all keys scope or a specific KeyVaultKey.

        Let's assign a role to a service principal so that it applies to all keys. To do this we'll need a service
        principal object ID and a role definition ID. A role definition ID can be obtained from the 'id' property of one
        of the role definitions returned from listRoleDefinitions(). Alternatively, you can use the following Azure CLI
        command: az keyvault role definition list --hsm-name <name> */
        String roleDefinitionId = "<role-definition-id>";
        String servicePrincipalId = "<service-principal-id>";

        KeyVaultRoleAssignment roleAssignmentForAllKeys =
            accessControlClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinitionId, servicePrincipalId);

        System.out.printf("Created role assignment with name: %s %n", roleAssignmentForAllKeys.getName());

        /* Now let's assign a role to a service principal so that it applies to a specific KeyVaultKey. To do this we'll
        use the role definition ID and a service principal object ID from the previous sample. We'll also need the ID of
        an existing KeyVaultKey, which can be obtained from the service using a KeyClient. */
        String keyId = "<key-id>";

        KeyVaultRoleAssignment roleAssignmentForSingleKey =
            accessControlClient.createRoleAssignment(KeyVaultRoleScope.fromString(keyId), roleDefinitionId,
                servicePrincipalId);

        System.out.printf("Created role assignment with name: %s %n", roleAssignmentForSingleKey.getName());
    }
}
