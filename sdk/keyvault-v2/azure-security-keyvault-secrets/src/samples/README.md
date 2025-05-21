---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-key-vault
urlFragment: keyvault-secrets-samples
---

# Azure Key Vault Secret Samples client library for Java

This document explains samples and how to use them.

## Key concepts

Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

## Getting started

Getting started explained in detail [here][SDK_README_GETTING_STARTED].

## Examples

Following section document various examples.

### Hello World Samples

* [HelloWorld.java][sample_helloWorld] - Contains samples for following scenarios:
  * Create a Secret
  * Retrieve a Secret
  * Update a Secret
  * Delete a Secret

### List Operations Samples

* [ListOperations.java][sample_list] - Contains samples for following scenarios:
  * Create a Secret
  * List Secrets
  * Create new version of existing secret.
  * List versions of an existing secret.

### Backup And Restore Operations Samples

* [BackupAndRestoreOperations.java][sample_BackupRestore] - Contains samples for following scenarios:
  * Create a Secret
  * Backup a Secret -- Write it to a file.
  * Delete a secret
  * Restore a secret

### Managing Deleted Secrets Samples

* [ManagingDeletedSecrets.java][sample_ManageDeleted] - Contains samples for following scenarios:
  * Create a Secret
  * Delete a secret
  * List deleted secrets
  * Recover a deleted secret
  * Purge Deleted secret

## Troubleshooting

### General

Key Vault clients raise exceptions. For example, if you try to retrieve a secret after it is deleted a `404` error is
returned, indicating resource not found. In the following snippet, the error is handled gracefully by catching the
exception and displaying additional information about the error.

```java
try {
    SecretClient.getSecret("deletedSecret")
} catch (ResourceNotFoundException e) {
    System.out.println(e.getMessage());
}
```

## Next steps

Start using KeyVault Java SDK in your solutions. Our SDK details could be found at [SDK README][SECRETS_SDK_README].

### Additional Documentation

For more extensive documentation on Azure Key Vault, see the [API reference documentation][azure_keyvault_rest].

## Contributing

This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[SECRETS_SDK_README]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-secrets/README.md
[SDK_README_GETTING_STARTED]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-secrets/README.md#getting-started
[SDK_README_KEY_CONCEPTS]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-secrets/README.md#key-concepts
[SDK_README_CONTRIBUTING]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-secrets/README.md#contributing
[azure_keyvault_rest]: https://learn.microsoft.com/rest/api/keyvault/
[sample_helloWorld]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-secrets/src/samples/java/com/azure/v2/security/keyvault/secrets/HelloWorld.java
[sample_list]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-secrets/src/samples/java/com/azure/v2/security/keyvault/secrets/ListOperations.java
[sample_BackupRestore]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-secrets/src/samples/java/com/azure/v2/security/keyvault/secrets/BackupAndRestoreOperations.java
[sample_ManageDeleted]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-secrets/src/samples/java/com/azure/v2/security/keyvault/secrets/ManagingDeletedSecrets.java
