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
input-file: ./2019-05-06/data/searchindex.json
title: SearchIndexRestClient
directive:
# Rename IndexBatch to IndexBatchImpl when processing the API spec
- rename-model:
    from: IndexBatch
    to: IndexBatchImpl
java:
  output-folder: ../azure-search-data
  namespace: com.azure.search.data.generated
  azure-arm: false
  add-credentials: true
  clear-output-folder: false
  generate-client-interfaces: true
  directive:

  # Use Document rather than Map<String, Object>
  - from: SearchResult.java
    where: $
    transform: >-
      return $
      .replace(/(package com.azure.search.data.generated.models;)/g, "$1\nimport com.azure.search.data.customization.Document;")
      .replace(/(Map<String, Object>)/g, "Document")
  
  # Use Document rather than Map<String, Object>
  - from: Documents.java
    where: $
    transform: >-
      return $
      .replace(/(package com.azure.search.data.generated;)/g, "$1\nimport com.azure.search.data.customization.Document;")
      .replace(/(Object)/g, "Document")

  # Use Document rather than Map<String, Object>
  # Enable configuration of RestProxy serializer
  - from: DocumentsImpl.java
    where: $
    transform: >-
      return $
      .replace(/(package com.azure.search.data.generated.implementation;)/g, "$1\nimport com.azure.search.data.customization.Document;")
      .replace(/(Object)/g, "Document")
      .replace(/(import com.azure.core.implementation.serializer.jackson.JacksonAdapter;)/g, "$1\nimport com.azure.core.implementation.serializer.SerializerAdapter;")
      .replace(/(@param client the instance of the service client containing this operation class.)/g, "$1\n     \* @param serializer the serializer to be used for service client requests.")
      .replace(/(public DocumentsImpl\(SearchIndexRestClientImpl client\) {)/g, "public DocumentsImpl(SearchIndexRestClientImpl client, SerializerAdapter serializer) {")
      .replace(/(this.service = RestProxy.create\(DocumentsService.class, client.getHttpPipeline\(\)\);)/g, "this.service = RestProxy.create(DocumentsService.class, client.getHttpPipeline(), serializer);")

  # Use Document rather than Map<String, Object>
  - from: SuggestResult.java
    where: $
    transform: >-
      return $
      .replace(/(import java.util.Map;)/g, "$1\nimport com.azure.search.data.customization.Document;")
      .replace(/(Map<String, Object>)/g, "Document")

  # Enable public access to client setters
  # Enable configuration of RestProxy serializer
  - from: SearchIndexRestClientImpl.java
    where: $
    transform: >-
      return $
      .replace(/(void setApiVersion)/g, "public void setApiVersion")
      .replace(/(void setIndexName)/g, "public void setIndexName")
      .replace(/(void setSearchDnsSuffix)/g, "public void setSearchDnsSuffix")
      .replace(/(void setSearchServiceName)/g, "public void setSearchServiceName")
      .replace(/(import com.azure.core.implementation.RestProxy;)/g, "$1\nimport com.azure.core.implementation.serializer.jackson.JacksonAdapter;\nimport com.azure.core.implementation.serializer.SerializerAdapter;")
      .replace(/(this\(RestProxy.createDefaultPipeline\(\)\);)/g, "this(RestProxy.createDefaultPipeline(), JacksonAdapter.createDefaultSerializerAdapter());")
      .replace(/(@param httpPipeline The HTTP pipeline to send requests through.)/g, "$1\n     \* @param serializer the serializer to be used for service client requests.")
      .replace(/(public SearchIndexRestClientImpl\(HttpPipeline httpPipeline\) {)/g, "public SearchIndexRestClientImpl(HttpPipeline httpPipeline, SerializerAdapter serializer) {")
      .replace(/(this.documents = new DocumentsImpl\(this\);)/g, "this.documents = new DocumentsImpl(this, serializer);")

  # Enable IndexAction to be used as a generic type
  # Enable serialization of both POJOs and Maps
  - from: IndexAction.java
    where: $
    transform: >-
      return $
      .replace(/(import com.fasterxml.jackson.annotation.JsonProperty;)/g, "import com.fasterxml.jackson.annotation.JsonAnyGetter;\nimport com.fasterxml.jackson.annotation.JsonIgnore;\n$1\nimport com.fasterxml.jackson.annotation.JsonUnwrapped;\n")
      .replace(/(class IndexAction)/g, "$1<T>")
      .replace(/(Unmatched properties from the message are deserialized this collection)/g, "The document on which the action will be performed.")
      .replace(/(@JsonProperty\(value = ""\))/g, "@JsonUnwrapped")
      .replace(/(private Map<String, Object> additionalProperties);/g, "private T document;\n\n    @JsonIgnore\n    private Map<String, Object> properties;\n\n    @JsonAnyGetter\n    private Map<String, Object> getParamMap() {\n        return properties;\n    }")
      .replace(/(Get the additionalProperties property: Unmatched properties from the\n\s+\* message are deserialized this collection.)/g, "Get the document on which the action will be performed; Fields other than the key are ignored for delete actions.")
      .replace(/(@return the additionalProperties value.)/g, "@return the document value.")
      .replace(/(public Map<String, Object> additionalProperties\(\) {\s+return this.additionalProperties;\s+})/g, "public T getDocument() { return this.document; }")
      .replace(/(Set the additionalProperties property: Unmatched properties from the\s+\* message are deserialized this collection.)/g, "Get the document on which the action will be performed; Fields other than the key are ignored for delete actions.")
      .replace(/(@param additionalProperties the additionalProperties value to set.)/g, "@param document the document value to set.")
      .replace(/(public IndexAction additionalProperties\(Map<String, Object> additionalProperties\) {\s+this.additionalProperties = additionalProperties;\s+return this;\s+})/g, "public IndexAction<T> document(T document) {\n        if (document instanceof Map) {\n            this.properties = (Map<String, Object>) document;\n            this.document = null;\n        } else {\n            this.document = document;\n            this.properties = null;\n        }\n        return this;\n    }")
      .replace(/(public IndexAction actionType\(IndexActionType actionType\) {)/g, "public IndexAction<T> actionType(IndexActionType actionType) {")

  # Enable configuration of RestProxy serializer
  - from: SearchIndexRestClientBuilder.java
    where: $
    transform: >-
      return $
      .replace(/(import com.azure.core.implementation.annotation.ServiceClientBuilder;)/g, "$1\nimport com.azure.core.implementation.serializer.SerializerAdapter;\nimport com.azure.core.implementation.serializer.jackson.JacksonAdapter;")
      .replace(/(\* The HTTP pipeline to send requests through)/g, "\* The serializer to use for requests\n     \*\/\n    private SerializerAdapter serializer;\n\n    \/\*\*\n     \* Sets The serializer to use for requests.\n     \*\n     \* @param serializer the serializer value.\n     \* @return the SearchIndexRestClientBuilder.\n     \*\/\n    public SearchIndexRestClientBuilder serializer\(SerializerAdapter serializer\) {\n        this.serializer = serializer;\n        return this;\n    }\n\n    \/\*\n     $1")
      .replace(/(new SearchIndexRestClientImpl\(pipeline)/g, "$1, serializer")
      .replace(/(this.pipeline = RestProxy.createDefaultPipeline\(\);\s+})/g, "$1\n        if \(serializer == null\) {\n            this.serializer = JacksonAdapter.createDefaultSerializerAdapter\(\);\n        }")

  # Enable IndexBatchImpl to be used as a generic type
  # TODO (Noel): Make IndexBatchImpl package private after reorganization
  - from: IndexBatchImpl.java
    where: $
    transform: >-
      return $
      .replace(/(final class IndexBatchImpl)/g, "class IndexBatchImpl<T>")
      .replace(/(private List<IndexAction> actions;)/g, "private List<IndexAction<T>> actions;")
      .replace(/(public List<IndexAction> actions\(\) {)/g, "public List<IndexAction<T>> actions() {")
      .replace(/(public IndexBatchImpl actions\(List<IndexAction> actions\) {)/g, "protected IndexBatchImpl<T> actions(List<IndexAction<T>> actions) {")

  # Replace use of generated IndexBatchImpl with custom IndexBatch class
  - from:
    - Documents.java
    - DocumentsImpl.java
    where: $
    transform: >-
      return $
      .replace(/(IndexBatchImpl)/g, "IndexBatch")
      .replace(/(import com.azure.search.data.generated.models.IndexBatch)/g, "import com.azure.search.data.customization.IndexBatch")
      .replace(/(IndexBatch )/g, "IndexBatch<T> ")
      .replace(/(Mono<DocumentIndexResult> indexAsync)/g, "<T> $1")
      .replace(/(Mono<SimpleResponse<DocumentIndexResult>> index)/g, "<T> $1")

```
