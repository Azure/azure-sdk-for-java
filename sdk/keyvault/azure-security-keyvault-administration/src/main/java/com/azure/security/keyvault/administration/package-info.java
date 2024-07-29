// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
/**
 * <a href="https://learn.microsoft.com/azure/key-vault/managed-hsm/">Azure Key Vault Managed HSM</a> is a
 * fully-managed, highly-available, single-tenant, standards-compliant cloud service that enables you to safeguard
 * cryptographic keys for your cloud applications using FIPS 140-2 Level 3 validated HSMs.
 *
 * <p>
 * The Azure Key Vault Administration client library allows developers to interact with the Azure Key Vault Managed
 * HSM service from their applications. The library provides a set of APIs that enable developers to perform
 * administrative tasks such as full backup/restore, key-level role-based access control (RBAC), and account settings
 * management.
 *
 * <p>
 * <strong>Key Concepts:</strong>
 *
 * <p>
 * <strong>What is a Key Vault Access Control Client?</strong>
 * <p>
 * The Key Vault Access Control client performs the interactions with the Azure Key Vault service for getting,
 * setting, deleting, and listing role assignments, as well as listing role definitions. Asynchronous
 * (KeyVaultAccessControlAsyncClient) and synchronous (KeyVaultAccessControlClient) clients exist in the SDK allowing
 * for the selection of a client based on an application's use case. Once you've initialized a role assignment, you can
 * interact with the primary resource types in Key Vault.
 *
 * <p>
 * <strong>What is a Role Definition?</strong>
 * <p>
 * A role definition is a collection of permissions. It defines the operations that can be performed, such as read,
 * write, and delete. It can also define the operations that are excluded from allowed operations.
 *
 * <p>
 * Role definitions can be listed and specified as part of a role assignment.
 *
 * <p>
 * <strong>What is a Role Assignment?</strong>
 * <p>
 * A role assignment is the association of a role definition to a service principal. They can be created, listed,
 * fetched individually, and deleted.
 *
 * <p>
 * <strong>What is a Key Vault Backup Client</strong>
 * <p>
 * The Key Vault Backup Client provides both synchronous and asynchronous operations for performing full key backups,
 * full key restores, and selective key restores. Asynchronous (KeyVaultBackupAsyncClient) and synchronous
 * (KeyVaultBackupClient) clients exist in the SDK allowing for the selection of a client based on an application's use
 * case.
 *
 * <p>
 * <strong>NOTE:</strong> The backing store for key backups is a blob storage container using Shared Access Signature
 * authentication. For more details on creating a SAS token using the BlobServiceClient, see the <a
 * href="https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/storage/azure-storage-blob#get-credentials">Azure
 * Storage Blobs client README</a>. Alternatively, it is possible to <a
 * href=
 * "https://docs.microsoft.com/azure/vs-azure-tools-storage-manage-with-storage-explorer?tabs=windows#generate-a-shared-access-signature-in-storage-explorer">
 * generate a SAS token in Storage Explorer</a>.
 *
 * <p>
 * <strong>What is a Backup Operation?</strong>
 * <p>
 * A backup operation represents a long-running operation for a full key backup.
 *
 * <p>
 * <strong>What is a Restore Operation</strong>
 * <p>
 * A restore operation represents a long-running operation for both a full key and selective key restore.
 *
 * <p>
 * <strong>What is a Key Vault Settings Client?</strong>
 * <p>
 * The Key Vault Settings client allows manipulation of an Azure Key Vault account's settings, with operations
 * such as: getting, updating, and listing. Asynchronous (KeyVaultSettingsAsyncClient) and synchronous
 * (KeyVaultSettingsClient) clients exist in the SDK allowing for the selection of a client based on an application's
 * use case.
 *
 * <h2>Getting Started</h2>
 *
 * In order to interact with the Azure Key Vault service, you will need to create an instance of the {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient} class, a vault url and a credential
 * object.
 *
 * <p>
 * The examples shown in this document use a credential object named DefaultAzureCredential for authentication, which
 * is appropriate for most scenarios, including local development and production environments. Additionally, we
 * recommend using a <a href="https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/">
 * managed identity</a> for authentication in production environments. You can find more information on different ways
 * of authenticating and their corresponding credential types in the <a
 * href="https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable">Azure Identity
 * documentation"</a>.
 *
 * <p>
 * <strong>Sample: Construct Synchronous Access Control Client</strong>
 *
 * <p>
 * The following code sample demonstrates the creation of a {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient}, using the {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClientBuilder} to configure it.
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlClient.instantiation -->
 * <pre>
 * KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder&#40;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-managed-hsm-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.instantiation -->
 *
 * <p>
 * <strong>Sample: Construct Asynchronous Access Control Client</strong>
 *
 * <p>
 * The following code sample demonstrates the creation of a {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient}, using the {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClientBuilder} to configure it.
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient.instantiation -->
 * <pre>
 * KeyVaultAccessControlAsyncClient keyVaultAccessControlAsyncClient = new KeyVaultAccessControlClientBuilder&#40;&#41;
 *     .vaultUrl&#40;&quot;&lt;your-managed-hsm-url&gt;&quot;&#41;
 *     .credential&#40;new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient.instantiation -->
 * <br>
 * <hr/>
 *
 * <h2>Set a Role Definition</h2>
 *
 * The {@link com.azure.security.keyvault.administration.KeyVaultAccessControlClient} can be used to set a role
 * definition in the key vault.
 *
 * <p>
 * <strong>Code Sample:</strong>
 *
 * <p>
 * The following code sample demonstrates how to asynchronously create a role definition in the key vault, using the
 * {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient#setRoleDefinition(com.azure.security.keyvault.administration.models.KeyVaultRoleScope,
 * java.lang.String) KeyVaultAccessControlClient.setRoleDefinition(KeyVaultRoleScope, String)} API.
 * <!-- src_embed
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope -->
 * <pre>
 * KeyVaultRoleDefinition roleDefinition = keyVaultAccessControlClient.setRoleDefinition&#40;KeyVaultRoleScope.GLOBAL&#41;;
 *
 * System.out.printf&#40;&quot;Created role definition with randomly generated name '%s' and role name '%s'.%n&quot;,
 *     roleDefinition.getName&#40;&#41;, roleDefinition.getRoleName&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultAccessControlClient.setRoleDefinition#KeyVaultRoleScope
 * -->
 *
 * <p>
 * <strong>Note:</strong> For the asynchronous sample, refer to {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient}. <br>
 * <hr/>
 *
 * <h2>Get a Role Definition</h2>
 *
 * The {@link com.azure.security.keyvault.administration.KeyVaultAccessControlClient} can be used to retrieve a role
 * definition from the key vault.
 *
 * <p>
 * <strong>Code Sample:</strong>
 *
 * <p>
 * The following code sample demonstrates how to asynchronously retrieve a role definition from the key vault, using
 * the {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient#getRoleDefinition(com.azure.security.keyvault.administration.models.KeyVaultRoleScope,
 * java.lang.String) KeyVaultAccessControlClient.getRoleDefinition(KeyVaultRoleScope, String)} API.
 * <!-- src_embed
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinition#KeyVaultRoleScope-String -->
 * <pre>
 * String roleDefinitionName = &quot;de8df120-987e-4477-b9cc-570fd219a62c&quot;;
 * KeyVaultRoleDefinition roleDefinition =
 *     keyVaultAccessControlClient.getRoleDefinition&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionName&#41;;
 *
 * System.out.printf&#40;&quot;Retrieved role definition with name '%s' and role name '%s'.%n&quot;, roleDefinition.getName&#40;&#41;,
 *     roleDefinition.getRoleName&#40;&#41;&#41;;
 * </pre>
 * <!-- end
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient.getRoleDefinition#KeyVaultRoleScope-String -->
 *
 * <p>
 * <strong>Note:</strong> For the asynchronous sample, refer to {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient}. <br>
 * <hr/>
 *
 * <h2>Delete a Role Definition</h2>
 *
 * The {@link com.azure.security.keyvault.administration.KeyVaultAccessControlClient} can be used to delete a role
 * definition from the key vault.
 *
 * <p>
 * <strong>Code Sample:</strong>
 *
 * <p>
 * The following code sample demonstrates how to asynchronously delete a role definition from the key vault, using
 * the {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient#deleteRoleDefinition(com.azure.security.keyvault.administration.models.KeyVaultRoleScope,
 * java.lang.String) KeyVaultAccessControlClient.deleteRoleDefinition(KeyVaultRoleScope, String)} API.
 * <!-- src_embed
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinition#KeyVaultRoleScope-String
 * -->
 * <pre>
 * String roleDefinitionName = &quot;6a709e6e-8964-4012-a99b-6b0131e8ce40&quot;;
 *
 * keyVaultAccessControlClient.deleteRoleDefinition&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionName&#41;;
 *
 * System.out.printf&#40;&quot;Deleted role definition with name '%s'.%n&quot;, roleDefinitionName&#41;;
 * </pre>
 * <!-- end
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleDefinition#KeyVaultRoleScope-String
 * -->
 *
 * <p>
 * <strong>Note:</strong> For the asynchronous sample, refer to {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient}. <br>
 * <hr/>
 *
 * <h2>Create a Role Assignment</h2>
 *
 * The {@link com.azure.security.keyvault.administration.KeyVaultAccessControlClient} can be used to set a role
 * assignment in the key vault.
 *
 * <p>
 * <strong>Code Sample:</strong>
 *
 * <p>
 * The following code sample demonstrates how to asynchronously create a role assignment in the key vault, using the
 * {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient#createRoleAssignment(com.azure.security.keyvault.administration.models.KeyVaultRoleScope,
 * java.lang.String, java.lang.String) KeyVaultAccessControlClient.createRoleAssignment(KeyVaultRoleScope, String,
 * String)} API.
 * <!-- src_embed
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String
 * -->
 * <pre>
 * String roleDefinitionId = &quot;b0b43a39-920c-475b-b34c-32ecc2bbb0ea&quot;;
 * String servicePrincipalId = &quot;169d6a86-61b3-4615-ac7e-2da09edfeed4&quot;;
 * KeyVaultRoleAssignment roleAssignment =
 *     keyVaultAccessControlClient.createRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, roleDefinitionId,
 *         servicePrincipalId&#41;;
 *
 * System.out.printf&#40;&quot;Created role assignment with randomly generated name '%s' for principal with id '%s'.%n&quot;,
 *     roleAssignment.getName&#40;&#41;, roleAssignment.getProperties&#40;&#41;.getPrincipalId&#40;&#41;&#41;;
 * </pre>
 * <!-- end
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient.createRoleAssignment#KeyVaultRoleScope-String-String
 * -->
 *
 * <p>
 * <strong>Note:</strong> For the asynchronous sample, refer to {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient}. <br>
 * <hr/>
 *
 * <h2>Get a Role Definition</h2>
 *
 * The {@link com.azure.security.keyvault.administration.KeyVaultAccessControlClient} can be used to retrieve a role
 * definition from the key vault.
 *
 * <p>
 * <strong>Code Sample:</strong>
 *
 * <p>
 * The following code sample demonstrates how to asynchronously retrieve a role definition from the key vault, using
 * the {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient#getRoleDefinition(com.azure.security.keyvault.administration.models.KeyVaultRoleScope,
 * java.lang.String) KeyVaultAccessControlClient.getRoleDefinition(KeyVaultRoleScope, String)} API.
 * <!-- src_embed
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignment#KeyVaultRoleScope-String -->
 * <pre>
 * String roleAssignmentName = &quot;06d1ae8b-0791-4f02-b976-f631251f5a95&quot;;
 * KeyVaultRoleAssignment roleAssignment =
 *     keyVaultAccessControlClient.getRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, roleAssignmentName&#41;;
 *
 * System.out.printf&#40;&quot;Retrieved role assignment with name '%s'.%n&quot;, roleAssignment.getName&#40;&#41;&#41;;
 * </pre>
 * <!-- end
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient.getRoleAssignment#KeyVaultRoleScope-String -->
 *
 * <p>
 * <strong>Note:</strong> For the asynchronous sample, refer to {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient}. <br>
 * <hr/>
 *
 * <h2>Delete a Role Definition</h2>
 *
 * The {@link com.azure.security.keyvault.administration.KeyVaultAccessControlClient} can be used to delete a role
 * definition from an Azure Key Vault account.
 *
 * <p>
 * <strong>Code Sample:</strong>
 *
 * <p>
 * The following code sample demonstrates how to asynchronously delete a role definition from the key vault, using
 * the {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient#deleteRoleDefinition(com.azure.security.keyvault.administration.models.KeyVaultRoleScope,
 * java.lang.String) KeyVaultAccessControlClient.deleteRoleDefinition(KeyVaultRoleScope, String)} API.
 * <!-- src_embed
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignment#KeyVaultRoleScope-String
 * -->
 * <pre>
 * String roleAssignmentName = &quot;c3ed874a-64a9-4a87-8581-2a1ad84b9ddb&quot;;
 *
 * keyVaultAccessControlClient.deleteRoleAssignment&#40;KeyVaultRoleScope.GLOBAL, roleAssignmentName&#41;;
 *
 * System.out.printf&#40;&quot;Deleted role assignment with name '%s'.%n&quot;, roleAssignmentName&#41;;
 * </pre>
 * <!-- end
 * com.azure.security.keyvault.administration.KeyVaultAccessControlClient.deleteRoleAssignment#KeyVaultRoleScope-String
 * -->
 *
 * <p>
 * <strong>Note:</strong> For the asynchronous sample, refer to {@link
 * com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient}. <br>
 * <hr/>
 *
 * <h2>Back Up a Collection of Keys</h2>
 *
 * The {@link com.azure.security.keyvault.administration.KeyVaultBackupClient} can be used to back up the entire
 * collection of keys from a key vault.
 *
 * <p>
 * <strong>Code Sample:</strong>
 *
 * <p>
 * The following code sample demonstrates how to synchronously back up an entire collection of keys using, using the
 * {@link com.azure.security.keyvault.administration.KeyVaultBackupClient#beginBackup(String, String)} API.
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String -->
 * <pre>
 * String blobStorageUrl = &quot;https:&#47;&#47;myaccount.blob.core.windows.net&#47;myContainer&quot;;
 * String sasToken = &quot;sv=2020-02-10&amp;ss=b&amp;srt=o&amp;sp=rwdlactfx&amp;se=2021-06-17T07:13:07Z&amp;st=2021-06-16T23:13:07Z&quot;
 *     + &quot;&amp;spr=https&amp;sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D&quot;;
 *
 * SyncPoller&lt;KeyVaultBackupOperation, String&gt; backupPoller = client.beginBackup&#40;blobStorageUrl, sasToken&#41;;
 *
 * PollResponse&lt;KeyVaultBackupOperation&gt; pollResponse = backupPoller.poll&#40;&#41;;
 *
 * System.out.printf&#40;&quot;The current status of the operation is: %s.%n&quot;, pollResponse.getStatus&#40;&#41;&#41;;
 *
 * PollResponse&lt;KeyVaultBackupOperation&gt; finalPollResponse = backupPoller.waitForCompletion&#40;&#41;;
 *
 * if &#40;finalPollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41; &#123;
 *     String folderUrl = backupPoller.getFinalResult&#40;&#41;;
 *
 *     System.out.printf&#40;&quot;Backup completed. The storage location of this backup is: %s.%n&quot;, folderUrl&#41;;
 * &#125; else &#123;
 *     KeyVaultBackupOperation operation = backupPoller.poll&#40;&#41;.getValue&#40;&#41;;
 *
 *     System.out.printf&#40;&quot;Backup failed with error: %s.%n&quot;, operation.getError&#40;&#41;.getMessage&#40;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupClient.beginBackup#String-String -->
 *
 * <p>
 * <strong>Note:</strong> For the asynchronous sample, refer to {@link
 * com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient}. <br>
 * <hr/>
 *
 * <h2>Restore a Collection of Keys</h2>
 *
 * The {@link com.azure.security.keyvault.administration.KeyVaultBackupClient} can be used to restore an entire
 * collection of keys from a backup.
 *
 * <p>
 * <strong>Code Sample:</strong>
 *
 * <p>
 * The following code sample demonstrates how to synchronously restore an entire collection of keys from a backup,
 * using the {@link com.azure.security.keyvault.administration.KeyVaultBackupClient#beginRestore(String, String)} API.
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultBackupClient.beginRestore#String-String -->
 * <pre>
 * String folderUrl = &quot;https:&#47;&#47;myaccount.blob.core.windows.net&#47;myContainer&#47;mhsm-myaccount-2020090117323313&quot;;
 * String sasToken = &quot;sv=2020-02-10&amp;ss=b&amp;srt=o&amp;sp=rwdlactfx&amp;se=2021-06-17T07:13:07Z&amp;st=2021-06-16T23:13:07Z&quot;
 *     + &quot;&amp;spr=https&amp;sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D&quot;;
 *
 * SyncPoller&lt;KeyVaultRestoreOperation, KeyVaultRestoreResult&gt; backupPoller =
 *     client.beginRestore&#40;folderUrl, sasToken&#41;;
 *
 * PollResponse&lt;KeyVaultRestoreOperation&gt; pollResponse = backupPoller.poll&#40;&#41;;
 *
 * System.out.printf&#40;&quot;The current status of the operation is: %s.%n&quot;, pollResponse.getStatus&#40;&#41;&#41;;
 *
 * PollResponse&lt;KeyVaultRestoreOperation&gt; finalPollResponse = backupPoller.waitForCompletion&#40;&#41;;
 *
 * if &#40;finalPollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41; &#123;
 *     System.out.printf&#40;&quot;Backup restored successfully.%n&quot;&#41;;
 * &#125; else &#123;
 *     KeyVaultRestoreOperation operation = backupPoller.poll&#40;&#41;.getValue&#40;&#41;;
 *
 *     System.out.printf&#40;&quot;Restore failed with error: %s.%n&quot;, operation.getError&#40;&#41;.getMessage&#40;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultBackupClient.beginRestore#String-String -->
 *
 * <p>
 * <strong>Note:</strong> For the asynchronous sample, refer to {@link
 * com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient}. <br>
 * <hr/>
 *
 * <h2>Selectively Restore a Key</h2>
 *
 * The {@link com.azure.security.keyvault.administration.KeyVaultBackupClient} can be used to restore a specific key
 * from a backup.
 *
 * <p>
 * <strong>Code Sample:</strong>
 *
 * <p>
 * The following code sample demonstrates how to synchronously restore a specific key from a backup, using the {@link
 * com.azure.security.keyvault.administration.KeyVaultBackupClient#beginSelectiveKeyRestore(String, String, String)}
 * API.
 * <!-- src_embed
 * com.azure.security.keyvault.administration.KeyVaultBackupClient.beginSelectiveKeyRestore#String-String-String -->
 * <pre>
 * String folderUrl = &quot;https:&#47;&#47;myaccount.blob.core.windows.net&#47;myContainer&#47;mhsm-myaccount-2020090117323313&quot;;
 * String sasToken = &quot;sv=2020-02-10&amp;ss=b&amp;srt=o&amp;sp=rwdlactfx&amp;se=2021-06-17T07:13:07Z&amp;st=2021-06-16T23:13:07Z&quot;
 *     &quot;&amp;spr=https&amp;sig=n5V6fnlkViEF9b7ij%2FttTHNwO2BdFIHKHppRxGAyJdc%3D&quot;;
 * String keyName = &quot;myKey&quot;;
 *
 * SyncPoller&lt;KeyVaultSelectiveKeyRestoreOperation, KeyVaultSelectiveKeyRestoreResult&gt; backupPoller =
 *     client.beginSelectiveKeyRestore&#40;folderUrl, sasToken, keyName&#41;;
 *
 * PollResponse&lt;KeyVaultSelectiveKeyRestoreOperation&gt; pollResponse = backupPoller.poll&#40;&#41;;
 *
 * System.out.printf&#40;&quot;The current status of the operation is: %s.%n&quot;, pollResponse.getStatus&#40;&#41;&#41;;
 *
 * PollResponse&lt;KeyVaultSelectiveKeyRestoreOperation&gt; finalPollResponse = backupPoller.waitForCompletion&#40;&#41;;
 *
 * if &#40;finalPollResponse.getStatus&#40;&#41; == LongRunningOperationStatus.SUCCESSFULLY_COMPLETED&#41; &#123;
 *     System.out.printf&#40;&quot;Key restored successfully.%n&quot;&#41;;
 * &#125; else &#123;
 *     KeyVaultSelectiveKeyRestoreOperation operation = backupPoller.poll&#40;&#41;.getValue&#40;&#41;;
 *
 *     System.out.printf&#40;&quot;Key restore failed with error: %s.%n&quot;, operation.getError&#40;&#41;.getMessage&#40;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end
 * com.azure.security.keyvault.administration.KeyVaultBackupClient.beginSelectiveKeyRestore#String-String-String -->
 *
 * <p>
 * <strong>Note:</strong> For the asynchronous sample, refer to {@link
 * com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient}. <br>
 * <hr/>
 *
 * <h2>Get All Settings</h2>
 *
 * The {@link com.azure.security.keyvault.administration.KeyVaultSettingsClient} can be used to list all the settings
 * for an Azure Key Vault account.
 *
 * <p>
 * <strong>Code Sample:</strong>
 *
 * <p>
 * The following code sample demonstrates how to synchronously back up an entire collection of keys using, using the
 * {@link com.azure.security.keyvault.administration.KeyVaultSettingsClient#getSettings()} API.
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsClient.getSettings -->
 * <pre>
 * KeyVaultGetSettingsResult getSettingsResult = keyVaultSettingsClient.getSettings&#40;&#41;;
 * List&lt;KeyVaultSetting&gt; settings = getSettingsResult.getSettings&#40;&#41;;
 *
 * settings.forEach&#40;setting -&gt;
 *     System.out.printf&#40;&quot;Retrieved setting with name '%s' and value %s'.%n&quot;, setting.getName&#40;&#41;,
 *         setting.asBoolean&#40;&#41;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsClient.getSettings -->
 *
 * <p>
 * <strong>Note:</strong> For the asynchronous sample, refer to {@link
 * com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient}. <br>
 * <hr/>
 *
 * <h2>Retrieve a Specific Setting</h2>
 *
 * The {@link com.azure.security.keyvault.administration.KeyVaultSettingsClient} can be used to retrieve a specific
 * setting.
 *
 * <p>
 * <strong>Code Sample:</strong>
 *
 * <p>
 * The following code sample demonstrates how to synchronously restore an entire collection of keys from a backup,
 * using the {@link com.azure.security.keyvault.administration.KeyVaultSettingsClient#getSetting(String)} API.
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsClient.getSetting#String -->
 * <pre>
 * KeyVaultSetting setting = keyVaultSettingsClient.getSetting&#40;settingName&#41;;
 *
 * System.out.printf&#40;&quot;Retrieved setting '%s' with value '%s'.%n&quot;, setting.getName&#40;&#41;, setting.asBoolean&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsClient.getSetting#String -->
 *
 * <p>
 * <strong>Note:</strong> For the asynchronous sample, refer to {@link
 * com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient}. <br>
 * <hr/>
 *
 * <h2>Update a Specific Setting</h2>
 *
 * The {@link com.azure.security.keyvault.administration.KeyVaultSettingsClient} can be used to restore a specific key
 * from a backup.
 *
 * <p>
 * <strong>Code Sample:</strong>
 *
 * <p>
 * The following code sample demonstrates how to synchronously restore a specific key from a backup, using the {@link
 * com.azure.security.keyvault.administration.KeyVaultSettingsClient#updateSetting(com.azure.security.keyvault.administration.models.KeyVaultSetting)
 * KeyVaultSettingsClient.updateSetting(KeyVaultSetting)}
 * <!-- src_embed com.azure.security.keyvault.administration.KeyVaultSettingsClient.updateSetting#KeyVaultSetting -->
 * <pre>
 * KeyVaultSetting settingToUpdate = new KeyVaultSetting&#40;settingName, true&#41;;
 * KeyVaultSetting updatedSetting = keyVaultSettingsClient.updateSetting&#40;settingToUpdate&#41;;
 *
 * System.out.printf&#40;&quot;Updated setting '%s' to '%s'.%n&quot;, updatedSetting.getName&#40;&#41;, updatedSetting.asBoolean&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.security.keyvault.administration.KeyVaultSettingsClient.updateSetting#KeyVaultSetting -->
 *
 * <p>
 * <strong>Note:</strong> For the asynchronous sample, refer to {@link
 * com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient}. <br>
 * <hr/>
 *
 * @see com.azure.security.keyvault.administration.KeyVaultAccessControlClient
 * @see com.azure.security.keyvault.administration.KeyVaultAccessControlAsyncClient
 * @see com.azure.security.keyvault.administration.KeyVaultAccessControlClientBuilder
 * @see com.azure.security.keyvault.administration.KeyVaultBackupClient
 * @see com.azure.security.keyvault.administration.KeyVaultBackupAsyncClient
 * @see com.azure.security.keyvault.administration.KeyVaultBackupClientBuilder
 * @see com.azure.security.keyvault.administration.KeyVaultSettingsClient
 * @see com.azure.security.keyvault.administration.KeyVaultSettingsAsyncClient
 * @see com.azure.security.keyvault.administration.KeyVaultSettingsClientBuilder
 */
package com.azure.security.keyvault.administration;
