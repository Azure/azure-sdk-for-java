# Azure Key Vault Administration library for Java

## Getting started
### Adding the package to your project
Maven dependency for the Azure Key Vault Administration library. Add it to your project's POM file.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-administration;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-administration</artifactId>
    <version>4.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- An existing [Azure Key Vault][azure_keyvault]. If you need to create a Key Vault, you can use the [Azure Cloud Shell](https://shell.azure.com/bash) to create one with this Azure CLI command. Replace `<your-resource-group-name>` and `<your-key-vault-name>` with your own, unique names:

    ```Bash
    az keyvault create --resource-group <your-resource-group-name> --name <your-key-vault-name>
    ```

### Authenticate the client
In order to interact with the Azure Key Vault service, you'll need to either create an instance of the [KeyVaultAccessControlClient](#create-access-control-client) or an instance of the class [KeyVaultBackupClient](#create-backup-client). You would need a **vault url** and **client secret credentials (client id, client secret, tenant id)** to instantiate a client object using the default `DefaultAzureCredential` examples shown in this document.

The `DefaultAzureCredential` way of authentication by providing client secret credentials is being used in this getting started section but you can find more ways to authenticate with [azure-identity][azure_identity].

#### Create/Get credentials
To create/get client secret credentials you can use the [Azure Portal][azure_create_application_in_portal], [Azure CLI][azure_keyvault_cli_full] or [Azure Cloud Shell](https://shell.azure.com/bash)

Here is an [Azure Cloud Shell](https://shell.azure.com/bash) snippet below to

 * Create a service principal and configure its access to Azure resources:

    ```Bash
    az ad sp create-for-rbac -n <your-application-name> --skip-assignment
    ```

    Output:

    ```json
    {
        "appId": "generated-app-ID",
        "displayName": "dummy-app-name",
        "name": "http://dummy-app-name",
        "password": "random-password",
        "tenant": "tenant-ID"
    }
    ```

* Use the above returned credentials information to set the **AZURE_CLIENT_ID** (appId), **AZURE_CLIENT_SECRET** (password), and **AZURE_TENANT_ID** (tenantId) environment variables. The following example shows a way to do this in Bash:

    ```Bash
    export AZURE_CLIENT_ID="generated-app-ID"
    export AZURE_CLIENT_SECRET="random-password"
    export AZURE_TENANT_ID="tenant-ID"
    ```

* Take note of the service principal objectId
    ```Bash
    az ad sp show --id <appId> --query objectId
    ```
  
    Output:
    ```
    "<your-service-principal-object-id>"
    ```

* Use the aforementioned Key Vault name to retrieve details of your Key Vault, which also contain your Key Vault URL:

    ```Bash
    az keyvault show --name <your-key-vault-name>
    ```

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
Once you've populated the **AZURE_CLIENT_ID**, **AZURE_CLIENT_SECRET**, and **AZURE_TENANT_ID** environment variables and replaced **your-key-vault-url** with the URI returned above, you can create the KeyVaultAccessControlClient:

```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.KeyVaultAccessControlClient;
import com.azure.security.keyvault.administration.KeyVaultAccessControlClientBuilder;

KeyVaultAccessControlClient accessControlClient = new KeyVaultAccessControlClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

> NOTE: For using an asynchronous client use KeyVaultAccessControlAsyncClient instead of KeyVaultAccessControlClient and call `buildAsyncClient()`

## Examples
### Sync API
The following sections provide several code snippets covering some of the most common Azure Key Vault Access Control service tasks, including:
- [List role definitions](#list-role-definitions)
- [List role assignments](#list-role-assignments)
- [Create a role assignment](#create-a-role-assignment)
- [Retrieve a role assignment](#retrieve-a-role-assignment)
- [Delete a role assignment](#delete-a-role-assignment)

### List role definitions
List the role definitions in the key vault by calling `listRoleDefinitions`.

```java
KeyVaultRoleAssignmentScope roleAssignmentScope = KeyVaultRoleAssignmentScope.GLOBAL;

for (KeyVaultRoleDefinition roleDefinition : accessControlClient.listRoleDefinitions(roleAssignmentScope)) {
    System.out.printf("Retrieved role definition with name \"%s\" and type \"%s\"%n", roleDefinition.getName(),
        roleDefinition.getType());
}
```

### List role assignments
List the role assignments in the key vault by calling `listRoleAssignments`.

```java
KeyVaultRoleAssignmentScope roleAssignmentScope = KeyVaultRoleAssignmentScope.GLOBAL;

for (KeyVaultRoleAssignment roleAssignment : accessControlClient.listRoleAssignments(roleAssignmentScope)) {
    System.out.printf("Retrieved role assignment with name \"%s\" and type \"%s\"%n", roleAssignment.getName(),
        roleAssignment.getType());
}
```

### Create a role assignment
Create a role assignment to in the Azure Key Vault. To do this a role definition ID and a service principal object ID are required.

A role definition ID can be obtained from the 'id' property of one of the role definitions returned from `listRoleDefinitions`.

See the [Create/Get Credentials section](#createget-credentials) for links and instructions on how to generate a new service principal and obtain it's object ID. You can also get the object ID for your currently signed in account by running the following Azure CLI command:

```Bash
az ad signed-in-user show --query objectId
```

```java
String roleDefinitionIdToAssign = "<role-definition-id>";
String servicePrincipalObjectId = "<object-id>";

KeyVaultRoleAssignmentProperties properties =
    new KeyVaultRoleAssignmentProperties(roleDefinitionIdToAssign, servicePrincipalObjectId);
KeyVaultRoleAssignment createdAssignment =
    accessControlClient.createRoleAssignment(KeyVaultRoleAssignmentScope.GLOBAL, properties);

System.out.printf("Created role assignment with name \"%s\" and type \"%s\"%n", createdAssignment.getName(),
    createdAssignment.getType());
```

### Retrieve a role assignment
Get an existing role assignment. To do this, the 'name' property from an existing role assignment is required. Let's use the `createdAssignment` from the previous example.

```java
KeyVaultRoleAssignment retrievedAssignment =
    accessControlClient.getRoleAssignment(KeyVaultRoleAssignmentScope.GLOBAL, createdAssignment.getName());

System.out.printf("Retrieved role assignment with name \"%s\" and type \"%s\"%n", retrievedAssignment.getName(),
    retrievedAssignment.getType());
```
### Delete a role assignment
To remove a role assignment from a service principal, the role assignment must be deleted. Let's delete the `createdAssignment` from the previous example.

```java
KeyVaultRoleAssignment deletedAssignment =
    accessControlClient.deleteRoleAssignment(KeyVaultRoleAssignmentScope.GLOBAL, createdAssignment.getName());

System.out.printf("Deleted role assignment with name \"%s\" and type \"%s\"%n", deletedAssignment.getName(),
    deletedAssignment.getType());
```

### Async API
The following sections provide several code snippets covering some of the most common asynchronous Azure Key Vault Access Control service tasks, including:
- [List role definitions asynchronously](#list-role-definitions-asynchronously)
- [List role assignments asynchronously](#list-role-assignments-asynchronously)
- [Create a role assignment asynchronously](#create-a-role-assignment-asynchronously)
- [Retrieve a role assignment asynchronously](#retrieve-a-role-assignment-asynchronously)
- [Delete a role assignment asynchronously](#delete-a-role-assignment-asynchronously)

> Note : You should add `System.in.read()` or `Thread.sleep()` after the function calls in the main class/thread to allow async functions/operations to execute and finish before the main application/thread exits.

### List role definitions asynchronously
List the role definitions in the key vault by calling `listRoleDefinitions`.

```java
KeyVaultRoleAssignmentScope roleAssignmentScope = KeyVaultRoleAssignmentScope.GLOBAL;

accessControlAsyncClient.listRoleDefinitions(roleAssignmentScope))
    .subscribe(roleDefinition ->
        System.out.printf("Retrieved role definition with name \"%s\" and type \"%s\"%n", roleDefinition.getName(),
                roleDefinition.getType()));
```

### List role assignments asynchronously
List the role assignments in the key vault by calling `listRoleAssignments`.

```java
KeyVaultRoleAssignmentScope roleAssignmentScope = KeyVaultRoleAssignmentScope.GLOBAL;

accessControlAsyncClient.listRoleAssignments(roleAssignmentScope))
    .subscribe(roleAssignment ->
        System.out.printf("Retrieved role assignment with name \"%s\" and type \"%s\"%n", roleAssignment.getName(),
            roleAssignment.getType()));
```

### Create a role assignment asynchronously
Create a role assignment to in the Azure Key Vault. To do this a role definition ID and a service principal object ID are required.

A role definition ID can be obtained from the 'id' property of one of the role definitions returned from `listRoleDefinitions`.

See the [Create/Get Credentials section](#createget-credentials) for links and instructions on how to generate a new service principal and obtain it's object ID. You can also get the object ID for your currently signed in account by running the following Azure CLI command:

```Bash
az ad signed-in-user show --query objectId
```

```java
String roleDefinitionIdToAssign = "<role-definition-id>";
String servicePrincipalObjectId = "<object-id>";

KeyVaultRoleAssignmentProperties properties =
    new KeyVaultRoleAssignmentProperties(roleDefinitionIdToAssign, servicePrincipalObjectId);

accessControlAsyncClient.createRoleAssignment(KeyVaultRoleAssignmentScope.GLOBAL, properties)
    .subscribe(createdAssignment ->
        System.out.printf("Created role assignment with name \"%s\" and type \"%s\"%n", createdAssignment.getName(),
            createdAssignment.getType()));
```

### Retrieve a role assignment asynchronously
Get an existing role assignment. To do this, the 'name' property from an existing role assignment is required. Let's use the `createdAssignment` from the previous example.

```java
accessControlAsyncClient.getRoleAssignment(KeyVaultRoleAssignmentScope.GLOBAL, createdAssignment.getName())
    .subscribe(retrievedAssignment ->
        System.out.printf("Retrieved role assignment with name \"%s\" and type \"%s\"%n", retrievedAssignment.getName(),
            retrievedAssignment.getType()));
```
### Delete a role assignment asynchronously
To remove a role assignment from a service principal, the role assignment must be deleted. Let's delete the `createdAssignment` from the previous example.

```java
accessControlAsyncClient.deleteRoleAssignment(KeyVaultRoleAssignmentScope.GLOBAL, createdAssignment.getName())
    .subscribe(deletedAssignment ->
        System.out.printf("Deleted role assignment with name \"%s\" and type \"%s\"%n", deletedAssignment.getName(),
            deletedAssignment.getType()));
```

### Create a Backup client
Once you've populated the **AZURE_CLIENT_ID**, **AZURE_CLIENT_SECRET**, and **AZURE_TENANT_ID** environment variables and replaced **your-key-vault-url** with the URI returned above, you can create the KeyVaultBackupClient:

```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.administration.KeyVaultBackupClient;
import com.azure.security.keyvault.administration.KeyVaultBackupClientBuilder;

KeyVaultBackupClient backupClient = new KeyVaultBackupClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

> NOTE: For using an asynchronous client use KeyVaultBackupAsyncClient instead of KeyVaultBackupClient and call `buildAsyncClient()`

## Examples
### Sync API
The following sections provide several code snippets covering some of the most common Azure Key Vault Backup client tasks, including:
- [Backup a Key Vault](#backup-a-collection-of-keys)
- [Restore a Key Vault](#restore-a-collection-of-keys)
- [Restore a key](#selectively-restore-a-key)

### Backup a collection of keys
Back up an entire collection of keys using `beginBackup`.

```java
SyncPoller<KeyVaultBackupOperation, Void> backupPoller =
    backupClient.beginBackup("<blob-storage-url-to-save-backup>", "<sas-token>");

backupPoller.waitForCompletion();

System.out.printf("Backup created");
```

### Restore a collection of keys
Restore an entire collection of keys from a backup using `beginRestore`.

```java
SyncPoller<KeyVaultBackupOperation, String restorePoller =
    backupClient.beginRestore("<blob-storage-url-to-backup>", "<sas-token>", "<folder-name>");

restorePoller.waitForCompletion();
String blobUri = restorePoller.getFinalResult();

System.out.printf("Backup restored from: %s%n", blobUri);
```

### Selectively restore a key
Restore a specific key from a backup using `beginSelectiveRestore`.

```java
SyncPoller<KeyVaultBackupOperation, String> selectiveRestorePoller =
    backupClient.beginSelectiveRestore("<name-of-key-to-restore>", "<blob-storage-url-to-backup>", "<sas-token>",
        "<folder-name>");

selectiveRestorePoller.waitForCompletion();
String blobUri = selectiveRestorePoller.getFinalResult();

System.out.printf("Key backup restored from: %s%n", blobUri);
```

### Async API
The following sections provide several code snippets covering some of the most common asynchronous Azure Key Vault Backup client tasks, including:
- [Backup a Key Vault asynchronously](#backup-a-collection-of-keys-asynchronously)
- [Restore a Key Vault asynchronously](#restore-a-collection-of-keys-asynchronously)
- [Restore a key asynchronously](#selectively-restore-a-key-asynchronously)

> Note : You should add `System.in.read()` or `Thread.sleep()` after the function calls in the main class/thread to allow async functions/operations to execute and finish before the main application/thread exits.

### Backup a collection of keys asynchronously
Back up an entire collection of keys using `beginBackup`.

```java
asyncClient.beginBackup(blobStorageUrl, sasToken)
    .subscribe(
        pollResponse -> {
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println(pollResponse.getStatus());
            System.out.println(pollResponse.getValue().getStatus());
            System.out.println(pollResponse.getValue().getStatusDetails());
    
            if (pollResponse.getStatus().isComplete()) {
                pollResponse.getFinalResult().subscribe(result -> System.out.println("Backup location: " + result));
            }
        }, errorResponse -> {
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println("Error found: " + errorResponse.getMessage());
            errorResponse.printStackTrace();
        }, () -> {
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println("Backup complete");
        });
```

### Restore a collection of keys asynchronously
Restore an entire collection of keys from a backup using `beginRestore`.

```java
backupAsyncClient.beginRestore("<blob-storage-url-to-backup>", "<sas-token>", "<folder-name>")
    .subscribe(
        pollResponse -> {
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println(pollResponse.getStatus());
            System.out.println(pollResponse.getValue().getStatus());
            System.out.println(pollResponse.getValue().getStatusDetails());
        }, errorResponse -> {
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println("Error found: " + errorResponse.getMessage());
            errorResponse.printStackTrace();
        }, () -> {
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println("Restore operation complete");
        });
```

### Selectively restore a key asynchronously
Restore an entire collection of keys from a backup using `beginSelectiveRestore`.

```java
backupAsyncClient.beginSelectiveRestore("<name-of-key-to-restore>", "<blob-storage-url-to-backup>", "<sas-token>",
    .subscribe(
        pollResponse -> {
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println(pollResponse.getStatus());
            System.out.println(pollResponse.getValue().getStatus());
            System.out.println(pollResponse.getValue().getStatusDetails());
        }, errorResponse -> {
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println("Error found: " + errorResponse.getMessage());
            errorResponse.printStackTrace();
        }, () -> {
            System.out.println("---------------------------------------------------------------------------------");
            System.out.println("Selective restore operation complete");
        });
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
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the [HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL operations. The Boring SSL library is an Uber JAR containing native libraries for Linux / macOS / Windows, and provides better performance compared to the default SSL implementation within the JDK. For more information, including how to reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps
Several Key Vault Java SDK samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Azure Key Vault.

### Additional documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact <opencode@microsoft.com> with any additional questions or comments.

<!-- LINKS -->
[source_code]: src
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[azkeyvault_docs]: https://docs.microsoft.com/azure/key-vault/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity
[maven]: https://maven.apache.org/
[azure_subscription]: https://azure.microsoft.com/
[azure_keyvault]: https://docs.microsoft.com/azure/key-vault/quick-create-portal
[azure_cli]: https://docs.microsoft.com/cli/azure
[rest_api]: https://docs.microsoft.com/rest/api/keyvault/
[azkeyvault_rest]: https://docs.microsoft.com/rest/api/keyvault/
[azure_create_application_in_portal]: https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal
[azure_keyvault_cli]: https://docs.microsoft.com/azure/key-vault/quick-create-cli
[azure_keyvault_cli_full]: https://docs.microsoft.com/cli/azure/keyvault?view=azure-cli-latest
[storage_readme_sas_token]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/storage/azure-storage-blob#get-credentials
[portal_sas_token]: https://docs.microsoft.com/azure/vs-azure-tools-storage-manage-with-storage-explorer?tabs=windows#generate-a-shared-access-signature-in-storage-explorer
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fkeyvault%2Fazure-security-keyvault-administration%2FREADME.png)
