// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration.codesnippets;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.KeyVaultAccessControlClient;
import com.azure.security.keyvault.administration.KeyVaultAccessControlClientBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultDataAction;
import com.azure.security.keyvault.administration.models.KeyVaultPermission;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.administration.models.SetRoleDefinitionOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains code samples for generating javadocs through doclets for {@link KeyVaultAccessControlClient}.
 */
public class KeyVaultAccessControlClientJavaDocCodeSnippets {
    /**
     * Generates a code sample for creating a {@link KeyVaultAccessControlClient}.
     *
     * @return An instance of {@link KeyVaultAccessControlClient}.
     */
    public KeyVaultAccessControlClient createClient() {
        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.instantiation
        KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
            .vaultUrl("https://myaccount.managedhsm.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.instantiation

        return keyVaultAccessControlClient;
    }

    /**
     * Generates code samples for using {@link KeyVaultAccessControlClient#listRoleDefinitions(KeyVaultRoleScope)} and
     * {@link KeyVaultAccessControlClient#listRoleDefinitions(KeyVaultRoleScope, Context)}.
     */
    public void listRoleDefinitions() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope
        PagedIterable<KeyVaultRoleDefinition> roleDefinitions =
            keyVaultAccessControlClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL);

        roleDefinitions.forEach(roleDefinition ->
            System.out.printf("Retrieved role definition with name '%s'.%n", roleDefinition.getName()));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope-Context
        PagedIterable<KeyVaultRoleDefinition> keyVaultRoleDefinitions =
            keyVaultAccessControlClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL, new Context("key1", "value1"));

        keyVaultRoleDefinitions.forEach(roleDefinition ->
            System.out.printf("Retrieved role definition with name '%s'.%n", roleDefinition.getName()));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope-Context
    }

    /**
     * Generates code samples for using {@link KeyVaultAccessControlClient#setRoleDefinition(KeyVaultRoleScope)},
     * {@link KeyVaultAccessControlClient#setRoleDefinition(KeyVaultRoleScope, String)} and
     * {@link KeyVaultAccessControlClient#setRoleDefinitionWithResponse(SetRoleDefinitionOptions, Context)}.
     */
    public void setRoleDefinition() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope
        KeyVaultRoleDefinition roleDefinition = keyVaultAccessControlClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL);

        System.out.printf("Created role definition with randomly generated name '%s' and role name '%s'.%n",
            roleDefinition.getName(), roleDefinition.getRoleName());
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope-String
        String myRoleDefinitionName = "b67c3cf4-cbfd-451e-89ab-97c01906a2e0";
        KeyVaultRoleDefinition myRoleDefinition =
            keyVaultAccessControlClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, myRoleDefinitionName);

        System.out.printf("Set role definition with name '%s' and role name '%s'.%n", myRoleDefinition.getName(),
            myRoleDefinition.getRoleName());
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope-String

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.setRoleDefinitionWithResponse#SetRoleDefinitionOptions-Context
        String roleDefinitionName = "a86990e4-2080-4666-bd36-6e1664d3706f";

        List<KeyVaultRoleScope> assignableScopes = new ArrayList<>();
        assignableScopes.add(KeyVaultRoleScope.GLOBAL);
        assignableScopes.add(KeyVaultRoleScope.KEYS);

        List<KeyVaultDataAction> dataActions = new ArrayList<>();
        dataActions.add(KeyVaultDataAction.START_HSM_RESTORE);
        dataActions.add(KeyVaultDataAction.START_HSM_BACKUP);
        dataActions.add(KeyVaultDataAction.READ_HSM_BACKUP_STATUS);
        dataActions.add(KeyVaultDataAction.READ_HSM_RESTORE_STATUS);
        dataActions.add(KeyVaultDataAction.BACKUP_HSM_KEYS);
        dataActions.add(KeyVaultDataAction.RESTORE_HSM_KEYS);

        List<KeyVaultPermission> permissions = new ArrayList<>();
        permissions.add(new KeyVaultPermission(null, null, dataActions, null));

        SetRoleDefinitionOptions setRoleDefinitionOptions =
            new SetRoleDefinitionOptions(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
                .setRoleName("Backup and Restore Role Definition")
                .setDescription("Can backup and restore a whole Managed HSM, as well as individual keys.")
                .setAssignableScopes(assignableScopes)
                .setPermissions(permissions);

        Response<KeyVaultRoleDefinition> response =
            keyVaultAccessControlClient.setRoleDefinitionWithResponse(setRoleDefinitionOptions,
                new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Role definition with name '%s' and role name '%s' "
            + "was set.%n", response.getStatusCode(), response.getValue().getName(), response.getValue().getRoleName());
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.setRoleDefinitionWithResponse#SetRoleDefinitionOptions-Context
    }

    /**
     * Generates code samples for using {@link KeyVaultAccessControlClient#getRoleDefinition(KeyVaultRoleScope, String)}
     * and {@link KeyVaultAccessControlClient#getRoleDefinitionWithResponse(KeyVaultRoleScope, String, Context)}.
     */
    public void getRoleDefinition() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.getRoleDefinition#KeyVaultRoleScope-String
        String roleDefinitionName = "de8df120-987e-4477-b9cc-570fd219a62c";
        KeyVaultRoleDefinition roleDefinition =
            keyVaultAccessControlClient.getRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

        System.out.printf("Retrieved role definition with name '%s' and role name '%s'.%n", roleDefinition.getName(),
            roleDefinition.getRoleName());
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.getRoleDefinition#KeyVaultRoleScope-String

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.getRoleDefinitionWithResponse#KeyVaultRoleScope-String-Context
        String myRoleDefinitionName = "cb15ef18-b32c-4224-b048-3a91cd68acc3";
        Response<KeyVaultRoleDefinition> response =
            keyVaultAccessControlClient.getRoleDefinitionWithResponse(KeyVaultRoleScope.GLOBAL, myRoleDefinitionName,
                new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Role definition with name '%s' and role name '%s'"
            + " was retrieved.%n", response.getStatusCode(), response.getValue().getName(),
            response.getValue().getRoleName());
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.getRoleDefinitionWithResponse#KeyVaultRoleScope-String-Context
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlClient#deleteRoleDefinition(KeyVaultRoleScope, String)} and
     * {@link KeyVaultAccessControlClient#deleteRoleDefinitionWithResponse(KeyVaultRoleScope, String, Context)}.
     */
    public void deleteRoleDefinition() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.deleteRoleDefinition#KeyVaultRoleScope-String
        String roleDefinitionName = "6a709e6e-8964-4012-a99b-6b0131e8ce40";

        keyVaultAccessControlClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

        System.out.printf("Deleted role definition with name '%s'.%n", roleDefinitionName);
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.deleteRoleDefinition#KeyVaultRoleScope-String

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.deleteRoleDefinitionWithResponse#KeyVaultRoleScope-String-Context
        String myRoleDefinitionName = "6b2d0b58-4108-44d6-b7e0-4fd02f77fe7e";
        Response<Void> response =
            keyVaultAccessControlClient.deleteRoleDefinitionWithResponse(KeyVaultRoleScope.GLOBAL, myRoleDefinitionName,
                new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Role definition with name '%s' was deleted.%n",
            response.getStatusCode(), myRoleDefinitionName);
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.deleteRoleDefinitionWithResponse#KeyVaultRoleScope-String-Context
    }

    /**
     * Generates code samples for using {@link KeyVaultAccessControlClient#listRoleAssignments(KeyVaultRoleScope)} and
     * {@link KeyVaultAccessControlClient#listRoleAssignments(KeyVaultRoleScope, Context)}.
     */
    public void listRoleAssignments() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope
        PagedIterable<KeyVaultRoleAssignment> roleAssignments =
            keyVaultAccessControlClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL);

        roleAssignments.forEach(roleAssignment ->
            System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope-Context
        PagedIterable<KeyVaultRoleAssignment> keyVaultRoleAssignments =
            keyVaultAccessControlClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL, new Context("key1", "value1"));

        keyVaultRoleAssignments.forEach(roleAssignment ->
            System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope-Context
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlClient#createRoleAssignment(KeyVaultRoleScope, String, String)},
     * {@link KeyVaultAccessControlClient#createRoleAssignment(KeyVaultRoleScope, String, String, String)} and
     * {@link KeyVaultAccessControlClient#createRoleAssignmentWithResponse(KeyVaultRoleScope, String, String, String, Context)}.
     */
    public void createRoleAssignment() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String
        String roleDefinitionId = "b0b43a39-920c-475b-b34c-32ecc2bbb0ea";
        String servicePrincipalId = "169d6a86-61b3-4615-ac7e-2da09edfeed4";
        KeyVaultRoleAssignment roleAssignment =
            keyVaultAccessControlClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinitionId,
                servicePrincipalId);

        System.out.printf("Created role assignment with randomly generated name '%s' for principal with id '%s'.%n",
            roleAssignment.getName(), roleAssignment.getProperties().getPrincipalId());
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String-String
        String myRoleDefinitionId = "c7d4f70f-944d-494a-a73e-ff62fe7f04da";
        String myServicePrincipalId = "4196fc8f-7312-46b9-9a08-05bf44fdff37";
        String myRoleAssignmentName = "d80e9366-47a6-4f42-ba84-f2eefb084972";
        KeyVaultRoleAssignment myRoleAssignment =
            keyVaultAccessControlClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, myRoleDefinitionId,
                myServicePrincipalId, myRoleAssignmentName);

        System.out.printf("Created role assignment with name '%s' for principal with id '%s'.%n",
            myRoleAssignment.getName(), myRoleAssignment.getProperties().getPrincipalId());
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String-String

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.createRoleAssignmentWithResponse#KeyVaultRoleScope-String-String-String-Context
        String someRoleDefinitionId = "11385c39-5efa-4e5f-8748-055aa51d4d23";
        String someServicePrincipalId = "eab943f7-a204-4434-9681-ef2cc0c85b51";
        String someRoleAssignmentName = "4d95e0ea-4808-43a4-b7f9-d9e61dba7ea9";

        Response<KeyVaultRoleAssignment> response =
            keyVaultAccessControlClient.createRoleAssignmentWithResponse(KeyVaultRoleScope.GLOBAL, someRoleDefinitionId,
                someServicePrincipalId, someRoleAssignmentName, new Context("key1", "value1"));
        KeyVaultRoleAssignment createdRoleAssignment = response.getValue();

        System.out.printf("Response successful with status code: %d. Role assignment with name '%s' for principal with"
            + "id '%s' was created.%n", response.getStatusCode(), createdRoleAssignment.getName(),
            createdRoleAssignment.getProperties().getPrincipalId());
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.createRoleAssignmentWithResponse#KeyVaultRoleScope-String-String-String-Context
    }

    /**
     * Generates code samples for using {@link KeyVaultAccessControlClient#getRoleAssignment(KeyVaultRoleScope, String)}
     * and {@link KeyVaultAccessControlClient#getRoleAssignmentWithResponse(KeyVaultRoleScope, String, Context)}.
     */
    public void getRoleAssignment() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.getRoleAssignment#KeyVaultRoleScope-String
        String roleAssignmentName = "06d1ae8b-0791-4f02-b976-f631251f5a95";
        KeyVaultRoleAssignment roleAssignment =
            keyVaultAccessControlClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

        System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName());
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.getRoleAssignment#KeyVaultRoleScope-String

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.getRoleAssignmentWithResponse#KeyVaultRoleScope-String-Context
        String myRoleAssignmentName = "b4a970d5-c581-4760-bba5-61d3d5aa24f9";
        Response<KeyVaultRoleAssignment> response =
            keyVaultAccessControlClient.getRoleAssignmentWithResponse(KeyVaultRoleScope.GLOBAL, myRoleAssignmentName,
                new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Role assignment with name '%s' was retrieved.%n",
            response.getStatusCode(), response.getValue().getName());
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.getRoleAssignmentWithResponse#KeyVaultRoleScope-String-Context
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlClient#deleteRoleAssignment(KeyVaultRoleScope, String)} and
     * {@link KeyVaultAccessControlClient#deleteRoleAssignmentWithResponse(KeyVaultRoleScope, String, Context)}.
     */
    public void deleteRoleAssignment() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.deleteRoleAssignment#KeyVaultRoleScope-String
        String roleAssignmentName = "c3ed874a-64a9-4a87-8581-2a1ad84b9ddb";

        keyVaultAccessControlClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

        System.out.printf("Deleted role assignment with name '%s'.%n", roleAssignmentName);
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.deleteRoleAssignment#KeyVaultRoleScope-String

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlClient.deleteRoleAssignmentWithResponse#KeyVaultRoleScope-String-Context
        String myRoleAssignmentName = "8ac293e1-1ac8-4a71-b254-7caf9f7c2646";
        Response<Void> response =
            keyVaultAccessControlClient.deleteRoleAssignmentWithResponse(KeyVaultRoleScope.GLOBAL, myRoleAssignmentName,
                new Context("key1", "value1"));

        System.out.printf("Response successful with status code: %d. Role assignment with name '%s' was deleted.%n",
            response.getStatusCode(), myRoleAssignmentName);
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlClient.deleteRoleAssignmentWithResponse#KeyVaultRoleScope-String-Context
    }
}
