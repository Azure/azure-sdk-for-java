# Azure Key Vault Key client library for Java
Azure Key Vault is a cloud service that provides secure storage of keys for encrypting your data. Multiple keys, and multiple versions of the same key, can be kept in the Azure Key Vault. Cryptographic keys in Azure Key Vault are represented as JSON Web Key (JWK) objects.

Azure Key Vault Managed HSM is a fully-managed, highly-available, single-tenant, standards-compliant cloud service that enables you to safeguard cryptographic keys for your cloud applications using FIPS 140-2 Level 3 validated HSMs.

The Azure Key Vault keys library client supports RSA keys and Elliptic Curve (EC) keys, each with corresponding support in hardware security modules (HSM). It offers operations to create, retrieve, update, delete, purge, backup, restore, and list the keys and its versions.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azkeyvault_docs] | [Samples][keys_samples]

## Getting started
### Adding the package to your project
Maven dependency for the Azure Key Vault Key client library. Add it to your project's POM file.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-keys;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-keys</artifactId>
    <version>4.3.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- An existing [Azure Key Vault][azure_keyvault]. If you need to create a Key Vault, you can use the [Azure Cloud Shell][azure_cloud_shell] to create one with this Azure CLI command. Replace `<your-resource-group-name>` and `<your-key-vault-name>` with your own, unique names:

    ```Bash
    az keyvault create --resource-group <your-resource-group-name> --name <your-key-vault-name>
    ```

### Authenticate the client
In order to interact with the Azure Key Vault service, you'll need to create an instance of the [KeyClient](#create-key-client) class. You would need a **vault url** and **client secret credentials (client id, client secret, tenant id)** to instantiate a client object using the default `DefaultAzureCredential` examples shown in this document.

The `DefaultAzureCredential` way of authentication by providing client secret credentials is being used in this getting started section but you can find more ways to authenticate with [azure-identity][azure_identity].

#### Create/Get credentials
To create/get client secret credentials you can use the [Azure Portal][azure_create_application_in_portal], [Azure CLI][azure_keyvault_cli_full] or [Azure Cloud Shell][azure_cloud_shell]

Here is an [Azure Cloud Shell][azure_cloud_shell] snippet below to

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

* Take note of the service principal objectId
    ```PowerShell
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

* Grant the aforementioned application authorization to perform key operations on the Key Vault:

    ```Bash
    az keyvault set-policy --name <your-key-vault-name> --spn $AZURE_CLIENT_ID --key-permissions backup delete get list create update encrypt decrypt
    ```

    > --key-permissions:
    > Accepted values: backup, delete, get, list, purge, recover, restore, create, update, encrypt, decrypt, import, wrapkey, unwrapkey, verify, sign

    If you have enabled role-based access control (RBAC) for Key Vault instead, you can find roles like "Key Vault Crypto Officer" in our [RBAC guide][rbac_guide].
    If you are managing your keys using Managed HSM, read about its [access control][access_control] that supports different built-in roles isolated from Azure Resource Manager (ARM).

* Use the aforementioned Key Vault name to retrieve details of your Key Vault, which also contain your Key Vault URL:

    ```Bash
    az keyvault show --name <your-key-vault-name>
    ```

* Create the Azure Key Vault or Managed HSM and grant the above mentioned application authorization to perform administrative operations on the Managed HSM (replace `<your-resource-group-name>` and `<your-key-vault-name>` with your own unique names and `<your-service-principal-object-id>` with the value from above):

If you are creating a standard Key Vault resource, use the following CLI command:
```bash
az keyvault create --resource-group <your-resource-group-name> --name <your-key-vault-name>
```

If you are creating a Managed HSM resource, use the following CLI command:
```bash
    az keyvault create --hsm-name <your-key-vault-name> --resource-group <your-resource-group-name> --administrators <your-service-principal-object-id> --location <your-azure-location>
```

#### Activate your managed HSM
This section only applies if you are creating a Managed HSM. All data plane commands are disabled until the HSM is activated. You will not be able to create keys or assign roles. Only the designated administrators that were assigned during the create command can activate the HSM. To activate the HSM you must download the security domain.

To activate your HSM you need:
- Minimum 3 RSA key-pairs (maximum 10)
- Specify minimum number of keys required to decrypt the security domain (quorum)

To activate the HSM you send at least 3 (maximum 10) RSA public keys to the HSM. The HSM encrypts the security domain with these keys and sends it back.
Once this security domain is successfully downloaded, your HSM is ready to use.
You also need to specify quorum, which is the minimum number of private keys required to decrypt the security domain.

The example below shows how to use openssl to generate 3 self signed certificate.

```bash
openssl req -newkey rsa:2048 -nodes -keyout cert_0.key -x509 -days 365 -out cert_0.cer
openssl req -newkey rsa:2048 -nodes -keyout cert_1.key -x509 -days 365 -out cert_1.cer
openssl req -newkey rsa:2048 -nodes -keyout cert_2.key -x509 -days 365 -out cert_2.cer
```

Use the `az keyvault security-domain download` command to download the security domain and activate your managed HSM.
The example below uses 3 RSA key pairs (only public keys are needed for this command) and sets the quorum to 2.

```bash
az keyvault security-domain download --hsm-name <your-key-vault-name> --sd-wrapping-keys ./certs/cert_0.cer ./certs/cert_1.cer ./certs/cert_2.cer --sd-quorum 2 --security-domain-file ContosoMHSM-SD.json
```

#### Create Key client
Once you've populated the **AZURE_CLIENT_ID**, **AZURE_CLIENT_SECRET**, and **AZURE_TENANT_ID** environment variables and replaced **your-key-vault-url** with the URI returned above, you can create the KeyClient:

```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;

KeyClient keyClient = new KeyClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

> NOTE: For using an asynchronous client use KeyAsyncClient instead of KeyClient and call `buildAsyncClient()`

#### Create Cryptography client
Once you've populated the **AZURE_CLIENT_ID**, **AZURE_CLIENT_SECRET**, and **AZURE_TENANT_ID** environment variables and replaced **your-vault-url** with the URI returned above, you can create the CryptographyClient:

```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;

// Create client with key identifier from key vault.
CryptographyClient cryptoClient = new CryptographyClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .keyIdentifier("<your-key-id-from-key-vault>")
    .buildClient();
```

> NOTE: For using an asynchronous client use CryptographyAsyncClient instead of CryptographyClient and call `buildAsyncClient()`

## Key concepts
### Key
Azure Key Vault supports multiple key types (`RSA` & `EC`) and algorithms, and enables the use of Hardware Security Modules (HSM) for high value keys. In addition to the key material, the following attributes may be specified:
* enabled: Specifies whether the key is enabled and usable for cryptographic operations.
* not_before: Identifies the time before which the key must not be used for cryptographic operations.
* expires: Identifies the expiration time on or after which the key MUST NOT be used for cryptographic operations.
* created: Indicates when this version of the key was created.
* updated: Indicates when this version of the key was updated.

### Key client:
The key client performs the interactions with the Azure Key Vault service for getting, setting, updating, deleting, and listing keys and its versions. Asynchronous (KeyAsyncClient) and synchronous (KeyClient) clients exist in the SDK allowing for the selection of a client based on an application's use case. Once you've initialized a key, you can interact with the primary resource types in Key Vault.

### Cryptography client:
The cryptography client performs the cryptographic operations locally or calls the Azure Key Vault service depending on how much key information is available locally. It supports encrypting, decrypting, signing, verifying, key wrapping, key unwrapping, and retrieving the configured key. Asynchronous (CryptographyAsyncClient) and synchronous (CryptographyClient) clients exist in the SDK allowing for the selection of a client based on an application's use case.

## Examples
### Sync API
The following sections provide several code snippets covering some of the most common Azure Key Vault Key service tasks, including:
- [Create a key](#create-a-key)
- [Retrieve a key](#retrieve-a-key)
- [Update an existing key](#update-an-existing-key)
- [Delete a key](#delete-a-key)
- [List keys](#list-keys)
- [Encrypt](#encrypt)
- [Decrypt](#decrypt)

### Create a key
Create a key to be stored in the Azure Key Vault.
- `createKey` creates a new key in the key vault. If a key with the same name already exists then a new version of the key is created.

```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;
import com.azure.security.keyvault.keys.models.KeyCurveName;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.keys.KeyClientBuilder;

KeyClient keyClient = new KeyClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();

KeyVaultKey rsaKey = keyClient.createRsaKey(new CreateRsaKeyOptions("CloudRsaKey")
    .setExpiresOn(OffsetDateTime.now().plusYears(1))
    .setKeySize(2048));
System.out.printf("Key created with name \"%s\" and id %s%n", rsaKey.getName(), rsaKey.getId());

KeyVaultKey ecKey = keyClient.createEcKey(new CreateEcKeyOptions("CloudEcKey")
    .setCurveName(KeyCurveName.P_256)
    .setExpiresOn(OffsetDateTime.now().plusYears(1)));
System.out.printf("Key created with name \"%s\" and id %s%n", ecKey.getName(), ecKey.getId());
```

### Retrieve a key
Retrieve a previously stored key by calling `getKey`.

```Java
KeyVaultKey key = keyClient.getKey("<key-name>");
System.out.printf("A key was returned with name \"%s\" and id %s%n", key.getName(), key.getId());
```

### Update an existing key
Update an existing key by calling `updateKeyProperties`.

```Java
// Get the key to update.
KeyVaultKey key = keyClient.getKey("<key-name>");
// Update the expiry time of the key.
key.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(30));
KeyVaultKey updatedKey = keyClient.updateKeyProperties(key.getProperties());
System.out.printf("Key's updated expiry time: %s%n", updatedKey.getProperties().getExpiresOn());
```

### Delete a key
Delete an existing key by calling `beginDeleteKey`.

```Java
SyncPoller<DeletedKey, Void> deletedKeyPoller = keyClient.beginDeleteKey("<key-name>");

PollResponse<DeletedKey> deletedKeyPollResponse = deletedKeyPoller.poll();

// Deleted key is accessible as soon as polling begins.
DeletedKey deletedKey = deletedKeyPollResponse.getValue();
// Deletion date only works for a SoftDelete-enabled Key Vault.
System.out.printf("Deletion date: %s%n", deletedKey.getDeletedOn());

// Key is being deleted on server.
deletedKeyPoller.waitForCompletion();
```

### List keys
List the keys in the key vault by calling `listPropertiesOfKeys`.

```java
// List operations don't return the keys with key material information. So, for each returned key we call getKey to
// get the key with its key material information.
for (KeyProperties keyProperties : keyClient.listPropertiesOfKeys()) {
    KeyVaultKey keyWithMaterial = keyClient.getKey(keyProperties.getName(), keyProperties.getVersion());
    System.out.printf("Received key with name \"%s\" and type \"%s\"%n", keyWithMaterial.getName(),
        keyWithMaterial.getKey().getKeyType());
}
```

### Encrypt
Encrypt plain text by calling `encrypt`.

```java
CryptographyClient cryptoClient = new CryptographyClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .keyIdentifier("<your-key-id-from-key-vault")
    .buildClient();

byte[] plaintext = new byte[100];
new Random(0x1234567L).nextBytes(plaintext);

// Let's encrypt a simple plain text of size 100 bytes.
EncryptResult encryptionResult = cryptoClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext);
System.out.printf("Returned ciphertext size is %d bytes with algorithm \"%s\"%n",
    encryptionResult.getCipherText().length, encryptionResult.getAlgorithm());
```

### Decrypt
Decrypt encrypted content by calling `decrypt`.

```java
byte[] plaintext = new byte[100];
new Random(0x1234567L).nextBytes(plaintext);
EncryptResult encryptionResult = cryptoClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext);

//Let's decrypt the encrypted result.
DecryptResult decryptionResult = cryptoClient.decrypt(EncryptionAlgorithm.RSA_OAEP, encryptionResult.getCipherText());
System.out.printf("Returned plaintext size is %d bytes%n", decryptionResult.getPlainText().length);
```

### Async API
The following sections provide several code snippets covering some of the most common asynchronous Azure Key Vault Key service tasks, including:
- [Create a key asynchronously](#create-a-key-asynchronously)
- [Retrieve a key asynchronously](#retrieve-a-key-asynchronously)
- [Update an existing key asynchronously](#update-an-existing-key-asynchronously)
- [Delete a key asynchronously](#delete-a-key-asynchronously)
- [List keys asynchronously](#list-keys-asynchronously)
- [Encrypt asynchronously](#encrypt-asynchronously)
- [Decrypt asynchronously](#decrypt-asynchronously)

> Note : You should add `System.in.read()` or `Thread.sleep()` after the function calls in the main class/thread to allow async functions/operations to execute and finish before the main application/thread exits.

### Create a key asynchronously
Create a key to be stored in the Azure Key Vault.
- `createKey` creates a new key in the key vault. If a key with the same name already exists then a new version of the key is created.

```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.models.CreateEcKeyOptions;
import com.azure.security.keyvault.keys.models.CreateRsaKeyOptions;

KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildAsyncClient();

keyAsyncClient.createRsaKey(new CreateRsaKeyOptions("CloudRsaKey")
    .setExpiresOn(OffsetDateTime.now().plusYears(1))
    .setKeySize(2048))
    .subscribe(key ->
        System.out.printf("Key created with name \"%s\" and id %s%n", key.getName(), key.getId()));

keyAsyncClient.createEcKey(new CreateEcKeyOptions("CloudEcKey")
    .setExpiresOn(OffsetDateTime.now().plusYears(1)))
    .subscribe(key ->
        System.out.printf("Key created with name \"%s\" and id %s%n", key.getName(), key.getId()));
```

### Retrieve a key asynchronously
Retrieve a previously stored key by calling `getKey`.

```Java
keyAsyncClient.getKey("<key-name>")
    .subscribe(key ->
        System.out.printf("Key was returned with name \"%s\" and id %s%n", key.getName(), key.getId()));
```

### Update an existing key asynchronously
Update an existing key by calling `updateKeyProperties`.

```Java
// Get the key.
keyAsyncClient.getKey("<key-name>")
    .subscribe(key -> {
        // Update the expiry time of the key.
        key.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(50));
        keyAsyncClient.updateKeyProperties(key.getProperties())
            .subscribe(updatedKey ->
                System.out.printf("Key's updated expiry time: %s%n", updatedKey.getProperties().getExpiresOn()));
   });
```

### Delete a key asynchronously
Delete an existing key by calling `beginDeleteKey`.

```java
keyAsyncClient.beginDeleteKey("<key-name>")
    .subscribe(pollResponse -> {
        System.out.printf("Deletetion status: %s%n", pollResponse.getStatus());
        System.out.printf("Deleted key name: %s%n", pollResponse.getValue().getName());
        System.out.printf("Key deletion date: %s%n", pollResponse.getValue().getDeletedOn());
    });
```

### List keys asynchronously
List the keys in the Azure Key Vault by calling `listPropertiesOfKeys`.

```Java
// The List Keys operation returns keys without their value, so for each key returned we call `getKey` to get its value
// as well.
keyAsyncClient.listPropertiesOfKeys()
    .subscribe(keyProperties ->
        keyAsyncClient.getKey(keyProperties.getName(), keyProperties.getVersion())
            .subscribe(key ->
                System.out.printf("Received key with name \"%s\" and type \"%s\"", key.getName(), key.getKeyType())));
```

### Encrypt asynchronously
Encrypt plain text by calling `encrypt`.

```java
CryptographyAsyncClient cryptoAsyncClient = new CryptographyClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .keyIdentifier("<your-key-id-from-key-vault>")
    .buildAsyncClient();

byte[] plaintext = new byte[100];
new Random(0x1234567L).nextBytes(plaintext);

// Let's encrypt a simple plain text of size 100 bytes.
cryptoAsyncClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext)
    .subscribe(encryptionResult -> {
        System.out.printf("Returned ciphertext size is %d bytes with algorithm \"%s\"%n",
            encryptionResult.getCipherText().length, encryptionResult.getAlgorithm());
    });
```

### Decrypt asynchronously
Decrypt encrypted content by calling `decrypt`.

```java
byte[] plaintext = new byte[100];
new Random(0x1234567L).nextBytes(plaintext);

// Let's encrypt a simple plain text of size 100 bytes.
cryptoAsyncClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plaintext)
    .subscribe(encryptionResult -> {
        System.out.printf("Returned ciphertext size is %d bytes with algorithm \"%s\"%n",
            encryptionResult.getCipherText().length, encryptionResult.getAlgorithm());
        //Let's decrypt the encrypted response.
        cryptoAsyncClient.decrypt(EncryptionAlgorithm.RSA_OAEP, encryptionResult.getCipherText())
            .subscribe(decryptionResult ->
                System.out.printf("Returned plaintext size is %d bytes%n", decryptionResult.getPlainText().length));
    });
```

## Troubleshooting
### General
Azure Key Vault Key clients raise exceptions. For example, if you try to retrieve a key after it is deleted a `404` error is returned, indicating the resource was not found. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.

```java
try {
    keyClient.getKey("<deleted-key-name>")
} catch (ResourceNotFoundException e) {
    System.out.println(e.getMessage());
}
```

### Default HTTP client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the [HTTP clients wiki][http_clients_wiki].

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL operations. The Boring SSL library is an Uber JAR containing native libraries for Linux / macOS / Windows, and provides better performance compared to the default SSL implementation within the JDK. For more information, including how to reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps
Several Key Vault Java SDK samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Azure Key Vault.

## Next steps samples
Samples are explained in detail [here][samples_readme].

### Additional documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][microsoft_code_of_conduct]. For more information see the Code of Conduct FAQ or contact <opencode@microsoft.com> with any additional questions or comments.

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[azkeyvault_docs]: https://docs.microsoft.com/azure/key-vault/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[maven]: https://maven.apache.org/
[azure_subscription]: https://azure.microsoft.com/
[azure_keyvault]: https://docs.microsoft.com/azure/key-vault/keys/quick-create-portal
[azure_cli]: https://docs.microsoft.com/cli/azure
[rest_api]: https://docs.microsoft.com/rest/api/keyvault/
[azkeyvault_rest]: https://docs.microsoft.com/rest/api/keyvault/
[azure_create_application_in_portal]: https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal
[azure_keyvault_cli]: https://docs.microsoft.com/azure/key-vault/quick-create-cli
[azure_keyvault_cli_full]: https://docs.microsoft.com/cli/azure/keyvault?view=azure-cli-latest
[keys_samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/README.md
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_cloud_shell]: https://shell.azure.com/bash
[jwk_specification]: https://tools.ietf.org/html/rfc7517
[http_clients_wiki]: https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients
[microsoft_code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[access_control]: https://docs.microsoft.com/azure/key-vault/managed-hsm/access-control
[rbac_guide]: https://docs.microsoft.com/azure/key-vault/general/rbac-guide

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fkeyvault%2Fazure-security-keyvault-keys%2FREADME.png)
