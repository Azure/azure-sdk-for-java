
## Azure Azure Storage Blob Samples client library for Java
This document explains samples and how to use them.

## Key concepts
### Certificate
  Azure Key Vault supports certificates with secret content types(`PKCS12` & `PEM`). The certificate can be backed by keys in key vault of types(`EC` & `RSA`). In addition to the certificate policy, the following attributes may be specified:
* enabled: Specifies whether the certificate is enabled and useable.
* created: Indicates when this version of the certificate was created.
* updated: Indicates when this version of the certificate was updated.

### Certificate Client:
The Certificate client performs the interactions with the Azure Key Vault service for getting, setting, updating, deleting, and listing certificates and its versions. The client also supports CRUD operations for certificate issuers and contacts in the key vault. An asynchronous and synchronous, CertificateClient, client exists in the SDK allowing for selection of a client based on an application's use case. Once you've initialized a Certificate, you can interact with the primary resource types in Key Vault.

# Samples Azure Key Vault Keys APIs
This document describes how to use samples and what is done in each sample.

## Getting started

### Adding the package to your project

Maven dependency for Azure Storage blob Client library. Add it to your project's pom file.

[//]: # ({x-version-update-start;com.azure:azure-storage-blob;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-blob</artifactId>
    <version>12.0.0-preview.5</version>
</dependency>
```
[//]: # ({x-version-update-end})

For details on including this dependency in other build tools (Gradle, SBT, etc), refer [here](https://search.maven.org/artifact/com.azure/azure-core).

## How to run
These sample can be run in your IDE with default JDK.

## Examples
   Following section document various examples.

1. [Basic Examples][samples_basic]: Create storage, container and blob clients. Upload, download and list blobs.
2. [File Transfer Examples][samples_file_transfer]: Upload and download a large file through blobs.
3. [Storage Error Examples][samples_storage_error]: Handle the exceptions thrown from the Storage Blob service side.
4. [List Container Examples][samples_list_containers]: Create, list and delete containers.
5. [Set Metadata and HTTPHeaders Examples][samples_metadata]: Set metadata for containers and blobs, and set HTTPHeaders for blobs.
6. [Azure Identity Examples][samples_identity]: Use `DefaultAzureCredential` to do the authentication.

## Troubleshooting
### General
When interacting with blobs using this Java client library, errors returned by the service correspond to the same HTTP
status codes returned for [REST API][error_codes] requests. For example, if you try to retrieve a container or blob that
doesn't exist in your Storage Account, a `404` error is returned, indicating `Not Found`

## Next steps
Start using Storage blob Java SDK in your solutions. Our SDK details could be found at [SDK README] [BLOB_SDK_README]. 

###  Additional Documentation
For more extensive documentation on Azure Storage blob, see the [API reference documentation][storageblob_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- LINKS -->
[source_code]:  src
[BLOB_SDK_README]: ../../README.md
[samples_basic]: java/com/azure/storage/blob/BasicExample.java
[samples_file_transfer]: java/com/azure/storage/blob/FileTransferExample.java
[samples_storage_error]: java/com/azure/storage/blob/StorageErrorHandlingExample.java
[samples_list_containers]: java/com/azure/storage/blob/ListContainersExample.java
[samples_metadata]: java/com/azure/storage/blob/SetMetadataAndHTTPHeadersExample.java
[samples_identity]: java/com/azure/storage/blob/AzureIdentityExample.java
[storageblob_rest]: https://docs.microsoft.com/en-us/rest/api/storageservices/blob-service-rest-api
[error_codes]: https://docs.microsoft.com/rest/api/storageservices/blob-service-error-codes
![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/storage/azure-storage-blob/README.png)
