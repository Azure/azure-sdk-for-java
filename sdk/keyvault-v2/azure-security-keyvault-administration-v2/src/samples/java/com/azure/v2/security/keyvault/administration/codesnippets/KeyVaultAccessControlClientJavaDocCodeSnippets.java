// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.administration.codesnippets;

import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient;
import com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClientBuilder;
import com.azure.v2.security.keyvault.administration.models.KeyVaultDataAction;
import com.azure.v2.security.keyvault.administration.models.KeyVaultPermission;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.v2.security.keyvault.administration.models.SetRoleDefinitionOptions;

import io.clientcore.core.http.client.HttpClient;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions.HttpLogLevel;

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
        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.instantiation
        KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
            .endpoint("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.instantiation

        return keyVaultAccessControlClient;
    }

    /**
     * Generates code sample for creating a {@link KeyVaultAccessControlClient} using a custom {@link HttpClient}.
     *
     * @return An instance of {@link KeyVaultAccessControlClient}.
     */
    public KeyVaultAccessControlClient createClientWithHttpClient() {
        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.instantiation.withHttpClient
        KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
            .endpoint("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpInstrumentationOptions(new HttpInstrumentationOptions().setHttpLogLevel(HttpLogLevel.BODY_AND_HEADERS))
            .httpClient(HttpClient.getSharedInstance())
            .buildClient();
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.instantiation.withHttpClient

        return keyVaultAccessControlClient;
    }

    /**
     * Generates code samples for using {@link KeyVaultAccessControlClient#listRoleDefinitions(KeyVaultRoleScope)} and
     * {@link KeyVaultAccessControlClient#listRoleDefinitions(KeyVaultRoleScope, RequestContext)}.
     */
    public void listRoleDefinitions() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope
        keyVaultAccessControlClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL).forEach(roleDefinition ->
            System.out.printf("Retrieved role definition with name '%s'.%n", roleDefinition.getName()));
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions.iterableByPage#KeyVaultRoleScope
        keyVaultAccessControlClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL).iterableByPage().forEach(pagedResponse -> {
            pagedResponse.getValue().forEach(roleDefinition ->
                System.out.printf("Retrieved role definition with name '%s'.%n", roleDefinition.getName()));
        });
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions.iterableByPage#KeyVaultRoleScope

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        keyVaultAccessControlClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL, requestContext)
            .forEach(roleDefinition ->
                System.out.printf("Retrieved role definition with name '%s'.%n", roleDefinition.getName()));
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions#KeyVaultRoleScope-RequestContext

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions.iterableByPage#KeyVaultRoleScope-RequestContext
        keyVaultAccessControlClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL, requestContext).iterableByPage()
            .forEach(pagedResponse -> {
                pagedResponse.getValue().forEach(roleDefinition ->
                    System.out.printf("Retrieved role definition with name '%s'.%n", roleDefinition.getName()));
            });
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleDefinitions.iterableByPage#KeyVaultRoleScope-RequestContext
    }

    /**
     * Generates code samples for using {@link KeyVaultAccessControlClient#setRoleDefinition(KeyVaultRoleScope)} and
     * {@link KeyVaultAccessControlClient#setRoleDefinition(KeyVaultRoleScope, String)}.
     */
    public void setRoleDefinition() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope
        KeyVaultRoleDefinition roleDefinition = keyVaultAccessControlClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL);

        System.out.printf("Created role definition with randomly generated name '%s' and role name '%s'.%n",
            roleDefinition.getName(), roleDefinition.getRoleName());
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope-String
        String myRoleDefinitionName = "b67c3cf4-cbfd-451e-89ab-97c01906a2e0";
        KeyVaultRoleDefinition myRoleDefinition =
            keyVaultAccessControlClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL, myRoleDefinitionName);

        System.out.printf("Set role definition with name '%s' and role name '%s'.%n", myRoleDefinition.getName(),
            myRoleDefinition.getRoleName());
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope-String
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlClient#setRoleDefinitionWithResponse(SetRoleDefinitionOptions, RequestContext)}.
     */
    public void setRoleDefinitionWithResponse() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinitionWithResponse#SetRoleDefinitionOptions-RequestContext
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
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultRoleDefinition> response =
            keyVaultAccessControlClient.setRoleDefinitionWithResponse(setRoleDefinitionOptions,
                requestContext);

        System.out.printf("Response successful with status code: %d. Role definition with name '%s' and role name '%s' "
            + "was set.%n", response.getStatusCode(), response.getValue().getName(), response.getValue().getRoleName());
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinitionWithResponse#SetRoleDefinitionOptions-RequestContext
    }

    /**
     * Generates code samples for using {@link KeyVaultAccessControlClient#getRoleDefinition(KeyVaultRoleScope, String)}.
     */
    public void getRoleDefinition() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinition#KeyVaultRoleScope-String
        String roleDefinitionName = "de8df120-987e-4477-b9cc-570fd219a62c";
        KeyVaultRoleDefinition roleDefinition =
            keyVaultAccessControlClient.getRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

        System.out.printf("Retrieved role definition with name '%s' and role name '%s'.%n", roleDefinition.getName(),
            roleDefinition.getRoleName());
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinition#KeyVaultRoleScope-String
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlClient#getRoleDefinitionWithResponse(KeyVaultRoleScope, String, RequestContext)}.
     */
    public void getRoleDefinitionWithResponse() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinitionWithResponse#KeyVaultRoleScope-String-RequestContext
        String myRoleDefinitionName = "cb15ef18-b32c-4224-b048-3a91cd68acc3";
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultRoleDefinition> response =
            keyVaultAccessControlClient.getRoleDefinitionWithResponse(KeyVaultRoleScope.GLOBAL, myRoleDefinitionName,
                requestContext);

        System.out.printf("Response successful with status code: %d. Role definition with name '%s' and role name '%s'"
            + " was retrieved.%n", response.getStatusCode(), response.getValue().getName(),
            response.getValue().getRoleName());
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinitionWithResponse#KeyVaultRoleScope-String-RequestContext
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlClient#deleteRoleDefinition(KeyVaultRoleScope, String)}.
     */
    public void deleteRoleDefinition() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinition#KeyVaultRoleScope-String
        String roleDefinitionName = "6a709e6e-8964-4012-a99b-6b0131e8ce40";

        keyVaultAccessControlClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

        System.out.printf("Deleted role definition with name '%s'.%n", roleDefinitionName);
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinition#KeyVaultRoleScope-String
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlClient#deleteRoleDefinitionWithResponse(KeyVaultRoleScope, String, RequestContext)}.
     */
    public void deleteRoleDefinitionWithResponse() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinitionWithResponse#KeyVaultRoleScope-String-RequestContext
        String myRoleDefinitionName = "6b2d0b58-4108-44d6-b7e0-4fd02f77fe7e";
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();
        Response<Void> response =
            keyVaultAccessControlClient.deleteRoleDefinitionWithResponse(KeyVaultRoleScope.GLOBAL, myRoleDefinitionName,
                requestContext);

        System.out.printf("Response successful with status code: %d. Role definition with name '%s' was deleted.%n",
            response.getStatusCode(), myRoleDefinitionName);
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinitionWithResponse#KeyVaultRoleScope-String-RequestContext
    }

    /**
     * Generates code samples for using {@link KeyVaultAccessControlClient#listRoleAssignments(KeyVaultRoleScope)} and
     * {@link KeyVaultAccessControlClient#listRoleAssignments(KeyVaultRoleScope, RequestContext)}.
     */
    public void listRoleAssignments() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope
        keyVaultAccessControlClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL).forEach(roleAssignment ->
            System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments.iterableByPage#KeyVaultRoleScope
        keyVaultAccessControlClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL).iterableByPage()
            .forEach(pagedResponse -> {
                pagedResponse.getValue().forEach(roleAssignment ->
                    System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
            });
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments.iterableByPage#KeyVaultRoleScope

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope-RequestContext
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        keyVaultAccessControlClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL, requestContext)
            .forEach(roleAssignment ->
                System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments#KeyVaultRoleScope-RequestContext

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments.iterableByPage#KeyVaultRoleScope-RequestContext
        RequestContext reqContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        keyVaultAccessControlClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL, reqContext).iterableByPage()
            .forEach(pagedResponse -> {
                pagedResponse.getValue().forEach(roleAssignment ->
                    System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
            });
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.listRoleAssignments.iterableByPage#KeyVaultRoleScope-RequestContext
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlClient#createRoleAssignment(KeyVaultRoleScope, String, String)} and
     * {@link KeyVaultAccessControlClient#createRoleAssignment(KeyVaultRoleScope, String, String, String)}.
     */
    public void createRoleAssignment() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String
        String roleDefinitionId = "b0b43a39-920c-475b-b34c-32ecc2bbb0ea";
        String servicePrincipalId = "169d6a86-61b3-4615-ac7e-2da09edfeed4";
        KeyVaultRoleAssignment roleAssignment =
            keyVaultAccessControlClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinitionId,
                servicePrincipalId);

        System.out.printf("Created role assignment with randomly generated name '%s' for principal with id '%s'.%n",
            roleAssignment.getName(), roleAssignment.getProperties().getPrincipalId());
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String-String
        String myRoleDefinitionId = "c7d4f70f-944d-494a-a73e-ff62fe7f04da";
        String myServicePrincipalId = "4196fc8f-7312-46b9-9a08-05bf44fdff37";
        String myRoleAssignmentName = "d80e9366-47a6-4f42-ba84-f2eefb084972";
        KeyVaultRoleAssignment myRoleAssignment =
            keyVaultAccessControlClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, myRoleDefinitionId,
                myServicePrincipalId, myRoleAssignmentName);

        System.out.printf("Created role assignment with name '%s' for principal with id '%s'.%n",
            myRoleAssignment.getName(), myRoleAssignment.getProperties().getPrincipalId());
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String-String
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlClient#createRoleAssignmentWithResponse(KeyVaultRoleScope, String, String, String, RequestContext)}.
     */
    public void createRoleAssignmentWithResponse() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignmentWithResponse#KeyVaultRoleScope-String-String-String-RequestContext
        String someRoleDefinitionId = "11385c39-5efa-4e5f-8748-055aa51d4d23";
        String someServicePrincipalId = "eab943f7-a204-4434-9681-ef2cc0c85b51";
        String someRoleAssignmentName = "4d95e0ea-4808-43a4-b7f9-d9e61dba7ea9";
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultRoleAssignment> response =
            keyVaultAccessControlClient.createRoleAssignmentWithResponse(KeyVaultRoleScope.GLOBAL, someRoleDefinitionId,
                someServicePrincipalId, someRoleAssignmentName, requestContext);
        KeyVaultRoleAssignment createdRoleAssignment = response.getValue();

        System.out.printf("Response successful with status code: %d. Role assignment with name '%s' for principal with"
            + "id '%s' was created.%n", response.getStatusCode(), createdRoleAssignment.getName(),
            createdRoleAssignment.getProperties().getPrincipalId());
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignmentWithResponse#KeyVaultRoleScope-String-String-String-RequestContext
    }

    /**
     * Generates code samples for using {@link KeyVaultAccessControlClient#getRoleAssignment(KeyVaultRoleScope, String)}.
     */
    public void getRoleAssignment() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignment#KeyVaultRoleScope-String
        String roleAssignmentName = "06d1ae8b-0791-4f02-b976-f631251f5a95";
        KeyVaultRoleAssignment roleAssignment =
            keyVaultAccessControlClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

        System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName());
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignment#KeyVaultRoleScope-String
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlClient#getRoleAssignmentWithResponse(KeyVaultRoleScope, String, RequestContext)}.
     */
    public void getRoleAssignmentWithResponse() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignmentWithResponse#KeyVaultRoleScope-String-RequestContext
        String myRoleAssignmentName = "b4a970d5-c581-4760-bba5-61d3d5aa24f9";
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<KeyVaultRoleAssignment> response =
            keyVaultAccessControlClient.getRoleAssignmentWithResponse(KeyVaultRoleScope.GLOBAL, myRoleAssignmentName,
                requestContext);

        System.out.printf("Response successful with status code: %d. Role assignment with name '%s' was retrieved.%n",
            response.getStatusCode(), response.getValue().getName());
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignmentWithResponse#KeyVaultRoleScope-String-RequestContext
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlClient#deleteRoleAssignment(KeyVaultRoleScope, String)}.
     */
    public void deleteRoleAssignment() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignment#KeyVaultRoleScope-String
        String roleAssignmentName = "c3ed874a-64a9-4a87-8581-2a1ad84b9ddb";

        keyVaultAccessControlClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

        System.out.printf("Deleted role assignment with name '%s'.%n", roleAssignmentName);
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignment#KeyVaultRoleScope-String
    }

    /**
     * Generates code samples for using
     * {@link KeyVaultAccessControlClient#deleteRoleAssignmentWithResponse(KeyVaultRoleScope, String, RequestContext)}.
     */
    public void deleteRoleAssignmentWithResponse() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = createClient();

        // BEGIN: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignmentWithResponse#KeyVaultRoleScope-String-RequestContext
        String myRoleAssignmentName = "8ac293e1-1ac8-4a71-b254-7caf9f7c2646";
        RequestContext requestContext = RequestContext.builder()
            .putMetadata("key1", "value1")
            .build();

        Response<Void> response =
            keyVaultAccessControlClient.deleteRoleAssignmentWithResponse(KeyVaultRoleScope.GLOBAL, myRoleAssignmentName,
                requestContext);

        System.out.printf("Response successful with status code: %d. Role assignment with name '%s' was deleted.%n",
            response.getStatusCode(), myRoleAssignmentName);
        // END: com.azure.v2.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignmentWithResponse#KeyVaultRoleScope-String-RequestContext
    }
}
