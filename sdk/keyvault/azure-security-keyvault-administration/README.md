# Azure Key Vault Administration library for Java
Azure Key Vault Managed HSM is a fully-managed, highly-available, single-tenant, standards-compliant cloud service that enables you to safeguard cryptographic keys for your cloud applications using FIPS 140-2 Level 3 validated HSMs.

The Azure Key Vault Administration library clients support administrative tasks such as full backup/restore and key-level role-based access control (RBAC).

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azkeyvault_docs] | [Samples][administration_samples]

## Getting started
### Include the package
#### Include the BOM file
Please include the `azure-sdk-bom` to your project to take dependency on the General Availability (GA) version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number. To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

and then include the direct dependency in the dependencies section without the version tag as shown below.

```xml
<dependencies>
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-security-keyvault-administration</artifactId>
    </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM, add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-administration;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-administration</artifactId>
    <version>4.5.4</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
  - Here are details about [Java 8 client compatibility with Azure Certificate Authority](https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis).
- An [Azure Subscription][azure_subscription].
- An existing [Azure Key Vault Managed HSM][azure_keyvault_mhsm]. If you need to create a Managed HSM, you can do so using the Azure CLI by following the steps in [this document][azure_keyvault_mhsm_cli].

### Authenticate the client
In order to interact with the Azure Key Vault service, you will need to create an instance of either the [`KeyVaultAccessControlClient`](#create-an-access-control-client)class or the [`KeyVaultBackupClient`](#create-a-backup-client) class, as well as a **vault url** (which you may see as "DNS Name" in the Azure Portal) and a credential object. The examples shown in this document use a credential object named  [`DefaultAzureCredential`][default_azure_credential], which is appropriate for most scenarios, including local development and production environments. Additionally, we recommend using a [managed identity][managed_identity] for authentication in production environments.

You can find more information on different ways of authenticating and their corresponding credential types in the [Azure Identity documentation][azure_identity].

#### Create an access control client
Once you perform [the authentication set up that suits you best][default_azure_credential] and replaced **your-managed-hsm-url** with the URL for your key vault, you can create the `KeyVaultAccessControlClient`:

```java readme-sample-createAccessControlClient
KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
    .vaultUrl("<your-managed-hsm-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

> NOTE: For using an asynchronous client use `KeyVaultAccessControlAsyncClient` instead of `KeyVaultAccessControlClient` and call `buildAsyncClient()`.

#### Create a backup client
Once you perform [the authentication set up that suits you best][default_azure_credential] and replaced **your-managed-hsm-url** with the URL for your key vault, you can create the `KeyVaultBackupClient`:

```java readme-sample-createBackupClient
KeyVaultBackupClient keyVaultBackupClient = new KeyVaultBackupClientBuilder()
    .vaultUrl("<your-managed-hsm-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

> NOTE: For using an asynchronous client use `KeyVaultBackupAsyncClient`  instead of `KeyVaultBackupClient` and call `buildAsyncClient()`.

#### Create a settings client
Once you perform [the authentication set up that suits you best][default_azure_credential] and replaced **your-managed-hsm-url** with the URL for your key vault, you can create the `KeyVaultSettingsClient`:

```java readme-sample-createBackupClient
KeyVaultBackupClient keyVaultBackupClient = new KeyVaultBackupClientBuilder()
    .vaultUrl("<your-managed-hsm-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

> NOTE: For using an asynchronous client use `KeyVaultSettingsAsyncClient`  instead of `KeyVaultSettingsClient` and call `buildAsyncClient()`.

## Key concepts
### Key Vault Access Control client
The Key Vault Access Control client performs the interactions with the Azure Key Vault service for getting, setting, deleting, and listing role assignments, as well as listing role definitions. Asynchronous (`KeyVaultAccessControlAsyncClient`) and synchronous (`KeyVaultAccessControlClient`) clients exist in the SDK allowing for the selection of a client based on an application's use case. Once you've initialized a role assignment, you can interact with the primary resource types in Key Vault.

### Role Definition
A role definition is a collection of permissions. It defines the operations that can be performed, such as read, write, and delete. It can also define the operations that are excluded from allowed operations.

Role definitions can be listed and specified as part of a role assignment.

### Role Assignment
A role assignment is the association of a role definition to a service principal. They can be created, listed, fetched individually, and deleted.

### Key Vault Backup client
The Key Vault Backup Client provides both synchronous and asynchronous operations for performing full key backups, full key restores, and selective key restores. Asynchronous (`KeyVaultBackupAsyncClient`) and synchronous (`KeyVaultBackupClient`) clients exist in the SDK allowing for the selection of a client based on an application's use case.

> NOTE: The backing store for key backups is a blob storage container using Shared Access Signature authentication. For more details on creating a SAS token using the `BlobServiceClient`, see the [Azure Storage Blobs client README][storage_readme_sas_token]. Alternatively, it is possible to [generate a SAS token in Storage Explorer][portal_sas_token].

### Pre-Backup Operation
A pre-backup operation represents a long-running operation that checks if it is possible to perform a full key backup.

### Backup Operation
A backup operation represents a long-running operation for a full key backup.

### Pre-Restore Operation
A pre-restore operation represents a long-running operation that checks if it is possible to perform a full key restore from a backup.

### Restore Operation
A restore operation represents a long-running operation for both a full key and selective key restore.

### Key Vault Settings client
The Key Vault Access Control client allows manipulation of an Azure Key Vault account's settings, with operations such as: getting, updating, and listing. Asynchronous (`KeyVaultSettingsAsyncClient`) and synchronous (`KeyVaultSettingsClient`) clients exist in the SDK allowing for the selection of a client based on an application's use case.

## Access control operations
### Examples
#### Sync API
The following sections provide several code snippets covering some of the most common Azure Key Vault Access Control service tasks, including:
- [List role definitions](#list-role-definitions)
- [Create or update a role definition](#create-or-update-a-role-definition)
- [Retrieve a role definition](#retrieve-a-role-definition)
- [List role assignments](#list-role-assignments)
- [Create a role assignment](#create-a-role-assignment)
- [Retrieve a role assignment](#retrieve-a-role-assignment)
- [Delete a role assignment](#delete-a-role-assignment)

##### List role definitions
List the role definitions in the key vault by calling `listRoleDefinitions()`.

```java readme-sample-listRoleDefinitions
PagedIterable<KeyVaultRoleDefinition> roleDefinitions =
    keyVaultAccessControlClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL);

roleDefinitions.forEach(roleDefinition ->
    System.out.printf("Retrieved role definition with name '%s'.%n", roleDefinition.getName()));
```

##### Create or update a role definition
Create or update a role definition in the key vault. The following example shows how to create a role definition with a randomly generated name.

```java readme-sample-setRoleDefinition
KeyVaultRoleDefinition roleDefinition = keyVaultAccessControlClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL);

System.out.printf("Created role definition with randomly generated name '%s' and role name '%s'.%n",
    roleDefinition.getName(), roleDefinition.getRoleName());
```

##### Retrieve a role definition
Get an existing role definition. To do this, the scope and 'name' property from an existing role definition are required.

```java readme-sample-getRoleDefinition
String roleDefinitionName = "<role-definition-name>";
KeyVaultRoleDefinition roleDefinition =
    keyVaultAccessControlClient.getRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

System.out.printf("Retrieved role definition with name '%s' and role name '%s'.%n", roleDefinition.getName(),
    roleDefinition.getRoleName());
```

##### Delete a role definition
Delete a role definition. To do this, the scope and 'name' property property from an existing role definition are required.

```java readme-sample-deleteRoleDefinition
String roleDefinitionName = "<role-definition-name>";

keyVaultAccessControlClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

System.out.printf("Deleted role definition with name '%s'.%n", roleDefinitionName);
```

##### List role assignments
List the role assignments in the key vault by calling `listRoleAssignments()`.

```java readme-sample-listRoleAssignments
PagedIterable<KeyVaultRoleAssignment> roleAssignments =
    keyVaultAccessControlClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL);

roleAssignments.forEach(roleAssignment ->
    System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
```

##### Create a role assignment
Create a role assignment in the key vault. To do this a role definition ID and a service principal object ID are required.

A role definition ID can be obtained from the 'id' property of one of the role definitions returned from `listRoleDefinitions()`.

See the [Create/Get Credentials section](#createget-credentials) for links and instructions on how to generate a new service principal and obtain it's object ID. You can also get the object ID for your currently signed in account by running the following Azure CLI command:

```bash
az ad signed-in-user show --query objectId
```

```java readme-sample-createRoleAssignment
String roleDefinitionId = "<role-definition-id>";
String servicePrincipalId = "<service-principal-id>";
KeyVaultRoleAssignment roleAssignment =
    keyVaultAccessControlClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinitionId,
        servicePrincipalId);

System.out.printf("Created role assignment with randomly generated name '%s' for principal with id '%s'.%n",
    roleAssignment.getName(), roleAssignment.getProperties().getPrincipalId());
```

##### Retrieve a role assignment
Get an existing role assignment. To do this, the 'name' property from an existing role assignment is required.

```java readme-sample-getRoleAssignment
String roleAssignmentName = "<role-assignment-name>";
KeyVaultRoleAssignment roleAssignment =
    keyVaultAccessControlClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName());
```
##### Delete a role assignment
To remove a role assignment from a service principal, the role assignment must be deleted. To do this, the 'name' property from an existing role assignment is required.

```java readme-sample-deleteRoleAssignment
String roleAssignmentName = "<role-assignment-name>";

keyVaultAccessControlClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

System.out.printf("Deleted role assignment with name '%s'.%n", roleAssignmentName);
```

#### Async API
The following sections provide several code snippets covering some of the most common asynchronous Azure Key Vault Access Control service tasks, including:
- [List role definitions asynchronously](#list-role-definitions-asynchronously)
- [Create or update a role definition asynchronously](#create-or-update-a-role-definition-asynchronously)
- [Retrieve a role definition asynchronously](#retrieve-a-role-definition-asynchronously)
- [Delete a role definition asynchronously](#delete-a-role-definition-asynchronously)
- [List role assignments asynchronously](#list-role-assignments-asynchronously)
- [Create a role assignment asynchronously](#create-a-role-assignment-asynchronously)
- [Retrieve a role assignment asynchronously](#retrieve-a-role-assignment-asynchronously)
- [Delete a role assignment asynchronously](#delete-a-role-assignment-asynchronously)

> Note : You should add `System.in.read()` or `Thread.sleep()` after the function calls in the main class/thread to allow async functions/operations to execute and finish before the main application/thread exits.

##### List role definitions asynchronously
List the role definitions in the key vault by calling `listRoleDefinitions()`.

```java readme-sample-listRoleDefinitionsAsync
keyVaultAccessControlAsyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL)
    .subscribe(roleDefinition ->
        System.out.printf("Retrieved role definition with name '%s'.%n", roleDefinition.getName()));
```

##### Create or update a role definition asynchronously
Create or update a role definition in the key vault. The following example shows how to create a role definition with a randomly generated name.

```java readme-sample-setRoleDefinitionAsync
keyVaultAccessControlAsyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL)
    .subscribe(roleDefinition ->
        System.out.printf("Created role definition with randomly generated name '%s' and role name '%s'.%n",
            roleDefinition.getName(), roleDefinition.getRoleName()));
```

##### Retrieve a role definition asynchronously
Get an existing role definition. To do this, the 'name' property from an existing role definition is required.

```java readme-sample-getRoleDefinitionAsync
String roleDefinitionName = "<role-definition-name>";

keyVaultAccessControlAsyncClient.getRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
    .subscribe(roleDefinition ->
        System.out.printf("Retrieved role definition with name '%s' and role name '%s'.%n",
            roleDefinition.getName(), roleDefinition.getRoleName()));
```

##### Delete a role definition asynchronously
Delete a role definition. To do this, the 'name' property from an existing role definition is required.

```java readme-sample-deleteRoleDefinitionAsync
String roleDefinitionName = "<role-definition-name>";

keyVaultAccessControlAsyncClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
    .subscribe(unused -> System.out.printf("Deleted role definition with name '%s'.%n", roleDefinitionName));
```

##### List role assignments asynchronously
List the role assignments in the key vault by calling `listRoleAssignments()`.

```java readme-sample-listRoleAssignmentsAsync
keyVaultAccessControlAsyncClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL)
    .subscribe(roleAssignment ->
        System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
```

##### Create a role assignment asynchronously
Create a role assignment in the key vault. To do this a role definition ID and a service principal object ID are required.

A role definition ID can be obtained from the 'id' property of one of the role definitions returned from `listRoleDefinitions()`.

See the [Create/Get Credentials section](#createget-credentials) for links and instructions on how to generate a new service principal and obtain it's object ID. You can also get the object ID for your currently signed in account by running the following Azure CLI command:

```bash
az ad signed-in-user show --query objectId
```

```java readme-sample-createRoleAssignmentAsync
String roleDefinitionId = "<role-definition-id>";
String servicePrincipalId = "<service-principal-id>";

keyVaultAccessControlAsyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinitionId,
    servicePrincipalId).subscribe(roleAssignment ->
        System.out.printf("Created role assignment with randomly generated name '%s' for principal with id"
            + "'%s'.%n", roleAssignment.getName(), roleAssignment.getProperties().getPrincipalId()));
```

##### Retrieve a role assignment asynchronously
Get an existing role assignment. To do this, the 'name' property from an existing role assignment is required.

```java readme-sample-getRoleAssignmentAsync
String roleAssignmentName = "<role-assignment-name>";

keyVaultAccessControlAsyncClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName)
    .subscribe(roleAssignment ->
        System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
```

##### Delete a role assignment asynchronously
To remove a role assignment from a service principal, the role assignment must be deleted. To do this, the 'name' property from an existing role assignment is required.

```java readme-sample-deleteRoleAssignmentAsync
String roleAssignmentName = "<role-assignment-name>";

keyVaultAccessControlAsyncClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName)
    .subscribe(unused ->
        System.out.printf("Deleted role assignment with name '%s'.%n", roleAssignmentName));
```

## Backup and restore operations
### Examples
#### Sync API
The following sections provide several code snippets covering some of the most common Azure Key Vault Backup client tasks, including:
- [Pre-backup check for a Key Vault](#run-pre-backup-check-for-a-collection-of-keys)
- [Backup a Key Vault](#backup-a-collection-of-keys)
- [Pre-restore check for a Key Vault](#run-pre-restore-check-for-a-collection-of-keys)
- [Restore a Key Vault](#restore-a-collection-of-keys)
- [Restore a key](#selectively-restore-a-key)

##### Run pre-backup check for a collection of keys
Check if an entire collection of keys can be backed up by using `beginPreBackup()`.

```java readme-sample-beginPreBackup
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
```

##### Backup a collection of keys
Back up an entire collection of keys using `beginBackup()`.

```java readme-sample-beginBackup
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
```

##### Run pre-restore check for a collection of keys
Check if an entire collection of keys can be restored from a backup by using `beginPreRestore()`.

```java readme-sample-beginPreRestore
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
```

##### Restore a collection of keys
Restore an entire collection of keys from a backup using `beginRestore()`.

```java readme-sample-beginRestore
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
```

##### Selectively restore a key
Restore a specific key from a backup using `beginSelectiveRestore()`.

```java readme-sample-beginSelectiveKeyRestore
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
```

#### Async API
The following sections provide several code snippets covering some of the most common asynchronous Azure Key Vault Backup client tasks, including:
- [Run pre-backup check for a collection of keys asynchronously](#run-pre-backup-check-for-a-collection-of-keys-asynchronously)
- [Backup a Key Vault asynchronously](#backup-a-collection-of-keys-asynchronously)
- [Run pre-restore check for a collection of keys asynchronously](#run-pre-restore-check-for-a-collection-of-keys-asynchronously)
- [Restore a Key Vault asynchronously](#restore-a-collection-of-keys-asynchronously)
- [Restore a key asynchronously](#selectively-restore-a-key-asynchronously)

> Note : You should add `System.in.read()` or `Thread.sleep()` after the function calls in the main class/thread to allow async functions/operations to execute and finish before the main application/thread exits.

##### Run pre-backup check for a collection of keys asynchronously
Check if an entire collection of keys can be backed up by using `beginPreBackup()`.

```java readme-sample-beginPreBackupAsync
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
```

##### Backup a collection of keys asynchronously
Back up an entire collection of keys using `beginBackup()`.

```java readme-sample-beginBackupAsync
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
```

##### Run pre-restore check for a collection of keys asynchronously
Check if an entire collection of keys can be restored from a backup by using `beginPreRestore()`.

```java readme-sample-beginPreRestoreAsync
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
```

##### Restore a collection of keys asynchronously
Restore an entire collection of keys from a backup using `beginRestore()`.

```java readme-sample-beginRestoreAsync
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
```

##### Selectively restore a key asynchronously
Restore an entire collection of keys from a backup using `beginSelectiveRestore()`.

```java readme-sample-beginSelectiveKeyRestoreAsync
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
```

## Settings operations
### Examples
#### Sync API
The following sections provide several code snippets covering some of the most common Azure Key Vault Settings client tasks, including:
- [Listing settings](#get-all-settings)
- [Retrieving a setting](#retrieve-a-specific-setting)
- [Updating a setting](#update-a-specific-setting)

##### Get all settings
List all the settings for an Azure Key Vault account.

```java readme-sample-getSettings
KeyVaultGetSettingsResult getSettingsResult = keyVaultSettingsClient.getSettings();

for (KeyVaultSetting setting : getSettingsResult.getSettings()) {
    System.out.printf("Retrieved setting '%s' with value '%s'.%n", setting.getName(), setting.asBoolean());
}
```

##### Retrieve a specific setting
Retrieve a specific setting.

```java readme-sample-getSetting
String settingName = "<setting-to-get>";
KeyVaultSetting setting = keyVaultSettingsClient.getSetting(settingName);

System.out.printf("Retrieved setting '%s' with value '%s'.%n", setting.getName(),
    setting.asBoolean());
```

##### Update a specific setting
Update a specific setting.
```java readme-sample-updateSetting
String settingName = "<setting-to-update>";
KeyVaultSetting settingToUpdate = new KeyVaultSetting(settingName, true);
KeyVaultSetting updatedSetting = keyVaultSettingsClient.updateSetting(settingToUpdate);

System.out.printf("Updated setting '%s' to '%s'.%n", updatedSetting.getName(), updatedSetting.asBoolean());
```

#### Async API
The following sections provide several code snippets covering some of the most common asynchronous Azure Key Vault Settings client tasks, including:
- [Listing settings](#get-all-settings-asynchronously)
- [Retrieving a setting](#retrieve-a-specific-setting-asynchronously)
- [Updating a setting](#update-a-specific-setting-asynchronously)

##### Get all settings asynchronously
List all the settings for a Key Vault account.

```java readme-sample-getSettingsAsync
keyVaultSettingsAsyncClient.getSettings()
    .subscribe(settingsResult ->
        settingsResult.getSettings().forEach(setting ->
            System.out.printf("Retrieved setting with name '%s' and value '%s'.%n", setting.getName(),
                setting.asBoolean())));
```

##### Retrieve a specific setting asynchronously
Retrieve a specific setting.

```java readme-sample-getSettingAsync
String settingName = "<setting-to-get>";

keyVaultSettingsAsyncClient.getSetting(settingName)
    .subscribe(setting ->
        System.out.printf("Retrieved setting with name '%s' and value '%s'.%n", setting.getName(),
            setting.asBoolean()));
```

##### Update a specific setting asynchronously
Update a specific setting.

```java readme-sample-updateSettingAsync
String settingName = "<setting-to-update>";
KeyVaultSetting settingToUpdate = new KeyVaultSetting(settingName, true);

keyVaultSettingsAsyncClient.updateSetting(settingToUpdate)
    .subscribe(updatedSetting ->
        System.out.printf("Updated setting with name '%s' and value '%s'.%n", updatedSetting.getName(),
            updatedSetting.asBoolean()));
```

## Troubleshooting
See our [troubleshooting guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-administration/TROUBLESHOOTING.md) for details on how to diagnose various failure scenarios.

### General
Azure Key Vault Access Control clients raise exceptions. For example, if you try to retrieve a role assignment after it is deleted a `404` error is returned, indicating the resource was not found. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.

```java readme-sample-troubleshooting
try {
    keyVaultAccessControlClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, "<role-assginment-name>");
} catch (HttpResponseException e) {
    System.out.println(e.getMessage());
}
```

### Default HTTP client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the [HTTP clients wiki][http_clients_wiki].

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL operations. The Boring SSL library is an Uber JAR containing native libraries for Linux / macOS / Windows, and provides better performance compared to the default SSL implementation within the JDK. For more information, including how to reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps
Several Key Vault Java SDK samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Azure Key Vault.

### Additional documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][microsoft_code_of_conduct]. For more information see the Code of Conduct FAQ or contact <opencode@microsoft.com> with any additional questions or comments.

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-administration/src
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[azkeyvault_docs]: https://docs.microsoft.com/azure/key-vault/
[azure_identity]: https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable
[azure_subscription]: https://azure.microsoft.com/
[azure_keyvault]: https://docs.microsoft.com/azure/key-vault/quick-create-portal
[azure_keyvault_mhsm]: https://docs.microsoft.com/azure/key-vault/managed-hsm/overview
[azure_keyvault_mhsm_cli]: https://docs.microsoft.com/azure/key-vault/managed-hsm/quick-create-cli
[default_azure_credential]: https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable#defaultazurecredential
[managed_identity]: https://docs.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview
[azkeyvault_rest]: https://docs.microsoft.com/rest/api/keyvault/
[administration_samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-administration/src/samples/java/com/azure/security/keyvault/administration
[storage_readme_sas_token]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/storage/azure-storage-blob#get-credentials
[portal_sas_token]: https://docs.microsoft.com/azure/vs-azure-tools-storage-manage-with-storage-explorer?tabs=windows#generate-a-shared-access-signature-in-storage-explorer
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[http_clients_wiki]: https://learn.microsoft.com/azure/developer/java/sdk/http-client-pipeline#http-clients
[microsoft_code_of_conduct]: https://opensource.microsoft.com/codeofconduct/

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fkeyvault%2Fazure-security-keyvault-administration%2FREADME.png)
