// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf;

import com.azure.search.perf.core.SearchPerfStressOptions;
import com.azure.search.perf.core.ServiceTest;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Performs autocomplete operations.
 */
public class AutocompleteTest extends ServiceTest<SearchPerfStressOptions> {
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
        AtomicInteger count = new AtomicInteger();
        searchClient.autocomplete("historic", SUGGESTER_NAME).iterator()
            .forEachRemaining(ignored -> count.incrementAndGet());

        assert count.get() > 0;
    }

    @Override
    public Mono<Void> runAsync() {
        return searchAsyncClient.autocomplete("historic", SUGGESTER_NAME)
            .count()
            .flatMap(count -> count > 0
                ? Mono.empty()
                : Mono.error(new RuntimeException("Expected autocomplete results.")));
    }
}
