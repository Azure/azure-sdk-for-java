# Azure Key Vault Certificate client library for Java
Azure Key Vault allows you to securely manage and tightly control your certificates. The Azure Key Vault Certificate client library supports certificates backed by RSA and EC keys.

Multiple certificates and multiple versions of the same certificate can be kept in the Key Vault. Cryptographic keys in Key Vault backing the certificates are represented as [JSON Web Key [JWK]][jwk_specification] objects. This library offers operations to create, retrieve, update, delete, purge, backup, restore, and list the certificates, as well as its versions.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azkeyvault_docs] | [Samples][certificates_samples]

## Getting started
### Include the package

#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on the General Availability (GA) version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

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
        <artifactId>azure-security-keyvault-certificates</artifactId>
    </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-certificates;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-certificates</artifactId>
    <version>4.3.3</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Prerequisites
- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- An existing [Azure Key Vault][azure_keyvault]. If you need to create a Key Vault, you can use the [Azure Cloud Shell][azure_cloud_shell] to create one with this Azure CLI command. Replace `<your-resource-group-name>` and `<your-key-vault-name>` with your own, unique names:

```bash
az keyvault create --resource-group <your-resource-group-name> --name <your-key-vault-name>
```

### Authenticate the client
In order to interact with the Azure Key Vault service, you'll need to create an instance of the [CertificateClient](#create-certificate-client) class. You need a **vault url** and **client secret credentials (client id, client secret, tenant id)** to instantiate a client object using the `DefaultAzureCredential` examples shown in this document.

The `DefaultAzureCredential` way of authentication by providing client secret credentials is being used in this getting started section, but you can find more ways to authenticate with [azure-identity][azure_identity].

#### Create/Get credentials
To create/get client secret credentials you can use the [Azure Portal][azure_create_application_in_portal], [Azure CLI][azure_keyvault_cli_full] or [Azure Cloud Shell][azure_cloud_shell]

Here is an [Azure Cloud Shell][azure_cloud_shell] snippet below to

 * Create a service principal and configure its access to Azure resources:

```bash
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

```bash
export AZURE_CLIENT_ID="generated-app-ID"
export AZURE_CLIENT_SECRET="random-password"
export AZURE_TENANT_ID="tenant-ID"
```

* Grant the aforementioned application authorization to perform certificate operations on the Key Vault:

```bash
az keyvault set-policy --name <your-key-vault-name> --spn $AZURE_CLIENT_ID --certificate-permissions backup delete get list create update
```

> --certificate-permissions:
> Accepted values: backup, create, delete, deleteissuers, get, getissuers, import, list, listissuers, managecontacts, manageissuers, purge, recover, restore, setissuers, update

If you have enabled role-based access control (RBAC) for Key Vault instead, you can find roles like "Key Vault Certificates Officer" in our [RBAC guide][rbac_guide].

* Use the aforementioned Key Vault name to retrieve details of your Key Vault, which also contain your Key Vault URL:

```bash
az keyvault show --name <your-key-vault-name>
```

#### Create certificate client
Once you've populated the **AZURE_CLIENT_ID**, **AZURE_CLIENT_SECRET**, and **AZURE_TENANT_ID** environment variables and replaced **your-key-vault-url** with the URI returned above, you can create the CertificateClient:

```java readme-sample-createCertificateClient
CertificateClient certificateClient = new CertificateClientBuilder()
    .vaultUrl("<your-key-vault-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

> NOTE: For using an asynchronous client use CertificateAsyncClient instead of CertificateClient and call `buildAsyncClient()`

## Key concepts
### Certificate
Azure Key Vault supports certificates with secret content types (`PKCS12` & `PEM`). The certificate can be backed by keys in Azure Key Vault of types (`EC` & `RSA`). In addition to the certificate policy, the following attributes may be specified:
* enabled: Specifies whether the certificate is enabled and usable.
* created: Indicates when this version of the certificate was created.
* updated: Indicates when this version of the certificate was updated.

### Certificate client
The certificate client performs the interactions with the Azure Key Vault service for getting, setting, updating, deleting, and listing certificates and its versions. The client also supports CRUD operations for certificate issuers and contacts in the key vault. Asynchronous (CertificateAsyncClient) and synchronous (CertificateClient) clients exist in the SDK allowing for the selection of a client based on an application's use case. Once you've initialized a certificate, you can interact with the primary resource types in Azure Key Vault.

## Examples
### Sync API
The following sections provide several code snippets covering some of the most common Azure Key Vault Certificate service tasks, including:
- [Create a certificate](#create-a-certificate)
- [Retrieve a certificate](#retrieve-a-certificate)
- [Update an existing certificate](#update-an-existing-certificate)
- [Delete a certificate](#delete-a-certificate)
- [List certificates](#list-certificates)

### Create a certificate
Create a certificate to be stored in the Azure Key Vault.
- `beginCreateCertificate` creates a new certificate in the Azure Key Vault. If a certificate with the same name already exists then a new version of the certificate is created.

```java readme-sample-createCertificate
SyncPoller<CertificateOperation, KeyVaultCertificateWithPolicy> certificatePoller =
    certificateClient.beginCreateCertificate("certificateName", CertificatePolicy.getDefault());
certificatePoller.waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
KeyVaultCertificate certificate = certificatePoller.getFinalResult();
System.out.printf("Certificate created with name \"%s\"%n", certificate.getName());
```

### Retrieve a certificate
Retrieve a previously stored certificate by calling `getCertificate` or `getCertificateVersion`.

```java readme-sample-retrieveCertificate
KeyVaultCertificateWithPolicy certificate = certificateClient.getCertificate("<certificate-name>");
System.out.printf("Received certificate with name \"%s\", version %s and secret id %s%n",
    certificate.getProperties().getName(), certificate.getProperties().getVersion(), certificate.getSecretId());
```

### Update an existing certificate
Update an existing certificate by calling `updateCertificateProperties`.

```java readme-sample-updateCertificate
// Get the certificate to update.
KeyVaultCertificate certificate = certificateClient.getCertificate("<certificate-name>");
// Update certificate enabled status.
certificate.getProperties().setEnabled(false);
KeyVaultCertificate updatedCertificate = certificateClient.updateCertificateProperties(certificate.getProperties());
System.out.printf("Updated certificate with name \"%s\" and enabled status \"%s\"%n",
    updatedCertificate.getProperties().getName(), updatedCertificate.getProperties().isEnabled());
```

### Delete a certificate
Delete an existing certificate by calling `beginDeleteCertificate`.

```java readme-sample-deleteCertificate
SyncPoller<DeletedCertificate, Void> deleteCertificatePoller =
    certificateClient.beginDeleteCertificate("<certificate-name>");

// Deleted certificate is accessible as soon as polling beings.
PollResponse<DeletedCertificate> pollResponse = deleteCertificatePoller.poll();

// Deletion date only works for a SoftDelete-enabled Key Vault.
System.out.printf("Deleted certificate with name \"%s\" and recovery id %s", pollResponse.getValue().getName(),
    pollResponse.getValue().getRecoveryId());

// Certificate is being deleted on server.
deleteCertificatePoller.waitForCompletion();
```

### List certificates
List the certificates in the key vault by calling `listPropertiesOfCertificates`.

```java readme-sample-listCertificates
// List operations don't return the certificates with their full information. So, for each returned certificate we call
// getCertificate to get the certificate with all its properties excluding the policy.
for (CertificateProperties certificateProperties : certificateClient.listPropertiesOfCertificates()) {
    KeyVaultCertificate certificateWithAllProperties =
        certificateClient.getCertificateVersion(certificateProperties.getName(), certificateProperties.getVersion());
    System.out.printf("Received certificate with name \"%s\" and secret id %s",
        certificateWithAllProperties.getProperties().getName(), certificateWithAllProperties.getSecretId());
}
```

### Async API
The following sections provide several code snippets covering some of the most common asynchronous Azure Key Vault Certificate service tasks, including:
- [Create a certificate asynchronously](#create-a-certificate-asynchronously)
- [Retrieve a certificate asynchronously](#retrieve-a-certificate-asynchronously)
- [Update an existing certificate asynchronously](#update-an-existing-certificate-asynchronously)
- [Delete a certificate asynchronously](#delete-a-certificate-asynchronously)
- [List certificates asynchronously](#list-certificates-asynchronously)

> Note : You should add `System.in.read()` or `Thread.sleep()` after the function calls in the main class/thread to allow async functions/operations to execute and finish before the main application/thread exits.

### Create a certificate asynchronously
Create a certificate to be stored in the Azure Key Vault.
- `beginCreateCertificate` creates a new certificate in the Azure Key Vault. If a certificate with same name already exists then a new version of the certificate is created.

```java readme-sample-createCertificateAsync
// Creates a certificate using the default policy and polls on its progress.
certificateAsyncClient.beginCreateCertificate("<certificate-name>", CertificatePolicy.getDefault())
    .subscribe(pollResponse -> {
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println(pollResponse.getStatus());
        System.out.println(pollResponse.getValue().getStatus());
        System.out.println(pollResponse.getValue().getStatusDetails());
    });
```

### Retrieve a certificate asynchronously
Retrieve a previously stored certificate by calling `getCertificate` or `getCertificateVersion`.

```java readme-sample-retrieveCertificateAsync
certificateAsyncClient.getCertificate("<certificate-name>")
    .subscribe(certificateResponse ->
        System.out.printf("Certificate was returned with name \"%s\" and secretId %s%n",
            certificateResponse.getProperties().getName(), certificateResponse.getSecretId()));
```

### Update an existing certificate asynchronously
Update an existing certificate by calling `updateCertificateProperties`.

```java readme-sample-updateCertificateAsync
certificateAsyncClient.getCertificate("<certificate-name>")
    .flatMap(certificate -> {
        // Update enabled status of the certificate.
        certificate.getProperties().setEnabled(false);
        return certificateAsyncClient.updateCertificateProperties(certificate.getProperties());
    }).subscribe(certificateResponse -> System.out.printf("Certificate's enabled status: %s%n",
        certificateResponse.getProperties().isEnabled()));
```

### Delete a certificate asynchronously
Delete an existing certificate by calling `beginDeleteCertificate`.

```java readme-sample-deleteCertificateAsync
certificateAsyncClient.beginDeleteCertificate("<certificate-name>")
    .subscribe(pollResponse -> {
        System.out.printf("Deletion status: %s%n", pollResponse.getStatus());
        System.out.printf("Deleted certificate name: %s%n", pollResponse.getValue().getName());
        System.out.printf("Certificate deletion date: %s%n", pollResponse.getValue().getDeletedOn());
    });
```

### List certificates asynchronously
List the certificates in the Azure Key Vault by calling `listPropertiesOfCertificates`.

```java readme-sample-listCertificateAsync
// The List Certificates operation returns certificates without their full properties, so for each certificate returned
// we call `getCertificate` to get all its attributes excluding the policy.
certificateAsyncClient.listPropertiesOfCertificates()
    .flatMap(certificateProperties -> certificateAsyncClient
        .getCertificateVersion(certificateProperties.getName(), certificateProperties.getVersion()))
    .subscribe(certificateResponse ->
        System.out.printf("Received certificate with name \"%s\" and key id %s", certificateResponse.getName(),
            certificateResponse.getKeyId()));
```

## Troubleshooting
See our [troubleshooting guide](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/TROUBLESHOOTING.md) for details on how to diagnose various failure scenarios.

### General
Azure Key Vault Certificate clients raise exceptions. For example, if you try to retrieve a certificate after it is deleted a `404` error is returned, indicating the resource was not found. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.

```java readme-sample-troubleshooting
try {
    certificateClient.getCertificate("<deleted-certificate-name>");
} catch (ResourceNotFoundException e) {
    System.out.println(e.getMessage());
}
```

### Default HTTP client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the [HTTP clients wiki][http_clients_wiki].

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL operations. The Boring SSL library is an Uber JAR containing native libraries for Linux / macOS / Windows, and provides better performance compared to the default SSL implementation within the JDK. For more information, including how to reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Next steps
Several Key Vault Java SDK samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Key Vault.

## Next steps samples
Samples are explained in detail [here][samples_readme].

### Additional documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][microsoft_code_of_conduct]. For more information see the Code of Conduct FAQ or contact <opencode@microsoft.com> with any additional questions or comments.

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/src
[api_documentation]: https://azure.github.io/azure-sdk-for-java
[azkeyvault_docs]: https://docs.microsoft.com/azure/key-vault/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[maven]: https://maven.apache.org/
[azure_subscription]: https://azure.microsoft.com/
[azure_keyvault]: https://docs.microsoft.com/azure/key-vault/certificates/quick-create-portal
[azure_cli]: https://docs.microsoft.com/cli/azure
[rest_api]: https://docs.microsoft.com/rest/api/keyvault/
[azkeyvault_rest]: https://docs.microsoft.com/rest/api/keyvault/
[azure_create_application_in_portal]: https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal
[azure_keyvault_cli]: https://docs.microsoft.com/azure/key-vault/quick-create-cli
[azure_keyvault_cli_full]: https://docs.microsoft.com/cli/azure/keyvault?view=azure-cli-latest
[certificates_samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/src/samples/java/com/azure/security/keyvault/certificates
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/src/samples/README.md
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[jdk_link]: https://docs.microsoft.com/java/azure/jdk/?view=azure-java-stable
[azure_cloud_shell]: https://shell.azure.com/bash
[jwk_specification]: https://tools.ietf.org/html/rfc7517
[http_clients_wiki]: https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients
[microsoft_code_of_conduct]: https://opensource.microsoft.com/codeofconduct/
[rbac_guide]: https://docs.microsoft.com/azure/key-vault/general/rbac-guide

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fkeyvault%2Fazure-security-keyvault-certificates%2FREADME.png)
