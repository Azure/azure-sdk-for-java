# Azure Key Vault Administration library for Java
Azure Key Vault Managed HSM is a fully-managed, highly-available, single-tenant, standards-compliant cloud service that enables you to safeguard cryptographic keys for your cloud applications using FIPS 140-2 Level 3 validated HSMs.

The Azure Key Vault Administration library clients support administrative tasks such as full backup/restore and key-level role-based access control (RBAC).

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azkeyvault_docs] | [Samples][administration_samples]

## Getting started
### Adding the package to your project
Maven dependency for the Azure Key Vault Administration library. Add it to your project's POM file.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-administration;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-administration</artifactId>
    <version>4.0.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- An existing [Azure Key Vault Managed HSM][azure_keyvault_mhsm]. If you need to create a Managed HSM, you can use the [Azure Cloud Shell][azure_cloud_shell] to create one with this Azure CLI command. Replace `<your-resource-group-name>` and `<your-key-vault-mhsm-name>` with your own, unique names:

    ```Bash
    az keyvault create --hsm-name <your-key-vault-mhsm-name> --resource-group <your-resource-group-name> --administrators <your-service-principal-object-id> --location <your-azure-location>
    ```

### Authenticate the client
In order to interact with the Azure Key Vault service, you'll need to either create an instance of the [KeyVaultAccessControlClient](#create-an-access-control-client) or an instance of the class [KeyVaultBackupClient](#create-a-backup-client). You would need a **vault url**, which you may see as "DNS Name" in the portal, and **client secret credentials (client id, client secret, tenant id)** to instantiate a client object using the default `DefaultAzureCredential` examples shown in this document.

The `DefaultAzureCredential` way of authentication by providing client secret credentials is being used in this getting started section but you can find more ways to authenticate with [azure-identity][azure_identity].

#### Create/Get credentials
To create/get client secret credentials you can use the [Azure Portal][azure_create_application_in_portal], [Azure CLI][azure_keyvault_cli_full] or [Azure Cloud Shell][azure_cloud_shell].

Here is an [Azure Cloud Shell][azure_cloud_shell] snippet below to

 * Create a service principal and configure its access to Azure resources:

    ```Bash
    az ad sp create-for-rbac -n <your-application-name> --skip-assignment
    ```

    Output:

    ```json
    {
        "appId": "generated-app-ID",
        "displayName": "some-app-name",
        "name": "https://some-app-name",
        "password": "random-password",
        "tenant": "tenant-ID"
    }
    ```

* Take note of the service principal objectId
    ```Bash
    az ad sp show --id <appId> --query objectId
    ```

  Output:
    ```
    "<your-service-principal-object-id>"
    ```

* Use the returned credentials above to set the **AZURE_CLIENT_ID** (appId), **AZURE_CLIENT_SECRET** (password), and **AZURE_TENANT_ID** (tenantId) environment variables. The following example shows a way to do this in Bash:

    ```Bash
    export AZURE_CLIENT_ID="generated-app-ID"
    export AZURE_CLIENT_SECRET="random-password"
    export AZURE_TENANT_ID="tenant-ID"
    ```

* Create the Managed HSM and grant the above mentioned service principal authorization to perform administrative operations on the Managed HSM (replace `<your-resource-group-name>` and `<your-key-vault-mhsm-name>` with your own, unique names and `<your-service-principal-object-id>` with the value from above):
    
    ```bash
    az keyvault create --hsm-name <your-key-vault-mhsm-name> --resource-group <your-resource-group-name> --administrators <your-service-principal-object-id> --location <your-azure-location>
    ```
  
  This service principal is automatically added to the "Managed HSM Administrators" [built-in role][built_in_roles].

* Use the aforementioned Azure Key Vault name to retrieve details of your Key Vault, which also contain your Azure Key Vault URL:

    ```Bash
    az keyvault show --name <your-key-vault-mhsm-name>
    ```

#### Activate your managed HSM
All data plane commands are disabled until the HSM is activated. You will not be able to create keys or assign roles. Only the designated administrators that were assigned during the create command can activate the HSM. To activate the HSM you must download the security domain.

To activate your HSM you need:
- Minimum 3 RSA key-pairs (maximum 10).
- Specify minimum number of keys required to decrypt the security domain (quorum).

To activate the HSM you send at least 3 (maximum 10) RSA public keys to the HSM, the HSM encrypts the security domain with these keys and sends it back. Once this security domain is successfully downloaded, your HSM is ready to use. You also need to specify quorum, which is the minimum number of private keys required to decrypt the security domain.

The example below shows how to use openssl to generate 3 self signed certificate.

```bash
openssl req -newkey rsa:2048 -nodes -keyout cert_0.key -x509 -days 365 -out cert_0.cer
openssl req -newkey rsa:2048 -nodes -keyout cert_1.key -x509 -days 365 -out cert_1.cer
openssl req -newkey rsa:2048 -nodes -keyout cert_2.key -x509 -days 365 -out cert_2.cer
```

Use the `az keyvault security-domain download` command to download the security domain and activate your managed HSM.

The example below, uses 3 RSA key pairs (only public keys are needed for this command) and sets the quorum to 2.

```bash
az keyvault security-domain download --hsm-name <your-key-vault-mhsm-name> --sd-wrapping-keys ./certs/cert_0.cer ./certs/cert_1.cer ./certs/cert_2.cer --sd-quorum 2 --security-domain-file ContosoMHSM-SD.json
```

#### Controlling access to your managed HSM
The designated administrators assigned during creation are automatically added to the "Managed HSM Administrators" [built-in role][built_in_roles], who are able to download a security domain and [manage roles for data plane access][access_control], among other limited permissions.

To perform other actions on keys, you need to assign principals to other roles such as "Managed HSM Crypto User", which can perform non-destructive key operations:

```PowerShell
az keyvault role assignment create --hsm-name <your-key-vault-mhsm-name> --role "Managed HSM Crypto User" --scope / --assignee-object-id <principal-or-user-object-ID> --assignee-principal-type <principal-type>
```

Please read [best practices][best_practices] for properly securing your managed HSM.

## Key concepts
### Key Vault Access Control client:
The Key Vault Access Control client performs the interactions with the Azure Key Vault service for getting, setting, deleting, and listing role assignments, as well as listing role definitions. Asynchronous (KeyVaultAccessControlAsyncClient) and synchronous (KeyVaultAccessControlClient) clients exist in the SDK allowing for the selection of a client based on an application's use case. Once you've initialized a role assignment, you can interact with the primary resource types in Key Vault.

### Role Definition
A role definition is a collection of permissions. It defines the operations that can be performed, such as read, write, and delete. It can also define the operations that are excluded from allowed operations.

Role definitions can be listed and specified as part of a role assignment.

### Role Assignment
A role assignment is the association of a role definition to a service principal. They can be created, listed, fetched individually, and deleted.

### Key Vault Backup client
The Key Vault Backup Client provides both synchronous and asynchronous operations for performing full key backups, full key restores, and selective key restores. Asynchronous (KeyVaultBackupAsyncClient) and synchronous (KeyVaultBackupClient) clients exist in the SDK allowing for the selection of a client based on an application's use case.

> NOTE: The backing store for key backups is a blob storage container using Shared Access Signature authentication. For more details on creating a SAS token using the BlobServiceClient, see the [Azure Storage Blobs client README][storage_readme_sas_token]. Alternatively, it is possible to [generate a SAS token in Storage Explorer][portal_sas_token].

### Backup Operation
A backup operation represents a long running operation for a full key backup.

### Restore Operation
A restore operation represents a long running operation for both a full key and selective key restore.

## Create an Access Control client
Once you've populated the **AZURE_CLIENT_ID**, **AZURE_CLIENT_SECRET**, and **AZURE_TENANT_ID** environment variables and replaced **your-key-vault-url** with the URI returned above, you can create the `KeyVaultAccessControlClient`:

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#40-L43 -->
```java
KeyVaultAccessControlClient accessControlClient = new KeyVaultAccessControlClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

> NOTE: For using an asynchronous client use `KeyVaultAccessControlAsyncClient` instead of `KeyVaultAccessControlClient` and call `buildAsyncClient()`

## Examples
### Sync API
The following sections provide several code snippets covering some of the most common Azure Key Vault Access Control service tasks, including:
- [List role definitions](#list-role-definitions)
- [Create or update a role definition](#create-or-update-a-role-definition)
- [Retrieve a role definition](#retrieve-a-role-definition)
- [List role assignments](#list-role-assignments)
- [Create a role assignment](#create-a-role-assignment)
- [Retrieve a role assignment](#retrieve-a-role-assignment)
- [Delete a role assignment](#delete-a-role-assignment)

### List role definitions
List the role definitions in the key vault by calling `listRoleDefinitions()`.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#50-L54 -->
```java
PagedIterable<KeyVaultRoleDefinition> roleDefinitions =
    keyVaultAccessControlClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL);

roleDefinitions.forEach(roleDefinition ->
    System.out.printf("Retrieved role definition with name '%s'.%n", roleDefinition.getName()));
```

### Create or update a role definition
Create or update a role definition in the key vault. The following example shows how to create a role definition with a randomly generated name.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#L61-L64 -->
```java
KeyVaultRoleDefinition roleDefinition = keyVaultAccessControlClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL);

System.out.printf("Created role definition with randomly generated name '%s' and role name '%s'.%n",
    roleDefinition.getName(), roleDefinition.getRoleName());
```

### Retrieve a role definition
Get an existing role definition. To do this, the scope and 'name' property from an existing role definition are required.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#L71-76 -->
```java
String roleDefinitionName = "<role-definition-name>";
KeyVaultRoleDefinition roleDefinition =
    keyVaultAccessControlClient.getRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

System.out.printf("Retrieved role definition with name '%s' and role name '%s'.%n", roleDefinition.getName(),
    roleDefinition.getRoleName());
```

### Delete a role definition
Delete a role definition. To do this, the scope and 'name' property property from an existing role definition are required.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#83-87 -->
```java
String roleDefinitionName = "<role-definition-name>";

keyVaultAccessControlClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

System.out.printf("Deleted role definition with name '%s'.%n", roleDefinitionName);
```

### List role assignments
List the role assignments in the key vault by calling `listRoleAssignments()`.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#94-98 -->
```java
PagedIterable<KeyVaultRoleAssignment> roleAssignments =
    keyVaultAccessControlClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL);

roleAssignments.forEach(roleAssignment ->
    System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
```

### Create a role assignment
Create a role assignment in the key vault. To do this a role definition ID and a service principal object ID are required.

A role definition ID can be obtained from the 'id' property of one of the role definitions returned from `listRoleDefinitions()`.

See the [Create/Get Credentials section](#createget-credentials) for links and instructions on how to generate a new service principal and obtain it's object ID. You can also get the object ID for your currently signed in account by running the following Azure CLI command:

```Bash
az ad signed-in-user show --query objectId
```

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#105-112 -->
```java
String roleDefinitionId = "<role-definition-id>";
String servicePrincipalId = "<service-principal-id>";
KeyVaultRoleAssignment roleAssignment =
    keyVaultAccessControlClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinitionId,
        servicePrincipalId);

System.out.printf("Created role assignment with randomly generated name '%s' for principal with id '%s'.%n",
    roleAssignment.getName(), roleAssignment.getProperties().getPrincipalId());
```

### Retrieve a role assignment
Get an existing role assignment. To do this, the 'name' property from an existing role assignment is required.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#119-123 -->
```java
String roleAssignmentName = "<role-assignment-name>";
KeyVaultRoleAssignment roleAssignment =
    keyVaultAccessControlClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName());
```
### Delete a role assignment
To remove a role assignment from a service principal, the role assignment must be deleted. To do this, the 'name' property from an existing role assignment is required.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#130-134 -->
```java
String roleAssignmentName = "<role-assignment-name>";

keyVaultAccessControlClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

System.out.printf("Deleted role assignment with name '%s'.%n", roleAssignmentName);
```

### Async API
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

### List role definitions asynchronously
List the role definitions in the key vault by calling `listRoleDefinitions()`.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#141-143 -->
```java
keyVaultAccessControlAsyncClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL)
    .subscribe(roleDefinition ->
        System.out.printf("Retrieved role definition with name '%s'.%n", roleDefinition.getName()));
```

### Create or update a role definition asynchronously
Create or update a role definition in the key vault. The following example shows how to create a role definition with a randomly generated name.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#150-153 -->
```java
keyVaultAccessControlAsyncClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL)
    .subscribe(roleDefinition ->
        System.out.printf("Created role definition with randomly generated name '%s' and role name '%s'.%n",
            roleDefinition.getName(), roleDefinition.getRoleName()));
```

### Retrieve a role definition asynchronously
Get an existing role definition. To do this, the 'name' property from an existing role definition is required.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#160-165 -->
```java
String roleDefinitionName = "<role-definition-name>";

keyVaultAccessControlAsyncClient.getRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
    .subscribe(roleDefinition ->
        System.out.printf("Retrieved role definition with name '%s' and role name '%s'.%n",
            roleDefinition.getName(), roleDefinition.getRoleName()));
```

### Delete a role definition asynchronously
Delete a role definition. To do this, the 'name' property from an existing role definition is required.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#172-175 -->
```java
String roleDefinitionName = "<role-definition-name>";

keyVaultAccessControlAsyncClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName)
    .subscribe(unused -> System.out.printf("Deleted role definition with name '%s'.%n", roleDefinitionName));
```

### List role assignments asynchronously
List the role assignments in the key vault by calling `listRoleAssignments()`.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#182-184 -->
```java
keyVaultAccessControlAsyncClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL)
    .subscribe(roleAssignment ->
        System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
```

### Create a role assignment asynchronously
Create a role assignment in the key vault. To do this a role definition ID and a service principal object ID are required.

A role definition ID can be obtained from the 'id' property of one of the role definitions returned from `listRoleDefinitions()`.

See the [Create/Get Credentials section](#createget-credentials) for links and instructions on how to generate a new service principal and obtain it's object ID. You can also get the object ID for your currently signed in account by running the following Azure CLI command:

```Bash
az ad signed-in-user show --query objectId
```

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#191-197 -->
```java
String roleDefinitionId = "<role-definition-id>";
String servicePrincipalId = "<service-principal-id>";

keyVaultAccessControlAsyncClient.createRoleAssignment(KeyVaultRoleScope.GLOBAL, roleDefinitionId,
    servicePrincipalId).subscribe(roleAssignment ->
        System.out.printf("Created role assignment with randomly generated name '%s' for principal with id"
            + "'%s'.%n", roleAssignment.getName(), roleAssignment.getProperties().getPrincipalId()));
```

### Retrieve a role assignment asynchronously
Get an existing role assignment. To do this, the 'name' property from an existing role assignment is required.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#204-208 -->
```java
String roleAssignmentName = "<role-assignment-name>";

keyVaultAccessControlAsyncClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName)
    .subscribe(roleAssignment ->
        System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
```

### Delete a role assignment asynchronously
To remove a role assignment from a service principal, the role assignment must be deleted. To do this, the 'name' property from an existing role assignment is required.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#215-219 -->
```java
String roleAssignmentName = "<role-assignment-name>";

keyVaultAccessControlAsyncClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName)
    .subscribe(unused ->
        System.out.printf("Deleted role assignment with name '%s'.%n", roleAssignmentName));
```

### Create a Backup client
Once you've populated the **AZURE_CLIENT_ID**, **AZURE_CLIENT_SECRET**, and **AZURE_TENANT_ID** environment variables and replaced **your-key-vault-url** with the URI returned above, you can create the `KeyVaultBackupClient`:

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#226-229 -->
```java
KeyVaultBackupClient keyVaultBackupClient = new KeyVaultBackupClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

> NOTE: For using an asynchronous client use `KeyVaultBackupAsyncClient`  instead of `KeyVaultBackupClient` and call `buildAsyncClient()`

## Examples
### Sync API
The following sections provide several code snippets covering some of the most common Azure Key Vault Backup client tasks, including:
- [Backup a Key Vault](#backup-a-collection-of-keys)
- [Restore a Key Vault](#restore-a-collection-of-keys)
- [Restore a key](#selectively-restore-a-key)

### Backup a collection of keys
Back up an entire collection of keys using `beginBackup()`.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#236-256 -->
```java
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
```

### Restore a collection of keys
Restore an entire collection of keys from a backup using `beginRestore()`.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#263-281 -->
```java
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
```

### Selectively restore a key
Restore a specific key from a backup using `beginSelectiveRestore()`.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#288-307 -->
```java
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
```

### Async API
The following sections provide several code snippets covering some of the most common asynchronous Azure Key Vault Backup client tasks, including:
- [Backup a Key Vault asynchronously](#backup-a-collection-of-keys-asynchronously)
- [Restore a Key Vault asynchronously](#restore-a-collection-of-keys-asynchronously)
- [Restore a key asynchronously](#selectively-restore-a-key-asynchronously)

> Note : You should add `System.in.read()` or `Thread.sleep()` after the function calls in the main class/thread to allow async functions/operations to execute and finish before the main application/thread exits.

### Backup a collection of keys asynchronously
Back up an entire collection of keys using `beginBackup()`.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#314-325 -->
```java
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
```

### Restore a collection of keys asynchronously
Restore an entire collection of keys from a backup using `beginRestore()`.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#332-342 -->
```java
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
```

### Selectively restore a key asynchronously
Restore an entire collection of keys from a backup using `beginSelectiveRestore()`.

<!-- embedme ./src/samples/java/com/azure/security/keyvault/administration/ReadmeSamples.java#350-361 -->
```java
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
```

## Troubleshooting
### General
Azure Key Vault Access Control clients raise exceptions. For example, if you try to retrieve a role assignment after it is deleted a `404` error is returned, indicating the resource was not found. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.

```java
try {
    client.getRoleAssignment(KeyVaultRoleAssignmentScope.GLOBAL, "<role-assginment-name>")
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
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[maven]: https://maven.apache.org/
[azure_subscription]: https://azure.microsoft.com/
[azure_keyvault]: https://docs.microsoft.com/azure/key-vault/quick-create-portal
[azure_keyvault_mhsm]: https://docs.microsoft.com/azure/key-vault/managed-hsm/quick-create-cli
[azure_cli]: https://docs.microsoft.com/cli/azure
[rest_api]: https://docs.microsoft.com/rest/api/keyvault/
[azkeyvault_rest]: https://docs.microsoft.com/rest/api/keyvault/
[azure_create_application_in_portal]: https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal
[azure_keyvault_cli]: https://docs.microsoft.com/azure/key-vault/quick-create-cli
[azure_keyvault_cli_full]: https://docs.microsoft.com/cli/azure/keyvault?view=azure-cli-latest
[administration_samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-administration/src/samples/java/com/azure/security/keyvault/administration
[access_control]: https://docs.microsoft.com/azure/key-vault/managed-hsm/access-control
[best_practices]: https://docs.microsoft.com/azure/key-vault/managed-hsm/best-practices
[built_in_roles]: https://docs.microsoft.com/azure/key-vault/managed-hsm/built-in-roles
[storage_readme_sas_token]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/storage/azure-storage-blob#get-credentials
[portal_sas_token]: https://docs.microsoft.com/azure/vs-azure-tools-storage-manage-with-storage-explorer?tabs=windows#generate-a-shared-access-signature-in-storage-explorer
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_cloud_shell]: https://shell.azure.com/bash
[http_clients_wiki]: https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients
[microsoft_code_of_conduct]: https://opensource.microsoft.com/codeofconduct/

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fkeyvault%2Fazure-security-keyvault-administration%2FREADME.png)
