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
input-file: ./2019-05-06/modifiedSearchindex.json
java:
    output-folder: azure-search-data
    namespace: com.azure.search.data.generated
    azure-arm: false
    add-credentials: true
    clear-output-folder: false
    generate-client-interfaces: true
directive:
	- from: src/main/java/com/azure/search/azure-search-data/Documents.java
	  where: $
	  transform: >-
		return $
		.replace(/(package com.azure.search.data.generated;)/g, "$1\nimport com.azure.search.data.customization.Document;")
		.replace(/(Object)/g, "Document")
    # reduce accessibility to the generated class
    - from: src/main/java/com/azure/search/azure-search-data/implementation/DocumentsImpl.java
      where: $
      transform: >-
        return $
		.replace(/(package com.azure.search.data.generated.implementation;)/g, "$1\nimport com.azure.search.data.customization.Document;")
        .replace(/(public final class DocumentsImpl implements Documents)/g, "final class DocumentsImpl implements Documents")
		.replace(/(Object)/g, "Document")
    # reduce accessibility to the generated class
    - from: src/main/java/com/azure/search/azure-search-data/rest/implementation/SearchIndexClientBuilder.java
      where: $
      transform: >-
        return $
        .replace(/(public final class SearchIndexRestClientBuilder)/g, "final class SearchIndexRestClientBuilder")
    # reduce accessibility to the generated class
    - from: src/main/java/com/azure/search/azure-search-data/rest/implementation/SearchIndexRestClientImpl.java
      where: $
      transform: >-
        return $
        .replace(/(public final class SearchIndexRestClientImpl)/g, "final class SearchIndexRestClientImpl")
```
