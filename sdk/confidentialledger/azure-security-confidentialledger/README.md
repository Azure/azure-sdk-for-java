# Azure Confidential Ledger client library for Java

Azure Confidential Ledger provides a service for logging to an immutable, tamper-proof ledger. As part of the [Azure Confidential Computing][azure_confidential_computing]
portfolio, Azure Confidential Ledger runs in SGX enclaves. It is built on Microsoft Research's [Confidential Consortium Framework][ccf].

[Source code][source_code] | [Package (Maven)][package] | [Product Documentation][product_documentation] | [Samples][samples_readme]

## Getting started

### Prerequisites

- A [Java Development Kit (JDK)][jdk_link], version 8 or later.
- [Azure Subscription][azure_subscription]
- A running instance of Azure Confidential Ledger.
- A registered user in the Confidential Ledger, typically assigned during [ARM][azure_resource_manager] resource creation, with `Administrator` privileges.

### Include the Package

[//]: # ({x-version-update-start;com.azure:azure-security-confidentialledger;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-security-confidentialledger</artifactId>
  <version>1.1.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authenticate the client

#### Using Azure Active Directory

In order to interact with the Azure Confidential Ledger service, your client must present an Azure Active Directory bearer token to the service.

The simplest way of providing a bearer token is to use the `DefaultAzureCredential` authentication method by providing client secret credentials is being used in this getting started section but you can find more ways to authenticate with [azure-identity][azure_identity].

#### Using a client certificate

As an alternative to Azure Active Directory, clients may choose to use a client certificate to authenticate via mutual TLS. `CertificateCredential` may be used for this purpose. This is not the recommended approach for anyone new to the service. 

#### Create LedgerBaseClient with Azure Active Directory Credential

You can authenticate with Azure Active Directory using the [Azure Identity library][azure_identity].

After setup, you can choose which type of [credential][azure_identity_credential_type] from `azure-identity` to use.
We recommend using [DefaultAzureCredential][identity_dac], configured through the `AZURE_TOKEN_CREDENTIALS` environment variable.
Set this variable as described in the [Learn documentation][customize_defaultAzureCredential], which provides the most up-to-date guidance and examples.

To use the [DefaultAzureCredential][DefaultAzureCredential] provider shown below, or other credential providers provided with the Azure SDK, please include the `azure-identity` package:

[//]: # ({x-version-update-start;com.azure:azure-identity;dependency})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-identity</artifactId>
    <version>1.15.3</version>
</dependency>
```

## Key concepts

### Ledger entries and transactions

Every write to Azure Confidential Ledger generates an immutable ledger entry in the service. Writes, also referred to as transactions, are uniquely identified by transaction ids that increment with each write. Once written, ledger entries may be retrieved at any time.

#### Tags
It is possible to further organize data within a collection as part of the latest preview version dated `2024-12-09-preview` or newer.

Specify the `tags` parameter as part of the create entry operation. Multiple tags can be specified using commas. There is a limit of five tags per transaction.

### Receipts

State changes to the Confidential Ledger are saved in a data structure called a Merkle tree. To cryptographically verify that writes were correctly saved, a Merkle proof, or receipt, can be retrieved for any transaction id.

### Sub-ledgers

While most use cases will involve one ledger, we provide the sub-ledger feature in case semantically or logically different groups of data need to be stored in the same Confidential Ledger.

Ledger entries are retrieved by their sub-ledger identifier. The Confidential Ledger will always assume a constant, service-determined sub-ledger id for entries submitted without a sub-ledger specified.

### Users

Users are managed directly with the Confidential Ledger instead of through Azure. Users may be AAD-based, identified by their AAD object id, or certificate-based, identified by their PEM certificate fingerprint.

### Confidential computing

[Azure Confidential Computing][azure_confidential_computing] allows you to isolate and protect your data while it is being processed in the cloud. Azure Confidential Ledger runs on Azure Confidential Computing virtual machines, thus providing stronger data protection with encryption of data in use.

### Confidential Consortium Framework

Azure Confidential Ledger is built on Microsoft Research's open-source [Confidential Consortium Framework (CCF)][ccf]. Under CCF, applications are managed by a consortium of members with the ability to submit proposals to modify and govern application operation. In Azure Confidential Ledger, Microsoft Azure owns a member identity, allowing it to perform governance actions like replacing unhealthy nodes in the Confidential Ledger, or upgrading the enclave code.

## Examples
Examples can be found in [samples][samples_code] and the [samples README][samples_readme].

## Troubleshooting

### Enabling Logging

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Next steps

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[ccf]: https://github.com/Microsoft/CCF
[azure_confidential_computing]: https://azure.microsoft.com/solutions/confidential-compute
[confidential_ledger_docs]: https://aka.ms/confidentialledger-servicedocs
[samples]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/java/com/azure/security/confidentialledger/
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src
[samples_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/
[azure_subscription]: https://azure.microsoft.com/free/
[product_documentation]: https://aka.ms/confidentialledger-servicedocs
[ledger_base_client_class]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/confidentialledger/azure-security-confidentialledger/src/main/java/com/azure/security/confidentialledger/LedgerBaseClient.java
[azure_portal]: https://portal.azure.com
[jdk_link]: https://learn.microsoft.com/java/azure/jdk/?view=azure-java-stable
[package]: https://central.sonatype.com/artifact/com.azure/azure-security-confidentialledger
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/confidentialledger/azure-security-confidentialledger/src/samples/README.md
[azure_resource_manager]: https://learn.microsoft.com/azure/azure-resource-manager/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity
[DefaultAzureCredential]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity/README.md#defaultazurecredential
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
[cla]: https://cla.opensource.microsoft.com/
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[azure_identity_credential_type]: https://github.com/Azure/azure-sdk-for-java/tree/main/sdk/identity/azure-identity#credentials
[customize_defaultAzureCredential]: https://aka.ms/azsdk/java/identity/credential-chains#how-to-customize-defaultazurecredential
[identity_dac]: https://aka.ms/azsdk/java/identity/credential-chains#defaultazurecredential-overview

