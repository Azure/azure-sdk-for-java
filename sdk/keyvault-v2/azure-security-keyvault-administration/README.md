# Azure Key Vault Administration client library for Java

Azure Key Vault Managed HSM is a fully-managed, highly-available, single-tenant, standards-compliant cloud service that
enables you to safeguard cryptographic keys for your cloud applications using FIPS 140-2 Level 3 validated HSMs.

The Azure Key Vault Administration library clients support administrative tasks such as full backup/restore and
key-level role-based access control (RBAC).

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azure_keyvault_docs] | [Samples][administration_samples]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
  - Here are details about [Java 8 client compatibility with Azure Certificate Authority][azure_ca]
- An [Azure Subscription][azure_subscription].
- An existing [Azure Key Vault][azure_keyvault]. If you need to create a key vault, you can do so in the Azure Portal by
  following the steps in [this document][azure_keyvault_portal]. Alternatively, you can use the [Azure CLI][azure_cli]
  by following the steps in [this document for Azure Key Vault][azure_keyvault_cli] or
  [this document for Managed HSM][azure_keyvault_mhsm_cli].

### Adding the package to your product

#### Use the Azure SDK BOM

Please include the `azure-sdk-bom` to your project to take dependency on the General Availability (GA) version of the
library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number. To learn
more about the BOM, see the [AZURE SDK BOM README][azure_sdk_bom].

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure.v2</groupId>
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
        <groupId>com.azure.v2</groupId>
        <artifactId>azure-security-keyvault-administration</artifactId>
    </dependency>
</dependencies>
```

#### Use a direct dependency

If you want to take dependency on a particular version of the library that is not present in the BOM, add the direct
dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure.v2:azure-security-keyvault-administration;current})

```xml
<dependency>
    <groupId>com.azure.v2</groupId>
    <artifactId>azure-security-keyvault-administration</artifactId>
    <version>5.0.0-beta.1</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Authenticate the client

In order to interact with the Azure Key Vault service, you will need to create an instance of either the
[`KeyVaultAccessControlClient`](#create-an-access-control-client) class, the
[`KeyVaultBackupClient`](#create-a-backup-client) class, or the [`KeyVaultSettingsClient`](#create-a-settings-client)
class; as well as a vault **endpoint** and a credential object. The examples shown in this document use a credential
object named [`DefaultAzureCredential`][default_azure_credential], which is appropriate for most scenarios, including
local development and production environments. Additionally, we recommend using a [managed identity][managed_identity]
for authentication in production environments.

You can find more information on different ways of authenticating and their corresponding credential types in the
[Azure Identity documentation][azure_identity].

#### Create an access control client

Once you perform [the authentication set up that suits you best][default_azure_credential] and replaced
**your-managed-hsm-endpoint** with the URL for your key vault or managed HSM, you can create the
`KeyVaultAccessControlClient`:

```java readme-sample-createAccessControlClient
KeyVaultAccessControlClient keyVaultAccessControlClient = new KeyVaultAccessControlClientBuilder()
    .endpoint("<your-managed-hsm-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

#### Create a backup client

Once you perform [the authentication set up that suits you best][default_azure_credential] and replaced
**your-managed-hsm-endpoint** with the URL for your key vault or managed HSM, you can create the `KeyVaultBackupClient`:

```java readme-sample-createBackupClient
KeyVaultBackupClient keyVaultBackupClient = new KeyVaultBackupClientBuilder()
    .endpoint("<your-managed-hsm-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

#### Create a settings client

Once you perform [the authentication set up that suits you best][default_azure_credential] and replaced
**your-managed-hsm-endpoint** with the URL for your key vault or managed HSM, you can create the
`KeyVaultSettingsClient`:

```java readme-sample-createBackupClient
KeyVaultBackupClient keyVaultBackupClient = new KeyVaultBackupClientBuilder()
    .endpoint("<your-managed-hsm-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

## Key concepts

### Key Vault Access Control Client

The Key Vault Access Control Client performs the interactions with the Azure Key Vault or Managed HSM services for
getting, setting, deleting, and listing role assignments, as well as listing role definitions. Once you've initialized a
role assignment, you can interact with the primary resource types on the service.

### Role Definition

A role definition is a collection of permissions. It defines the operations that can be performed, such as read, write,
and delete. It can also define the operations that are excluded from allowed operations.

Role definitions can be listed and specified as part of a role assignment.

### Role Assignment

A role assignment is the association of a role definition to a service principal. They can be created, listed, fetched
individually, and deleted.

### Key Vault Backup Client

The Key Vault Backup Client provides operations for performing full key backups, full key restores, and selective key
restores.

> NOTE: The backing store for key backups is a blob storage container using Shared Access Signature authentication. For
> more details on creating a SAS token using the `BlobServiceClient`, see the
> [Azure Storage Blobs client README][storage_readme_sas_token]. Alternatively, it is possible to
> [generate a SAS token in Storage Explorer][portal_sas_token].

### Pre-Backup Operation

A pre-backup operation represents a long-running operation that checks if it is possible to perform a full key backup.

### Backup Operation

A backup operation represents a long-running operation for a full key backup.

### Pre-Restore Operation

A pre-restore operation represents a long-running operation that checks if it is possible to perform a full key restore
from a backup.

### Restore Operation

A restore operation represents a long-running operation for both a full key and selective key restore.

### Key Vault Settings Client

The Key Vault Settings client allows manipulation of an Azure Key Vault or Managed HSM account's settings, with
operations such as: getting, updating, and listing.

## Access control operations

### Examples

The following sections provide several code snippets covering some of the most common role-based access control tasks,
including:

- [List role definitions](#list-role-definitions)
- [Create or update a role definition](#create-or-update-a-role-definition)
- [Retrieve a role definition](#retrieve-a-role-definition)
- [List role assignments](#list-role-assignments)
- [Create a role assignment](#create-a-role-assignment)
- [Retrieve a role assignment](#retrieve-a-role-assignment)
- [Delete a role assignment](#delete-a-role-assignment)

#### List role definitions

List the role definitions in the key vault or managed HSM by calling `listRoleDefinitions()`.

```java readme-sample-listRoleDefinitions
PagedIterable<KeyVaultRoleDefinition> roleDefinitions =
    keyVaultAccessControlClient.listRoleDefinitions(KeyVaultRoleScope.GLOBAL);

roleDefinitions.forEach(roleDefinition ->
    System.out.printf("Retrieved role definition with name '%s'.%n", roleDefinition.getName()));
```

#### Create or update a role definition

Create or update a role definition. The following example shows how to create a role definition with a randomly
generated name.

```java readme-sample-setRoleDefinition
KeyVaultRoleDefinition roleDefinition = keyVaultAccessControlClient.setRoleDefinition(KeyVaultRoleScope.GLOBAL);

System.out.printf("Created role definition with randomly generated name '%s' and role name '%s'.%n",
    roleDefinition.getName(), roleDefinition.getRoleName());
```

#### Retrieve a role definition

Get an existing role definition. To do this, the scope and 'name' property from an existing role definition are
required.

```java readme-sample-getRoleDefinition
String roleDefinitionName = "<role-definition-name>";
KeyVaultRoleDefinition roleDefinition =
    keyVaultAccessControlClient.getRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

System.out.printf("Retrieved role definition with name '%s' and role name '%s'.%n", roleDefinition.getName(),
    roleDefinition.getRoleName());
```

#### Delete a role definition

Delete a role definition. To do this, the scope and 'name' property from an existing role definition are required.

```java readme-sample-deleteRoleDefinition
String roleDefinitionName = "<role-definition-name>";

keyVaultAccessControlClient.deleteRoleDefinition(KeyVaultRoleScope.GLOBAL, roleDefinitionName);

System.out.printf("Deleted role definition with name '%s'.%n", roleDefinitionName);
```

#### List role assignments

List the role assignments in the key vault or managed HSM by calling `listRoleAssignments()`.

```java readme-sample-listRoleAssignments
PagedIterable<KeyVaultRoleAssignment> roleAssignments =
    keyVaultAccessControlClient.listRoleAssignments(KeyVaultRoleScope.GLOBAL);

roleAssignments.forEach(roleAssignment ->
    System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName()));
```

#### Create a role assignment

Create a role assignment. To do this, a role definition id and a service principal object id are required.

A role definition id can be obtained from the 'id' property of one of the role definitions returned from
`listRoleDefinitions()`.

See [this document for Azure Key Vault][azure_keyvault_cli] or [this document for Managed HSM][azure_keyvault_mhsm_cli]
instructions on how to generate a new service principal and obtain its object id. You can also get the object id for
your currently signed in account by running the following [Azure CLI][azure_cli] command:

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

#### Retrieve a role assignment

Get an existing role assignment. To do this, the 'name' property from an existing role assignment is required.

```java readme-sample-getRoleAssignment
String roleAssignmentName = "<role-assignment-name>";
KeyVaultRoleAssignment roleAssignment =
    keyVaultAccessControlClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

System.out.printf("Retrieved role assignment with name '%s'.%n", roleAssignment.getName());
```

#### Delete a role assignment

To remove a role assignment from a service principal, the role assignment must be deleted. To do this, the 'name'
property from an existing role assignment is required.

```java readme-sample-deleteRoleAssignment
String roleAssignmentName = "<role-assignment-name>";

keyVaultAccessControlClient.deleteRoleAssignment(KeyVaultRoleScope.GLOBAL, roleAssignmentName);

System.out.printf("Deleted role assignment with name '%s'.%n", roleAssignmentName);
```

## Backup and restore operations

### Examples

The following sections provide several code snippets covering some of the most common backup-related tasks, including:

- [Backup a Key Vault](#backup-a-collection-of-keys)
- [Restore a Key Vault](#restore-a-collection-of-keys)
- [Restore a key](#selectively-restore-a-key)

#### Backup a collection of keys

Back up an entire collection of keys using `beginBackup()`.

```java readme-sample-beginBackup
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
```

#### Restore a collection of keys

Restore an entire collection of keys from a backup using `beginRestore()`.

```java readme-sample-beginRestore
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
```

#### Selectively restore a key

Restore a specific key from a backup using `beginSelectiveRestore()`.

```java readme-sample-beginSelectiveKeyRestore
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
```

## Settings operations

### Examples

The following sections provide several code snippets covering some of the most common settings-related tasks, including:

- [Listing settings](#get-all-settings)
- [Retrieving a setting](#retrieve-a-specific-setting)
- [Updating a setting](#update-a-specific-setting)

#### Get all settings

List all the settings for an Azure Key Vault or Managed HSM account.

```java readme-sample-getSettings
KeyVaultGetSettingsResult getSettingsResult = keyVaultSettingsClient.getSettings();

for (KeyVaultSetting setting : getSettingsResult.getSettings()) {
    System.out.printf("Retrieved setting '%s' with value '%s'.%n", setting.getName(), setting.asBoolean());
}
```

#### Retrieve a specific setting

Retrieve a specific setting.

```java readme-sample-getSetting
String settingName = "<setting-to-get>";
KeyVaultSetting setting = keyVaultSettingsClient.getSetting(settingName);

System.out.printf("Retrieved setting '%s' with value '%s'.%n", setting.getName(),
    setting.asBoolean());
```

#### Update a specific setting

Update a specific setting.

```java readme-sample-updateSetting
String settingName = "<setting-to-update>";
KeyVaultSetting settingToUpdate = new KeyVaultSetting(settingName, true);
KeyVaultSetting updatedSetting = keyVaultSettingsClient.updateSetting(settingToUpdate);

System.out.printf("Updated setting '%s' to '%s'.%n", updatedSetting.getName(), updatedSetting.asBoolean());
```

## Troubleshooting

See our [troubleshooting guide][troubleshooting_guide] for details on how to diagnose various failure scenarios.

### General

Azure Key Vault clients raise exceptions. For example, if you try to retrieve a key after it is deleted a `404` error
is returned, indicating the resource was not found. In the following snippet, the error is handled gracefully by
catching the exception and displaying additional information about the error.

```java readme-sample-troubleshooting
try {
    keyVaultAccessControlClient.getRoleAssignment(KeyVaultRoleScope.GLOBAL, "<role-assignment-name>");
} catch (HttpResponseException e) {
    System.out.println(e.getMessage());
}
```

### Default HTTP client
<!-- TODO (vcolin7): Update with default client after discussing with the team. -->
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure the
client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki][http_clients_wiki].

### Default SSL library
<!-- TODO (vcolin7): Confirm if we're still using the Boring SSL library with clientcore/azure-core-v2. -->
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL
operations. The Boring SSL library is an Uber JAR containing native libraries for Linux / macOS / Windows, and provides
better performance compared to the default SSL implementation within the JDK. For more information, including how to
reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps

Several Azure Key Vault Java client library samples are available to you in the SDK's GitHub repository. These samples
provide example code for additional scenarios commonly encountered while working with Azure Key Vault.

## Next steps samples

Samples are explained in detail [here][samples_readme].

### Additional documentation

For more extensive documentation on Azure Key Vault, see the [API reference documentation][azure_keyvault_rest].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License
Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For
details, see the [Microsoft CLA][microsoft_cla].

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate
the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to
do this once across all repos using our [CLA][microsoft_cla].

This project has adopted the [Microsoft Open Source Code of Conduct][microsoft_code_of_conduct]. For more information
see the Code of Conduct FAQ or contact <opencode@microsoft.com> with any additional questions or comments.

For details on contributing to this repository, see the [contributing guide][contributing_guide].

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[administration_samples]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-administration/src/samples/java/com/azure/v2/security/keyvault/administration
[azure_keyvault_docs]: https://learn.microsoft.com/azure/key-vault/
[azure_keyvault_rest]: https://learn.microsoft.com/rest/api/keyvault/
[azure_ca]: https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis
[azure_cli]: https://learn.microsoft.com/cli/azure
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_keyvault]: https://learn.microsoft.com/azure/key-vault/general/overview
[azure_keyvault_cli]: https://learn.microsoft.com/azure/key-vault/general/quick-create-cli
[azure_keyvault_mhsm_cli]: https://learn.microsoft.com/azure/key-vault/managed-hsm/quick-create-cli
[azure_keyvault_portal]: https://learn.microsoft.com/azure/key-vault/general/quick-create-portal
[azure_subscription]: https://azure.microsoft.com/free/
[azure_sdk_bom]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md
[contributing_guide]: https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md
[default_azure_credential]: https://learn.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable#defaultazurecredential
[http_clients_wiki]: https://learn.microsoft.com/azure/developer/java/sdk/http-client-pipeline#http-clients
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[managed_identity]: https://learn.microsoft.com/azure/active-directory/managed-identities-azure-resources/overview
[microsoft_cla]: https://cla.microsoft.com
[microsoft_code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[samples_readme]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-administration/src/samples/README.md
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[portal_sas_token]: https://learn.microsoft.com/azure/vs-azure-tools-storage-manage-with-storage-explorer?tabs=windows#generate-a-shared-access-signature-in-storage-explorer
[source_code]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-administration/src
[storage_readme_sas_token]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/storage/azure-storage-blob#get-credentials
[troubleshooting_guide]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-administration/TROUBLESHOOTING.md
