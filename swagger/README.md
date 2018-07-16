# Azure Storage Java Proto
> see https://aka.ms/autorest
```yaml
title: StorageClient
description: Storage Client
java: true
enable-xml: true
namespace: com.microsoft.azure.storage
license-header: MICROSOFT_MIT_NO_VERSION
output-folder: ../
input-file:
- D:\Azure\Storage\XStore\src\XFE\OpenApi\Microsoft.BlobStorage\preview\2017-07-29\blob.json
directive:
  # removes the x-ms-error-code from default response headers
  where: $..default.headers["x-ms-error-code"]
  transform: return undefined;
  reason: Default models with header parameters will generated properties on non-default models 
  authorized-by: "@fearthecowboy"
```
