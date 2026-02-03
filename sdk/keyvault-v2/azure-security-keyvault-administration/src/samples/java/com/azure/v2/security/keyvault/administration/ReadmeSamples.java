// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.administration;

import com.azure.v2.core.http.polling.LongRunningOperationStatus;
import com.azure.v2.core.http.polling.PollResponse;
import com.azure.v2.core.http.polling.Poller;
import com.azure.v2.identity.DefaultAzureCredentialBuilder;
import com.azure.v2.security.keyvault.administration.models.KeyVaultBackupOperation;
import com.azure.v2.security.keyvault.administration.models.KeyVaultGetSettingsResult;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRestoreOperation;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRestoreResult;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleAssignment;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleDefinition;
import com.azure.v2.security.keyvault.administration.models.KeyVaultRoleScope;
import com.azure.v2.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreOperation;
import com.azure.v2.security.keyvault.administration.models.KeyVaultSelectiveKeyRestoreResult;
import com.azure.v2.security.keyvault.administration.models.KeyVaultSetting;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.paging.PagedIterable;

/**
 * Class containing code snippets that will be injected to README.md.
 */
@SuppressWarnings("unused")
public class ReadmeSamples {
    private final KeyVaultAccessControlClient keyVaultAccessControlClient =
        new KeyVaultAccessControlClientBuilder()
            .endpoint("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    private final KeyVaultBackupClient keyVaultBackupClient =
        new KeyVaultBackupClientBuilder()
            .endpoint("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

    private final KeyVaultSettingsClient keyVaultSettingsClient =
        new KeyVaultSettingsClientBuilder()
            .endpoint("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

    /**
     * Code sample for creating a {@link KeyVaultAccessControlClient}.
     */
    public void createAccessControlClient() {
        // BEGIN: readme-sample-createAccessControlClient
        KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
            .endpoint("<your-managed-hsm-url>")
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
     * Code sample for creating a {@link KeyVaultBackupClient}.
     */
    public void createBackupClient() {
        // BEGIN: readme-sample-createBackupClient
        KeyVaultBackupClient keyVaultBackupClient = new KeyVaultBackupClientBuilder()
            .endpoint("<your-managed-hsm-url>")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
        // END: readme-sample-createBackupClient
    }

    /**
     * Code sample for starting a {@link KeyVaultBackupOperation backup operation}.
     */
    public void beginBackup() {
        // BEGIN: readme-sample-beginBackup
        String blobStorageUrl = "https://myaccount.blob.core.windows.net/myContainer";
        String sasToken = "<sas-token>";

        Poller<KeyVaultBackupOperation, String> backupPoller =
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
     * Code sample for starting a {@link KeyVaultRestoreOperation restore operation}.
     */
    public void beginRestore() {
        // BEGIN: readme-sample-beginRestore
        String folderUrl = "https://myaccount.blob.core.windows.net/myContainer/mhsm-myaccount-2020090117323313";
        String sasToken = "<sas-token>";

        Poller<KeyVaultRestoreOperation, KeyVaultRestoreResult> restorePoller =
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

        Poller<KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult> restorePoller =
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

    public void troubleshooting() {
        // BEGIN: readme-sample-troubleshooting
        try {
            keyVaultAccessControlClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, "<role-assignment-name>");
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
}
