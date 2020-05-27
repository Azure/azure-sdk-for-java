# Azure Blob Storage change feed client library for Java

The purpose of the change feed is to provide transaction logs of all the changes that occur to
the blobs and the blob metadata in your storage account. The change feed provides ordered,
guaranteed, durable, immutable, read-only log of these changes. Client applications can read these
logs at any time. The change feed enables you to build efficient and scalable solutions that
process change events that occur in your Blob Storage account at a low cost.

TODO (gapra) : This is a placeholder. Add docs and point users to blob readme and add changefeed specific samples. 

## Getting started
### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Create Storage Account][storage_account]

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-storage-blob-changefeed;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-blob-changefeed</artifactId>
    <version>12.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Create a Storage Account
To create a Storage Account you can use the [Azure Portal][storage_account_create_portal] or [Azure CLI][storage_account_create_cli].

```bash
az storage account create \
    --resource-group <resource-group-name> \
    --name <storage-account-name> \
    --location <location>
```

Your storage account URL, subsequently identified as <your-storage-account-url>, would be formatted as follows
http(s)://<storage-account-name>.blob.core.windows.net

### Authenticate the client

In order to interact with the Storage Service (Blob, Queue, Message, MessageId, File) you'll need to create an instance of the Service Client class.
To make this possible you'll need the Account SAS (shared access signature) string of the Storage Account. Learn more at [SAS Token][sas_token]

#### Get credentials

##### SAS Token

a. Use the Azure CLI snippet below to get the SAS token from the Storage Account.

```bash
az storage blob generate-sas \
    --account-name {Storage Account name} \
    --container-name {container name} \
    --name {blob name} \
    --permissions {permissions to grant} \
    --expiry {datetime to expire the SAS token} \
    --services {storage services the SAS allows} \
    --resource-types {resource types the SAS allows}
```

Example:

```bash
CONNECTION_STRING=<connection-string>

az storage blob generate-sas \
    --account-name MyStorageAccount \
    --container-name MyContainer \
    --name MyBlob \
    --permissions racdw \
    --expiry 2020-06-15
```

b. Alternatively, get the Account SAS Token from the Azure Portal.

1. Go to your Storage Account
2. Select `Shared access signature` from the menu on the left
3. Click on `Generate SAS and connection string` (after setup)

##### **Shared Key Credential**

a. Use Account name and Account key. Account name is your Storage Account name.

1. Go to your Storage Account
2. Select `Access keys` from the menu on the left
3. Under `key1`/`key2` copy the contents of the `Key` field

or

b. Use the connection string.

1. Go to your Storage Account
2. Select `Access keys` from the menu on the left
3. Under `key1`/`key2` copy the contents of the `Connection string` field

## Key concepts

The change feed is stored as blobs in a special container in your storage account at standard blob
pricing cost. You can control the retention period of these files based on your requirements
(See the conditions of the current release). Change events are appended to the change feed as records
in the Apache Avro format specification: a compact, fast, binary format that provides rich data structures
with inline schema. This format is widely used in the Hadoop ecosystem, Stream Analytics, and Azure Data
Factory.

You can process these logs incrementally or in-full. Any number of client applications can independently
read the change feed, in parallel, and at their own pace. Analytics applications such as Apache Drill or
Apache Spark can consume logs directly as Avro files, which let you process them at a low-cost, with
high-bandwidth, and without having to write a custom application.

## Examples

## Troubleshooting

## Next steps

## Next steps Samples

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-blob-changefeed%2FREADME.png)
