# Azure Key Vault Certificate client library for Java
Azure Key Vault allows you to create and store certificates in the Key Vault. Azure Key Vault client supports certificates backed by Rsa keys and Ec keys. It allows you to securely manage, tightly control your certificates.

 Multiple certificates, and multiple versions of the same certificate, can be kept in the Key Vault. Cryptographic keys in Key Vault backing the certificates are represented as [JSON Web Key [JWK]](https://tools.ietf.org/html/rfc7517) objects. This library offers operations to create, retrieve, update, delete, purge, backup, restore and list the certificates and its versions.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azkeyvault_docs] | [Samples][certificates_samples]

## Getting started
### Adding the package to your project

Maven dependency for Azure Key Client library. Add it to your project's pom file.
[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-certificates;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-certificates</artifactId>
    <version>4.0.0-preview.5</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Default HTTP Client
All client libraries, by default, use Netty HTTP client. Adding the above dependency will automatically configure 
KeyVault Certificates to use Netty HTTP client. 

### Alternate HTTP client
If, instead of Netty it is preferable to use OkHTTP, there is a HTTP client available for that too. Exclude the default
Netty and include OkHTTP client in your pom.xml.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-certificates;current})
```xml
<!-- Add KeyVault Certificates dependency without Netty HTTP client -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-certificates</artifactId>
    <version>4.0.0-preview.5</version>
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
<!-- Add OkHTTP client to use with KeyVault Certificates -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-okhttp</artifactId>
  <version>1.0.0-preview.7</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Configuring HTTP Clients
When an HTTP client is included on the classpath, as shown above, it is not necessary to specify it in the client library [builders](#create-certificate-client), unless you want to customize the HTTP client in some fashion. If this is desired, the `httpClient` builder method is often available to achieve just this, by allowing users to provide a custom (or customized) `com.azure.core.http.HttpClient` instances.

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
In order to interact with the Key Vault service, you'll need to create an instance of the [CertificateClient](#create-certificate-client) class. You would need a **vault url** and **client secret credentials (client id, client key, tenant id)** to instantiate a client object using the default `AzureCredential` examples shown in this document.

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
    az keyvault set-policy --name <your-key-vault-name> --spn $AZURE_CLIENT_ID --certificate-permissions backup delete get list create
    ```
    > --certificate-permissions:
    > Accepted values: backup, create, delete, deleteissuers, get, getissuers, import, list, listissuers, managecontacts, manageissuers, purge, recover, restore, setissuers, update

* Use the above mentioned Key Vault name to retreive details of your Vault which also contains your Key Vault URL:
    ```Bash
    az keyvault show --name <your-key-vault-name>
    ```

#### Create Certificate client
Once you've populated the **AZURE_CLIENT_ID**, **AZURE_CLIENT_SECRET** and **AZURE_TENANT_ID** environment variables and replaced **your-vault-url** with the above returned URI, you can create the CertificateClient:

```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.CertificateClient;

CertificateClient client = new CertificateClientBuilder()
        .endpoint(<your-vault-url>)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();
```
> NOTE: For using Asynchronous client use CertificateAsyncClient instead of CertificateClient and call buildAsyncClient()

## Key concepts
### Certificate
  Azure Key Vault supports certificates with secret content types(`PKCS12` & `PEM`). The certificate can be backed by keys in key vault of types(`EC` & `RSA`). In addition to the certificate policy, the following attributes may be specified:
* enabled: Specifies whether the certificate is enabled and useable.
* created: Indicates when this version of the certificate was created.
* updated: Indicates when this version of the certificate was updated.

### Certificate Client:
The Certificate client performs the interactions with the Azure Key Vault service for getting, setting, updating, deleting, and listing certificates and its versions. The client also supports CRUD operations for certificate issuers and contacts in the key vault. An asynchronous and synchronous, CertificateClient, client exists in the SDK allowing for selection of a client based on an application's use case. Once you've initialized a Certificate, you can interact with the primary resource types in Key Vault.

## Examples
### Sync API
The following sections provide several code snippets covering some of the most common Azure Key Vault Key Service tasks, including:
- [Create a Certificate](#create-a-certificate)
- [Retrieve a Certificate](#retrieve-a-certificate)
- [Update an existing Certificate](#update-an-existing-certificate)
- [Delete a Certificate](#delete-a-certificate)
- [List Certificates](#list-certificates)

### Create a Certificate

Create a Certificate to be stored in the Azure Key Vault.
- `beginCreateCertificate` creates a new certificate in the key vault. if the certificate with name already exists then a new version of the certificate is created.
```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.Certificate;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.CertificateClient;

CertificateClient certificateClient = new CertificateClientBuilder()
        .endpoint(<your-vault-url>)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();

CertificatePolicy certificatePolicyPkcsSelf = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12");
SyncPoller<CertificateOperation, Certificate> certPoller = certificateClient.beginCreateCertificate("certificateName", certificatePolicyPkcsSelf);
certPoller.waitUntil(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED);
Certificate cert = certPoller.getFinalResult();
System.out.printf("Certificate created with name %s", cert.getName());
```

### Retrieve a Certificate

Retrieve a previously stored Certificate by calling `getCertificate` or `getCertificateWithPolicy`.
```Java
Certificate certificate = certificateClient.getCertificateWithPolicy("certificateName");
System.out.printf("Recevied certificate with name %s and version %s and secret id %s", certificate.getName(),
    certificate.getProperties().getVersion(), certificate.getSecretId());
```

### Update an existing Certificate

Update an existing Certificate by calling `updateCertificate`.
```Java
// Get the certificate to update.
Certificate certificate = certificateClient.getCertificateWithPolicy("certificateName");
Map<String, String> tags = new HashMap<>();
tags.put("foo", "bar");
// Update certificate enabled status
certificate.getProperties().setEnabled(false);
Certificate updatedCertificate = certificateClient.updateCertificateProperties(certificate.getProperties());
System.out.printf("Updated Certificate with name %s and enabled status %s", updatedCertificate.getName(),
    updatedCertificate.getProperties().isEnabled());
```

### Delete a Certificate

Delete an existing Certificate by calling `deleteCertificate`.
```Java
DeletedCertificate deletedCertificate = certificateClient.deleteCertificate("certificateName");
System.out.printf("Deleted certificate with name %s and recovery id %s", deletedCertificate.getName(),
    deletedCertificate.getRecoveryId());
```

### List Certificates

List the certificates in the key vault by calling `listCertificates`.
```java
// List operations don't return the certificates with their full information. So, for each returned certificate we call getCertificate to get the certificate with all its properties excluding the policy.
for (CertificateProperties certificateProperties : certificateClient.listCertificates()) {
    Certificate certificateWithAllProperties = certificateClient.getCertificate(certificateProperties);
    System.out.printf("Received certificate with name %s and secret id %s", certificateWithAllProperties.getName(),
        certificateWithAllProperties.getSecretId());
}
```

### Async API
The following sections provide several code snippets covering some of the most common asynchronous Azure Key Vault Key Service tasks, including:
- [Create a Certificate Asynchronously](#create-a-certificate-asynchronously)
- [Retrieve a Certificate Asynchronously](#retrieve-a-certificate-asynchronously)
- [Update an existing Certificate Asynchronously](#update-an-existing-certificate-asynchronously)
- [Delete a Certficate Asynchronously](#delete-a-certificate-asynchronously)
- [List Certificates Asynchronously](#list-certificates-asynchronously)

> Note : You should add "System.in.read()" or "Thread.Sleep()" after the function calls in the main class/thread to allow Async functions/operations to execute and finish before the main application/thread exits.

### Create a Certificate Asynchronously

Create a Certificate to be stored in the Azure Key Vault.
- `beginCreateCertificate` creates a new key in the key vault. if the certificate with name already exists then a new version of the certificate is created.
```Java
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.Certificate;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.CertificateAsyncClient;

CertificatePolicy policy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12");
Map<String, String> tags = new HashMap<>();
tags.put("foo", "bar");
//Creates a certificate and polls on its progress.
CertificatePolicy policy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12");
certificateAsyncClient.beginCreateCertificate("myCertificate", policy)
    .subscribe(pollResponse -> {
        System.out.println("---------------------------------------------------------------------------------");
        System.out.println(pollResponse.getStatus());
        System.out.println(pollResponse.getValue().getStatus());
        System.out.println(pollResponse.getValue().getStatusDetails());
    });
```

### Retrieve a Certificate Asynchronously

Retrieve a previously stored Certificate by calling `getCertificateWithPolicy` or `getCertificate`.
```Java
certificateAsyncClient.getCertificateWithPolicy("certificateName")
    .subscribe(certificateResponse ->
        System.out.printf("Certificate is returned with name %s and secretId %s %n", certificateResponse.getName(),
            certificateResponse.getSecretId()));
```

### Update an existing Certificate Asynchronously

Update an existing Certificate by calling `updateCertificate`.
```Java
certificateAsyncClient.getCertificateWithPolicy("certificateName")
    .subscribe(certificateResponseValue -> {
        Certificate certificate = certificateResponseValue;
        //Update enabled status of the certificate
        certificate.getProperties().setEnabled(false);
        certificateAsyncClient.updateCertificateProperties(certificate.getProperties())
            .subscribe(certificateResponse ->
                System.out.printf("Certificate's enabled status %s \n",
                    certificateResponse.getProperties().isEnabled().toString()));
    });
```

### Delete a Certificate Asynchronously

Delete an existing Certificate by calling `deleteCertificate`.
```java
certificateAsyncClient.deleteCertificate("certificateName")
    .subscribe(deletedSecretResponse ->
        System.out.printf("Deleted Certificate's Recovery Id %s \n", deletedSecretResponse.getRecoveryId()));
```

### List Certificates Asynchronously

List the certificates in the key vault by calling `listCertificates`.
```Java
// The List Certificates operation returns certificates without their full properties, so for each certificate returned we call `getCertificate` to get all its attributes excluding the policy.
certificateAsyncClient.listCertificates()
    .subscribe(certificateProperties -> certificateAsyncClient.getCertificate(certificateProperties)
        .subscribe(certificateResponse -> System.out.printf("Received certificate with name %s and key id %s",
            certificateResponse.getName(), certificateResponse.getKeyId())));
```

## Troubleshooting
### General
Certificate Vault clients raise exceptions. For example, if you try to retrieve a certificate after it is deleted a `404` error is returned, indicating resource not found. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.
```java
try {
    certificateClient.getCertificate("certificateName")
} catch (ResourceNotFoundException e) {
    System.out.println(e.getMessage());
}
```

## Next steps
Several KeyVault Java SDK samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Key Vault:

### Hello World Samples
* [HelloWorld.java][sample_helloWorld] - and [HelloWorldAsync.java][sample_helloWorldAsync] - Contains samples for following scenarios:
    * Create a Certificate & Certificate Issuer
    * Retrieve a Certificate & Certificate Issuer
    * Update a Certificate
    * Delete a Certificate

### List Operations Samples
* [ListOperations.java][sample_list] and [ListOperationsAsync.java][sample_listAsync] - Contains samples for following scenarios:
    * Create a Certificate, Certificate Issuer & Certificate Contact
    * List Certificates, Certificate Issuers & Certificate Contacts
    * Create new version of existing certificate.
    * List versions of an existing certificate.

### Backup And Restore Operations Samples
* [BackupAndRestoreOperations.java][sample_BackupRestore] and [BackupAndRestoreOperationsAsync.java][sample_BackupRestoreAsync] - Contains samples for following scenarios:
    * Create a Certificate
    * Backup a Certificate -- Write it to a file.
    * Delete a certificate
    * Restore a certificate

### Managing Deleted Certificates Samples:
* [ManagingDeletedCertificates.java][sample_ManageDeleted] and [ManagingDeletedCertificatesAsync.java][sample_ManageDeletedAsync] - Contains samples for following scenarios:
    * Create a Certificate
    * Delete a certificate
    * List deleted certificates
    * Recover a deleted certificate
    * Purge Deleted certificate

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
[certificates_samples]: src/samples/java/com/azure/security/keyvault/certificates
[sample_helloWorld]: src/samples/java/com/azure/security/keyvault/certificates/HelloWorld.java
[sample_helloWorldAsync]: src/samples/java/com/azure/security/keyvault/certificates/HelloWorldAsync.java
[sample_list]: src/samples/java/com/azure/security/keyvault/certificates/ListOperations.java
[sample_listAsync]: src/samples/java/com/azure/security/keyvault/certificates/ListOperationsAsync.java
[sample_BackupRestore]: src/samples/java/com/azure/security/keyvault/certificates/BackupAndRestoreOperations.java
[sample_BackupRestoreAsync]: src/samples/java/com/azure/security/keyvault/certificates/BackupAndRestoreOperationsAsync.java
[sample_ManageDeleted]: src/samples/java/com/azure/security/keyvault/certificates/ManagingDeletedCertificates.java
[sample_ManageDeletedAsync]: src/samples/java/com/azure/security/keyvault/certificates/ManagingDeletedCertificatesAsync.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/keyvault/azure-security-keyvault-certificates/README.png)
