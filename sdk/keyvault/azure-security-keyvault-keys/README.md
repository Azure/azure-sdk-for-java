# Azure Key Vault Key client library for Java
Azure Key Vault allows you to create and store keys in the Key Vault. Azure Key Vault client supports RSA keys and elliptic curve keys, each with corresponding support in hardware security modules (HSM).

 Multiple keys, and multiple versions of the same key, can be kept in the Key Vault. Cryptographic keys in Key Vault are represented as [JSON Web Key [JWK]](https://tools.ietf.org/html/rfc7517) objects. This library offers operations to create, retrieve, update, delete, purge, backup, restore and list the keys and its versions.


[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azkeyvault_docs] | [Samples][keys_samples]

## Getting started
### Adding the package to your project

Maven dependency for Azure Key Client library. Add it to your project's pom file.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-keys;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-keys</artifactId>
    <version>4.0.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Default HTTP Client
All client libraries, by default, use Netty HTTP client. Adding the above dependency will automatically configure 
KeyVault Keys to use Netty HTTP client. 

### Alternate HTTP client
If, instead of Netty it is preferable to use OkHTTP, there is a HTTP client available for that too. Exclude the default
Netty and include OkHTTP client in your pom.xml.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-keys;current})
```xml
<!-- Add KeyVault Keys dependency without Netty HTTP client -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-keys</artifactId>
    <version>4.0.1</version>
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
  <version>1.0.0</version>
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

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides better performance compared to the default SSL implementation within the JDK. For more information, including how to reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

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
    az keyvault set-policy --name <your-key-vault-name> --spn $AZURE_CLIENT_ID --key-permissions backup delete get list create
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
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.KeyClient;

KeyClient client = new KeyClientBuilder()
        .vaultUrl(<your-vault-url>)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();
```

> NOTE: For using Asynchronous client use KeyAsyncClient instead of KeyClient and call buildAsyncClient()


#### Create Cryptography Client
Once you've populated the **AZURE_CLIENT_ID**, **AZURE_CLIENT_SECRET** and **AZURE_TENANT_ID** environment variables and replaced **your-vault-url** with the above returned URI, you can create the CryptographyClient:

```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;

// Create client with key identifier from key vault.
CryptographyClient cryptoClient = new CryptographyClientBuilder()
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
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.KeyClient;

KeyClient keyClient = new KeyClientBuilder()
        .vaultUrl(<your-vault-url>)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

KeyVaultKey rsaKey = keyClient.createRsaKey(new CreateRsaKeyOptions("CloudRsaKey")
    .setExpiresOn(OffsetDateTime.now().plusYears(1))
    .setKeySize(2048));
System.out.printf("Key is created with name %s and id %s \n", rsaKey.getName(), rsaKey.getId());

KeyVaultKey ecKey = keyClient.createEcKey(new CreateEcKeyOptions("CloudEcKey")
    .setCurveName(KeyCurveName.P_256)
    .setExpiresOn(OffsetDateTime.now().plusYears(1)));
System.out.printf("Key is created with name %s and id %s \n", ecKey.getName(), ecKey.getId());
```

### Retrieve a Key

Retrieve a previously stored Key by calling `getKey`.

```Java
KeyVaultKey key = keyClient.getKey("key_name");
System.out.printf("Key is returned with name %s and id %s \n", key.getName(), key.getId());
```

### Update an existing Key

Update an existing Key by calling `updateKeyProperties`.

```Java
// Get the key to update.
KeyVaultKey key = keyClient.getKey("key_name");
// Update the expiry time of the key.
key.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(30));
KeyVaultKey updatedKey = keyClient.updateKeyProperties(key.getProperties());
System.out.printf("Key's updated expiry time %s \n", updatedKey.getProperties().getExpiresOn().toString());
```

### Delete a Key

Delete an existing Key by calling `beginDeleteKey`.

```Java
SyncPoller<DeletedKey, Void> deletedKeyPoller = keyClient.beginDeleteKey("keyName");

PollResponse<DeletedKey> deletedKeyPollResponse = deletedKeyPoller.poll();

// Deleted key is accessible as soon as polling begins
DeletedKey deletedKey = deletedKeyPollResponse.getValue();
System.out.println("Deleted Date  %s" + deletedKey.getDeletedOn().toString());

// Key is being deleted on server.
deletedKeyPoller.waitForCompletion();
```

### List Keys

List the keys in the key vault by calling `listPropertiesOfKeys`.

```java
// List operations don't return the keys with key material information. So, for each returned key we call getKey to get the key with its key material information.
for (KeyProperties keyProperties : keyClient.listPropertiesOfKeys()) {
    KeyVaultKey keyWithMaterial = keyClient.getKey(keyProperties.getName(), keyProperties.getVersion());
    System.out.printf("Received key with name %s and type %s %n", keyWithMaterial.getName(), keyWithMaterial.getKey().getKeyType());
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

> Note : You should add "System.in.read()" or "Thread.Sleep()" after the function calls in the main class/thread to allow Async functions/operations to execute and finish before the main application/thread exits.

### Create a Key Asynchronously

Create a Key to be stored in the Azure Key Vault.
- `setKey` creates a new key in the key vault. if the key with name already exists then a new version of the key is created.

```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.KeyAsyncClient;

KeyAsyncClient keyAsyncClient = new KeyClientBuilder()
        .vaultUrl(<your-vault-url>)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildAsyncClient();

keyAsyncClient.createRsaKey(new CreateRsaKeyOptions("CloudRsaKey")
    .setExpiresOn(OffsetDateTime.now().plusYears(1))
    .setKeySize(2048))
    .subscribe(key ->
        System.out.printf("Key is created with name %s and id %s \n", key.getName(), key.getId()));

keyAsyncClient.createEcKey(new CreateEcKeyOptions("CloudEcKey")
    .setExpiresOn(OffsetDateTime.now().plusYears(1)))
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

Update an existing Key by calling `updateKeyProperties`.

```Java
keyAsyncClient.getKey("keyName").subscribe(keyResponse -> {
     // Get the Key
     KeyVaultKey key = keyResponse;
     // Update the expiry time of the key.
     key.getProperties().setExpiresOn(OffsetDateTime.now().plusDays(50));
     keyAsyncClient.updateKeyProperties(key.getProperties()).subscribe(updatedKey ->
         System.out.printf("Key's updated expiry time %s \n", updatedKey.getProperties().getExpiresOn().toString()));
   });
```

### Delete a Key Asynchronously

Delete an existing Key by calling `beginDeleteKey`.

```java
keyAsyncClient.beginDeleteKey("keyName")
    .subscribe(pollResponse -> {
        System.out.println("Delete Status: " + pollResponse.getStatus().toString());
        System.out.println("Delete Key Name: " + pollResponse.getValue().getName());
        System.out.println("Key Delete Date: " + pollResponse.getValue().getDeletedOn().toString());
    });
```

### List Keys Asynchronously

List the keys in the key vault by calling `listPropertiesOfKeys`.

```Java
// The List Keys operation returns keys without their value, so for each key returned we call `getKey` to get its // value as well.
keyAsyncClient.listPropertiesOfKeys()
    .subscribe(keyProperties -> keyAsyncClient.getKey(keyProperties.getName(), keyProperties.getVersion())
        .subscribe(keyResponse -> System.out.printf("Received key with name %s and type %s",
            keyResponse.getName(),
             keyResponse.getKeyType())));
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

## Next steps Samples
Samples are explained in detail [here][samples_readme].

###  Additional Documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- LINKS -->
[source_code]:  src
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[azkeyvault_docs]: https://docs.microsoft.com/azure/key-vault/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/identity/azure-identity
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
[samples_readme]: src/samples/README.md
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fkeyvault%2Fazure-security-keyvault-keys%2FREADME.png)
