# SearchIndexClient

This is the AutoRest configuration file for SearchIndexClient

## Building the SDK
1. Install [AutoRest](https://github.com/Azure/autorest/blob/master/README.md#installing-autorest) 
2. Execute  autorest in this folder. It will use this readme file as the configuration:

```bash
autorest .\readme.md
```

## Configuration

```yaml
openapi-type: data-plane
tag: package-2019-05
input-file: ./2019-05-06/data/modifiedSearchindex.json
java:
  output-folder: ../azure-search-data
  namespace: com.azure.search.data.generated
  azure-arm: false
  add-credentials: true
  clear-output-folder: false
  generate-client-interfaces: true
  directive:
  - from: src/main/java/com/azure/search/data/generated/models/SearchResult.java
    where: $
    transform: >-
      return $
      .replace(/(package com.azure.search.data.generated.models;)/g, "$1\nimport com.azure.search.data.customization.Document;")
      .replace(/(Map<String, Object>)/g, "Document")
  - from: src/main/java/com/azure/search/data/generated/Documents.java
    where: $
    transform: >-
      return $
      .replace(/(package com.azure.search.data.generated;)/g, "$1\nimport com.azure.search.data.customization.Document;")
      .replace(/(Object)/g, "Document")
  # reduce accessibility to the generated class
  - from: src/main/java/com/azure/search/data/generated/implementation/DocumentsImpl.java
    where: $
    transform: >-
      return $
      .replace(/(package com.azure.search.data.generated.implementation;)/g, "$1\nimport com.azure.search.data.customization.Document;")
      .replace(/(Object)/g, "Document")
  - from: src/main/java/com/azure/search/data/generated/models/SuggestResult.java
    where: $
    transform: >-
      return $
      .replace(/(import java.util.Map;)/g, "$1\nimport com.azure.search.data.customization.Document;")
      .replace(/(Map<String, Object>)/g, "Document")
  - from: src/main/java/com/azure/search/data/generated/implementation/SearchIndexRestClientBuilder.java
    where: $
    transform: >-
      return $
  - from: src/main/java/com/azure/search/data/generated/implementation/SearchIndexRestClientImpl.java
    where: $
    transform: >-
      return $
      .replace(/(void setApiVersion)/g, "public void setApiVersion")
      .replace(/(void setIndexName)/g, "public void setIndexName")
      .replace(/(void setSearchDnsSuffix)/g, "public void setSearchDnsSuffix")
      .replace(/(void setSearchServiceName)/g, "public void setSearchServiceName")
```
