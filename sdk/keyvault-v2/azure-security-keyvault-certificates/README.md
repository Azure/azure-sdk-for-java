# Azure Key Vault Certificates client library for Java

Azure Key Vault allows you to securely manage and tightly control your certificates. The Azure Key Vault Certificates
client library supports certificates backed by RSA and EC keys.

Multiple certificates and multiple versions of the same certificate can be kept in the key vault. Cryptographic keys in
Azure Key Vault backing the certificates are represented as [JSON Web Key (JWK)][jwk_specification] objects. This
library offers operations to create, retrieve, update, delete, purge, backup, restore, and list the certificates, as
well as its versions.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][azure_keyvault_docs] | [Samples][certificates_samples]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
  - Here are details about [Java 8 client compatibility with Azure Certificate Authority][azure_ca]
- An [Azure Subscription][azure_subscription].
- An existing [Azure Key Vault][azure_keyvault]. If you need to create a key vault, you can do so in the Azure Portal by
  following the steps in [this document][azure_keyvault_portal]. Alternatively, you can use the Azure CLI by following the
  steps in [this document][azure_keyvault_cli].

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
        <artifactId>azure-security-keyvault-certificates</artifactId>
    </dependency>
</dependencies>
```

#### Include direct dependency

If you want to take dependency on a particular version of the library that is not present in the BOM, add the direct
dependency to your project as follows.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-certificates;current})

```xml
<dependency>
    <groupId>com.azure.v2</groupId>
    <artifactId>azure-security-keyvault-certificates</artifactId>
    <version>5.0.0-beta.1</version>
</dependency>
```

[//]: # ({x-version-update-end})

### Authentication

In order to interact with the Azure Key Vault service, you will need to create an instance of the
[`CertificateClient`](#create-certificate-client) class, a vault **endpoint** and a credential object. The examples
shown in this document use a credential object named  [`DefaultAzureCredential`][default_azure_credential], which is
appropriate for most scenarios, including local development and production environments. Additionally, we recommend
using a [managed identity][managed_identity] for authentication in production environments.

You can find more information on different ways of authenticating and their corresponding credential types in the
[Azure Identity documentation][azure_identity].

#### Create certificate client

Once you perform [the authentication set up that suits you best][default_azure_credential] and replaced\
**your-key-vault-endpoint** with the URL for your key vault, you can create the `CertificateClient`:

```java readme-sample-createCertificateClient
CertificateClient certificateClient = new CertificateClientBuilder()
    .endpoint("<your-key-vault-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

## Key concepts

### Certificate

Azure Key Vault supports certificates with secret content types (`PKCS12` & `PEM`). The certificate can be backed by
keys in Azure Key Vault of types (`EC` & `RSA`). In addition to the certificate policy, the following attributes may be
specified:

- **enabled:** Specifies whether the certificate is enabled and usable.
- **created:** Indicates when this version of the certificate was created.
- **updated:** Indicates when this version of the certificate was updated.

### Certificate client

The certificate client performs the interactions with the Azure Key Vault service for getting, setting, updating,
deleting, and listing certificates and its versions. The client also supports CRUD operations for certificate issuers
and contacts in the key vault. Once you've initialized a certificate, you can interact with the primary resource types
in Azure Key Vault.

## Examples

The following sections provide several code snippets covering some of the most common Azure Key Vault service tasks,
including:

- [Create a certificate](#create-a-certificate)
- [Retrieve a certificate](#retrieve-a-certificate)
- [Update an existing certificate](#update-an-existing-certificate)
- [Delete a certificate](#delete-a-certificate)
- [List certificates](#list-certificates)

### Create a certificate

Create a certificate to be stored in the key vault. `beginCreateCertificate` creates a new certificate in the key vault.
If a certificate with the same name already exists then a new version of the certificate is created.

```java readme-sample-createCertificate
Poller<CertificateOperation, KeyVaultCertificateWithPolicy> certificatePoller =
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
Poller<DeletedCertificate, Void> deleteCertificatePoller =
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
        certificateClient.getCertificate(certificateProperties.getName(), certificateProperties.getVersion());
    System.out.printf("Received certificate with name \"%s\" and secret id %s",
        certificateWithAllProperties.getProperties().getName(), certificateWithAllProperties.getSecretId());
}
```

## Troubleshooting

See our [troubleshooting guide][troubleshooting_guide] for details on how to diagnose various failure scenarios.

### General

Azure Key Vault clients raise exceptions. For example, if you try to retrieve a certificate after it is deleted a `404`
error is returned, indicating the resource was not found. In the following snippet, the error is handled gracefully by
catching the exception and displaying additional information about the error.

```java readme-sample-troubleshooting
try {
    certificateClient.getCertificate("<deleted-certificate-name>");
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
[azure_keyvault_docs]: https://learn.microsoft.com/azure/key-vault/
[azure_keyvault_rest]: https://learn.microsoft.com/rest/api/keyvault/
[azure_ca]: https://learn.microsoft.com/azure/security/fundamentals/azure-ca-details?tabs=root-and-subordinate-cas-list#client-compatibility-for-public-pkis
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[azure_keyvault]: https://learn.microsoft.com/azure/key-vault/general/overview
[azure_keyvault_cli]: https://learn.microsoft.com/azure/key-vault/general/quick-create-cli
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
[certificates_samples]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-certificates/src/samples/java/com/azure/v2/security/keyvault/certificates
[samples_readme]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-certificates/src/samples/README.md
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[source_code]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-certificates/src
[troubleshooting_guide]: https://github.com/vcolin7/azure-sdk-for-java/blob/feature/vicolina/keyvault/v2/sdk/keyvault-v2/azure-security-keyvault-certificates/TROUBLESHOOTING.md
