// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf;

import com.azure.search.documents.models.AutocompleteOptions;
import com.azure.search.perf.core.SearchPerfStressOptions;
import com.azure.search.perf.core.ServiceTest;
import reactor.core.publisher.Mono;

/**
 * Performs autocomplete operations.
 */
public class AutocompleteTest extends ServiceTest<SearchPerfStressOptions> {
    /**
     * Creates an autocomplete operations performance test.
     *
     * @param options Performance test configuration options.
     */
    public AutocompleteTest(SearchPerfStressOptions options) {
        super(options);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        /*
         * First, run the global setup in the super class. That will create the index to be used for performance
         * testing. Then populate the index with a given number of documents.
         */
        return super.globalSetupAsync().then(populateIndex(options.getCount(), options.getDocumentSize()));
    }

    @Override
    public void run() {
        assert !searchClient.autocomplete(new AutocompleteOptions("historic", SUGGESTER_NAME)).getResults().isEmpty();
    }

    @Override
    public Mono<Void> runAsync() {
        return searchAsyncClient.autocomplete(new AutocompleteOptions("historic", SUGGESTER_NAME))
            .flatMap(result -> result.getResults().isEmpty()
                ? Mono.error(new RuntimeException("Expected autocomplete results."))
                : Mono.empty());
    }
}
