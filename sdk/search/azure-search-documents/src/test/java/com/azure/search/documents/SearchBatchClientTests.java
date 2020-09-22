// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.search.documents.implementation.models.IndexDocumentsResult;
import com.azure.search.documents.implementation.models.IndexingResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.invocation.Invocation;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.readJsonFileToList;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Tests {@link SearchBatchClient}.
 */
public class SearchBatchClientTests extends SearchTestBase {
    private static final JacksonAdapter JACKSON_ADAPTER;
    private String indexToDelete;
    private SearchClientBuilder clientBuilder;

    static {
        JacksonAdapter adapter = new JacksonAdapter();
        adapter.serializer().setAnnotationIntrospector(new IgnoreJacksonWriteOnlyAccess());

        JACKSON_ADAPTER = adapter;
    }


    private void setupIndex() {
        String indexName = createHotelIndex();
        this.indexToDelete = indexName;

        this.clientBuilder = getSearchClientBuilder(indexName);
    }

    @Override
    protected void afterTest() {
        super.afterTest();

        if (!CoreUtils.isNullOrEmpty(indexToDelete)) {
            getSearchIndexClientBuilder().buildClient().deleteIndex(indexToDelete);
        }
    }

    /**
     * Tests that construction of a batching client fails is a batch size of less than one is used.
     */
    @Test
    public void clientThrowsIfBatchSizeIsLessThanOne() {
        assertThrows(IllegalArgumentException.class, () -> new SearchBatchClientBuilder().batchSize(0));
        assertThrows(IllegalArgumentException.class, () -> getSearchClientBuilder("index").buildAsyncClient()
            .getSearchBatchAsyncClient(null, null, 0, null));
        assertThrows(IllegalArgumentException.class, () -> getSearchClientBuilder("index").buildClient()
            .getSearchBatchClient(null, null, 0, null));
    }

    /**
     * Tests that flushing the batch sends the documents to the service.
     */
    @Test
    public void flushBatch() {
        setupIndex();

        SearchClient client = clientBuilder.buildClient();
        SearchBatchClient batchingClient = client
            .getSearchBatchClient(false, null, null, null);

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));
        batchingClient.flush();

        sleepIfRunningAgainstService(3000);

        assertEquals(10, client.getDocumentCount());
    }

    /**
     * Tests that the batch will automatically flush when the configured batch size is reached, if configured for auto
     * flushing.
     */
    @Test
    public void autoFlushBatchOnSize() {
        setupIndex();

        SearchClient client = clientBuilder.buildClient();
        SearchBatchClient batchingClient = client
            .getSearchBatchClient(true, Duration.ofMinutes(5), 10, null);

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        sleepIfRunningAgainstService(3000);

        assertEquals(10, client.getDocumentCount());
    }

    /**
     * Tests that batching will automatically flush when the flush windows completes, if configured for auto flushing.
     */
    @Test
    public void autoFlushBatchOnDelay() {
        setupIndex();

        SearchClient client = clientBuilder.buildClient();
        SearchBatchClient batchingClient = client
            .getSearchBatchClient(true, Duration.ofSeconds(3), 1000, null);

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        sleepIfRunningAgainstService((long) (3000 * 1.5));

        assertEquals(10, client.getDocumentCount());
    }

    /**
     * Tests that the batch will flush when the client is closed and documents still exist in the batch.
     */
    @Test
    public void batchFlushesOnClose() {
        setupIndex();

        SearchClient client = clientBuilder.buildClient();
        SearchBatchClient batchingClient = client
            .getSearchBatchClient(true, Duration.ofMinutes(5), 1000, null);

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));
        batchingClient.close();

        sleepIfRunningAgainstService(3000);

        assertEquals(10, client.getDocumentCount());
    }

    /**
     * Tests that when a batch has documents added but flush, auto-flush, or close is never called the documents don't
     * get indexed.
     */
    @Test
    public void batchGetsDocumentsButNeverFlushes() {
        setupIndex();

        SearchClient client = clientBuilder.buildClient();
        SearchBatchClient batchingClient = client
            .getSearchBatchClient(true, Duration.ofMinutes(5), 1000, null);

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        sleepIfRunningAgainstService(3000);

        assertEquals(0, client.getDocumentCount());
    }

    @Test
    public void indexManyDocuments() {
        setupIndex();

        SearchClient client = clientBuilder.buildClient();
        SearchBatchAsyncClient spyClient = spy(clientBuilder.buildAsyncClient()
            .getSearchBatchAsyncClient(true, Duration.ofSeconds(5), 10, null));
        SearchBatchClient batchingClient = new SearchBatchClient(spyClient);

        List<Map<String, Object>> documents = readJsonFileToList(HOTELS_DATA_JSON);
        for (int i = 0; i < 100; i++) {
            final int offset = i;
            batchingClient.addUploadActions(documents.stream()
                .map(HashMap::new)
                .peek(document -> {
                    int originalId = Integer.parseInt(document.get("HotelId").toString());
                    document.put("HotelId", String.valueOf((offset * 10) + originalId));
                })
                .collect(Collectors.toList()));
        }

        sleepIfRunningAgainstService((long) (5000 * 1.5));

        assertEquals(1000, client.getDocumentCount());
        verify(spyClient, atLeast(100)).flushInternal(anyList(), anyInt(), any());
    }

    /**
     * Tests that an empty batch doesn't attempt to index.
     */
    @Test
    public void emptyBatchIsNeverSent() {
        SearchBatchAsyncClient spyClient = spy(getSearchClientBuilder("index").buildAsyncClient()
            .getSearchBatchAsyncClient());
        SearchBatchClient batchingClient = new SearchBatchClient(spyClient);

        batchingClient.flush();

        // flushInternal should never be called if the batch doesn't have any documents.
        verify(spyClient, never()).flushInternal(anyList(), anyInt(), any());
    }

    /**
     * Tests that a batch can timeout will indexing.
     */
    @Test
    public void flushTimesOut() {
        SearchBatchClient batchingClient = getSearchClientBuilder("index")
            .httpClient(request -> Mono.<HttpResponse>empty().delayElement(Duration.ofSeconds(5)))
            .buildClient()
            .getSearchBatchClient();

        batchingClient.addUploadActions(Collections.singletonList(1));

        assertThrows(RuntimeException.class, () -> batchingClient.flush(Duration.ofSeconds(1), Context.NONE));
    }

    /**
     * Tests that when a batch has some failures the indexing hook is properly notified.
     */
    @Test
    public void batchHasSomeFailures() {
        IndexingHook hook = mock(IndexingHook.class);

        SearchBatchClient batchingClient = getSearchClientBuilder("index")
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 207, new HttpHeaders(),
                createMockResponseData(201, 400, 201, 404, 200, 200, 404, 400, 400, 201))))
            .buildClient()
            .getSearchBatchClient(false, null, null, hook);

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        // Exception is thrown as the batch has partial failures.
        assertThrows(RuntimeException.class, batchingClient::flush);

        /*
         * The hook should have 30 overall interactions. 10 for the number of documents added to the batch, 5 for
         * successful indexing, 5 for unsuccessful indexing, and 10 for actions removed for reaching a terminal state.
         */
        Collection<Invocation> invocations = mockingDetails(hook).getInvocations();
        assertEquals(30, invocations.size());

        Map<String, AtomicInteger> callMapping = getIndexingHookInvocationDetails(invocations);
        assertEquals(10, callMapping.get("actionAdded").get());
        assertEquals(5, callMapping.get("actionSuccess").get());
        assertEquals(5, callMapping.get("actionError").get());
        assertEquals(10, callMapping.get("actionRemoved").get());

        /*
         * No documents failed with retryable errors, so we should expect zero documents are added back into the batch.
         */
        assertEquals(0, batchingClient.getActions().size());
    }

    /**
     * Tests that a batch will retry documents that fail with retryable status code.
     */
    @Test
    public void retryableDocumentsAreAddedBackToTheBatch() {
        IndexingHook hook = mock(IndexingHook.class);

        SearchBatchClient batchingClient = getSearchClientBuilder("index")
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 207, new HttpHeaders(),
                createMockResponseData(201, 409, 201, 422, 200, 200, 503, 409, 422, 201))))
            .buildClient()
            .getSearchBatchClient(false, null, null, hook);

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        // Exception is thrown as the batch has partial failures.
        assertThrows(RuntimeException.class, batchingClient::flush);

        /*
         * The hook should have 20 overall interactions. 10 for the number of documents added to the batch, 5 for
         * successful indexing, 0 for unsuccessful indexing, and 5 for actions removed for reaching a terminal state.
         */
        Collection<Invocation> invocations = mockingDetails(hook).getInvocations();
        assertEquals(20, invocations.size());

        Map<String, AtomicInteger> callMapping = getIndexingHookInvocationDetails(invocations);
        assertEquals(10, callMapping.get("actionAdded").get());
        assertEquals(5, callMapping.get("actionSuccess").get());
        assertFalse(callMapping.containsKey("actionError"));
        assertEquals(5, callMapping.get("actionRemoved").get());

        /*
         * 5 documents failed with retryable errors, so we should expect 5 documents are added back into the batch.
         */
        assertEquals(5, batchingClient.getActions().size());
    }

    /**
     * Tests that a batch splits if the service responds with a 413.
     */
    @Test
    public void batchSplits() {
        IndexingHook hook = mock(IndexingHook.class);
        AtomicInteger callCount = new AtomicInteger();

        SearchBatchClient batchingClient = getSearchClientBuilder("index")
            .httpClient(request -> (callCount.getAndIncrement() == 0)
                ? Mono.just(new MockHttpResponse(request, 413))
                : createMockBatchSplittingResponse(request, 5))
            .buildClient()
            .getSearchBatchClient(false, null, null, hook);

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON));

        // No exception is thrown as the batch splits and retries successfully.
        assertDoesNotThrow((Executable) batchingClient::flush);

        /*
         * The hook should have 30 overall interactions. 10 for the number of documents added to the batch, 10 for
         * successful indexing, 0 for unsuccessful indexing, and 10 for actions removed for reaching a terminal state.
         */
        Collection<Invocation> invocations = mockingDetails(hook).getInvocations();
        assertEquals(30, invocations.size());

        Map<String, AtomicInteger> callMapping = getIndexingHookInvocationDetails(invocations);
        assertEquals(10, callMapping.get("actionAdded").get());
        assertEquals(10, callMapping.get("actionSuccess").get());
        assertFalse(callMapping.containsKey("actionError"));
        assertEquals(10, callMapping.get("actionRemoved").get());

        /*
         * No documents failed, so we should expect zero documents are added back into the batch.
         */
        assertEquals(0, batchingClient.getActions().size());
    }

    /**
     * Tests that an operation will be dumped into a "dead letter" queue if it is retried too many times.
     */
    @Test
    public void batchRetriesUntilLimit() {
        IndexingHook hook = mock(IndexingHook.class);

        SearchBatchClient batchingClient = getSearchClientBuilder("index")
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 207, new HttpHeaders(),
                createMockResponseData(409))))
            .buildClient()
            .getSearchBatchClient(false, null, null, hook);

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON).subList(0, 1));

        // Batch split until it was size of one and failed.
        for (int i = 0; i < 9; i++) {
            assertThrows(RuntimeException.class, batchingClient::flush);

            // Document should be added back into the batch as it is retryable.
            assertEquals(1, batchingClient.getActions().size());
        }

        // Final call which will trigger the retry limit for the document.
        assertThrows(RuntimeException.class, batchingClient::flush);

        /*
         * The hook should have 3 overall interactions. 1 for the number of documents added to the batch, 0 for
         * successful indexing, 1 for unsuccessful indexing, and 1 for actions removed for reaching a terminal state.
         */
        Collection<Invocation> invocations = mockingDetails(hook).getInvocations();
        assertEquals(3, invocations.size());

        Map<String, AtomicInteger> callMapping = getIndexingHookInvocationDetails(invocations);
        assertEquals(1, callMapping.get("actionAdded").get());
        assertEquals(1, callMapping.get("actionError").get());
        assertFalse(callMapping.containsKey("actionSuccess"));
        assertEquals(1, callMapping.get("actionRemoved").get());

        /*
         * All documents failed, so we should expect zero documents are added back into the batch.
         */
        assertEquals(0, batchingClient.getActions().size());
    }

    /**
     * Tests that a batch will split until it is a size of one if the service continues returning 413. When the service
     * returns 413 on a batch size of one it will be deemed a final error state.
     */
    @Test
    public void batchSplitsUntilOneAndFails() {
        IndexingHook hook = mock(IndexingHook.class);

        SearchBatchClient batchingClient = getSearchClientBuilder("index")
            .httpClient(request -> Mono.just(new MockHttpResponse(request, 413)))
            .buildClient()
            .getSearchBatchClient(false, null, null, hook);

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON).subList(0, 2));

        // Batch split until it was size of one and failed.
        assertThrows(RuntimeException.class, batchingClient::flush);

        /*
         * The hook should have 6 overall interactions. 2 for the number of documents added to the batch, 0 for
         * successful indexing, 2 for unsuccessful indexing, and 2 for actions removed for reaching a terminal state.
         */
        Collection<Invocation> invocations = mockingDetails(hook).getInvocations();
        assertEquals(6, invocations.size());

        Map<String, AtomicInteger> callMapping = getIndexingHookInvocationDetails(invocations);
        assertEquals(2, callMapping.get("actionAdded").get());
        assertEquals(2, callMapping.get("actionError").get());
        assertFalse(callMapping.containsKey("actionSuccess"));
        assertEquals(2, callMapping.get("actionRemoved").get());

        /*
         * No documents failed, so we should expect zero documents are added back into the batch.
         */
        assertEquals(0, batchingClient.getActions().size());
    }

    /**
     * Tests that a batch will split until all sub-batches are one document and some of the sub batches fail with 413
     * while others do not.
     */
    @Test
    public void batchSplitsUntilOneAndPartiallyFails() {
        IndexingHook hook = mock(IndexingHook.class);
        AtomicInteger callCount = new AtomicInteger();

        SearchBatchClient batchingClient = getSearchClientBuilder("index")
            .httpClient(request -> (callCount.getAndIncrement() < 2)
                ? Mono.just(new MockHttpResponse(request, 413))
                : createMockBatchSplittingResponse(request, 1))
            .buildClient()
            .getSearchBatchClient(false, null, null, hook);

        batchingClient.addUploadActions(readJsonFileToList(HOTELS_DATA_JSON).subList(0, 2));

        // Batch split until it was size of one and failed.
        assertThrows(RuntimeException.class, batchingClient::flush);

        /*
         * The hook should have 6 overall interactions. 2 for the number of documents added to the batch, 1 for
         * successful indexing, 1 for unsuccessful indexing, and 2 for actions removed for reaching a terminal state.
         */
        Collection<Invocation> invocations = mockingDetails(hook).getInvocations();
        assertEquals(6, invocations.size());

        Map<String, AtomicInteger> callMapping = getIndexingHookInvocationDetails(invocations);
        assertEquals(2, callMapping.get("actionAdded").get());
        assertEquals(1, callMapping.get("actionError").get());
        assertEquals(1, callMapping.get("actionSuccess").get());
        assertEquals(2, callMapping.get("actionRemoved").get());

        /*
         * No documents failed, so we should expect zero documents are added back into the batch.
         */
        assertEquals(0, batchingClient.getActions().size());
    }

    /*
     * Helper method that creates mock results with the status codes given. This will create a mock indexing result
     * and turn it into a byte[] so it can be put in a mock response.
     */
    private static byte[] createMockResponseData(int... statusCodes) {
        List<IndexingResult> results = new ArrayList<>();

        for (int i = 0; i < statusCodes.length; i++) {
            int statusCode = statusCodes[i];
            results.add(new IndexingResult(String.valueOf(i + 1), statusCode == 200 || statusCode == 201, statusCode));
        }

        try {
            return JACKSON_ADAPTER.serialize(new IndexDocumentsResult(results), SerializerEncoding.JSON)
                .getBytes(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /*
     * Helper class to ignore write only properties that need to be spoofed.
     */
    private static class IgnoreJacksonWriteOnlyAccess extends JacksonAnnotationIntrospector {
        @Override
        public JsonProperty.Access findPropertyAccess(Annotated m) {
            JsonProperty.Access access = super.findPropertyAccess(m);
            if (access == JsonProperty.Access.WRITE_ONLY) {
                return JsonProperty.Access.AUTO;
            }
            return access;
        }
    }

    /*
     * Helper method which converts the invocations of an IndexingHook mock into a method call-count map.
     */
    private static Map<String, AtomicInteger> getIndexingHookInvocationDetails(Collection<Invocation> invocations) {
        Map<String, AtomicInteger> callMapping = new HashMap<>();
        for (Invocation invocation : invocations) {
            String methodName = invocation.getMethod().getName();
            if (callMapping.containsKey(methodName)) {
                callMapping.get(methodName).incrementAndGet();
            } else {
                callMapping.put(methodName, new AtomicInteger(1));
            }
        }

        return callMapping;
    }

    private static Mono<HttpResponse> createMockBatchSplittingResponse(HttpRequest request, int expectedBatchSize) {
        return FluxUtil.collectBytesInByteBufferStream(request.getBody())
            .flatMap(bodyBytes -> {
                try {
                    // Request documents are in a sub-node called value.
                    JsonNode jsonNode = JACKSON_ADAPTER.serializer().readTree(bodyBytes).get("value");

                    // Given the initial size was 10 and it was split we should expect 5 elements.
                    assertTrue(jsonNode.isArray());
                    assertEquals(expectedBatchSize, jsonNode.size());

                    int[] statusCodes = new int[expectedBatchSize];
                    Arrays.fill(statusCodes, 200);

                    return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders(),
                        createMockResponseData(statusCodes)));
                } catch (IOException e) {
                    return Mono.error(e);
                }
            });
    }
}
