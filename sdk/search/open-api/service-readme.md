# SearchIndexClient

This is the AutoRest configuration file for the search service

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
input-file: ./2019-05-06/service/searchservice.json
java:
  output-folder: ../azure-search-service
  namespace: com.azure.search.service.generated
  azure-arm: false
  add-credentials: true
  clear-output-folder: false
  generate-client-interfaces: true
  directive:

  - from: src/main/java/com/azure/search/service/generated/implementation/SearchServiceClientImpl.java
    where: $
    transform: >-
      return $
      .replace(/(void setSearchDnsSuffix)/g, "public void setSearchDnsSuffix")
      .replace(/(void setSearchServiceName)/g, "public void setSearchServiceName")
      .replace(/(void setApiVersion)/g, "public void setApiVersion")

  - from: src/main/java/com/azure/search/service/generated/implementation/SynonymMapsImpl.java
    where: $
    transform: >-
      return $
      .replace(/(this.getSearchServiceName)/g, "this.client.getSearchServiceName")
      .replace(/(this.getSearchDnsSuffix)/g, "this.client.getSearchDnsSuffix")

  - from: src/main/java/com/azure/search/service/generated/implementation/SkillsetsImpl.java
    where: $
    transform: >-
      return $
      .replace(/(this.getSearchServiceName)/g, "this.client.getSearchServiceName")
      .replace(/(this.getSearchDnsSuffix)/g, "this.client.getSearchDnsSuffix")

  - from: src/main/java/com/azure/search/service/generated/implementation/IndexesImpl.java
    where: $
    transform: >-
      return $
      .replace(/(this.getSearchServiceName)/g, "this.client.getSearchServiceName")
      .replace(/(this.getSearchDnsSuffix)/g, "this.client.getSearchDnsSuffix")

  - from: src/main/java/com/azure/search/service/generated/implementation/DataSourcesImpl.java
    where: $
    transform: >-
      return $
      .replace(/(this.getSearchServiceName)/g, "this.client.getSearchServiceName")
      .replace(/(this.getSearchDnsSuffix)/g, "this.client.getSearchDnsSuffix")

  - from: src/main/java/com/azure/search/service/generated/implementation/IndexersImpl.java
    where: $
    transform: >-
      return $
      .replace(/(this.getSearchServiceName)/g, "this.client.getSearchServiceName")
      .replace(/(this.getSearchDnsSuffix)/g, "this.client.getSearchDnsSuffix")
```
