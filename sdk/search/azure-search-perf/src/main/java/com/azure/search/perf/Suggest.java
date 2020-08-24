// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf;

import com.azure.search.perf.core.Hotel;
import com.azure.search.perf.core.SearchPerfStressOptions;
import com.azure.search.perf.core.ServiceTest;
import reactor.core.publisher.Mono;

/**
 * Performs suggestion operations.
 */
public class Suggest extends ServiceTest<SearchPerfStressOptions> {
    public Suggest(SearchPerfStressOptions options) {
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
        searchClient.suggest("historic", SUGGESTER_NAME).forEach(result -> result.getDocument(Hotel.class));
    }

    @Override
    public Mono<Void> runAsync() {
        return searchAsyncClient.suggest("historic", SUGGESTER_NAME)
            .map(result -> result.getDocument(Hotel.class))
            .then();
    }
}
