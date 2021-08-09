// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreResult;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreResult;

import java.time.Duration;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS ARE USED TO EXTRACT
 * APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING LINE NUMBERS OF EXISTING CODE
 * SAMPLES.
 *
 * Class containing code snippets that will be injected to README.md.
 */
public class ReadmeSamples {
    private final KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClient(null);
    private final KeyVaultAccessControlAsyncClient keyVaultAccessControlAsyncClient = null;
    private final KeyVaultBackupClient keyVaultBackupClient = new KeyVaultBackupClient(null);
    private final KeyVaultBackupAsyncClient keyVaultBackupAsyncClient = null;

    /**
     * Code sample for creating a {@link KeyVaultAccessControlClient}.
     */
    public void createAccessControlClient() {
        KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
            .vaultUrl("<your-key-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    }

    /**
     * Code sample for listing {@link KeyVaultRoleDefinition role definitions}.
     */
    public void listRoleDefinitions() {
        PagedIterable<KeyVaultRoleDefinition> roleDefinitions =
            keyVaultAccessControlClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL);

        roleDefinitions.forEach(roleDefinition ->
            System.out.printf("Retrieved role definition with name '%s'.%n", roleDefinition.getName()));
    }

    /**
     * Code sample for creating or updating a {@link KeyVaultRoleDefinition role definition}.
     */
    public void setRoleDefinition() {
        KeyVaultRoleDefinition roleDefinition = keyVaultAccessControlClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL);

        System.out.printf("Created role definition with randomly generated name '%s' and role name '%s'.%n",
            roleDefinition.getName(), roleDefinition.getRoleName());
    }

    /**
     * Code sample for getting a {@link KeyVaultRoleDefinition role definition}.
     */
    public void getRoleDefinition() {
        String roleDefinitionName = "<role-definition-name>";
        KeyVaultRoleDefinition roleDefinition =
            keyVaultAccessControlClient.getRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

        System.out.printf("Retrieved role definition with name '%s' and role name '%s'.%n", roleDefinition.getName(),
            roleDefinition.getRoleName());
    }

    /**
     * Code sample for deleting a {@link KeyVaultRoleDefinition role definition}.
     */
    public void deleteRoleDefinition() {
        String roleDefinitionName = "<role-definition-name>";

        keyVaultAccessControlClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

        System.out.printf("Deleted role definition with name '%s'.%n", roleDefinitionName);
    }

    /**
     * Code sample for listing {@link KeyVaultRoleAssignment role assignments}.
     */
    public void listRoleAssignments() {
        PagedIterable<KeyVaultRoleAssignment> roleAssignments =
            keyVaultAccessControlClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL);

        roleAssignments.forEach(roleAssignment ->
            System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
    }

    /**
     * Code sample for creating a {@link KeyVaultRoleAssignment role assignment}.
     */
    public void createRoleAssignment() {
        String roleDefinitionId = "<role-definition-id>";
        String servicePrincipalId = "<service-principal-id>";
        KeyVaultRoleAssignment roleAssignment =
            keyVaultAccessControlClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinitionId,
                servicePrincipalId);

        System.out.printf("Created role assignment with randomly generated name '%s' for principal with id '%s'.%n",
            roleAssignment.getName(), roleAssignment.getProperties().getPrincipalId());
    }

    /**
     * Code sample for getting a {@link KeyVaultRoleAssignment role assignment}.
     */
    public void getRoleAssignment() {
        String roleAssignmentName = "<role-assignment-name>";
        KeyVaultRoleAssignment roleAssignment =
            keyVaultAccessControlClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

        System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName());
    }

    /**
     * Code sample for deleting a {@link KeyVaultRoleAssignment role assignment}.
     */
    public void deleteRoleAssignment() {
        String roleAssignmentName = "<role-assignment-name>";

        keyVaultAccessControlClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

        System.out.printf("Deleted role assignment with name '%s'.%n", roleAssignmentName);
    }

    /**
     * Code sample for listing {@link KeyVaultRoleDefinition role definitions} asynchronously.
     */
    public void listRoleDefinitionsAsync() {
        keyVaultAccessControlAsyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL)
            .subscribe(roleDefinition ->
                System.out.printf("Retrieved role definition with name '%s'.%n", roleDefinition.getName()));
    }

    /**
     * Code sample for creating or updating a {@link KeyVaultRoleDefinition role definition} asynchronously.
     */
    public void setRoleDefinitionAsync() {
        keyVaultAccessControlAsyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL)
            .subscribe(roleDefinition ->
                System.out.printf("Created role definition with randomly generated name '%s' and role name '%s'.%n",
                    roleDefinition.getName(), roleDefinition.getRoleName()));
    }

    /**
     * Code sample for getting a {@link KeyVaultRoleDefinition role definition} asynchronously.
     */
    public void getRoleDefinitionAsync() {
        String roleDefinitionName = "<role-definition-name>";

        keyVaultAccessControlAsyncClient.getRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
            .subscribe(roleDefinition ->
                System.out.printf("Retrieved role definition with name '%s' and role name '%s'.%n",
                    roleDefinition.getName(), roleDefinition.getRoleName()));
    }

    /**
     * Code sample for deleting a {@link KeyVaultRoleDefinition role definition} asynchronously.
     */
    public void deleteRoleDefinitionAsync() {
        String roleDefinitionName = "<role-definition-name>";

        keyVaultAccessControlAsyncClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
            .subscribe(unused -> System.out.printf("Deleted role definition with name '%s'.%n", roleDefinitionName));
    }

    /**
     * Code sample for listing {@link KeyVaultRoleAssignment role assignments} asynchronously.
     */
    public void listRoleAssignmentsAsync() {
        keyVaultAccessControlAsyncClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL)
            .subscribe(roleAssignment ->
                System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
    }

    /**
     * Code sample for creating a {@link KeyVaultRoleAssignment role assignment} asynchronously.
     */
    public void createRoleAssignmentAsync() {
        String roleDefinitionId = "<role-definition-id>";
        String servicePrincipalId = "<service-principal-id>";

        keyVaultAccessControlAsyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinitionId,
            servicePrincipalId).subscribe(roleAssignment ->
                System.out.printf("Created role assignment with randomly generated name '%s' for principal with id"
                    + "'%s'.%n", roleAssignment.getName(), roleAssignment.getProperties().getPrincipalId()));
    }

    /**
     * Code sample for getting a {@link KeyVaultRoleAssignment role assignment} asynchronously.
     */
    public void getRoleAssignmentAsync() {
        String roleAssignmentName = "<role-assignment-name>";

        keyVaultAccessControlAsyncClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName)
            .subscribe(roleAssignment ->
                System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
    }

    /**
     * Code sample for deleting a {@link KeyVaultRoleAssignment role assignment} asynchronously.
     */
    public void deleteRoleAssignmentAsync() {
        String roleAssignmentName = "<role-assignment-name>";

        keyVaultAccessControlAsyncClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName)
            .subscribe(unused ->
                System.out.printf("Deleted role assignment with name '%s'.%n", roleAssignmentName));
    }

    /**
     * Code sample for creating a {@link KeyVaultBackupClient}.
     */
    public void createBackupClient() {
        KeyVaultBackupClient keyVaultBackupClient = new KeyVaultBackupClientBuilder()
            .vaultUrl("<your-key-vault-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    }

    /**
     * Code sample for starting a {@link KeyVaultBackupOperation backup operation}.
     */
    public void beginBackup() {
        String blobStorageUrl = "https://myaccount.blob.core.windows.net/myContainer";
        String sasToken = "sv=2020-02-10&ss=b&srt=o&sp=rwdlactfx&se=2021-06-17T07:13:07Z&st=2021-06-16T23:13:07Z&spr=https&sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D";

        SyncPoller<KeyVaultBackupOperation, String> backupPoller =
            keyVaultBackupClient.beginBackup(blobStorageUrl, sasToken);

        PollResponse<KeyVaultBackupOperation> pollResponse = backupPoller.poll();

        System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus());

        PollResponse<KeyVaultBackupOperation> finalPollResponse = backupPoller.waitForCompletion();

        if (finalPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            String folderUrl = backupPoller.getFinalResult();

            System.out.printf("Backup completed. The storage location of this backup is: %s.%n", folderUrl);
        } else {
            KeyVaultBackupOperation operation = backupPoller.poll().getValue();

            System.out.printf("Backup failed with error: %s.%n", operation.getError().getMessage());
        }
    }

    /**
     * Code sample for starting a {@link KeyVaultRestoreOperation restore operation}.
     */
    public void beginRestore() {
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "sv=2020-02-10&ss=b&srt=o&sp=rwdlactfx&se=2021-06-17T07:13:07Z&st=2021-06-16T23:13:07Z&spr=https&sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D";

        SyncPoller<KeyVaultRestoreOperation, KeyVaultRestoreResult> backupPoller =
            keyVaultBackupClient.beginRestore(folderUrl, sasToken);

        PollResponse<KeyVaultRestoreOperation> pollResponse = backupPoller.poll();

        System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus());

        PollResponse<KeyVaultRestoreOperation> finalPollResponse = backupPoller.waitForCompletion();

        if (finalPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            System.out.printf("Backup restored successfully.%n");
        } else {
            KeyVaultRestoreOperation operation = backupPoller.poll().getValue();

            System.out.printf("Restore failed with error: %s.%n", operation.getError().getMessage());
        }
    }

    /**
     * Code sample for starting a {@link KeyVaultSelectiveKeyRestoreOperation selective key restore operation}.
     */
    public void beginSelectiveKeyRestore() {
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "sv=2020-02-10&ss=b&srt=o&sp=rwdlactfx&se=2021-06-17T07:13:07Z&st=2021-06-16T23:13:07Z&spr=https&sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D";
        String keyName = "myKey";

        SyncPoller<KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult> backupPoller =
            keyVaultBackupClient.beginSelectiveKeyRestore(folderUrl, sasToken, keyName);

        PollResponse<KeyVaultSelectiveKeyRestoreOperation> pollResponse = backupPoller.poll();

        System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus());

        PollResponse<KeyVaultSelectiveKeyRestoreOperation> finalPollResponse = backupPoller.waitForCompletion();

        if (finalPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            System.out.printf("Key restored successfully.%n");
        } else {
            KeyVaultSelectiveKeyRestoreOperation operation = backupPoller.poll().getValue();

            System.out.printf("Key restore failed with error: %s.%n", operation.getError().getMessage());
        }
    }

    /**
     * Code sample for starting a {@link KeyVaultBackupOperation backup operation} asynchronously.
     */
    public void beginBackupAsync() {
        String blobStorageUrl = "https://myaccount.blob.core.windows.net/myContainer";
        String sasToken = "sv=2020-02-10&ss=b&srt=o&sp=rwdlactfx&se=2021-06-17T07:13:07Z&st=2021-06-16T23:13:07Z&spr=https&sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D";

        keyVaultBackupAsyncClient.beginBackup(blobStorageUrl, sasToken)
            .setPollInterval(Duration.ofSeconds(1)) // You can set a custom polling interval.
            .doOnError(e -> System.out.printf("Backup failed with error: %s.%n", e.getMessage()))
            .doOnNext(pollResponse ->
                System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus()))
            .filter(pollResponse -> pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(folderUrl ->
                System.out.printf("Backup completed. The storage location of this backup is: %s.%n", folderUrl));
    }

    /**
     * Code sample for starting a {@link KeyVaultRestoreOperation restore operation} asynchronously.
     */
    public void beginRestoreAsync() {
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "sv=2020-02-10&ss=b&srt=o&sp=rwdlactfx&se=2021-06-17T07:13:07Z&st=2021-06-16T23:13:07Z&spr=https&sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D";

        keyVaultBackupAsyncClient.beginRestore(folderUrl, sasToken)
            .setPollInterval(Duration.ofSeconds(1)) // You can set a custom polling interval.
            .doOnError(e -> System.out.printf("Restore failed with error: %s.%n", e.getMessage()))
            .doOnNext(pollResponse ->
                System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus()))
            .filter(pollResponse -> pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(unused -> System.out.printf("Backup restored successfully.%n"));
    }

    /**
     * Code sample for starting a {@link KeyVaultSelectiveKeyRestoreOperation selective key restore operation}
     * asynchronously.
     */
    public void beginSelectiveKeyRestoreAsync() {
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "sv=2020-02-10&ss=b&srt=o&sp=rwdlactfx&se=2021-06-17T07:13:07Z&st=2021-06-16T23:13:07Z&spr=https&sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D";
        String keyName = "myKey";

        keyVaultBackupAsyncClient.beginSelectiveKeyRestore(folderUrl, sasToken, keyName)
            .setPollInterval(Duration.ofSeconds(1)) // You can set a custom polling interval.
            .doOnError(e -> System.out.printf("Key restoration failed with error: %s.%n", e.getMessage()))
            .doOnNext(pollResponse ->
                System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus()))
            .filter(pollResponse -> pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(unused -> System.out.printf("Key restored successfully.%n"));
    }
}
