---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-key-vault
urlFragment: keyvault-keys-samples
---
# Azure Key Vault Administration Samples client library for Java

This document explains samples and how to use them.

## Key concepts

Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

## Getting started

Getting started explained in detail [here][SDK_README_GETTING_STARTED].

## Examples

Following section document various examples.

### Access Control Hello World Samples

* [AccessControlHelloWorld.java][sample_accesscontrolhelloworld] - Contains samples for following scenarios:
  * Create a role assignment
  * Retrieve a role assignment
  * Delete a role assignment
  * List role assignments
  * List role definitions

### Creating Role Assignments for Different Scopes Samples

* [CreateRoleAssignmentsForDifferentScopes.java][sample_createroleassignmentsfordifferentscopes] - Contains samples for following scenarios:
  * Create a role assignment for a key vault
  * Create a role assignment for a resource group
  * Create a role assignment for a subscription

### Backup and Restore Hello World Samples

* [BackupAndRestoreOperations.java][sample_backuprestorehelloworld] - Contains samples for following scenarios:
  * Backup a key vault
  * Restore a key vault

### Selective Key Restore Samples

* [SelectiveKeyRestore.java][sample_selectivekeyrestore] - Contains samples for following scenarios:
  * Restore a specific key

### Settings Hello World Samples

* [SettingsHelloWorld.java][sample_settingshelloworld] - Contains samples for following scenarios:
  * Retrieve a specific setting from a key vault account
  * Update a specific setting from a key vault account
  * List all settings from a key vault account

## Troubleshooting

### General

Key Vault clients raise exceptions. For example, if you try to retrieve a key after it is deleted a `404` error is
returned, indicating resource not found. In the following snippet, the error is handled gracefully by catching the
exception and displaying additional information about the error.

```java
try {
    keyClient.getKey("deletedKey")
} catch (ResourceNotFoundException e) {
    System.out.println(e.getMessage());
}
```

## Next steps

Start using KeyVault Java SDK in your solutions. Our SDK details could be found at [SDK README][ADMINISTRATION_README].

### Additional Documentation

For more extensive documentation on Azure Key Vault, see the [API reference documentation][azure_keyvault_rest].

## Contributing

This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[ADMINISTRATION_README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault-v2/azure-security-keyvault-administration/README.md
[SDK_README_CONTRIBUTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault-v2/azure-security-keyvault-administration/README.md#contributing
[SDK_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault-v2/azure-security-keyvault-administration/README.md#getting-started
[SDK_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault-v2/azure-security-keyvault-administration/README.md#key-concepts
[azure_keyvault_rest]: https://learn.microsoft.com/rest/api/keyvault/
[sample_accesscontrolhelloworld]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault-v2/azure-security-keyvault-administration/src/samples/java/com/azure/security/keyvault/administration/AccessControlHelloWorld.java
[sample_createroleassignmentsfordifferentscopes]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault-v2/azure-security-keyvault-administration/src/samples/java/com/azure/security/keyvault/administration/CreateRoleAssignmentsForDifferentScopes.java
[sample_backuprestorehelloworld]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault-v2/azure-security-keyvault-administration/src/samples/java/com/azure/security/keyvault/administration/BackupAndRestoreHelloWorld.java
[sample_selectivekeyrestore]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault-v2/azure-security-keyvault-administration/src/samples/java/com/azure/security/keyvault/administration/SelectiveKeyRestore.java
[sample_settingshelloworld]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault-v2/azure-security-keyvault-administration/src/samples/java/com/azure/security/keyvault/administration/SettingsHelloWorld.java