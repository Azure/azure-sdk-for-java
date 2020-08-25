// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf.core;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.ProxyOptions;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.search.documents.SearchAsyncClient;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.SearchSuggester;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Base class for Azure Search performance tests.
 */
public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    private static final String CONFIGURATION_ERROR = "Configuration %s must be set in either environment variables "
        + "or system properties.%n";
    private static final String ALLOWED_INDEX_CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int INDEX_NAME_LENGTH = 24;

    protected static final String SUGGESTER_NAME = "sg";

    protected final SearchClient searchClient;
    protected final SearchAsyncClient searchAsyncClient;

    private final SearchIndexAsyncClient searchIndexAsyncClient;
    private final String indexName;

    public ServiceTest(TOptions options) {
        super(options);

        String searchEndpoint = Configuration.getGlobalConfiguration().get("SEARCH_ENDPOINT");
        if (CoreUtils.isNullOrEmpty(searchEndpoint)) {
            System.out.printf(CONFIGURATION_ERROR, "SEARCH_ENDPOINT");
            System.exit(1);
        }

        String searchApiKey = Configuration.getGlobalConfiguration().get("SEARCH_API_KEY");
        if (CoreUtils.isNullOrEmpty(searchApiKey)) {
            System.out.printf(CONFIGURATION_ERROR, "SEARCH_API_KEY");
            System.exit(1);
        }

        SearchIndexClientBuilder builder = new SearchIndexClientBuilder()
            .endpoint(searchEndpoint)
            .credential(new AzureKeyCredential(searchApiKey))
            .httpClient(new NettyAsyncHttpClientBuilder()
                .proxy(new ProxyOptions(ProxyOptions.Type.HTTP, new InetSocketAddress("localhost", 8888)))
                .build());
            //.httpClient(PerfStressHttpClient.create(options));

        this.searchIndexAsyncClient = builder.buildAsyncClient();

        this.indexName = new Random().ints(INDEX_NAME_LENGTH, 0, ALLOWED_INDEX_CHARACTERS.length())
            .mapToObj(ALLOWED_INDEX_CHARACTERS::charAt)
            .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
            .toString();

        this.searchClient = builder.buildClient().getSearchClient(this.indexName);
        this.searchAsyncClient = this.searchIndexAsyncClient.getSearchAsyncClient(this.indexName);
    }

    @Override
    public Mono<Void> globalSetupAsync() {
        return searchIndexAsyncClient
            .createIndex(new SearchIndex(indexName, SearchIndexAsyncClient.buildSearchFields(Hotel.class, null))
                .setSuggesters(new SearchSuggester(SUGGESTER_NAME, Arrays.asList("Description", "HotelName"))))
            .then();
    }

    @Override
    public Mono<Void> globalCleanupAsync() {
        return searchIndexAsyncClient.deleteIndex(indexName);
    }

    protected Mono<Void> populateIndex(int documentCount, String documentSize) {
        /*
         * Generate the count of documents using the given size. Then, upload the documents in batches of 100, this
         * prevents the batch from triggering the services request size limit to fail. Finally, continuously poll the
         * index for its document count until it is equal to the count passed.
         */
        return Mono.defer(() -> {
            List<Hotel> hotels = DocumentGenerator.generateHotels(documentCount, DocumentSize.valueOf(documentSize));

            return Flux.range(0, (int) Math.ceil(hotels.size() / 100D))
                .map(i -> hotels.subList(i * 100, Math.min((i + 1) * 100, hotels.size())))
                .flatMap(hotelDocuments -> searchAsyncClient.indexDocuments(new IndexDocumentsBatch<Hotel>()
                    .addUploadActions(hotelDocuments)))
                .then();
        }).then(Mono.defer(() -> searchAsyncClient.getDocumentCount()
            .delaySubscription(Duration.ofSeconds(1))
            .filter(count -> count == documentCount)
            .repeatWhenEmpty(Flux::repeat)
            .then()));
    }
}
