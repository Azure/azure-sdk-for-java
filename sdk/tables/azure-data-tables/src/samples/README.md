---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-table-storage
urlFragment: tables-samples
---

# Azure Tables client library samples for Java

Azure Tables samples are a set of self-contained Java programs that demonstrate interacting with Azure Tables
using the client library. Each sample focuses on a specific scenario and can be executed independently.

## Key concepts
Key concepts are explained in detail [here][sdk_readme_key_concepts].

## Getting started
Please refer to the [Getting Started][sdk_readme_getting_started] section.

### Obtaining a Tables account connection string

Most of the samples authorize with Tables using a connection string generated for that Storage or Cosmos DB Tables API
account. You can obtain your connection string from the Azure Portal (click **Access keys** under **Settings** in the
Portal Storage account blade, or **Connection String** under **Settings** in the Portal Cosmos DB account blade) or
using the Azure CLI:

```bash
# Storage account
az storage account show-connection-string \
    --resource-group <resource-group-name> \
    --name <storage-account-name>

# Cosmos DB Table API account
az cosmosdb list-connection-strings \
    --resource-group <resource-group-name> \
    --name <cosmosdb-account-name>
```

## Examples

- [Interact with the Tables service using the async clients][sample_async_client_java_doc_code_snippets]
- [Interact with the Tables service using the synchronous clients][sample_sync_client_java_doc_code_snippets]

## Troubleshooting
See [Troubleshooting][sdk_readme_troubleshooting].

## Next steps
See [Next steps][sdk_readme_next_steps].

## Contributing
This project welcomes contributions and suggestions. See [Contributing][sdk_readme_contributing] for guidelines.

<!-- Links -->
[sdk_readme_key_concepts]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/azure-data-tables/README.md#key-concepts
[sdk_readme_getting_started]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/azure-data-tables/README.md#getting-started
[sdk_readme_troubleshooting]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/azure-data-tables/README.md#troubleshooting
[sdk_readme_next_steps]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/azure-data-tables/README.md#next-steps
[sdk_readme_contributing]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/azure-data-tables/README.md#contributing
[sample_async_client_java_doc_code_snippets]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/azure-data-tables/src/samples/java/com/azure/data/tables/codesnippets/TableServiceAsyncClientJavaDocCodeSnippets.java
[sample_sync_client_java_doc_code_snippets]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/tables/azure-data-tables/src/samples/java/com/azure/data/tables/codesnippets/TableServiceClientJavaDocCodeSnippets.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%tables%2Fazure-data-tables%2Fsrc%2Fsamples%2README.png)
