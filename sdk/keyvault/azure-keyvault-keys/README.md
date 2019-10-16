# Azure Key Vault Key client library for Java
Azure Key Vault allows you to create and store keys in the Key Vault. Azure Key Vault client supports RSA keys and elliptic curve keys, each with corresponding support in hardware security modules (HSM).

 Multiple keys, and multiple versions of the same key, can be kept in the Key Vault. Cryptographic keys in Key Vault are represented as [JSON Web Key [JWK]](https://tools.ietf.org/html/rfc7517) objects. This library offers operations to create, retrieve, update, delete, purge, backup, restore and list the keys and its versions.


[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azkeyvault_docs] | [Samples][keys_samples]

## Getting started
### Adding the package to your project

Maven dependency for Azure Key Client library. Add it to your project's pom file.
[//]: # ({x-version-update-start;com.azure:azure-keyvault-keys;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-keyvault-keys</artifactId>
    <version>4.0.0-preview.6</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Default HTTP Client
All client libraries, by default, use Netty HTTP client. Adding the above dependency will automatically configure 
KeyVault Keys to use Netty HTTP client. 

### Alternate HTTP client
If, instead of Netty it is preferable to use OkHTTP, there is a HTTP client available for that too. Exclude the default
Netty and include OkHTTP client in your pom.xml.

[//]: # ({x-version-update-start;com.azure:azure-keyvault-keys;current})
```xml
<!-- Add KeyVault Keys dependency without Netty HTTP client -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-keyvault-keys</artifactId>
    <version>4.0.0-preview.6</version>
    <exclusions>
      <exclusion>
        <groupId>com.azure</groupId>
        <artifactId>azure-core-http-netty</artifactId>
      </exclusion>
    </exclusions>
</dependency>
```
[//]: # ({x-version-update-end})
[//]: # ({x-version-update-start;com.azure:azure-core-http-okhttp;current})
```xml
<!-- Add OkHTTP client to use with KeyVault Keys -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-okhttp</artifactId>
  <version>1.0.0-preview.7</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Configuring HTTP Clients
When an HTTP client is included on the classpath, as shown above, it is not necessary to specify it in the client library [builders](#create-key-client), unless you want to customize the HTTP client in some fashion. If this is desired, the `httpClient` builder method is often available to achieve just this, by allowing users to provide a custom (or customized) `com.azure.core.http.HttpClient` instances.

For starters, by having the Netty or OkHTTP dependencies on your classpath, as shown above, you can create new instances of these `HttpClient` types using their builder APIs. For example, here is how you would create a Netty HttpClient instance:

```java
HttpClient client = new NettyAsyncHttpClientBuilder()
    .port(8080)
    .wiretap(true)
    .build();
```

### Prerequisites

- Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- An existing [Azure Key Vault][azure_keyvault]. If you need to create a Key Vault, you can use the [Azure Cloud Shell](https://shell.azure.com/bash) to create one with this Azure CLI command. Replace `<your-resource-group-name>` and `<your-key-vault-name>` with your own, unique names:

    ```Bash
    az keyvault create --resource-group <your-resource-group-name> --name <your-key-vault-name>
    ```

### Authenticate the client
In order to interact with the Key Vault service, you'll need to create an instance of the [KeyClient](#create-key-client) class. You would need a **vault url** and **client secret credentials (client id, client key, tenant id)** to instantiate a client object using the default `AzureCredential` examples shown in this document.

The `DefaultAzureCredential` way of authentication by providing client secret credentials is being used in this getting started section but you can find more ways to authenticate with [azure-identity][azure_identity].

 #### Create/Get credentials
To create/get client key credentials you can use the [Azure Portal][azure_create_application_in_portal], [Azure CLI][azure_keyvault_cli_full] or [Azure Cloud Shell](https://shell.azure.com/bash)

Here is [Azure Cloud Shell](https://shell.azure.com/bash) snippet below to

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
* Use the above returned credentials information to set **AZURE_CLIENT_ID**(appId), **AZURE_CLIENT_SECRET**(password) and **AZURE_TENANT_ID**(tenant) environment variables. The following example shows a way to do this in Bash:
  ```Bash
    export AZURE_CLIENT_ID="generated-app-ID"
    export AZURE_CLIENT_SECRET="random-password"
    export AZURE_TENANT_ID="tenant-ID"
  ```

* Grant the above mentioned application authorization to perform key operations on the keyvault:
    ```Bash
    az keyvault set-policy --name <your-key-vault-name> --spn $AZURE_CLIENT_ID --key-permissions backup delete get list set
    ```
    > --key-permissions:
    > Accepted values: backup, delete, get, list, purge, recover, restore, create

* Use the above mentioned Key Vault name to retreive details of your Vault which also contains your Key Vault URL:
    ```Bash
    az keyvault show --name <your-key-vault-name>
    ```

#### Create Key client
Once you've populated the **AZURE_CLIENT_ID**, **AZURE_CLIENT_SECRET** and **AZURE_TENANT_ID** environment variables and replaced **your-vault-url** with the above returned URI, you can create the KeyClient:

```Java
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;

KeyClient client = new KeyClientBuilder()
        .endpoint(<your-vault-url>)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();
```
> NOTE: For using Asynchronous client use KeyAsyncClient instead of KeyClient and call buildAsyncClient()


#### Create Cryptography Client
Once you've populated the **AZURE_CLIENT_ID**, **AZURE_CLIENT_SECRET** and **AZURE_TENANT_ID** environment variables and replaced **your-vault-url** with the above returned URI, you can create the CryptographyClient:

```Java
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;

// 1. Create client with json web key.
CryptographyClient cryptoClient = new CryptographyClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .jsonWebKey("<My-JWK>")
    .buildClient();
  
// 2. Create client with key identifier from key vault.
cryptoClient = new CryptographyClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .keyIdentifier("<Your-Key-Id-From-Keyvault>")
    .buildClient();
```
> NOTE: For using Asynchronous client use CryptographyAsyncClient instead of CryptographyClient and call buildAsyncClient()

## Key concepts
### Key
  Azure Key Vault supports multiple key types(`RSA` & `EC`) and algorithms, and enables the use of Hardware Security Modules (HSM) for high value keys. In addition to the key material, the following attributes may be specified:
* enabled: Specifies whether the key is enabled and useable for cryptographic operations.
* not_before: Identifies the time before which the key must not be used for cryptographic operations.
* expires: Identifies the expiration time on or after which the key MUST NOT be used for cryptographic operation.
* created: Indicates when this version of the key was created.
* updated: Indicates when this version of the key was updated.

### Key Client:
The Key client performs the interactions with the Azure Key Vault service for getting, setting, updating, deleting, and listing keys and its versions. An asynchronous and synchronous, KeyClient, client exists in the SDK allowing for selection of a client based on an application's use case. Once you've initialized a Key, you can interact with the primary resource types in Key Vault.

### Cryptography Client:
The Cryptography client performs the cryptographic operations locally or calls the Azure Key Vault service depending on how much key information is available locally. It supports encrypting, decrypting, signing, verifying, key wrapping, key unwrapping and retrieving the configured key. An asynchronous and synchronous, CryptographyClient, client exists in the SDK allowing for selection of a client based on an application's use case.


## Examples
### Sync API
The following sections provide several code snippets covering some of the most common Azure Key Vault Key Service tasks, including:
- [Create a Key](#create-a-key)
- [Retrieve a Key](#retrieve-a-key)
- [Update an existing Key](#update-an-existing-key)
- [Delete a Key](#delete-a-key)
- [List Keys](#list-keys)
- [Encrypt](#encrypt)
- [Decrypt](#decrypt)

### Create a Key

Create a Key to be stored in the Azure Key Vault.
- `setKey` creates a new key in the key vault. if the key with name already exists then a new version of the key is created.
```Java
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.KeyClient;

KeyClient keyClient = new KeyClientBuilder()
        .endpoint(<your-vault-url>)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

Key rsaKey = keyClient.createRsaKey(new RsaKeyCreateOptions("CloudRsaKey")
                .setExpires(OffsetDateTime.now().plusYears(1))
                .setKeySize(2048));
System.out.printf("Key is created with name %s and id %s \n", rsaKey.getName(), rsaKey.getId());

Key ecKey = keyClient.createEcKey(new EcKeyCreateOptions("CloudEcKey")
                .setCurve(KeyCurveName.P_256)
                .setExpires(OffsetDateTime.now().plusYears(1)));
System.out.printf("Key is created with name %s and id %s \n", ecKey.getName(), ecKey.getId());
```

### Retrieve a Key

Retrieve a previously stored Key by calling `getKey`.
```Java
Key key = keyClient.getKey("key_name");
System.out.printf("Key is returned with name %s and id %s \n", key.getName(), key.getId());
```

### Update an existing Key

Update an existing Key by calling `updateKey`.
```Java
// Get the key to update.
Key key = keyClient.getKey("key_name");
// Update the expiry time of the key.
key.getProperties().setExpires(OffsetDateTime.now().plusDays(30));
Key updatedKey = keyClient.updateKeyProperties(key.getProperties());
System.out.printf("Key's updated expiry time %s \n", updatedKey.getProperties().getExpires().toString());
```

### Delete a Key

Delete an existing Key by calling `deleteKey`.
```Java
DeletedKey deletedKey = client.deleteKey("key_name");
System.out.printf("Deleted Key's deletion date %s", deletedKey.getDeletedDate().toString());
```

### List Keys

List the keys in the key vault by calling `listKeys`.
```java
// List operations don't return the keys with key material information. So, for each returned key we call getKey to get the key with its key material information.
for (KeyProperties keyProperties : keyClient.listKeys()) {
    Key keyWithMaterial = keyClient.getKey(keyProperties);
    System.out.printf("Received key with name %s and type %s", keyWithMaterial.getName(),   keyWithMaterial.getKeyMaterial().getKty());
}

```

### Encrypt

Encrypt plain text by calling `encrypt`.
```java
CryptographyClient cryptoClient = new CryptographyClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .keyIdentifier("<Your-Key-Id-From-Keyvault")
    .buildClient();

byte[] plainText = new byte[100];
new Random(0x1234567L).nextBytes(plainText);

// Let's encrypt a simple plain text of size 100 bytes.
EncryptResult encryptResult = cryptoClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plainText);
System.out.printf("Returned cipherText size is %d bytes with algorithm %s \n", encryptResult.getCipherText().length, encryptResult.getAlgorithm().toString());
```

### Decrypt

Decrypt encrypted content by calling `decrypt`.

```java
byte[] plainText = new byte[100];
new Random(0x1234567L).nextBytes(plainText);
EncryptResult encryptResult = cryptoClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plainText);

//Let's decrypt the encrypted result.
DecryptResult decryptResult = cryptoClient.decrypt(EncryptionAlgorithm.RSA_OAEP, encryptResult.getCipherText());
System.out.printf("Returned plainText size is %d bytes \n", decryptResult.getPlainText().length);
```

### Async API
The following sections provide several code snippets covering some of the most common asynchronous Azure Key Vault Key Service tasks, including:
- [Create a Key Asynchronously](#create-a-key-asynchronously)
- [Retrieve a Key Asynchronously](#retrieve-a-key-asynchronously)
- [Update an existing Key Asynchronously](#update-an-existing-key-asynchronously)
- [Delete a Key Asynchronously](#delete-a-key-asynchronously)
- [List Keys Asynchronously](#list-keys-asynchronously)
- [Encrypt Asynchronously](#encryp-asynchronously)
- [Decrypt Asynchronously](#decrypt-asynchronously)


### Create a Key Asynchronously

Create a Key to be stored in the Azure Key Vault.
- `setKey` creates a new key in the key vault. if the key with name already exists then a new version of the key is created.
```Java
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.KeyAsyncClient;

KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
        .endpoint(<your-vault-url>)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildAsyncClient();

keyAsyncClient.createRsaKey(new RsaKeyCreateOptions("CloudRsaKey")
    .setExpires(OffsetDateTime.now().plusYears(1))
    .setKeySize(2048))
    .subscribe(key ->
       System.out.printf("Key is created with name %s and id %s \n", key.getName(), key.getId()));

keyAsyncClient.createEcKey(new EcKeyCreateOptions("CloudEcKey")
    .setExpires(OffsetDateTime.now().plusYears(1)))
    .subscribe(key ->
      System.out.printf("Key is created with name %s and id %s \n", key.getName(), key.getId()));
```

### Retrieve a Key Asynchronously

Retrieve a previously stored Key by calling `getKey`.
```Java
keyAsyncClient.getKey("keyName").subscribe(key ->
  System.out.printf("Key is returned with name %s and id %s \n", key.getName(), key.getId()));
```

### Update an existing Key Asynchronously

Update an existing Key by calling `updateKey`.
```Java
keyAsyncClient.getKey("keyName").subscribe(keyResponse -> {
     // Get the Key
     Key key = keyResponse;
     // Update the expiry time of the key.
     key.getProperties().setExpires(OffsetDateTime.now().plusDays(50));
     keyAsyncClient.updateKeyProperties(key.getProperties()).subscribe(updatedKey ->
         System.out.printf("Key's updated expiry time %s \n", updatedKey.getProperties().getExpires().toString()));
   });
```

### Delete a Key Asynchronously

Delete an existing Key by calling `deleteKey`.
```java
keyAsyncClient.deleteKey("keyName").subscribe(deletedKey ->
   System.out.printf("Deleted Key's deletion time %s \n", deletedKey.getDeletedDate().toString()));
```

### List Keys Asynchronously

List the keys in the key vault by calling `listKeys`.
```Java
// The List Keys operation returns keys without their value, so for each key returned we call `getKey` to get its // value as well.
keyAsyncClient.listKeys()
  .flatMap(keyAsyncClient::getKey).subscribe(key ->
    System.out.printf("Key returned with name %s and id %s \n", key.getName(), key.getId()));
```

### Encrypt Asynchronously

Encrypt plain text by calling `encrypt`.
```java
CryptographyAsyncClient cryptoAsyncClient = new CryptographyClientBuilder()
    .credential(new DefaultAzureCredentialBuilder().build())
    .keyIdentifier("<Your-Key-Id-From-Keyvault>")
    .buildAsyncClient();

byte[] plainText = new byte[100];
new Random(0x1234567L).nextBytes(plainText);

// Let's encrypt a simple plain text of size 100 bytes.
cryptoAsyncClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plainText)
    .subscribe(encryptResult -> {
        System.out.printf("Returned cipherText size is %d bytes with algorithm %s\n", encryptResult.getCipherText().length, encryptResult.getAlgorithm().toString());
    });
```

### Decrypt Asynchronously

Decrypt encrypted content by calling `decrypt`.
```java
byte[] plainText = new byte[100];
new Random(0x1234567L).nextBytes(plainText);

// Let's encrypt a simple plain text of size 100 bytes.
cryptoAsyncClient.encrypt(EncryptionAlgorithm.RSA_OAEP, plainText)
    .subscribe(encryptResult -> {
        System.out.printf("Returned cipherText size is %d bytes with algorithm %s\n", encryptResult.getCipherText().length, encryptResult.getAlgorithm().toString());
        //Let's decrypt the encrypted response.
        cryptoAsyncClient.decrypt(EncryptionAlgorithm.RSA_OAEP, encryptResult.getCipherText())
            .subscribe(decryptResult -> System.out.printf("Returned plainText size is %d bytes\n", decryptResult.getPlainText().length));
    });
```

## Troubleshooting
### General
Key Vault clients raise exceptions. For example, if you try to retrieve a key after it is deleted a `404` error is returned, indicating resource not found. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.
```java
try {
    keyClient.getKey("deletedKey")
} catch (ResourceNotFoundException e) {
    System.out.println(e.getMessage());
}
```

## Next steps
Several KeyVault Java SDK samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Key Vault:

### Hello World Samples
* [HelloWorld.java][sample_helloWorld] - and [HelloWorldAsync.java][sample_helloWorldAsync] - Contains samples for following scenarios:
    * Create a Key
    * Retrieve a Key
    * Update a Key
    * Delete a Key

### List Operations Samples
* [ListOperations.java][sample_list] and [ListOperationsAsync.java][sample_listAsync] - Contains samples for following scenarios:
    * Create a Key
    * List Keys
    * Create new version of existing key.
    * List versions of an existing key.

### Backup And Restore Operations Samples
* [BackupAndRestoreOperations.java][sample_BackupRestore] and [BackupAndRestoreOperationsAsync.java][sample_BackupRestoreAsync] - Contains samples for following scenarios:
    * Create a Key
    * Backup a Key -- Write it to a file.
    * Delete a key
    * Restore a key

### Managing Deleted Keys Samples:
* [ManagingDeletedKeys.java][sample_ManageDeleted] and [ManagingDeletedKeysAsync.java][sample_ManageDeletedAsync] - Contains samples for following scenarios:
    * Create a Key
    * Delete a key
    * List deleted keys
    * Recover a deleted key
    * Purge Deleted key
    
### Encrypt And Decrypt Operations Samples:
* [EncryptAndDecryptOperations.java][sample_encryptDecrypt] and [EncryptAndDecryptOperationsAsync.java][sample_encryptDecryptAsync] - Contains samples for following scenarios:
    * Encrypting plain text with asymmetric key
    * Decrypting plain text with asymmetric key
    * Encrypting plain text with symmetric key
    * Decrypting plain text with symmetric key
    
### Sign And Verify Operations Samples:
* [SignAndVerifyOperations.java][sample_signVerify] and [SignAndVerifyOperationsAsync.java][sample_signVerifyAsync] - Contains samples for following scenarios:
    * Signing a digest
    * Verifying signature against a digest
    * Signing raw data content
    * Verifyng signature against raw data content
    
### Key Wrap And Unwrap Operations Samples:
* [KeyWrapUnwrapOperations.java][sample_wrapUnwrap] and [KeyWrapUnwrapOperationsAsync.java][sample_wrapUnwrapAsync] - Contains samples for following scenarios:
    * Wrapping a key with asymmetric key
    * Unwrapping a key with asymmetric key
    * Wrapping a key with symmetric key
    * Unwrapping a key with symmetric key

###  Additional Documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- LINKS -->
[source_code]:  src
[api_documentation]: https://azure.github.io/azure-sdk-for-java/track2reports/index.html
[azkeyvault_docs]: https://docs.microsoft.com/azure/key-vault/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/master/identity/client
[maven]: https://maven.apache.org/
[azure_subscription]: https://azure.microsoft.com/
[azure_keyvault]: https://docs.microsoft.com/azure/key-vault/quick-create-portal
[azure_cli]: https://docs.microsoft.com/cli/azure
[rest_api]: https://docs.microsoft.com/rest/api/keyvault/
[azkeyvault_rest]: https://docs.microsoft.com/rest/api/keyvault/
[azure_create_application_in_portal]:https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal
[azure_keyvault_cli]:https://docs.microsoft.com/azure/key-vault/quick-create-cli
[azure_keyvault_cli_full]:https://docs.microsoft.com/cli/azure/keyvault?view=azure-cli-latest
[keys_samples]: src/samples/java/com/azure/security/keyvault/keys
[sample_helloWorld]: src/samples/java/com/azure/security/keyvault/keys/HelloWorld.java
[sample_helloWorldAsync]: src/samples/java/com/azure/security/keyvault/keys/HelloWorldAsync.java
[sample_list]: src/samples/java/com/azure/security/keyvault/keys/ListOperations.java
[sample_listAsync]: src/samples/java/com/azure/security/keyvault/keys/ListOperationsAsync.java
[sample_BackupRestore]: src/samples/java/com/azure/security/keyvault/keys/BackupAndRestoreOperations.java
[sample_BackupRestoreAsync]: src/samples/java/com/azure/security/keyvault/keys/BackupAndRestoreOperationsAsync.java
[sample_ManageDeleted]: src/samples/java/com/azure/security/keyvault/keys/ManagingDeletedKeys.java
[sample_ManageDeletedAsync]: src/samples/java/com/azure/security/keyvault/keys/ManagingDeletedKeysAsync.java
[sample_encryptDecrypt]: src/samples/java/com/azure/security/keyvault/keys/cryptography/EncryptDecryptOperations.java
[sample_encryptDecryptAsync]: src/samples/java/com/azure/security/keyvault/keys/cryptography/EncryptDecryptOperationsAsync.java
[sample_signVerify]: src/samples/java/com/azure/security/keyvault/keys/cryptography/SignVerifyOperations.java
[sample_signVerifyAsync]: src/samples/java/com/azure/security/keyvault/keys/cryptography/SignVerifyOperationsAsync.java
[sample_wrapUnwrap]: src/samples/java/com/azure/security/keyvault/keys/cryptography/KeyWrapUnwrapOperations.java
[sample_wrapUnwrapAsync]: src/samples/java/com/azure/security/keyvault/keys/cryptography/KeyWrapUnwrapOperationsAsync.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/keyvault/azure-keyvault-keys/README.png)
