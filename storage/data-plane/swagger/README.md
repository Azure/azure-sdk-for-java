# Azure Storage Java Proto
> see https://aka.ms/autorest
```yaml
title: StorageClient
description: Storage Client
java: true
enable-xml: true
namespace: com.microsoft.azure.storage.blob
license-header: MICROSOFT_MIT_NO_VERSION
output-folder: ../
input-file:
- path\to\json
directive:
  # removes the x-ms-error-code from default response headers
  where: $..default.headers["x-ms-error-code"]
  transform: return undefined;
  reason: Default models with header parameters will generated properties on non-default models 
  authorized-by: "@fearthecowboy"
```
