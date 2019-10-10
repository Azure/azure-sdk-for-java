# Java SDK for Azure Search

This is the Java SDK for [Azure Search](https://docs.microsoft.com/en-us/rest/api/searchservice/). Azure Search is a fully managed cloud search service that provides a rich search experience to custom applications. One way to add search capability is through a REST API, with operations that create and manage indexes, load data, implement search features, execute queries, and handle results.

## Key concepts

Azure Search has the concepts of search services and indexes and documents, where a search service contains one or more indexes that provides persistent storage of searchable data, and data is loaded in the form of JSON documents. Data is typically pushed to an index from an external data source, but if you use an indexer, it's possible to crawl a data source to extract and load data into an index.

There are several types of operations that can be executed against the service:

* [Index management operations](https://docs.microsoft.com/en-us/rest/api/searchservice/index-operations). Create, delete, update, or configure a search index.

* [Document operations](https://docs.microsoft.com/en-us/rest/api/searchservice/document-operations). Add, update, or delete documents in the index, query the index, or look up specific documents by ID.

* [Indexer operations](https://docs.microsoft.com/en-us/rest/api/searchservice/indexer-operations). Automate aspects of an indexing operation by configuring a data source and an indexer that you can schedule or run on demand. This feature is supported for a limited number of data source types.

* [Skillset operations](https://docs.microsoft.com/en-us/rest/api/searchservice/skillset-operations). Part of a cognitive search workload, a skillset defines a series of enrichment processing. A skillset is consumed by an indexer.

* [Synonym map operations](https://docs.microsoft.com/en-us/rest/api/searchservice/synonym-map-operations). A synonym map is service-level resource that containers user-defined synonyms. This resource is maintained independently from search indexes. Once uploaded, you can point any searchable field to the synonym map (one per field).

A separate REST API is provided for service administration, including provisioning the service or altering capacity. For more information, see [Azure Search Management REST](https://docs.microsoft.com/en-us/rest/api/searchmanagement/index).

## Using the SDK

The APIs documented in this section provide access to operations on search data, such as index creation and population, document upload, and queries. When calling APIs, keep the following points in mind:

* All APIs will be issued over HTTPS (on the default port 443).

* Your search service is uniquely identified by a fully-qualified domain name (for example: `mysearchservice.search.windows.net`).

* All SDK requests must include an `api-key` that was generated for the Search service you provisioned. Having a valid key establishes trust, on a per request basis, between the application sending the request and the service that handles it.

* SDK requests can optionally specify an `api-version`. The default value matches the version of the current service release: `2019-05-06`.

* You will need to provide `index name`and the `search service name` which is the URL of the Azure Search service you provisioned: `https://<yourService>.search.windows.net`.

## This library is built using:

* JDK / JRE 1.8
* Microsoft Azure - adal4j 1.3.0
* Junit 4.12

## Building the SDK

1. Clone this repo 
2. In [IntelliJ](https://www.jetbrains.com/idea/), import the project by opening the client pom - [pom.client.xml](../../../pom.client.xml) it will open all the SDKs, including the search one and will make sure all dependencies are met. For contributing to Search specifically, select `...\azure-sdk-for-java-pr\sdk\search\pom.service.xml`.
   * Check "Search for projects recursively" and "Import Maven projects automatically" checkboxes if they are not checked by default.
   * In JDK for importer, select 1.8. Make sure to have JDK 1.8 installed and included in project settings.
   * For profiles, check "javadoc-doclet-compatibility". 
   * For Maven projects to import, check com.azure:azure-search-service-parent:1.0.0.
   * For project SDK, select Java SDK 1.8. 
3. Build module azure-search, and run the samples
## Samples

* [A simple search index example](/sdk/search/azure-search/src/samples/java/com/azure/search/SearchIndexClientExample.java).
* [Handle a generic document search results](/sdk/search/azure-search/src/samples/java/com/azure/search/GenericDocumentSearchExample.java).
* [Get a single generic document](/sdk/search/azure-search/src/samples/java/com/azure/search/GenericSingleDocumentGetExample.java).
* [Using suggestions](/sdk/search/azure-search/src/samples/java/com/azure/search/SearchSuggestionExample.java).

## How to provide feedback

See our [Contribution Guidelines](./.github/CONTRIBUTING.md).

## How to get support
See our [Support Guidelines](./.github/SUPPORT.md)

## Known issues

TBD

## FAQ

### How to run unit tests

Please see the [Test Framework documentation](/sdk/search/azure-search/src/test)

## Contributing
If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

## Run style check locally
Style check runs as part of Continuous Integration (CI) pipeline. Running it locally allows you to verify the style rules in an isolated way and quickly iterate on them, rather than having to wait for a build agent to free up to run all the steps. 
Navigate to `..\azure-sdk-for-java-pr`, run:

`mvn -f pom.client.xml -DskipTests -Dgpg.skip spotbugs:check checkstyle:checkstyle-aggregate --projects com.azure.search:azure-search-data,com.azure:azure-client-sdk-parent --also-make`
