// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.administration.codesnippets;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient;
import com.azure.security.keyvault.administration.KeyVaultAccessControlClientBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultDataAction;
import com.azure.security.keyvault.administration.models.KeyVaultPermission;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.administration.models.SetRoleDefinitionOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains code samples for generating javadocs through doclets for
 * {@link KeyVaultAccessControlAsyncClient}.
 */
public class KeyVaultAccessControlAsyncClientJavaDocCodeSnippets {
    /**
     * Generates a code sample for creating a {@link KeyVaultAccessControlAsyncClient}.
     *
     * @return An instance of {@link KeyVaultAccessControlAsyncClient}.
     */
    public KeyVaultAccessControlAsyncClient createAsyncClient() {
        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.instantiation
        KeyVaultAccessControlAsyncClient keyVaultAccessControlAsyncClient = new KeyVaultAccessControlClientBuilder()
            .vaultUrl("https://myaccount.managedhsm.azure.net/")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.instantiation

        return keyVaultAccessControlAsyncClient;
    }

    /**
     * Generates code samples for using {@link KeyVaultAccessControlAsyncClient#listRoleDefinitions(KeyVaultRoleScope)}.
     */
    public void listRoleDefinitions() {
        KeyVaultAccessControlAsyncClient keyVaultAccessControlAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.listRoleDefinitions#KeyVaultRoleScope
        keyVaultAccessControlAsyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL)
            .subscribe(roleDefinition ->
                System.out.printf("Retrieved role definition with name '%s'.%n", roleDefinition.getName()));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.listRoleDefinitions#KeyVaultRoleScope
    }

    /**
     * Generates code samples for using {@link KeyVaultAccessControlAsyncClient#setRoleDefinition(KeyVaultRoleScope)},
     * {@link KeyVaultAccessControlAsyncClient#setRoleDefinition(KeyVaultRoleScope, String)} and
     * {@link KeyVaultAccessControlAsyncClient#setRoleDefinitionWithResponse(SetRoleDefinitionOptions)}.
     */
    public void setRoleDefinition() {
        KeyVaultAccessControlAsyncClient keyVaultAccessControlAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.setRoleDefinition#KeyVaultRoleScope
        keyVaultAccessControlAsyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL)
            .subscribe(roleDefinition ->
                System.out.printf("Created role definition with randomly generated name '%s' and role name '%s'.%n",
                    roleDefinition.getName(), roleDefinition.getRoleName()));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.setRoleDefinition#KeyVaultRoleScope

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.setRoleDefinition#KeyVaultRoleScope-String
        String myRoleDefinitionName = "504a3d11-5a63-41a9-b603-41bdf88df03e";

        keyVaultAccessControlAsyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, myRoleDefinitionName)
            .subscribe(roleDefinition ->
                System.out.printf("Set role definition with name '%s' and role name '%s'.%n", roleDefinition.getName(),
                    roleDefinition.getRoleName()));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.setRoleDefinition#KeyVaultRoleScope-String

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.setRoleDefinitionWithResponse#SetRoleDefinitionOptions
        String roleDefinitionName = "9de303d3-6ea8-4b8f-a20b-18e67f77e42a";

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
                .setDescription("Can backup and restore a whole Managed HSM, as well as individual keys.%n")
                .setAssignableScopes(assignableScopes)
                .setPermissions(permissions);

        keyVaultAccessControlAsyncClient.setRoleDefinitionWithResponse(setRoleDefinitionOptions)
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Role definition with name '%s' and role"
                    + " name '%s' was set.%n", response.getStatusCode(), response.getValue().getName(),
                    response.getValue().getRoleName()));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.setRoleDefinitionWithResponse#SetRoleDefinitionOptions
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlAsyncClient#getRoleDefinition(KeyVaultRoleScope, String)} and
     * {@link KeyVaultAccessControlAsyncClient#getRoleDefinitionWithResponse(KeyVaultRoleScope, String)}.
     */
    public void getRoleDefinition() {
        KeyVaultAccessControlAsyncClient keyVaultAccessControlAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.getRoleDefinition#KeyVaultRoleScope-String
        String roleDefinitionName = "8f90b099-7361-4db6-8321-719adaf6e4ca";

        keyVaultAccessControlAsyncClient.getRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
            .subscribe(roleDefinition ->
                System.out.printf("Retrieved role definition with name '%s' and role name '%s'.%n",
                    roleDefinition.getName(), roleDefinition.getRoleName()));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.getRoleDefinition#KeyVaultRoleScope-String

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.getRoleDefinitionWithResponse#KeyVaultRoleScope-String
        String myRoleDefinitionName = "0877b4ee-6275-4559-89f1-c289060ef398";

        keyVaultAccessControlAsyncClient.getRoleDefinitionWithResponse(KeyVaultRoleScope.GLOBAL, myRoleDefinitionName)
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Role definition with name '%s' and role"
                    + " name '%s' was retrieved.%n", response.getStatusCode(), response.getValue().getName(),
                    response.getValue().getRoleName()));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.getRoleDefinitionWithResponse#KeyVaultRoleScope-String
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlAsyncClient#deleteRoleDefinition(KeyVaultRoleScope, String)} and
     * {@link KeyVaultAccessControlAsyncClient#deleteRoleDefinitionWithResponse(KeyVaultRoleScope, String)}.
     */
    public void deleteRoleDefinition() {
        KeyVaultAccessControlAsyncClient keyVaultAccessControlAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.deleteRoleDefinition#KeyVaultRoleScope-String
        String roleDefinitionName = "e3c7c51a-8abd-4b1b-9201-48ded34d0358";

        keyVaultAccessControlAsyncClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
            .subscribe(unused -> System.out.printf("Deleted role definition with name '%s'.%n", roleDefinitionName));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.deleteRoleDefinition#KeyVaultRoleScope-String

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.deleteRoleDefinitionWithResponse#KeyVaultRoleScope-String
        String myRoleDefinitionName = "ccaafb00-31fb-40fe-9ccc-39a2ad2af082";

        keyVaultAccessControlAsyncClient.deleteRoleDefinitionWithResponse(KeyVaultRoleScope.GLOBAL,
            myRoleDefinitionName).subscribe(response ->
                System.out.printf("Response successful with status code: %d. Role definition with name '%s' was"
                    + " deleted.%n", response.getStatusCode(), myRoleDefinitionName));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.deleteRoleDefinitionWithResponse#KeyVaultRoleScope-String
    }

    /**
     * Generates code samples for using {@link KeyVaultAccessControlAsyncClient#listRoleAssignments(KeyVaultRoleScope)}.
     */
    public void listRoleAssignments() {
        KeyVaultAccessControlAsyncClient keyVaultAccessControlAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.listRoleAssignments#KeyVaultRoleScope
        keyVaultAccessControlAsyncClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL)
            .subscribe(roleAssignment ->
                System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.listRoleAssignments#KeyVaultRoleScope
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlAsyncClient#createRoleAssignment(KeyVaultRoleScope, String, String)},
     * {@link KeyVaultAccessControlAsyncClient#createRoleAssignment(KeyVaultRoleScope, String, String, String)} and
     * {@link KeyVaultAccessControlAsyncClient#createRoleAssignmentWithResponse(KeyVaultRoleScope, String, String, String)}.
     */
    public void createRoleAssignment() {
        KeyVaultAccessControlAsyncClient keyVaultAccessControlAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.createRoleAssignment#KeyVaultRoleScope-String-String
        String roleDefinitionId = "142e42c1-ab29-4dc7-9dfa-8fd7c0815128";
        String servicePrincipalId = "07dca82e-b625-4a60-977b-859d2a162ca7";

        keyVaultAccessControlAsyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinitionId,
            servicePrincipalId).subscribe(roleAssignment ->
                System.out.printf("Created role assignment with randomly generated name '%s' for principal with id"
                    + "'%s'.%n", roleAssignment.getName(), roleAssignment.getProperties().getPrincipalId()));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.createRoleAssignment#KeyVaultRoleScope-String-String

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.createRoleAssignment#KeyVaultRoleScope-String-String-String
        String myRoleDefinitionId = "e1ca67d0-4332-465c-b9cd-894b2834401b";
        String myServicePrincipalId = "31af81fe-6123-4838-92c0-7c2531ec13d7";
        String myRoleAssignmentName = "94d7827f-f8c9-4a5d-94fd-9fd2cd02d12f";

        keyVaultAccessControlAsyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, myRoleDefinitionId,
            myServicePrincipalId, myRoleAssignmentName).subscribe(roleAssignment ->
                System.out.printf("Created role assignment with name '%s' for principal with id '%s'.%n",
                    roleAssignment.getName(), roleAssignment.getProperties().getPrincipalId()));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.createRoleAssignment#KeyVaultRoleScope-String-String-String

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.createRoleAssignmentWithResponse#KeyVaultRoleScope-String-String-String
        String someRoleDefinitionId = "686b0f78-5012-4def-8a70-eba36aa54d3d";
        String someServicePrincipalId = "345ec980-904b-4238-aafc-1eaeed3e23cf";
        String someRoleAssignmentName = "1c79927c-6e08-4e5c-8a6c-f58c13c9bbb5";

        keyVaultAccessControlAsyncClient.createRoleAssignmentWithResponse(KeyVaultRoleScope.GLOBAL,
            someRoleDefinitionId, someServicePrincipalId, someRoleAssignmentName).subscribe(response -> {
                KeyVaultRoleAssignment createdRoleAssignment = response.getValue();

                System.out.printf("Response successful with status code: %d. Role assignment with name '%s' for"
                    + " principal with id '%s' was created.%n", response.getStatusCode(),
                    createdRoleAssignment.getName(), createdRoleAssignment.getProperties().getPrincipalId());
            });
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.createRoleAssignmentWithResponse#KeyVaultRoleScope-String-String-String
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlAsyncClient#getRoleAssignment(KeyVaultRoleScope, String)} and
     * {@link KeyVaultAccessControlAsyncClient#getRoleAssignmentWithResponse(KeyVaultRoleScope, String)}.
     */
    public void getRoleAssignment() {
        KeyVaultAccessControlAsyncClient keyVaultAccessControlAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.getRoleAssignment#KeyVaultRoleScope-String
        String roleAssignmentName = "c5a305c0-e17a-40f5-af79-73801bdd8867";

        keyVaultAccessControlAsyncClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName)
            .subscribe(roleAssignment ->
                System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.getRoleAssignment#KeyVaultRoleScope-String

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.getRoleAssignmentWithResponse#KeyVaultRoleScope-String
        String myRoleAssignmentName = "76ccbf52-4d49-4fcc-ad3f-044c254be114";

        keyVaultAccessControlAsyncClient.getRoleAssignmentWithResponse(KeyVaultRoleScope.GLOBAL, myRoleAssignmentName)
            .subscribe(response ->
                System.out.printf("Response successful with status code: %d. Role assignment with name '%s' was"
                    + " retrieved.%n", response.getStatusCode(), response.getValue().getName()));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.getRoleAssignmentWithResponse#KeyVaultRoleScope-String
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlAsyncClient#deleteRoleAssignment(KeyVaultRoleScope, String)} and
     * {@link KeyVaultAccessControlAsyncClient#deleteRoleAssignmentWithResponse(KeyVaultRoleScope, String)}.
     */
    public void deleteRoleAssignment() {
        KeyVaultAccessControlAsyncClient keyVaultAccessControlAsyncClient = createAsyncClient();

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.deleteRoleAssignment#KeyVaultRoleScope-String
        String roleAssignmentName = "f05d11ce-578a-4524-950c-fb4c53e5fb96";

        keyVaultAccessControlAsyncClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName)
            .subscribe(unused ->
                System.out.printf("Deleted role assignment with name '%s'.%n", roleAssignmentName));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.deleteRoleAssignment#KeyVaultRoleScope-String

        // BEGIN: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.deleteRoleAssignmentWithResponse#KeyVaultRoleScope-String
        String myRoleAssignmentName = "06aaea13-e4f3-4d3f-8a93-088dff6e90ed";

        keyVaultAccessControlAsyncClient.deleteRoleAssignmentWithResponse(KeyVaultRoleScope.GLOBAL,
            myRoleAssignmentName).subscribe(response ->
                System.out.printf("Response successful with status code: %d. Role assignment with name '%s' was"
                    + " deleted.%n", response.getStatusCode(), myRoleAssignmentName));
        // END: com.azure.security.keyvault.administration.keyVaultAccessControlAsyncClient.deleteRoleAssignmentWithResponse#KeyVaultRoleScope-String
    }
}
