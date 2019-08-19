# Release History

## 12.0.0-preview.2 (2019-08-08)
Version 12.0.0-preview.2 is a preview of our efforts in creating a client library that is developer-friendly, idiomatic to the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide our efforts can be found in the [Azure SDK Design Guidelines for Java](https://azuresdkspecs.z5.web.core.windows.net/JavaSpec.html).

For details on the Azure SDK for Java (August 2019 Preview) release, you can refer to the [release announcement](https://aka.ms/azure-sdk-preview2-java).

This package's
[documentation](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-file/README.md)
and
[samples](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/storage/azure-storage-file/src/samples/java/com/azure/file)
demonstrate the new API.

### Features included in `azure-storage-file`
- This is initial SDK release for storage file service.
- Packages scoped by functionality
    - `azure-storage-file` contains a `FileServiceClient`,  `FileServiceAsyncClient`, `ShareClient`, `ShareAsyncClient`, `DirectoryClient`, `DirectoryAsyncClient`, `FileClient` and `FileAsyncClient` for storage file operations. 
- Client instances are scoped to storage file service.
- Reactive streams support using [Project Reactor](https://projectreactor.io/).

  
