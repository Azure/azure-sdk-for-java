package com.azure.search.data;

/**
 * The Fluent client builder.
 */
public interface SearchIndexClientBuilder {

    // Fluent builders
    SearchIndexClient buildClient();

    SearchIndexASyncClient buildAsyncClient();
}
