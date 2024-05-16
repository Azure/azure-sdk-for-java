// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.polling.AsyncPollResponse;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.security.keyvault.administration.models.KeyVaultGetSettingsResult;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultRestoreResult;
import com.azure.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import com.azure.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreResult;
import com.azure.security.keyvault.administration.models.KeyVaultSetting;

import java.time.Duration;

/**
 * Class containing code snippets that will be injected to README.md.
 */
@SuppressWarnings("unused")
public class ReadmeSamples {
    private final KeyVaultAccessControlClient keyVaultAccessControlClient =
        new KeyVaultAccessControlClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    private final KeyVaultAccessControlAsyncClient keyVaultAccessControlAsyncClient =
        new KeyVaultAccessControlClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();
    private final KeyVaultBackupClient keyVaultBackupClient =
        new KeyVaultBackupClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    private final KeyVaultBackupAsyncClient keyVaultBackupAsyncClient =
        new KeyVaultBackupClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

    private final KeyVaultSettingsClient keyVaultSettingsClient =
        new KeyVaultSettingsClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

    private final KeyVaultSettingsAsyncClient keyVaultSettingsAsyncClient =
        new KeyVaultSettingsClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildAsyncClient();

    /**
     * Code sample for creating a {@link KeyVaultAccessControlClient}.
     */
    public void createAccessControlClient() {
        // BEGIN: readme-sample-createAccessControlClient
        KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: readme-sample-createAccessControlClient
    }

    /**
     * Code sample for listing {@link KeyVaultRoleDefinition role definitions}.
     */
    public void listRoleDefinitions() {
        // BEGIN: readme-sample-listRoleDefinitions
        PagedIterable<KeyVaultRoleDefinition> roleDefinitions =
            keyVaultAccessControlClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL);

        roleDefinitions.forEach(roleDefinition ->
            System.out.printf("Retrieved role definition with name '%s'.%n", roleDefinition.getName()));
        // END: readme-sample-listRoleDefinitions
    }

    /**
     * Code sample for creating or updating a {@link KeyVaultRoleDefinition role definition}.
     */
    public void setRoleDefinition() {
        // BEGIN: readme-sample-setRoleDefinition
        KeyVaultRoleDefinition roleDefinition = keyVaultAccessControlClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL);

        System.out.printf("Created role definition with randomly generated name '%s' and role name '%s'.%n",
            roleDefinition.getName(), roleDefinition.getRoleName());
        // END: readme-sample-setRoleDefinition
    }

    /**
     * Code sample for getting a {@link KeyVaultRoleDefinition role definition}.
     */
    public void getRoleDefinition() {
        // BEGIN: readme-sample-getRoleDefinition
        String roleDefinitionName = "<role-definition-name>";
        KeyVaultRoleDefinition roleDefinition =
            keyVaultAccessControlClient.getRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

        System.out.printf("Retrieved role definition with name '%s' and role name '%s'.%n", roleDefinition.getName(),
            roleDefinition.getRoleName());
        // END: readme-sample-getRoleDefinition
    }

    /**
     * Code sample for deleting a {@link KeyVaultRoleDefinition role definition}.
     */
    public void deleteRoleDefinition() {
        // BEGIN: readme-sample-deleteRoleDefinition
        String roleDefinitionName = "<role-definition-name>";

        keyVaultAccessControlClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

        System.out.printf("Deleted role definition with name '%s'.%n", roleDefinitionName);
        // END: readme-sample-deleteRoleDefinition
    }

    /**
     * Code sample for listing {@link KeyVaultRoleAssignment role assignments}.
     */
    public void listRoleAssignments() {
        // BEGIN: readme-sample-listRoleAssignments
        PagedIterable<KeyVaultRoleAssignment> roleAssignments =
            keyVaultAccessControlClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL);

        roleAssignments.forEach(roleAssignment ->
            System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
        // END: readme-sample-listRoleAssignments
    }

    /**
     * Code sample for creating a {@link KeyVaultRoleAssignment role assignment}.
     */
    public void createRoleAssignment() {
        // BEGIN: readme-sample-createRoleAssignment
        String roleDefinitionId = "<role-definition-id>";
        String servicePrincipalId = "<service-principal-id>";
        KeyVaultRoleAssignment roleAssignment =
            keyVaultAccessControlClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinitionId,
                servicePrincipalId);

        System.out.printf("Created role assignment with randomly generated name '%s' for principal with id '%s'.%n",
            roleAssignment.getName(), roleAssignment.getProperties().getPrincipalId());
        // END: readme-sample-createRoleAssignment
    }

    /**
     * Code sample for getting a {@link KeyVaultRoleAssignment role assignment}.
     */
    public void getRoleAssignment() {
        // BEGIN: readme-sample-getRoleAssignment
        String roleAssignmentName = "<role-assignment-name>";
        KeyVaultRoleAssignment roleAssignment =
            keyVaultAccessControlClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

        System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName());
        // END: readme-sample-getRoleAssignment
    }

    /**
     * Code sample for deleting a {@link KeyVaultRoleAssignment role assignment}.
     */
    public void deleteRoleAssignment() {
        // BEGIN: readme-sample-deleteRoleAssignment
        String roleAssignmentName = "<role-assignment-name>";

        keyVaultAccessControlClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

        System.out.printf("Deleted role assignment with name '%s'.%n", roleAssignmentName);
        // END: readme-sample-deleteRoleAssignment
    }

    /**
     * Code sample for listing {@link KeyVaultRoleDefinition role definitions} asynchronously.
     */
    public void listRoleDefinitionsAsync() {
        // BEGIN: readme-sample-listRoleDefinitionsAsync
        keyVaultAccessControlAsyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL)
            .subscribe(roleDefinition ->
                System.out.printf("Retrieved role definition with name '%s'.%n", roleDefinition.getName()));
        // END: readme-sample-listRoleDefinitionsAsync
    }

    /**
     * Code sample for creating or updating a {@link KeyVaultRoleDefinition role definition} asynchronously.
     */
    public void setRoleDefinitionAsync() {
        // BEGIN: readme-sample-setRoleDefinitionAsync
        keyVaultAccessControlAsyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL)
            .subscribe(roleDefinition ->
                System.out.printf("Created role definition with randomly generated name '%s' and role name '%s'.%n",
                    roleDefinition.getName(), roleDefinition.getRoleName()));
        // END: readme-sample-setRoleDefinitionAsync
    }

    /**
     * Code sample for getting a {@link KeyVaultRoleDefinition role definition} asynchronously.
     */
    public void getRoleDefinitionAsync() {
        // BEGIN: readme-sample-getRoleDefinitionAsync
        String roleDefinitionName = "<role-definition-name>";

        keyVaultAccessControlAsyncClient.getRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
            .subscribe(roleDefinition ->
                System.out.printf("Retrieved role definition with name '%s' and role name '%s'.%n",
                    roleDefinition.getName(), roleDefinition.getRoleName()));
        // END: readme-sample-getRoleDefinitionAsync
    }

    /**
     * Code sample for deleting a {@link KeyVaultRoleDefinition role definition} asynchronously.
     */
    public void deleteRoleDefinitionAsync() {
        // BEGIN: readme-sample-deleteRoleDefinitionAsync
        String roleDefinitionName = "<role-definition-name>";

        keyVaultAccessControlAsyncClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
            .subscribe(unused -> System.out.printf("Deleted role definition with name '%s'.%n", roleDefinitionName));
        // END: readme-sample-deleteRoleDefinitionAsync
    }

    /**
     * Code sample for listing {@link KeyVaultRoleAssignment role assignments} asynchronously.
     */
    public void listRoleAssignmentsAsync() {
        // BEGIN: readme-sample-listRoleAssignmentsAsync
        keyVaultAccessControlAsyncClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL)
            .subscribe(roleAssignment ->
                System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
        // END: readme-sample-listRoleAssignmentsAsync
    }

    /**
     * Code sample for creating a {@link KeyVaultRoleAssignment role assignment} asynchronously.
     */
    public void createRoleAssignmentAsync() {
        // BEGIN: readme-sample-createRoleAssignmentAsync
        String roleDefinitionId = "<role-definition-id>";
        String servicePrincipalId = "<service-principal-id>";

        keyVaultAccessControlAsyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinitionId,
            servicePrincipalId).subscribe(roleAssignment ->
                System.out.printf("Created role assignment with randomly generated name '%s' for principal with id"
                    + "'%s'.%n", roleAssignment.getName(), roleAssignment.getProperties().getPrincipalId()));
        // END: readme-sample-createRoleAssignmentAsync
    }

    /**
     * Code sample for getting a {@link KeyVaultRoleAssignment role assignment} asynchronously.
     */
    public void getRoleAssignmentAsync() {
        // BEGIN: readme-sample-getRoleAssignmentAsync
        String roleAssignmentName = "<role-assignment-name>";

        keyVaultAccessControlAsyncClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName)
            .subscribe(roleAssignment ->
                System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
        // END: readme-sample-getRoleAssignmentAsync
    }

    /**
     * Code sample for deleting a {@link KeyVaultRoleAssignment role assignment} asynchronously.
     */
    public void deleteRoleAssignmentAsync() {
        // BEGIN: readme-sample-deleteRoleAssignmentAsync
        String roleAssignmentName = "<role-assignment-name>";

        keyVaultAccessControlAsyncClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName)
            .subscribe(unused ->
                System.out.printf("Deleted role assignment with name '%s'.%n", roleAssignmentName));
        // END: readme-sample-deleteRoleAssignmentAsync
    }

    /**
     * Code sample for creating a {@link KeyVaultBackupClient}.
     */
    public void createBackupClient() {
        // BEGIN: readme-sample-createBackupClient
        KeyVaultBackupClient keyVaultBackupClient = new KeyVaultBackupClientBuilder()
            .vaultUrl("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: readme-sample-createBackupClient
    }

    /**
     * Code sample for starting a {@link KeyVaultBackupOperation pre-backup check}.
     */
    public void beginPreBackup() {
        // BEGIN: readme-sample-beginPreBackup
        String blobStorageUrl = "https://myaccount.blob.core.windows.net/myContainer";
        String sasToken = "<sas-token>";

        SyncPoller<KeyVaultBackupOperation, String> preBackupPoller =
            keyVaultBackupClient.beginPreBackup(blobStorageUrl, sasToken);
        PollResponse<KeyVaultBackupOperation> pollResponse = preBackupPoller.poll();

        System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus());

        PollResponse<KeyVaultBackupOperation> finalPollResponse = preBackupPoller.waitForCompletion();

        if (finalPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            String folderUrl = preBackupPoller.getFinalResult();

            System.out.printf("Pre-backup check completed successfully.%n");
        } else {
            KeyVaultBackupOperation operation = preBackupPoller.poll().getValue();

            System.out.printf("Pre-backup check failed with error: %s.%n", operation.getError().getMessage());
        }
        // END: readme-sample-beginPreBackup
    }

    /**
     * Code sample for starting a {@link KeyVaultBackupOperation backup operation}.
     */
    public void beginBackup() {
        // BEGIN: readme-sample-beginBackup
        String blobStorageUrl = "https://myaccount.blob.core.windows.net/myContainer";
        String sasToken = "<sas-token>";

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
        // END: readme-sample-beginBackup
    }

    /**
     * Code sample for starting a {@link KeyVaultRestoreOperation pre-restore check}.
     */
    public void beginPreRestore() {
        // BEGIN: readme-sample-beginPreRestore
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "<sas-token>";

        SyncPoller<KeyVaultRestoreOperation, KeyVaultRestoreResult> preRestorePoller =
            keyVaultBackupClient.beginPreRestore(folderUrl, sasToken);
        PollResponse<KeyVaultRestoreOperation> pollResponse = preRestorePoller.poll();

        System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus());

        PollResponse<KeyVaultRestoreOperation> finalPollResponse = preRestorePoller.waitForCompletion();

        if (finalPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            System.out.printf("Pre-restore check completed successfully.%n");
        } else {
            KeyVaultRestoreOperation operation = preRestorePoller.poll().getValue();

            System.out.printf("Pre-restore check failed with error: %s.%n", operation.getError().getMessage());
        }
        // END: readme-sample-beginPreRestore
    }

    /**
     * Code sample for starting a {@link KeyVaultRestoreOperation restore operation}.
     */
    public void beginRestore() {
        // BEGIN: readme-sample-beginRestore
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "<sas-token>";

        SyncPoller<KeyVaultRestoreOperation, KeyVaultRestoreResult> restorePoller =
            keyVaultBackupClient.beginRestore(folderUrl, sasToken);
        PollResponse<KeyVaultRestoreOperation> pollResponse = restorePoller.poll();

        System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus());

        PollResponse<KeyVaultRestoreOperation> finalPollResponse = restorePoller.waitForCompletion();

        if (finalPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            System.out.printf("Backup restored successfully.%n");
        } else {
            KeyVaultRestoreOperation operation = restorePoller.poll().getValue();

            System.out.printf("Restore failed with error: %s.%n", operation.getError().getMessage());
        }
        // END: readme-sample-beginRestore
    }

    /**
     * Code sample for starting a {@link KeyVaultSelectiveKeyRestoreOperation selective key restore operation}.
     */
    public void beginSelectiveKeyRestore() {
        // BEGIN: readme-sample-beginSelectiveKeyRestore
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "<sas-token>";
        String keyName = "myKey";

        SyncPoller<KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult> restorePoller =
            keyVaultBackupClient.beginSelectiveKeyRestore(folderUrl, sasToken, keyName);
        PollResponse<KeyVaultSelectiveKeyRestoreOperation> pollResponse = restorePoller.poll();

        System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus());

        PollResponse<KeyVaultSelectiveKeyRestoreOperation> finalPollResponse = restorePoller.waitForCompletion();

        if (finalPollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED) {
            System.out.printf("Key restored successfully.%n");
        } else {
            KeyVaultSelectiveKeyRestoreOperation operation = restorePoller.poll().getValue();

            System.out.printf("Key restore failed with error: %s.%n", operation.getError().getMessage());
        }
        // END: readme-sample-beginSelectiveKeyRestore
    }

    /**
     * Code sample for starting a {@link KeyVaultBackupOperation pre-backup check} asynchronously.
     */
    public void beginPreBackupAsync() {
        // BEGIN: readme-sample-beginPreBackupAsync
        String blobStorageUrl = "https://myaccount.blob.core.windows.net/myContainer";
        String sasToken = "<sas-token>";

        keyVaultBackupAsyncClient.beginPreBackup(blobStorageUrl, sasToken)
            .setPollInterval(Duration.ofSeconds(1)) // You can set a custom polling interval.
            .doOnError(e -> System.out.printf("Pre-backup check failed with error: %s.%n", e.getMessage()))
            .doOnNext(pollResponse ->
                System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus()))
            .filter(pollResponse -> pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(folderUrl ->
                System.out.printf("Pre-backup check completed successfully.%n"));
        // END: readme-sample-beginPreBackupAsync
    }

    /**
     * Code sample for starting a {@link KeyVaultBackupOperation backup operation} asynchronously.
     */
    public void beginBackupAsync() {
        // BEGIN: readme-sample-beginBackupAsync
        String blobStorageUrl = "https://myaccount.blob.core.windows.net/myContainer";
        String sasToken = "<sas-token>";

        keyVaultBackupAsyncClient.beginBackup(blobStorageUrl, sasToken)
            .setPollInterval(Duration.ofSeconds(1)) // You can set a custom polling interval.
            .doOnError(e -> System.out.printf("Backup failed with error: %s.%n", e.getMessage()))
            .doOnNext(pollResponse ->
                System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus()))
            .filter(pollResponse -> pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(folderUrl ->
                System.out.printf("Backup completed. The storage location of this backup is: %s.%n", folderUrl));
        // END: readme-sample-beginBackupAsync
    }

    /**
     * Code sample for starting a {@link KeyVaultRestoreOperation pre-restore check} asynchronously.
     */
    public void beginPreRestoreAsync() {
        // BEGIN: readme-sample-beginPreRestoreAsync
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "<sas-token>";

        keyVaultBackupAsyncClient.beginPreRestore(folderUrl, sasToken)
            .setPollInterval(Duration.ofSeconds(1)) // You can set a custom polling interval.
            .doOnError(e -> System.out.printf("Pre-restore check failed with error: %s.%n", e.getMessage()))
            .doOnNext(pollResponse ->
                System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus()))
            .filter(pollResponse -> pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(unused -> System.out.printf("Pre-restore check completed successfully.%n"));
        // END: readme-sample-beginPreRestoreAsync
    }

    /**
     * Code sample for starting a {@link KeyVaultRestoreOperation restore operation} asynchronously.
     */
    public void beginRestoreAsync() {
        // BEGIN: readme-sample-beginRestoreAsync
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "<sas-token>";

        keyVaultBackupAsyncClient.beginRestore(folderUrl, sasToken)
            .setPollInterval(Duration.ofSeconds(1)) // You can set a custom polling interval.
            .doOnError(e -> System.out.printf("Restore failed with error: %s.%n", e.getMessage()))
            .doOnNext(pollResponse ->
                System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus()))
            .filter(pollResponse -> pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(unused -> System.out.printf("Backup restored successfully.%n"));
        // END: readme-sample-beginRestoreAsync
    }

    /**
     * Code sample for starting a {@link KeyVaultSelectiveKeyRestoreOperation selective key restore operation}
     * asynchronously.
     */
    public void beginSelectiveKeyRestoreAsync() {
        // BEGIN: readme-sample-beginSelectiveKeyRestoreAsync
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "<sas-token>";
        String keyName = "myKey";

        keyVaultBackupAsyncClient.beginSelectiveKeyRestore(folderUrl, sasToken, keyName)
            .setPollInterval(Duration.ofSeconds(1)) // You can set a custom polling interval.
            .doOnError(e -> System.out.printf("Key restoration failed with error: %s.%n", e.getMessage()))
            .doOnNext(pollResponse ->
                System.out.printf("The current status of the operation is: %s.%n", pollResponse.getStatus()))
            .filter(pollResponse -> pollResponse.getStatus() == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED)
            .flatMap(AsyncPollResponse::getFinalResult)
            .subscribe(unused -> System.out.printf("Key restored successfully.%n"));
        // END: readme-sample-beginSelectiveKeyRestoreAsync
    }

    public void troubleshooting() {
        // BEGIN: readme-sample-troubleshooting
        try {
            keyVaultAccessControlClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, "<role-assginment-name>");
        } catch (HttpResponseException e) {
            System.out.println(e.getMessage());
        }
        // END: readme-sample-troubleshooting
    }

    /**
     * Code sample for updating a {@link KeyVaultSetting setting}.
     */
    public void updateSetting() {
        // BEGIN: readme-sample-updateSetting
        String settingName = "<setting-to-update>";
        KeyVaultSetting settingToUpdate = new KeyVaultSetting(settingName, true);
        KeyVaultSetting updatedSetting = keyVaultSettingsClient.updateSetting(settingToUpdate);

        System.out.printf("Updated setting '%s' to '%s'.%n", updatedSetting.getName(), updatedSetting.asBoolean());
        // END: readme-sample-updateSetting
    }

    /**
     * Code sample for retrieving a {@link KeyVaultSetting setting}.
     */
    public void getSetting() {
        // BEGIN: readme-sample-getSetting
        String settingName = "<setting-to-get>";
        KeyVaultSetting setting = keyVaultSettingsClient.getSetting(settingName);

        System.out.printf("Retrieved setting '%s' with value '%s'.%n", setting.getName(),
            setting.asBoolean());
        // END: readme-sample-getSetting
    }

    /**
     * Code sample for retrieving an account's {@link KeyVaultSetting settings}.
     */
    public void getSettings() {
        // BEGIN: readme-sample-getSettings
        KeyVaultGetSettingsResult getSettingsResult = keyVaultSettingsClient.getSettings();

        for (KeyVaultSetting setting : getSettingsResult.getSettings()) {
            System.out.printf("Retrieved setting '%s' with value '%s'.%n", setting.getName(), setting.asBoolean());
        }
        // END: readme-sample-getSettings
    }

    /**
     * Code sample for updating a {@link KeyVaultSetting setting} asynchronously.
     */
    public void updateSettingAsync() {
        // BEGIN: readme-sample-updateSettingAsync
        String settingName = "<setting-to-update>";
        KeyVaultSetting settingToUpdate = new KeyVaultSetting(settingName, true);

        keyVaultSettingsAsyncClient.updateSetting(settingToUpdate)
            .subscribe(updatedSetting ->
                System.out.printf("Updated setting with name '%s' and value '%s'.%n", updatedSetting.getName(),
                    updatedSetting.asBoolean()));
        // END: readme-sample-updateSettingAsync
    }

    /**
     * Code sample for retrieving a {@link KeyVaultSetting setting} asynchronously.
     */
    public void getSettingAsync() {
        // BEGIN: readme-sample-getSettingAsync
        String settingName = "<setting-to-get>";

        keyVaultSettingsAsyncClient.getSetting(settingName)
            .subscribe(setting ->
                System.out.printf("Retrieved setting with name '%s' and value '%s'.%n", setting.getName(),
                    setting.asBoolean()));
        // END: readme-sample-getSettingAsync
    }

    /**
     * Code sample for retrieving an account's {@link KeyVaultSetting settings} asynchronously.
     */
    public void getSettingsAsync() {
        // BEGIN: readme-sample-getSettingsAsync
        keyVaultSettingsAsyncClient.getSettings()
            .subscribe(settingsResult ->
                settingsResult.getSettings().forEach(setting ->
                    System.out.printf("Retrieved setting with name '%s' and value '%s'.%n", setting.getName(),
                        setting.asBoolean())));
        // END: readme-sample-getSettingsAsync
    }
}
