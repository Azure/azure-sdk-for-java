# Azure Storage SDK Client library for Java

[![Build Status](https://dev.azure.com/azure-sdk/public/_apis/build/status/17?branchName=master)](https://dev.azure.com/azure-sdk/public/_build/latest?definitionId=17) [![Build Documentation](https://img.shields.io/badge/documentation-published-blue.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/index.html) [![Dependencies](https://img.shields.io/badge/dependencies-analyzed-blue.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/staging/dependencies.html) [![SpotBugs](https://img.shields.io/badge/SpotBugs-Clean-success.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/staging/spotbugsXml.html) [![CheckStyle](https://img.shields.io/badge/CheckStyle-Clean-success.svg)](https://azuresdkartifacts.blob.core.windows.net/azure-sdk-for-java/staging/checkstyle-aggregate.html)

Azure Storage is a Microsoft-managed service providing cloud storage that is highly available, secure, durable, scalable, and redundant. Azure Storage includes Blobs (objects), Queues, and Files.

## Getting started

To get started with a specific library, see the **README.md** file located in the library's project folder. You can find service libraries in the `/sdk/storage/azure-storage-<subcomponent>` directory.
- [Azure Storage Blob](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob/README.md) is Microsoft's object storage solution for the cloud. Blob storage is optimized for storing massive amounts of unstructured data that does not adhere to a particular data model or definition, such as text or binary data.
- [Azure Storage Blob Batch](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob-batch/README.md) allows you to batch multiple Azure Blob Storage operations in a single request.
- [Azure Storage Blob Cryptography](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-blob-cryptography/README.md) supports client-side encryption for Azure Storage block blobs.
- [Azure Storage Queue](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-queue/README.md) is a service for storing large numbers of messages.  A queue message can be up to 64 KB in size and a queue may contain millions of messages, up to the total capacity limit of a storage account.
- [Azure Storage Common](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-common/README.md) provides infrastructure shared by the other Azure Storage client libraries like shared key authentication and exceptions.
- [Azure Storage File Share](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-file-share/README.md) service shares are accessible via the Server Message Block (i.e. SMB) protocol, and also via REST APIs. The File Share service offers the following four resources: the storage account, shares, directories, and files. Shares provide a way to organize sets of files and also can be mounted as an SMB file share that is hosted in the cloud.
- [Azure Storage File Datalake](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-file-datalake/README.md) is Microsoft's optimized storage solution for for big data analytics workloads. A fundamental part of Data Lake Storage Gen2 is the addition of a hierarchical namespace to Blob storage. The hierarchical namespace organizes objects/files into a hierarchy of directories for efficient data access. 


![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2FREADME.png)
