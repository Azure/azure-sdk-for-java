// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.serializer.TypeReference;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.readJsonFileToList;
import static com.azure.search.documents.TestHelpers.waitForIndexing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link SearchIndexingBufferedSender}.
 */
public class SearchIndexingBufferedSenderTests extends SearchTestBase {
    private static final TypeReference<Map<String, Object>> HOTEL_DOCUMENT_TYPE;
    private static final Function<Map<String, Object>, String> HOTEL_ID_KEY_RETRIEVER;
    private String indexToDelete;
    private SearchClientBuilder clientBuilder;

    static {
        HOTEL_DOCUMENT_TYPE = new TypeReference<Map<String, Object>>() {
        };
        HOTEL_ID_KEY_RETRIEVER = document -> String.valueOf(document.get("HotelId"));
    }

    private void setupIndex(boolean isSync) {
        String indexName = createHotelIndex();
        this.indexToDelete = indexName;

        this.clientBuilder = getSearchClientBuilder(indexName, isSync);
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        if (!CoreUtils.isNullOrEmpty(indexToDelete)) {
            getSearchIndexClientBuilder(true).buildClient().deleteIndex(indexToDelete);
        }
    }

    /**
     * Tests that flushing the batch sends the documents to the service.
     */
    @Test
    public void flushBatch() {
        setupIndex(true);

        SearchClient client = clientBuilder.buildClient();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = clientBuilder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));
        batchingClient.flush();

        waitForIndexing();

        assertEquals(10, client.getDocumentCount());

        batchingClient.close();
    }

    /**
     * Tests that flushing the batch sends the documents to the service.
     */
    @Test
    public void flushBatchAsync() {
        setupIndex(false);

        SearchAsyncClient client = clientBuilder.buildAsyncClient();
        SearchIndexingBufferedAsyncSender<Map<String, Object>> batchingClient = clientBuilder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlush(false)
            .buildAsyncSender();

        StepVerifier.create(batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON))
            .then(batchingClient.flush()))
            .verifyComplete();

        waitForIndexing();

        StepVerifier.create(client.getDocumentCount())
            .assertNext(count -> assertEquals(10, count))
            .verifyComplete();

        StepVerifier.create(batchingClient.close()).verifyComplete();
    }

    /**
     * Tests that the batch will automatically flush when the configured batch size is reached, if configured for auto
     * flushing.
     */
    @Test
    public void autoFlushBatchOnSize() {
        setupIndex(true);

        SearchClient client = clientBuilder.buildClient();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = clientBuilder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlushInterval(Duration.ofMinutes(5))
            .initialBatchActionCount(10)
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        waitForIndexing();

        assertEquals(10, client.getDocumentCount());
        batchingClient.close();
    }

    /**
     * Tests that the batch will automatically flush when the configured batch size is reached, if configured for auto
     * flushing.
     */
    @Test
    public void autoFlushBatchOnSizeAsync() {
        setupIndex(false);

        SearchAsyncClient client = clientBuilder.buildAsyncClient();
        SearchIndexingBufferedAsyncSender<Map<String, Object>> batchingClient = clientBuilder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlushInterval(Duration.ofMinutes(5))
            .initialBatchActionCount(10)
            .buildAsyncSender();

        StepVerifier.create(batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON)))
            .verifyComplete();

        waitForIndexing();

        StepVerifier.create(client.getDocumentCount())
            .assertNext(count -> assertEquals(10, count))
            .verifyComplete();

        StepVerifier.create(batchingClient.close()).verifyComplete();
    }

    /**
     * Tests that batching will automatically flush when the flush windows completes, if configured for auto flushing.
     */
    @Test
    public void autoFlushBatchOnDelay() {
        setupIndex(true);

        SearchClient client = clientBuilder.buildClient();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = clientBuilder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .initialBatchActionCount(10)
            .autoFlushInterval(Duration.ofSeconds(3))
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        waitForIndexing();

        assertEquals(10, client.getDocumentCount());
        batchingClient.close();
    }

    /**
     * Tests that batching will automatically flush when the flush windows completes, if configured for auto flushing.
     */
    @Test
    public void autoFlushBatchOnDelayAsync() {
        setupIndex(false);

        SearchAsyncClient client = clientBuilder.buildAsyncClient();
        SearchIndexingBufferedAsyncSender<Map<String, Object>> batchingClient = clientBuilder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .initialBatchActionCount(10)
            .autoFlushInterval(Duration.ofSeconds(3))
            .buildAsyncSender();

        StepVerifier.create(batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON)))
            .verifyComplete();

        waitForIndexing();

        StepVerifier.create(client.getDocumentCount())
            .assertNext(count -> assertEquals(10, count))
            .verifyComplete();

        StepVerifier.create(batchingClient.close()).verifyComplete();
    }

    /**
     * Tests that the batch will flush when the client is closed and documents still exist in the batch.
     */
    @Test
    public void batchFlushesOnClose() {
        setupIndex(true);

        SearchClient client = clientBuilder.buildClient();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = clientBuilder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));
        batchingClient.close();

        waitForIndexing();

        assertEquals(10, client.getDocumentCount());
    }

    /**
     * Tests that the batch will flush when the client is closed and documents still exist in the batch.
     */
    @Test
    public void batchFlushesOnCloseAsync() {
        setupIndex(false);

        SearchAsyncClient client = clientBuilder.buildAsyncClient();
        SearchIndexingBufferedAsyncSender<Map<String, Object>> batchingClient = clientBuilder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .buildAsyncSender();

        StepVerifier.create(batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON))
            .then(batchingClient.close()))
            .verifyComplete();

        waitForIndexing();

        StepVerifier.create(client.getDocumentCount())
            .assertNext(count -> assertEquals(10, count))
            .verifyComplete();
    }

    /**
     * Tests that when a batch has documents added but flush, auto-flush, or close is never called the documents don't
     * get indexed.
     */
    @Test
    public void batchGetsDocumentsButNeverFlushes() {
        setupIndex(true);

        SearchClient client = clientBuilder.buildClient();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = clientBuilder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlushInterval(Duration.ofMinutes(5))
            .initialBatchActionCount(1000)
            .buildSender();

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        waitForIndexing();

        assertEquals(0, client.getDocumentCount());
        batchingClient.close();
    }

    /**
     * Tests that when a batch has documents added but flush, auto-flush, or close is never called the documents don't
     * get indexed.
     */
    @Test
    public void batchGetsDocumentsButNeverFlushesAsync() {
        setupIndex(false);

        SearchAsyncClient client = clientBuilder.buildAsyncClient();
        SearchIndexingBufferedAsyncSender<Map<String, Object>> batchingClient = clientBuilder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlushInterval(Duration.ofMinutes(5))
            .initialBatchActionCount(1000)
            .buildAsyncSender();

        StepVerifier.create(batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON)))
            .verifyComplete();

        waitForIndexing();

        StepVerifier.create(client.getDocumentCount())
            .assertNext(count -> assertEquals(0, count))
            .verifyComplete();

        StepVerifier.create(batchingClient.close()).verifyComplete();
    }

    @Test
    @LiveOnly
    public void indexManyDocumentsSmallDocumentSets() {
        interceptorManager.addMatchers(Collections.singletonList(new BodilessMatcher()));

        setupIndex(true);

        AtomicInteger requestCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failedCount = new AtomicInteger();

        SearchClientBuilder builder = clientBuilder.addPolicy((context, next) -> {
            requestCount.incrementAndGet();
            return next.process();
        });

        SearchClient client = builder.buildClient();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = builder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlushInterval(Duration.ofSeconds(5))
            .initialBatchActionCount(10)
            .onActionSucceeded(options -> successCount.incrementAndGet())
            .onActionError(options -> failedCount.incrementAndGet())
            .buildSender();

        List<Map<String, Object>> documents = readJsonFileToList(HOTELS_DATA_JSON);
        for (int i = 0; i < 100; i++) {
            final int offset = i;
            batchingClient.addUploadActions(documents.stream()
                .map(LinkedHashMap::new)
                .peek(document -> {
                    int originalId = Integer.parseInt(document.get("HotelId").toString());
                    document.put("HotelId", String.valueOf((offset * 10) + originalId));
                })
                .collect(Collectors.toList()));
        }

        batchingClient.close();

        sleepIfRunningAgainstService(10000);

        assertEquals(1000, successCount.get());
        assertEquals(0, failedCount.get());
        assertTrue(requestCount.get() >= 100);
        assertEquals(1000, client.getDocumentCount());
    }

    @Test
    @LiveOnly
    public void indexManyDocumentsSmallDocumentSetsAsync() {
        interceptorManager.addMatchers(Collections.singletonList(new BodilessMatcher()));

        setupIndex(false);

        AtomicInteger requestCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failedCount = new AtomicInteger();

        SearchClientBuilder builder = clientBuilder.addPolicy((context, next) -> {
            requestCount.incrementAndGet();
            return next.process();
        });

        SearchAsyncClient client = builder.buildAsyncClient();
        SearchIndexingBufferedAsyncSender<Map<String, Object>> batchingClient = builder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlushInterval(Duration.ofSeconds(5))
            .initialBatchActionCount(10)
            .onActionSucceeded(options -> successCount.incrementAndGet())
            .onActionError(options -> failedCount.incrementAndGet())
            .buildAsyncSender();

        List<Map<String, Object>> documents = readJsonFileToList(HOTELS_DATA_JSON);
        for (int i = 0; i < 100; i++) {
            final int offset = i;
            StepVerifier.create(batchingClient.addUploadActions(documents.stream()
                .map(LinkedHashMap::new)
                .peek(document -> document.put("HotelId",
                    String.valueOf((offset * 10) + Integer.parseInt(document.get("HotelId").toString()))))
                .collect(Collectors.toList())))
                .verifyComplete();
        }

        StepVerifier.create(batchingClient.close()).verifyComplete();

        sleepIfRunningAgainstService(10000);

        assertEquals(1000, successCount.get());
        assertEquals(0, failedCount.get());
        assertTrue(requestCount.get() >= 100);
        StepVerifier.create(client.getDocumentCount())
            .assertNext(count -> assertEquals(1000, count))
            .verifyComplete();
    }

    @Test
    @LiveOnly
    public void indexManyDocumentsOneLargeDocumentSet() {
        interceptorManager.addMatchers(Collections.singletonList(new BodilessMatcher()));

        setupIndex(true);

        AtomicInteger requestCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failedCount = new AtomicInteger();

        SearchClientBuilder builder = clientBuilder.addPolicy((context, next) -> {
            requestCount.incrementAndGet();
            return next.process();
        });

        SearchClient client = builder.buildClient();
        SearchIndexingBufferedSender<Map<String, Object>> batchingClient = builder.bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlushInterval(Duration.ofSeconds(5))
            .initialBatchActionCount(10)
            .onActionSucceeded(options -> successCount.incrementAndGet())
            .onActionError(options -> failedCount.incrementAndGet())
            .buildSender();

        List<Map<String, Object>> documents = readJsonFileToList(HOTELS_DATA_JSON);
        List<Map<String, Object>> documentBatch = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final int offset = i;
            documents.stream().map(LinkedHashMap::new)
                .forEach(document -> {
                    int originalId = Integer.parseInt(document.get("HotelId").toString());
                    document.put("HotelId", String.valueOf((offset * 10) + originalId));
                    documentBatch.add(document);
                });
        }

        batchingClient.addUploadActions(documentBatch);

        batchingClient.close();

        sleepIfRunningAgainstService(10000);

        assertEquals(1000, successCount.get());
        assertEquals(0, failedCount.get());
        assertTrue(requestCount.get() >= 100);
        assertEquals(1000, client.getDocumentCount());
    }

    @Test
    @LiveOnly
    public void indexManyDocumentsOneLargeDocumentSetAsync() {
        interceptorManager.addMatchers(Collections.singletonList(new BodilessMatcher()));

        setupIndex(false);

        AtomicInteger requestCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failedCount = new AtomicInteger();

        SearchClientBuilder builder = clientBuilder.addPolicy((context, next) -> {
            requestCount.incrementAndGet();
            return next.process();
        });

        SearchAsyncClient client = clientBuilder.buildAsyncClient();
        SearchIndexingBufferedAsyncSender<Map<String, Object>> batchingClient = builder
            .bufferedSender(HOTEL_DOCUMENT_TYPE)
            .documentKeyRetriever(HOTEL_ID_KEY_RETRIEVER)
            .autoFlushInterval(Duration.ofSeconds(5))
            .initialBatchActionCount(10)
            .onActionSucceeded(options -> successCount.incrementAndGet())
            .onActionError(options -> failedCount.incrementAndGet())
            .buildAsyncSender();

        List<Map<String, Object>> documents = readJsonFileToList(HOTELS_DATA_JSON);
        List<Map<String, Object>> documentBatch = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            final int offset = i;
            documents.stream().map(LinkedHashMap::new)
                .forEach(document -> {
                    int originalId = Integer.parseInt(document.get("HotelId").toString());
                    document.put("HotelId", String.valueOf((offset * 10) + originalId));
                    documentBatch.add(document);
                });
        }

        StepVerifier.create(batchingClient.addUploadActions(documentBatch)
            .then(batchingClient.close()))
            .verifyComplete();

        sleepIfRunningAgainstService(10000);

        assertEquals(1000, successCount.get());
        assertEquals(0, failedCount.get());
        assertTrue(requestCount.get() >= 100);

        StepVerifier.create(client.getDocumentCount())
            .assertNext(count -> assertEquals(1000, count))
            .verifyComplete();
    }
}
