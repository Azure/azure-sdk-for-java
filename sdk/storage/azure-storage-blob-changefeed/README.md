# Azure Blob Storage change feed client library for Java

The purpose of the change feed is to provide transaction logs of all the changes that occur to
the blobs and the blob metadata in your storage account. The change feed provides ordered,
guaranteed, durable, immutable, read-only log of these changes. Client applications can read these
logs at any time. The change feed enables you to build efficient and scalable solutions that
process change events that occur in your Blob Storage account at a low cost.

TODO (gapra) : This is a placeholder. Add docs and point users to blob readme and add changefeed specific samples. 

## Getting started

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
