# Guide for migrating to azure-security-keyvault-certificates from azure-keyvault
This guide is intended to assist in the migration to `azure-security-keyvault-certificates` from `azure-keyvault`. It will focus on side-by-side comparisons for similar operations between the two packages.

Familiarity with the `azure-keyvault` package is assumed. For those new to the Key Vault Certificate client library for Java, please refer to the [README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/README.md) rather than this guide.

## Table of contents
- [Migration benefits](#migration-benefits)
- [Cross Service SDK improvements](#cross-service-sdk-improvements)
- [Important changes](#important-changes)
    - [Separate packages and clients](#separate-packages-and-clients)
    - [Package names and namespaces](#package-names-and-namespaces)
    - [Client instantiation](#client-instantiation)
    - [Authentication](#authentication)
    - [Common scenarios](#common-scenarios)
        - [Async operations](#async-operations)
        - [Create a certificate](#create-a-certificate)
        - [Import a certificate](#import-a-certificate)
        - [Retrieve a certificate](#retrieve-a-certificate)
        - [List properties of certificates](#list-properties-of-certificates)
        - [Delete a certificate](#delete-a-certificate)
- [Additional samples](#additional-samples)

## Migration benefits
A natural question to ask when considering whether or not to adopt a new version or library is what the benefits of doing so would be. As Azure has matured and been embraced by a more diverse group of developers, we have been focused on learning the patterns and practices to best support developer productivity and to understand the gaps that the Java client libraries have.

There were several areas of consistent feedback expressed across the Azure client library ecosystem. One of the most important is that the client libraries for different Azure services have not had a consistent approach to organization, naming, and API structure. Additionally, many developers have felt that the learning curve was difficult, and the APIs did not offer a good, approachable, and consistent onboarding story for those learning Azure or exploring a specific Azure service.

To try and improve the development experience across Azure services, a set of uniform [design guidelines](https://azure.github.io/azure-sdk/general_introduction.html) was created for all languages to drive a consistent experience with established API patterns for all services. A set of [Java-specific guidelines](https://azure.github.io/azure-sdk/java_introduction.html) was also introduced to ensure that Java clients have a natural and idiomatic feel with respect to the Java ecosystem. Further details are available in the guidelines for those interested.

### Cross Service SDK improvements
The modern Key Vault Certificate client library also provides the ability to share in some of the cross-service improvements made to the Azure development experience, such as:

- Using the new Azure Identity library to share a single authentication approach between clients.
- A unified logging and diagnostics pipeline offering a common view of the activities across each of the client libraries.

## Important changes
### Separate packages and clients
In the interest of simplifying the API for working with Key Vault certificates, keys and secrets, the `azure-keyvault` was split into separate packages:

- `azure-security-keyvault-certificates` contains `CertificateClient` for working with Key Vault certificates.
- [`azure-security-keyvault-keys`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md) contains `KeyClient` for working with Key Vault keys and `CryptographyClient` for performing cryptographic operations.
- [`azure-security-keyvault-secrets`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/README.md) contains `SecretClient` for working with Key Vault secrets.

### Package names and namespaces
Package names and the namespace root for the modern Azure client libraries for Java have changed. Each will follow the pattern `com.azure.<area>.<service>` where the legacy clients followed the pattern `com.microsoft.azure.<service>`. This provides a quick and accessible means to help understand, at a glance, whether you are using the modern or legacy clients.

In the case of the Key Vault, the modern client libraries have packages and namespaces that begin with `com.azure.security.keyvault` and were released beginning with version `4.0.0`. The legacy client libraries have packages and namespaces that begin with `com.microsoft.azure.keyvault` and a version of `1.x.x` or below.

### Client instantiation
Previously in `azure-keyvault` you could create a `KeyVaultClient`, via a public constructor that took an authentication delegate and could be used for multiple Key Vault endpoints.

```java
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;

KeyVaultCredentials keyVaultCredentials = new MyKeyVaultCredentials("<client-id>", "<client-key>");
KeyVaultClient keyVaultClient = new KeyVaultClient(keyVaultCredentials);
```

Now, across all modern Azure client libraries, client instances are created via builders, which consistently take an endpoint or connection string along with token credentials. This means that you can use a single client builder to instantiate multiple clients that share some configuration.

```java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;

TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
CertificateClientBuilder certificateClientBuilder = new CertificateClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(tokenCredential);

// Create a client.
CertificateClient someCertificateClient = certificateClientBuilder.buildClient();

// Create a client with the same configuration, plus some more.
CertificateClient anotherCertificateClient = certificateClientBuilder
    .addPolicy(new AddDatePolicy())
    .buildClient();
```

### Authentication
Previously in `azure-keyvault` you could create a `KeyVaultClient` by passing either a `KeyVaultCredential` or `RestClient` from `client-runtime`:

```java
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;

KeyVaultCredentials keyVaultCredentials = new MyKeyVaultCredentials("<client-id>", "<client-key>");
KeyVaultClient keyVaultClient = new KeyVaultClient(keyVaultCredentials);
```

Now in `azure-security-keyvault-certificates` you can create a `CertificateClient` using any credential from [`azure-identity`](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md). Below is an example using [`DefaultAzureCredential`](https://docs.microsoft.com/java/api/overview/azure/identity-readme?view=azure-java-stable#defaultazurecredential):

```java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;

TokenCredential tokenCredentials = new DefaultAzureCredentialBuilder().build();

CertificateClient certificateClient = new CertificateClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(tokenCredential)
    .buildClient();
```

### Async operations
The modern `azure-security-keyvault-certificates` library includes a complete set of async APIs that return [Project Reactor-based types](https://projectreactor.io/), as opposed to `azure-keyvault` async APIs that return either [Observable](https://reactivex.io/RxJava/javadoc/io/reactivex/Observable.html) or [ServiceFuture](https://azure.github.io/ref-docs/java/com/microsoft/rest/ServiceFuture.html).

Another difference is that async operations are available on their own separate async clients, which include the word `Async` in their name, like `CertificateAsyncClient`.

All modern Azure async clients can be created virtually the same way as sync clients, with the slight difference of calling `buildAsyncClient` on the client builder instead of `buildClient`:

```java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;

TokenCredential tokenCredential = new DefaultAzureCredentialBuilder().build();
CertificateClientBuilder certificateClientBuilder = new CertificateClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(tokenCredential);

// To create an async client.
CertificateAsyncClient certificateAsyncClient = certificateClientBuilder.buildAsyncClient();

// To create a sync client.
CertificateClient certificateClient = certificateClientBuilder.buildClient();
```

### Common scenarios
#### Create a certificate
In `azure-keyvault` you could create a certificate by using `KeyVaultClient`'s `createCertificate` method, which required a vault endpoint, certificate name, and certificate value. This method returned a `CertificateOperation`, which can be used to check for the status of the creation operation and obtain certificate details.

```java
String keyVaultUrl = "https://<your-key-vault-name>.vault.azure.net/";

CertificateOperation certificate = keyVaultClient.createCertificate(keyVaultUrl, "<certificate-name>");

// Get the status of the the creation operation
String status = certificateOperation.status();

// Get the id of the certificate
String certificateId = certificateOperation.id();
```

Now in `azure-security-keyvault-certificates` you can create a certificate by providing a certificate name and a custom management policy or a default policy to the `beginCreateCertificate` method. This method returns a long operation poller object that can be used to wait/check on the operation. Calling `poll` on the poller will return information about the created certificate (as a `KeyVaultCertificateWithPolicy`) without waiting for the operation to complete, but calling `waitForCompletion` will wait for the deletion to complete. Once completed, the certificate can be retrieved by calling `getFinalResult`.

```java
// Create a certificate by providing name and policy.
SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certificatePoller =
    certificateClient.beginCreateCertificate("<certificate-name>", CertificatePolicy.getDefault());

// You can poll for the status of the creation operation.
PollResponse<CertificateOperation> pollResponse = certificatePoller.poll();
CertificateOperation certificateOperation = pollResponse.getValue();
LongRunningOperationStatus status = certificateOperationPollResponse.getStatus();

// Wait for completion before attempting to get the certificate.
certificatePoller.waitForCompletion();
KeyVaultCertificate certificate = certificatePoller.getFinalResult();
```

#### Import a certificate
In `azure-keyvault` you could import a certificate by using `KeyVaultClient`'s `importCertificate` method, which required a vault endpoint, certificate name, and base64-encoded certificate contents. This method returned a `CertificateBundle`.

```java
CertificateBundle certificateBundle =
    keyVaultClient.importCertificate(keyVaultUrl, "<certificate-name>", "<base64-encoded-certificate-contents>");
```

Now in `azure-security-keyvault-certificates` you can import a certificate by providing the certificate name and contents to `importCertificate`, which returns a `KeyVaultCertificateWithPolicy`.

```java
byte[] certificateContents;

KeyVaultCertificateWithPolicy certificate =
    certificateClient.importCertificate(
        new ImportCertificateOptions("<certificate-name>", certificateContents));
```

#### Retrieve a certificate
In `azure-keyvault` you could retrieve a certificate (in a `CertificateBundle`) by using `getCertificate` in one of the following ways:

- Using the desired key vault endpoint and certificate name to get the latest version of a certificate.
- Using the desired key vault endpoint, certificate name and certificate version to get a specific certificate version.
- Using the certificate identifier to get a specific certificate version.

Additionally, you could list the properties of the versions of a certificate with the `getCertificateVersions` method, which returned a `PagedList` of `CertificateItem`.

```java
String keyVaultUrl = "https://<your-key-vault-name>.vault.azure.net/";
    
// Get a certificate's latest version.
CertificateBundle certificate = keyVaultClient.getCertificate(keyVaultUrl, "<certificate-name>");

// Get a certificate's specific version.
CertificateBundle certificateVersion = keyVaultClient.getCertificate(keyVaultUrl, "<certificate-name>", "<certificate-version>");

// Get a certificate's specific version using its id.
String certificateIdentifier = "https://<your-key-vault-name>.vault.azure.net/certificates/<certificate-name>/<certificate-version>";
CertificateBundle certificateWithId = keyVaultClient.getCertificate(certificateIdentifier);

// Get a key's versions.
PagedList<CertificateItem> certificateVersions = keyVaultClient.getCertificateVersions(keyVaultUrl, "<certificate-name>");
```

Now in `azure-security-keyvault-certificates` you can retrieve a certificate's latest version (as a `KeyVaultCertificate`) by providing the certificate name to the `getCertificate` method. You can also get a certificate's specific version by passing the certificate name and certificate version to `getCertificateVersion`.

Additionally, you con list the properties of the versions of a certificate with the `getCertificateVersions` method, which returned a `PagedIterable` of `CertificateProperties`.

```java
// Get a certificate's latest version.
KeyVaultCertificate certificate = certificateClient.getCertificate("<certificate-name>");

// Get a certificate's specific version.
KeyVaultCertificate certificateVersion = certificateClient.getCertificateVersion("<certificate-name>", "<certificate-version>");

// Get a certificates's versions' properties.
PagedIterable<CertificateProperties> certificateVersionsProperties = certificateClient.listPropertiesOfCertificateVersions("<certificate-name>");
```

#### List properties of certificates
In `azure-keyvault` you could list the properties of certificates in a specified vault with the `getCertificates` methods. This returned a `PagedList` containing `CertificateItem` instances.

```java
PagedList<CertificateItem> certificatesProperties = keyVaultClient.getCertificates(keyVaultUrl);
```

Now in `azure-security-keyvault-certificates` you can list the properties of certificates in a vault with the `listPropertiesOfCertificates` method. This returns an iterator-like object containing `CertificateProperties` instances.

```java
PagedIterable<CertificateProperties> certificatesProperties = certificateClient.listPropertiesOfCertificates();
```

#### Delete a certificate
In `azure-keyvault` you could delete all versions of a certificate with the `deleteCertificate` method. This returned information about the deleted certificate (as a `DeletedCertificateBundle`), but you could not poll the deletion operation to know when it completed. This would be valuable information if you intended to permanently delete the deleted certificate with `purgeDeletedCertificate`.

```java
DeletedCertificateBundle deletedCertificate = keyVaultClient.deleteCertificate(keyVaultUrl, "<certificate-name>");

// This purge would fail if deletion hadn't finished
keyVaultClient.purgeDeletedCertificate(keyVaultUrl, "<certificate-name>");
```

Now in `azure-security-keyvault-certificates` you can delete a certificate with `beginDeleteCertificate`, which returns a long operation poller object that can be used to wait/check on the operation. Calling `poll` on the poller will return information about the deleted certificate (as a `DeletedCertificate`) without waiting for the operation to complete, but calling `waitForCompletion` will wait for the deletion to complete. Again, `purgeDeletedCertificate` will permanently delete your deleted certificate and make it unrecoverable.

```java
SyncPoller<DeletedCertificate, Void> deletedCertificatePoller =
    certificateClient.beginDeleteCertificate("<certificate-name>");
PollResponse<DeletedCertificate> pollResponse = deletedCertificatePoller.poll();
DeletedCertificate deletedCertificate = pollResponse.getValue();

// Wait for completion before attempting to purge the certificate.
deletedCertificatePoller.waitForCompletion();
certificateClient.purgeDeletedCertificate("<certificate-name>");
```

## Additional samples
More examples can be found [here](https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/keyvault/azure-security-keyvault-certificates/src/samples).
