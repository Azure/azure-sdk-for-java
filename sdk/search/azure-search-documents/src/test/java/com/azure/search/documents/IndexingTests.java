// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.http.rest.Response;
import com.azure.core.models.GeoPoint;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.ReadValueCallback;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.models.IndexAction;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexBatchException;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.IndexDocumentsOptions;
import com.azure.search.documents.models.IndexDocumentsResult;
import com.azure.search.documents.models.IndexingResult;
import com.azure.search.documents.test.environment.models.Author;
import com.azure.search.documents.test.environment.models.Book;
import com.azure.search.documents.test.environment.models.Hotel;
import com.azure.search.documents.test.environment.models.HotelAddress;
import com.azure.search.documents.test.environment.models.HotelRoom;
import com.azure.search.documents.test.environment.models.LoudHotel;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.assertMapEquals;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.convertFromMapStringObject;
import static com.azure.search.documents.TestHelpers.convertToIndexAction;
import static com.azure.search.documents.TestHelpers.convertToMapStringObject;
import static com.azure.search.documents.TestHelpers.createIndexAction;
import static com.azure.search.documents.TestHelpers.setupSharedIndex;
import static com.azure.search.documents.TestHelpers.verifyHttpResponseError;
import static com.azure.search.documents.TestHelpers.waitForIndexing;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
public class IndexingTests extends SearchTestBase {
    private static final String BOOKS_INDEX_JSON = "BooksIndexData.json";

    // Don't use more than three shared instances at once to support testing against free instances which only support
    // three indexes.
    private static final String HOTEL_INDEX_NAME = "azsearch-indexing-shared-hotel-instance";
    private static final String EMPTY_INDEX_NAME = "azsearch-indexing-shared-empty-instance";
    private static final String BOOKS_INDEX_NAME = "azsearch-indexing-shared-books-instance";

    private static SearchIndexClient searchIndexClient;

    @BeforeAll
    public static void setupClass() {
        TestProxyTestBase.setupClass();

        if (TEST_MODE == TestMode.PLAYBACK) {
            return;
        }

        searchIndexClient = setupSharedIndex(HOTEL_INDEX_NAME, HOTELS_TESTS_INDEX_DATA_JSON, null);
        setupSharedIndex(EMPTY_INDEX_NAME, HOTELS_TESTS_INDEX_DATA_JSON, null);
        setupSharedIndex(BOOKS_INDEX_NAME, BOOKS_INDEX_JSON, null);
    }

    @AfterAll
    public static void cleanupClass() {
        if (TEST_MODE != TestMode.PLAYBACK) {
            searchIndexClient.deleteIndex(HOTEL_INDEX_NAME);
            searchIndexClient.deleteIndex(EMPTY_INDEX_NAME);
            searchIndexClient.deleteIndex(BOOKS_INDEX_NAME);
        }
    }

    private SearchClient getClient(String indexName) {
        return getSearchClientBuilder(indexName, true).buildClient();
    }

    private SearchAsyncClient getAsyncClient(String indexName) {
        return getSearchClientBuilder(indexName, false).buildAsyncClient();
    }

    private String getRandomDocumentKey() {
        return testResourceNamer.randomName("key", 32);
    }

    @Test
    public void countingDocsOfNewIndexGivesZeroSync() {
        SearchClient client = getClient(EMPTY_INDEX_NAME);

        assertEquals(0L, client.getDocumentCount());
    }

    @Test
    public void countingDocsOfNewIndexGivesZeroAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(EMPTY_INDEX_NAME);

        StepVerifier.create(asyncClient.getDocumentCount())
            .assertNext(actual -> assertEquals(0, actual))
            .verifyComplete();
    }

    @Test
    @LiveOnly
    public void indexDoesNotThrowWhenAllActionsSucceedSync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String expectedHotelId = getRandomDocumentKey();
        IndexDocumentsBatch batch = new IndexDocumentsBatch(
            convertToIndexAction(new Hotel().hotelId(expectedHotelId), IndexActionType.UPLOAD));

        List<IndexingResult> result = client.indexDocuments(batch).getResults();
        assertIndexActionSucceeded(expectedHotelId, result.get(0), 201);
    }

    @Test
    @LiveOnly
    public void indexDoesNotThrowWhenAllActionsSucceedAsync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String expectedHotelId = getRandomDocumentKey();
        IndexDocumentsBatch batch = new IndexDocumentsBatch(
            convertToIndexAction(new Hotel().hotelId(expectedHotelId), IndexActionType.UPLOAD));

        StepVerifier.create(asyncClient.indexDocuments(batch))
            .assertNext(result -> assertIndexActionSucceeded(expectedHotelId, result.getResults().get(0), 201))
            .verifyComplete();
    }

    @Test
    @LiveOnly
    public void canIndexWithPascalCaseFieldsSync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchClient client = getClient(BOOKS_INDEX_NAME);

        String isbn = getRandomDocumentKey();
        IndexDocumentsBatch batch = new IndexDocumentsBatch(convertToIndexAction(new Book().ISBN(isbn)
            .title("Lord of the Rings")
            .author(new Author().firstName("J.R.R").lastName("Tolkien")), IndexActionType.UPLOAD));

        List<IndexingResult> result = client.indexDocuments(batch).getResults();
        assertIndexActionSucceeded(isbn, result.get(0), 201);
    }

    @Test
    @LiveOnly
    public void canIndexWithPascalCaseFieldsAsync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchAsyncClient asyncClient = getAsyncClient(BOOKS_INDEX_NAME);

        String isbn = getRandomDocumentKey();
        IndexDocumentsBatch batch = new IndexDocumentsBatch(convertToIndexAction(new Book().ISBN(isbn)
            .title("Lord of the Rings")
            .author(new Author().firstName("J.R.R").lastName("Tolkien")), IndexActionType.UPLOAD));

        StepVerifier.create(asyncClient.indexDocuments(batch))
            .assertNext(result -> assertIndexActionSucceeded(isbn, result.getResults().get(0), 201))
            .verifyComplete();
    }

    @Test
    @LiveOnly
    public void canDeleteBatchByKeysSync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotel1Key = getRandomDocumentKey();
        String hotel2Key = getRandomDocumentKey();

        client.indexDocuments(
            new IndexDocumentsBatch(convertToIndexAction(new Hotel().hotelId(hotel1Key), IndexActionType.UPLOAD),
                convertToIndexAction(new Hotel().hotelId(hotel2Key), IndexActionType.UPLOAD)));

        waitForIndexing();

        IndexDocumentsBatch deleteBatch = new IndexDocumentsBatch(
            createIndexAction(IndexActionType.DELETE, Collections.singletonMap("HotelId", hotel1Key)),
            createIndexAction(IndexActionType.DELETE, Collections.singletonMap("HotelId", hotel2Key)));

        IndexDocumentsResult documentIndexResult = client.indexDocuments(deleteBatch);

        assertEquals(2, documentIndexResult.getResults().size());
        assertIndexActionSucceeded(hotel1Key, documentIndexResult.getResults().get(0), 200);
        assertIndexActionSucceeded(hotel2Key, documentIndexResult.getResults().get(1), 200);
    }

    @Test
    @LiveOnly
    public void canDeleteBatchByKeysAsync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotel1Key = getRandomDocumentKey();
        String hotel2Key = getRandomDocumentKey();

        asyncClient
            .indexDocuments(
                new IndexDocumentsBatch(convertToIndexAction(new Hotel().hotelId(hotel1Key), IndexActionType.UPLOAD),
                    convertToIndexAction(new Hotel().hotelId(hotel2Key), IndexActionType.UPLOAD)))
            .block();

        waitForIndexing();

        IndexDocumentsBatch deleteBatch = new IndexDocumentsBatch(
            createIndexAction(IndexActionType.DELETE, Collections.singletonMap("HotelId", hotel1Key)),
            createIndexAction(IndexActionType.DELETE, Collections.singletonMap("HotelId", hotel2Key)));

        StepVerifier.create(asyncClient.indexDocuments(deleteBatch)).assertNext(result -> {
            assertEquals(2, result.getResults().size());
            assertIndexActionSucceeded(hotel1Key, result.getResults().get(0), 200);
            assertIndexActionSucceeded(hotel2Key, result.getResults().get(1), 200);
        }).verifyComplete();
    }

    @Test
    @LiveOnly
    public void indexDoesNotThrowWhenDeletingDocumentWithExtraFieldsSync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Hotel hotel = new Hotel().hotelId(hotelId).category("Luxury");

        client.indexDocuments(new IndexDocumentsBatch(convertToIndexAction(hotel, IndexActionType.UPLOAD)));
        waitForIndexing();

        hotel.category("ignored");
        IndexDocumentsResult documentIndexResult
            = client.indexDocuments(new IndexDocumentsBatch(convertToIndexAction(hotel, IndexActionType.DELETE)));

        assertEquals(1, documentIndexResult.getResults().size());
        assertIndexActionSucceeded(hotelId, documentIndexResult.getResults().get(0), 200);
    }

    @Test
    @LiveOnly
    public void indexDoesNotThrowWhenDeletingDocumentWithExtraFieldsAsync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Hotel hotel = new Hotel().hotelId(hotelId).category("Luxury");

        asyncClient.indexDocuments(new IndexDocumentsBatch(convertToIndexAction(hotel, IndexActionType.UPLOAD)))
            .block();
        waitForIndexing();

        hotel.category("ignored");

        StepVerifier
            .create(asyncClient
                .indexDocuments(new IndexDocumentsBatch(convertToIndexAction(hotel, IndexActionType.DELETE))))
            .assertNext(result -> {
                assertEquals(1, result.getResults().size());
                assertIndexActionSucceeded(hotelId, result.getResults().get(0), 200);
            })
            .verifyComplete();
    }

    @Test
    @LiveOnly
    public void indexDoesNotThrowWhenDeletingDynamicDocumentWithExtraFieldsSync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("HotelId", hotelId);
        searchDocument.put("Category", "Luxury");

        client.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, searchDocument)));

        waitForIndexing();

        searchDocument.put("Category", "ignored");
        IndexDocumentsResult documentIndexResult
            = client.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.DELETE, searchDocument)));

        assertEquals(1, documentIndexResult.getResults().size());
        assertIndexActionSucceeded(hotelId, documentIndexResult.getResults().get(0), 200);
    }

    @Test
    @LiveOnly
    public void indexDoesNotThrowWhenDeletingDynamicDocumentWithExtraFieldsAsync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Map<String, Object> searchDocument = new LinkedHashMap<>();
        searchDocument.put("HotelId", hotelId);
        searchDocument.put("Category", "Luxury");

        asyncClient.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, searchDocument)))
            .block();
        waitForIndexing();

        searchDocument.put("Category", "ignored");

        StepVerifier
            .create(asyncClient
                .indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.DELETE, searchDocument))))
            .assertNext(result -> {
                assertEquals(1, result.getResults().size());
                assertIndexActionSucceeded(hotelId, result.getResults().get(0), 200);
            })
            .verifyComplete();
    }

    @Test
    @LiveOnly
    public void canIndexStaticallyTypedDocumentsSync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotel1Id = getRandomDocumentKey();
        String hotel2Id = getRandomDocumentKey();
        String hotel3Id = getRandomDocumentKey();
        Hotel hotel1 = prepareStaticallyTypedHotel(hotel1Id);
        Hotel hotel2 = prepareStaticallyTypedHotel(hotel2Id);
        Hotel hotel3 = prepareStaticallyTypedHotel(hotel3Id);
        Hotel nonExistingHotel = prepareStaticallyTypedHotel("nonExistingHotel"); // merging with a non-existing document
        Hotel randomHotel = prepareStaticallyTypedHotel("randomId"); // deleting a non existing document

        IndexDocumentsBatch batch = new IndexDocumentsBatch(convertToIndexAction(hotel1, IndexActionType.UPLOAD),
            convertToIndexAction(randomHotel, IndexActionType.DELETE),
            convertToIndexAction(nonExistingHotel, IndexActionType.MERGE),
            convertToIndexAction(hotel3, IndexActionType.MERGE_OR_UPLOAD),
            convertToIndexAction(hotel2, IndexActionType.UPLOAD));

        IndexBatchException ex = assertThrows(IndexBatchException.class,
            () -> client.indexDocumentsWithResponse(batch, new IndexDocumentsOptions().setThrowOnAnyError(true), null));

        List<IndexingResult> results = ex.getIndexingResults();
        assertEquals(results.size(), batch.getActions().size());

        assertSuccessfulIndexResult(results.get(0), hotel1Id, 201);
        assertSuccessfulIndexResult(results.get(1), "randomId", 200);
        assertFailedIndexResult(results.get(2), "nonExistingHotel", 404);
        assertSuccessfulIndexResult(results.get(3), hotel3Id, 201);
        assertSuccessfulIndexResult(results.get(4), hotel2Id, 201);

        for (Hotel hotel : Arrays.asList(hotel1, hotel2, hotel3)) {
            Hotel actual = getAndConvertDocument(client, hotel.hotelId(), Hotel::fromJson);
            assertObjectEquals(hotel, actual, true);
        }
    }

    @Test
    @LiveOnly
    public void canIndexStaticallyTypedDocumentsAsync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotel1Id = getRandomDocumentKey();
        String hotel2Id = getRandomDocumentKey();
        String hotel3Id = getRandomDocumentKey();
        Hotel hotel1 = prepareStaticallyTypedHotel(hotel1Id);
        Hotel hotel2 = prepareStaticallyTypedHotel(hotel2Id);
        Hotel hotel3 = prepareStaticallyTypedHotel(hotel3Id);
        Hotel nonExistingHotel = prepareStaticallyTypedHotel("nonExistingHotel"); // merging with a non-existing document
        Hotel randomHotel = prepareStaticallyTypedHotel("randomId"); // deleting a non existing document

        IndexDocumentsBatch batch
            = new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(hotel1)),
                createIndexAction(IndexActionType.DELETE, convertToMapStringObject(randomHotel)),
                createIndexAction(IndexActionType.MERGE, convertToMapStringObject(nonExistingHotel)),
                createIndexAction(IndexActionType.MERGE_OR_UPLOAD, convertToMapStringObject(hotel3)),
                createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(hotel2)));

        StepVerifier
            .create(asyncClient.indexDocumentsWithResponse(batch, new IndexDocumentsOptions().setThrowOnAnyError(true),
                null))
            .verifyErrorSatisfies(throwable -> {
                IndexBatchException ex = assertInstanceOf(IndexBatchException.class, throwable);

                List<IndexingResult> results = ex.getIndexingResults();
                assertEquals(results.size(), batch.getActions().size());

                assertSuccessfulIndexResult(results.get(0), hotel1Id, 201);
                assertSuccessfulIndexResult(results.get(1), "randomId", 200);
                assertFailedIndexResult(results.get(2), "nonExistingHotel", 404);
                assertSuccessfulIndexResult(results.get(3), hotel3Id, 201);
                assertSuccessfulIndexResult(results.get(4), hotel2Id, 201);
            });

        for (Hotel hotel : Arrays.asList(hotel1, hotel2, hotel3)) {
            getAndValidateDocumentAsync(asyncClient, hotel.hotelId(), hotel,
                map -> convertFromMapStringObject(map, Hotel::fromJson),
                (expected, actual) -> assertObjectEquals(expected, actual, true));
        }
    }

    @Test
    @LiveOnly
    public void canIndexDynamicDocumentsNotThrowSync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotel1Id = getRandomDocumentKey();
        String hotel2Id = getRandomDocumentKey();
        String hotel3Id = getRandomDocumentKey();
        Map<String, Object> hotel1 = prepareDynamicallyTypedHotel(hotel1Id);
        Map<String, Object> hotel2 = prepareDynamicallyTypedHotel(hotel2Id);
        Map<String, Object> hotel3 = prepareDynamicallyTypedHotel(hotel3Id);
        Map<String, Object> nonExistingHotel = prepareDynamicallyTypedHotel("nonExistingHotel"); // deleting a non existing document
        Map<String, Object> randomHotel = prepareDynamicallyTypedHotel("randomId"); // deleting a non existing document

        IndexDocumentsBatch batch = new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, hotel1),
            createIndexAction(IndexActionType.DELETE, randomHotel),
            createIndexAction(IndexActionType.MERGE, nonExistingHotel),
            createIndexAction(IndexActionType.MERGE_OR_UPLOAD, hotel3),
            createIndexAction(IndexActionType.UPLOAD, hotel3));

        Response<IndexDocumentsResult> resultResponse
            = client.indexDocumentsWithResponse(batch, new IndexDocumentsOptions().setThrowOnAnyError(false), null);
        List<IndexingResult> results = resultResponse.getValue().getResults();
        assertEquals(207, resultResponse.getStatusCode());
        assertSuccessfulIndexResult(results.get(0), hotel1Id, 201);
        assertSuccessfulIndexResult(results.get(1), "randomId", 200);
        assertFailedIndexResult(results.get(2), "nonExistingHotel", 404);
        assertSuccessfulIndexResult(results.get(3), hotel3Id, 201);
        assertSuccessfulIndexResult(results.get(4), hotel2Id, 201);

        for (Map<String, Object> hotel : Arrays.asList(hotel1, hotel2, hotel3)) {
            Map<String, Object> actual = client.getDocument(hotel.get("HotelId").toString()).getAdditionalProperties();
            assertMapEquals(hotel, actual, true);
        }
    }

    @Test
    @LiveOnly
    public void canIndexDynamicDocumentsNotThrowAsync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotel1Id = getRandomDocumentKey();
        String hotel2Id = getRandomDocumentKey();
        String hotel3Id = getRandomDocumentKey();
        Map<String, Object> hotel1 = prepareDynamicallyTypedHotel(hotel1Id);
        Map<String, Object> hotel2 = prepareDynamicallyTypedHotel(hotel2Id);
        Map<String, Object> hotel3 = prepareDynamicallyTypedHotel(hotel3Id);
        Map<String, Object> nonExistingHotel = prepareDynamicallyTypedHotel("nonExistingHotel"); // deleting a non existing document
        Map<String, Object> randomHotel = prepareDynamicallyTypedHotel("randomId"); // deleting a non existing document

        IndexDocumentsBatch batch = new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, hotel1),
            createIndexAction(IndexActionType.DELETE, randomHotel),
            createIndexAction(IndexActionType.MERGE, nonExistingHotel),
            createIndexAction(IndexActionType.MERGE_OR_UPLOAD, hotel3),
            createIndexAction(IndexActionType.UPLOAD, hotel2));

        StepVerifier
            .create(asyncClient.indexDocumentsWithResponse(batch, new IndexDocumentsOptions().setThrowOnAnyError(false),
                null))
            .assertNext(resultResponse -> {
                List<IndexingResult> results = resultResponse.getValue().getResults();
                assertEquals(207, resultResponse.getStatusCode());
                assertSuccessfulIndexResult(results.get(0), hotel1Id, 201);
                assertSuccessfulIndexResult(results.get(1), "randomId", 200);
                assertFailedIndexResult(results.get(2), "nonExistingHotel", 404);
                assertSuccessfulIndexResult(results.get(3), hotel3Id, 201);
                assertSuccessfulIndexResult(results.get(4), hotel2Id, 201);
            })
            .verifyComplete();

        for (Map<String, Object> hotel : Arrays.asList(hotel1, hotel2, hotel3)) {
            getAndValidateDocumentAsync(asyncClient, hotel.get("HotelId").toString(), hotel, map -> map,
                (expected, actual) -> assertMapEquals(expected, actual, true));
        }
    }

    @Test
    @LiveOnly
    public void canIndexDynamicDocumentsThrowOnErrorSync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotel1Id = getRandomDocumentKey();
        String hotel2Id = getRandomDocumentKey();
        String hotel3Id = getRandomDocumentKey();
        Map<String, Object> hotel1 = prepareDynamicallyTypedHotel(hotel1Id);
        Map<String, Object> hotel2 = prepareDynamicallyTypedHotel(hotel2Id);
        Map<String, Object> hotel3 = prepareDynamicallyTypedHotel(hotel3Id);
        Map<String, Object> nonExistingHotel = prepareDynamicallyTypedHotel("nonExistingHotel"); // deleting a non existing document
        Map<String, Object> randomHotel = prepareDynamicallyTypedHotel("randomId"); // deleting a non existing document

        IndexDocumentsBatch batch = new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, hotel1),
            createIndexAction(IndexActionType.DELETE, randomHotel),
            createIndexAction(IndexActionType.MERGE, nonExistingHotel),
            createIndexAction(IndexActionType.MERGE_OR_UPLOAD, hotel3),
            createIndexAction(IndexActionType.UPLOAD, hotel2));

        IndexBatchException ex = assertThrows(IndexBatchException.class, () -> client.indexDocuments(batch));
        List<IndexingResult> results = ex.getIndexingResults();
        assertEquals(results.size(), batch.getActions().size());

        assertSuccessfulIndexResult(results.get(0), hotel1Id, 201);
        assertSuccessfulIndexResult(results.get(1), "randomId", 200);
        assertFailedIndexResult(results.get(2), "nonExistingHotel", 404);
        assertSuccessfulIndexResult(results.get(3), hotel3Id, 201);
        assertSuccessfulIndexResult(results.get(4), hotel2Id, 201);

        for (Map<String, Object> hotel : Arrays.asList(hotel1, hotel2, hotel3)) {
            Map<String, Object> actual = client.getDocument(hotel.get("HotelId").toString()).getAdditionalProperties();
            assertMapEquals(hotel, actual, true);
        }
    }

    @Test
    @LiveOnly
    public void canIndexDynamicDocumentsThrowOnErrorAsync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotel1Id = getRandomDocumentKey();
        String hotel2Id = getRandomDocumentKey();
        String hotel3Id = getRandomDocumentKey();
        Map<String, Object> hotel1 = prepareDynamicallyTypedHotel(hotel1Id);
        Map<String, Object> hotel2 = prepareDynamicallyTypedHotel(hotel2Id);
        Map<String, Object> hotel3 = prepareDynamicallyTypedHotel(hotel3Id);
        Map<String, Object> nonExistingHotel = prepareDynamicallyTypedHotel("nonExistingHotel"); // deleting a non existing document
        Map<String, Object> randomHotel = prepareDynamicallyTypedHotel("randomId"); // deleting a non existing document

        IndexDocumentsBatch batch = new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, hotel1),
            createIndexAction(IndexActionType.DELETE, randomHotel),
            createIndexAction(IndexActionType.MERGE, nonExistingHotel),
            createIndexAction(IndexActionType.MERGE_OR_UPLOAD, hotel3),
            createIndexAction(IndexActionType.UPLOAD, hotel2));

        StepVerifier.create(asyncClient.indexDocuments(batch)).verifyErrorSatisfies(throwable -> {
            IndexBatchException ex = assertInstanceOf(IndexBatchException.class, throwable);

            List<IndexingResult> results = ex.getIndexingResults();
            assertEquals(results.size(), batch.getActions().size());

            assertSuccessfulIndexResult(results.get(0), hotel1Id, 201);
            assertSuccessfulIndexResult(results.get(1), "randomId", 200);
            assertFailedIndexResult(results.get(2), "nonExistingHotel", 404);
            assertSuccessfulIndexResult(results.get(3), hotel3Id, 201);
            assertSuccessfulIndexResult(results.get(4), hotel2Id, 201);
        });

        for (Map<String, Object> hotel : Arrays.asList(hotel1, hotel2, hotel3)) {
            getAndValidateDocumentAsync(asyncClient, hotel.get("HotelId").toString(), hotel, map -> map,
                (expected, actual) -> assertMapEquals(expected, actual, true));
        }
    }

    @Test
    public void indexWithInvalidDocumentThrowsExceptionSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        assertHttpResponseException(
            () -> client.indexDocuments(
                new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, new LinkedHashMap<>()))),
            HttpURLConnection.HTTP_BAD_REQUEST, null);
    }

    @Test
    public void indexWithInvalidDocumentThrowsExceptionAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        StepVerifier
            .create(asyncClient.indexDocuments(
                new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, new LinkedHashMap<>()))))
            .verifyErrorSatisfies(
                throwable -> verifyHttpResponseError(throwable, HttpURLConnection.HTTP_BAD_REQUEST, null));
    }

    @Test
    public void canRoundtripBoundaryValuesSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        List<Hotel> boundaryConditionDocs = getBoundaryValues();

        client.indexDocuments(new IndexDocumentsBatch(boundaryConditionDocs.stream()
            .map(doc -> createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(doc)))
            .collect(Collectors.toList())));
        waitForIndexing();

        for (Hotel expected : boundaryConditionDocs) {
            Hotel actual = getAndConvertDocument(client, expected.hotelId(), Hotel::fromJson);

            assertObjectEquals(expected, actual, true);
        }
    }

    @Test
    public void canRoundtripBoundaryValuesAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        List<Hotel> boundaryConditionDocs = getBoundaryValues();

        asyncClient.indexDocuments(new IndexDocumentsBatch(boundaryConditionDocs.stream()
            .map(doc -> createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(doc)))
            .collect(Collectors.toList()))).block();
        waitForIndexing();

        for (Hotel expected : boundaryConditionDocs) {
            getAndValidateDocumentAsync(asyncClient, expected.hotelId(), expected,
                map -> convertFromMapStringObject(map, Hotel::fromJson),
                (ignored, actual) -> assertObjectEquals(expected, actual, true));
        }
    }

    @Test
    public void dynamicDocumentDateTimesRoundTripAsUtcSync() {
        SearchClient client = getClient(BOOKS_INDEX_NAME);

        OffsetDateTime utcTime = OffsetDateTime.of(LocalDateTime.of(2010, 1, 1, 0, 0, 0), ZoneOffset.UTC);
        // UTC-8
        OffsetDateTime utcTimeMinusEight
            = OffsetDateTime.of(LocalDateTime.of(2010, 1, 1, 0, 0, 0), ZoneOffset.ofHours(-8));

        String isbn1 = getRandomDocumentKey();
        Map<String, Object> book1 = new LinkedHashMap<>();
        book1.put("ISBN", isbn1);
        book1.put("PublishDate", utcTime);

        String isbn2 = getRandomDocumentKey();
        Map<String, Object> book2 = new LinkedHashMap<>();
        book2.put("ISBN", isbn2);
        book2.put("PublishDate", utcTimeMinusEight);

        client.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, book1),
            createIndexAction(IndexActionType.UPLOAD, book2)));
        waitForIndexing();

        Map<String, Object> actualBook1 = client.getDocument(isbn1).getAdditionalProperties();
        assertEquals(utcTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), actualBook1.get("PublishDate"));

        // Azure AI Search normalizes to UTC, so we compare instants
        Map<String, Object> actualBook2 = client.getDocument(isbn2).getAdditionalProperties();
        assertEquals(
            utcTimeMinusEight.withOffsetSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
            actualBook2.get("PublishDate"));
    }

    @Test
    public void dynamicDocumentDateTimesRoundTripAsUtcAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(BOOKS_INDEX_NAME);

        OffsetDateTime utcTime = OffsetDateTime.of(LocalDateTime.of(2010, 1, 1, 0, 0, 0), ZoneOffset.UTC);
        // UTC-8
        OffsetDateTime utcTimeMinusEight
            = OffsetDateTime.of(LocalDateTime.of(2010, 1, 1, 0, 0, 0), ZoneOffset.ofHours(-8));

        String isbn1 = getRandomDocumentKey();
        Map<String, Object> book1 = new LinkedHashMap<>();
        book1.put("ISBN", isbn1);
        book1.put("PublishDate", utcTime);

        String isbn2 = getRandomDocumentKey();
        Map<String, Object> book2 = new LinkedHashMap<>();
        book2.put("ISBN", isbn2);
        book2.put("PublishDate", utcTimeMinusEight);

        asyncClient.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, book1),
            createIndexAction(IndexActionType.UPLOAD, book2))).block();
        waitForIndexing();

        getAndValidateDocumentAsync(asyncClient, isbn1, book1, map -> map, (ignored,
            actual) -> assertEquals(utcTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), actual.get("PublishDate")));

        // Azure AI Search normalizes to UTC, so we compare instants
        getAndValidateDocumentAsync(asyncClient, isbn2, book2, map -> map,
            (ignored, actual) -> assertEquals(
                utcTimeMinusEight.withOffsetSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                actual.get("PublishDate")));
    }

    @Test
    public void staticallyTypedDateTimesRoundTripAsUtcSync() {
        SearchClient client = getClient(BOOKS_INDEX_NAME);

        String isbn1 = getRandomDocumentKey();
        String isbn2 = getRandomDocumentKey();
        List<Book> books = Arrays.asList(
            new Book().ISBN(isbn1)
                .publishDate(OffsetDateTime.of(LocalDateTime.of(2010, 1, 1, 0, 0, 0), ZoneOffset.UTC)),
            new Book().ISBN(isbn2)
                .publishDate(OffsetDateTime.of(LocalDateTime.of(2010, 1, 1, 0, 0, 0), ZoneOffset.ofHours(-8))));

        client.indexDocuments(new IndexDocumentsBatch(books.stream()
            .map(book -> createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(book)))
            .collect(Collectors.toList())));

        Book actualBook1 = getAndConvertDocument(client, isbn1, Book::fromJson);
        assertEquals(books.get(0).publishDate(), actualBook1.publishDate());

        // Azure AI Search normalizes to UTC, so we compare instants
        Book actualBook2 = getAndConvertDocument(client, isbn2, Book::fromJson);
        assertEquals(books.get(1).publishDate().withOffsetSameInstant(ZoneOffset.UTC),
            actualBook2.publishDate().withOffsetSameInstant(ZoneOffset.UTC));
    }

    @Test
    public void staticallyTypedDateTimesRoundTripAsUtcAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(BOOKS_INDEX_NAME);

        String isbn1 = getRandomDocumentKey();
        String isbn2 = getRandomDocumentKey();
        List<Book> books = Arrays.asList(
            new Book().ISBN(isbn1)
                .publishDate(OffsetDateTime.of(LocalDateTime.of(2010, 1, 1, 0, 0, 0), ZoneOffset.UTC)),
            new Book().ISBN(isbn2)
                .publishDate(OffsetDateTime.of(LocalDateTime.of(2010, 1, 1, 0, 0, 0), ZoneOffset.ofHours(-8))));

        asyncClient.indexDocuments(new IndexDocumentsBatch(books.stream()
            .map(book -> createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(book)))
            .collect(Collectors.toList()))).block();

        getAndValidateDocumentAsync(asyncClient, isbn1, null, map -> convertFromMapStringObject(map, Book::fromJson),
            (ignored, actual) -> assertEquals(books.get(0).publishDate(), actual.publishDate()));

        // Azure AI Search normalizes to UTC, so we compare instants
        getAndValidateDocumentAsync(asyncClient, isbn2, null, map -> convertFromMapStringObject(map, Book::fromJson),
            (ignored, actual) -> assertEquals(books.get(1).publishDate().withOffsetSameInstant(ZoneOffset.UTC),
                actual.publishDate().withOffsetSameInstant(ZoneOffset.UTC)));
    }

    @Test
    public void canMergeStaticallyTypedDocumentsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();

        // Define hotels
        Hotel originalDoc = canMergeStaticallyTypedDocumentsOriginal(hotelId);

        // Update category, tags, parking included, rating, and rooms. Erase description, last renovation date, location and address.
        Hotel updatedDoc = canMergeStaticallyTypedDocumentsUpdated(hotelId);

        // Fields whose values get updated are updated, and whose values get erased remain the same.
        Hotel expectedDoc = canMergeStaticallyTypedDocumentsExpected(hotelId);

        client.indexDocuments(
            new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(originalDoc))));

        client.indexDocuments(
            new IndexDocumentsBatch(createIndexAction(IndexActionType.MERGE, convertToMapStringObject(updatedDoc))));
        assertObjectEquals(expectedDoc, getAndConvertDocument(client, hotelId, Hotel::fromJson), true);

        client.indexDocuments(
            new IndexDocumentsBatch(createIndexAction(IndexActionType.MERGE, convertToMapStringObject(originalDoc))));
        assertObjectEquals(originalDoc, getAndConvertDocument(client, hotelId, Hotel::fromJson), true);
    }

    @Test
    public void canMergeStaticallyTypedDocumentsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();

        // Define hotels
        Hotel originalDoc = canMergeStaticallyTypedDocumentsOriginal(hotelId);

        // Update category, tags, parking included, rating, and rooms. Erase description, last renovation date, location and address.
        Hotel updatedDoc = canMergeStaticallyTypedDocumentsUpdated(hotelId);

        // Fields whose values get updated are updated, and whose values get erased remain the same.
        Hotel expectedDoc = canMergeStaticallyTypedDocumentsExpected(hotelId);

        asyncClient
            .indexDocuments(new IndexDocumentsBatch(
                createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(originalDoc))))
            .block();

        asyncClient
            .indexDocuments(
                new IndexDocumentsBatch(createIndexAction(IndexActionType.MERGE, convertToMapStringObject(updatedDoc))))
            .block();

        getAndValidateDocumentAsync(asyncClient, hotelId, expectedDoc,
            map -> convertFromMapStringObject(map, Hotel::fromJson),
            (expected, actual) -> assertObjectEquals(expected, actual, true));

        asyncClient
            .indexDocuments(new IndexDocumentsBatch(
                createIndexAction(IndexActionType.MERGE, convertToMapStringObject(originalDoc))))
            .block();

        getAndValidateDocumentAsync(asyncClient, hotelId, originalDoc,
            map -> convertFromMapStringObject(map, Hotel::fromJson),
            (expected, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    @LiveOnly
    public void mergeDocumentWithoutExistingKeyThrowsIndexingExceptionSync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        IndexBatchException ex = assertThrows(IndexBatchException.class,
            () -> client.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.MERGE,
                convertToMapStringObject(prepareStaticallyTypedHotel(hotelId))))));

        List<IndexingResult> results = ex.getIndexingResults();
        assertFailedIndexResult(results.get(0), hotelId, HttpResponseStatus.NOT_FOUND.code());
        assertEquals(1, results.size());
    }

    @Test
    @LiveOnly
    public void mergeDocumentWithoutExistingKeyThrowsIndexingExceptionAsync() {
        // Disable `("$..key")` sanitizer
        // if (!interceptorManager.isLiveMode()) {
        //    interceptorManager.removeSanitizers("AZSDK3447"));
        // }
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        StepVerifier.create(asyncClient.indexDocuments(new IndexDocumentsBatch(
            createIndexAction(IndexActionType.MERGE, convertToMapStringObject(prepareStaticallyTypedHotel(hotelId))))))
            .verifyErrorSatisfies(throwable -> {
                IndexBatchException ex = assertInstanceOf(IndexBatchException.class, throwable);

                List<IndexingResult> results = ex.getIndexingResults();
                assertFailedIndexResult(results.get(0), hotelId, HttpResponseStatus.NOT_FOUND.code());
                assertEquals(1, results.size());
            });
    }

    @Test
    public void canSetExplicitNullsInStaticallyTypedDocumentSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        LoudHotel originalDoc = canSetExplicitNullsInStaticallyTypedDocumentOriginal(hotelId);
        LoudHotel updatedDoc = canSetExplicitNullsInStaticallyTypedDocumentUpdated(hotelId);
        LoudHotel expectedDoc = canSetExplicitNullsInStaticallyTypedDocumentExpected(hotelId);

        client.indexDocuments(
            new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(originalDoc))));
        waitForIndexing();

        client.indexDocuments(
            new IndexDocumentsBatch(createIndexAction(IndexActionType.MERGE, convertToMapStringObject(updatedDoc))));
        waitForIndexing();

        LoudHotel actualDoc1 = getAndConvertDocument(client, hotelId, LoudHotel::fromJson);
        assertObjectEquals(expectedDoc, actualDoc1, true);

        client.indexDocuments(
            new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(originalDoc))));
        waitForIndexing();

        LoudHotel actualDoc2 = getAndConvertDocument(client, hotelId, LoudHotel::fromJson);
        assertObjectEquals(originalDoc, actualDoc2, true);
    }

    @Test
    public void canSetExplicitNullsInStaticallyTypedDocumentAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        LoudHotel originalDoc = canSetExplicitNullsInStaticallyTypedDocumentOriginal(hotelId);
        LoudHotel updatedDoc = canSetExplicitNullsInStaticallyTypedDocumentUpdated(hotelId);
        LoudHotel expectedDoc = canSetExplicitNullsInStaticallyTypedDocumentExpected(hotelId);

        asyncClient
            .indexDocuments(new IndexDocumentsBatch(
                createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(originalDoc))))
            .block();
        waitForIndexing();

        asyncClient
            .indexDocuments(
                new IndexDocumentsBatch(createIndexAction(IndexActionType.MERGE, convertToMapStringObject(updatedDoc))))
            .block();
        waitForIndexing();

        getAndValidateDocumentAsync(asyncClient, hotelId, expectedDoc,
            map -> convertFromMapStringObject(map, LoudHotel::fromJson),
            (expected, actual) -> assertObjectEquals(expected, actual, true));

        asyncClient
            .indexDocuments(new IndexDocumentsBatch(
                createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(originalDoc))))
            .block();
        waitForIndexing();

        getAndValidateDocumentAsync(asyncClient, hotelId, originalDoc,
            map -> convertFromMapStringObject(map, LoudHotel::fromJson),
            (expected, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void canMergeDynamicDocumentsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Map<String, Object> originalDoc = canMergeDynamicDocumentsOriginal(hotelId);
        Map<String, Object> updatedDoc = canMergeDynamicDocumentsUpdated(hotelId);
        Map<String, Object> expectedDoc = canMergeDynamicDocumentsExpected(hotelId);

        client.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.MERGE_OR_UPLOAD, originalDoc)));
        waitForIndexing();

        client.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.MERGE, updatedDoc)));
        waitForIndexing();

        Map<String, Object> actualDoc = client.getDocument(hotelId).getAdditionalProperties();
        assertObjectEquals(expectedDoc, actualDoc, true);

        client.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.MERGE_OR_UPLOAD, originalDoc)));
        waitForIndexing();

        actualDoc = client.getDocument(hotelId).getAdditionalProperties();
        assertMapEquals(originalDoc, actualDoc, false, "properties");
    }

    @Test
    public void canMergeDynamicDocumentsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Map<String, Object> originalDoc = canMergeDynamicDocumentsOriginal(hotelId);
        Map<String, Object> updatedDoc = canMergeDynamicDocumentsUpdated(hotelId);
        Map<String, Object> expectedDoc = canMergeDynamicDocumentsExpected(hotelId);

        asyncClient
            .indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.MERGE_OR_UPLOAD, originalDoc)))
            .block();
        waitForIndexing();

        asyncClient.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.MERGE, updatedDoc)))
            .block();
        waitForIndexing();

        getAndValidateDocumentAsync(asyncClient, hotelId, expectedDoc, map -> map,
            (expected, actual) -> assertObjectEquals(expected, actual, true));

        asyncClient
            .indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.MERGE_OR_UPLOAD, originalDoc)))
            .block();
        waitForIndexing();

        getAndValidateDocumentAsync(asyncClient, hotelId, originalDoc, map -> map,
            (expected, actual) -> assertObjectEquals(expected, actual, true, "properties"));
    }

    @Test
    public void canIndexAndAccessResponseSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotel1Id = getRandomDocumentKey();
        String hotel2Id = getRandomDocumentKey();
        String hotel3Id = getRandomDocumentKey();
        String hotel4Id = getRandomDocumentKey();

        List<IndexAction> hotelsToUpload = Arrays.asList(
            createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(new Hotel().hotelId(hotel1Id))),
            createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(new Hotel().hotelId(hotel2Id))));

        IndexAction hotelsToMerge = createIndexAction(IndexActionType.MERGE,
            convertToMapStringObject(new Hotel().hotelId(hotel1Id).rating(5)));

        List<IndexAction> hotelsToMergeOrUpload = Arrays.asList(
            createIndexAction(IndexActionType.MERGE_OR_UPLOAD,
                convertToMapStringObject(new Hotel().hotelId(hotel3Id).rating(4))),
            createIndexAction(IndexActionType.MERGE_OR_UPLOAD,
                convertToMapStringObject(new Hotel().hotelId(hotel4Id).rating(1))));

        IndexAction hotelsToDelete
            = createIndexAction(IndexActionType.DELETE, convertToMapStringObject(new Hotel().hotelId(hotel4Id)));

        List<IndexAction> batchActions = new ArrayList<>();
        batchActions.addAll(hotelsToUpload);
        batchActions.addAll(hotelsToMergeOrUpload);
        IndexDocumentsBatch batch = new IndexDocumentsBatch(batchActions);

        validateIndexResponseSync(
            client.indexDocumentsWithResponse(new IndexDocumentsBatch(hotelsToUpload), null, null), 2);
        waitForIndexing();

        validateIndexResponseSync(client.indexDocumentsWithResponse(new IndexDocumentsBatch(hotelsToMerge), null, null),
            1);
        validateIndexResponseSync(
            client.indexDocumentsWithResponse(new IndexDocumentsBatch(hotelsToMergeOrUpload), null, null), 2);
        waitForIndexing();

        validateIndexResponseSync(
            client.indexDocumentsWithResponse(new IndexDocumentsBatch(hotelsToDelete), null, null), 1);
        waitForIndexing();

        validateIndexResponseSync(client.indexDocumentsWithResponse(batch, null, null), 4);
        waitForIndexing();

        assertEquals(4, client.getDocument(hotel3Id).getAdditionalProperties().get("Rating"));
    }

    @Test
    public void canIndexAndAccessResponseAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotel1Id = getRandomDocumentKey();
        String hotel2Id = getRandomDocumentKey();
        String hotel3Id = getRandomDocumentKey();
        String hotel4Id = getRandomDocumentKey();

        List<IndexAction> hotelsToUpload = Arrays.asList(
            createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(new Hotel().hotelId(hotel1Id))),
            createIndexAction(IndexActionType.UPLOAD, convertToMapStringObject(new Hotel().hotelId(hotel2Id))));

        IndexAction hotelsToMerge = createIndexAction(IndexActionType.MERGE,
            convertToMapStringObject(new Hotel().hotelId(hotel1Id).rating(5)));

        List<IndexAction> hotelsToMergeOrUpload = Arrays.asList(
            createIndexAction(IndexActionType.MERGE_OR_UPLOAD,
                convertToMapStringObject(new Hotel().hotelId(hotel3Id).rating(4))),
            createIndexAction(IndexActionType.MERGE_OR_UPLOAD,
                convertToMapStringObject(new Hotel().hotelId(hotel4Id).rating(1))));

        IndexAction hotelsToDelete
            = createIndexAction(IndexActionType.DELETE, convertToMapStringObject(new Hotel().hotelId(hotel4Id)));

        List<IndexAction> batchActions = new ArrayList<>();
        batchActions.addAll(hotelsToUpload);
        batchActions.addAll(hotelsToMergeOrUpload);
        IndexDocumentsBatch batch = new IndexDocumentsBatch(batchActions);

        validateIndexResponseAsync(
            asyncClient.indexDocumentsWithResponse(new IndexDocumentsBatch(hotelsToUpload), null, null), 2);

        waitForIndexing();

        validateIndexResponseAsync(
            asyncClient.indexDocumentsWithResponse(new IndexDocumentsBatch(hotelsToMerge), null, null), 1);
        validateIndexResponseAsync(
            asyncClient.indexDocumentsWithResponse(new IndexDocumentsBatch(hotelsToMergeOrUpload), null, null), 2);

        waitForIndexing();

        validateIndexResponseAsync(
            asyncClient.indexDocumentsWithResponse(new IndexDocumentsBatch(hotelsToDelete), null, null), 1);

        waitForIndexing();

        validateIndexResponseAsync(asyncClient.indexDocumentsWithResponse(batch, null, null), 4);

        waitForIndexing();

        getAndValidateDocumentAsync(asyncClient, hotel3Id, null, map -> map,
            (ignored, actual) -> assertEquals(4, actual.get("Rating")));
    }

    private static <T extends JsonSerializable<T>> T getAndConvertDocument(SearchClient client, String key,
        ReadValueCallback<JsonReader, T> converter) {
        return convertFromMapStringObject(client.getDocument(key).getAdditionalProperties(), converter);
    }

    private static <T> void getAndValidateDocumentAsync(SearchAsyncClient asyncClient, String key, T expected,
        ReadValueCallback<Map<String, Object>, T> converter, BiConsumer<T, T> comparator) {
        StepVerifier.create(asyncClient.getDocument(key))
            .assertNext(actual -> comparator.accept(expected,
                assertDoesNotThrow(() -> converter.read(actual.getAdditionalProperties()))))
            .verifyComplete();
    }

    private static void validateIndexResponseSync(Response<IndexDocumentsResult> response, int resultCount) {
        assertEquals(200, response.getStatusCode());
        assertEquals(resultCount, response.getValue().getResults().size());
    }

    private static void validateIndexResponseAsync(Mono<Response<IndexDocumentsResult>> indexDocumentsWithResponse,
        int resultCount) {
        StepVerifier.create(indexDocumentsWithResponse).assertNext(response -> {
            assertEquals(200, response.getStatusCode());
            assertEquals(resultCount, response.getValue().getResults().size());
        }).verifyComplete();
    }

    @SuppressWarnings({ "UseOfObsoleteDateTimeApi" })
    static Hotel prepareStaticallyTypedHotel(String hotelId) {
        return new Hotel().hotelId(hotelId)
            .hotelName("Fancy Stay")
            .description("Best hotel in town if you like luxury hotels. They have an amazing infinity pool, a spa, and "
                + "a really helpful concierge. The location is perfect -- right downtown, close to all the tourist "
                + "attractions. We highly recommend this hotel.")
            .descriptionFr("Meilleur hôtel en ville si vous aimez les hôtels de luxe. Ils ont une magnifique piscine "
                + "à débordement, un spa et un concierge très utile. L'emplacement est parfait – en plein centre, à "
                + "proximité de toutes les attractions touristiques. Nous recommandons fortement cet hôtel.")
            .category("Luxury")
            .tags(Arrays.asList("pool", "view", "wifi", "concierge"))
            .parkingIncluded(false)
            .smokingAllowed(false)
            .lastRenovationDate(parseDate("2010-06-27T00:00:00Z"))
            .rating(5)
            .location(new GeoPoint(-122.131577, 47.678581))
            .address(new HotelAddress().streetAddress("1 Microsoft Way")
                .city("Redmond")
                .stateProvince("Washington")
                .postalCode("98052")
                .country("United States"));
    }

    Map<String, Object> prepareDynamicallyTypedHotel(String hotelId) {

        Map<String, Object> room1 = new LinkedHashMap<>();
        room1.put("Description", "Budget Room, 1 Queen Bed");
        room1.put("Description_fr", null);
        room1.put("Type", "Budget Room");
        room1.put("BaseRate", 149.99);
        room1.put("BedOptions", "1 Queen Bed");
        room1.put("SleepsCount", 2);
        room1.put("SmokingAllowed", true);
        room1.put("Tags", Arrays.asList("vcr/dvd", "great view"));

        Map<String, Object> room2 = new LinkedHashMap<>();
        room2.put("Description", "Budget Room, 1 King Bed");
        room2.put("Description_fr", null);
        room2.put("Type", "Budget Room");
        room2.put("BaseRate", 249.99);
        room2.put("BedOptions", "1 King Bed");
        room2.put("SleepsCount", 2);
        room2.put("SmokingAllowed", true);
        room2.put("Tags", Arrays.asList("vcr/dvd", "seaside view"));

        List<Map<String, Object>> rooms = Arrays.asList(room1, room2);

        Map<String, Object> address = new LinkedHashMap<>();
        address.put("StreetAddress", "One Microsoft way");
        address.put("City", "Redmond");
        address.put("StateProvince", "Washington");
        address.put("PostalCode", "98052");
        address.put("Country", "US");

        // TODO (alzimmer): Determine if this should be used to create the hotel document.
        Map<String, Object> location = new LinkedHashMap<>();
        location.put("type", "Point");
        location.put("coordinates", Arrays.asList(-122.131577, 47.678581));
        location.put("crs", null);

        Map<String, Object> hotel = new LinkedHashMap<>();
        hotel.put("HotelId", hotelId);
        hotel.put("HotelName", "Fancy Stay Hotel");
        hotel.put("Description",
            "Best hotel in town if you like luxury hotels. They have an amazing infinity pool, a "
                + "spa, and a really helpful concierge. The location is perfect -- right downtown, close to all the "
                + "tourist attractions. We highly recommend this hotel.");
        hotel.put("Description_fr", null);
        hotel.put("Address", address);
        hotel.put("Location", location);
        hotel.put("Category", "Luxury");
        hotel.put("Tags", Arrays.asList("pool", "view", "wifi", "concierge"));
        hotel.put("LastRenovationDate", OffsetDateTime.parse("2019-01-30T00:00:00Z"));
        hotel.put("ParkingIncluded", true);
        hotel.put("SmokingAllowed", true);
        hotel.put("Rating", 5);
        hotel.put("Rooms", rooms);

        return hotel;
    }

    void assertSuccessfulIndexResult(IndexingResult result, String key, int statusCode) {
        assertEquals(result.getKey(), key);
        assertEquals(result.getStatusCode(), statusCode);
        assertTrue(result.isSucceeded());
    }

    static void assertFailedIndexResult(IndexingResult result, String key, int statusCode) {
        assertEquals(result.getKey(), key);
        assertEquals(result.getStatusCode(), statusCode);
        assertEquals("Document not found.", result.getErrorMessage());
        assertFalse(result.isSucceeded());
    }

    static void assertIndexActionSucceeded(String key, IndexingResult result, int expectedStatusCode) {
        assertEquals(key, result.getKey());
        assertTrue(result.isSucceeded());
        assertNull(result.getErrorMessage());
        assertEquals(expectedStatusCode, result.getStatusCode());
    }

    @SuppressWarnings({ "UseOfObsoleteDateTimeApi", "deprecation" })
    List<Hotel> getBoundaryValues() {
        Date maxEpoch = Date.from(Instant.ofEpochMilli(253402300799000L));
        Date minEpoch = Date.from(Instant.ofEpochMilli(-2208988800000L));
        return Arrays.asList(
            // Minimum values
            new Hotel().hotelId(getRandomDocumentKey())
                .category("")
                .lastRenovationDate(new Date(minEpoch.getYear(), minEpoch.getMonth(), minEpoch.getDate(),
                    minEpoch.getHours(), minEpoch.getMinutes(), minEpoch.getSeconds()))
                .location(new GeoPoint(-180.0, -90.0))   // South pole, date line from the west
                .parkingIncluded(false)
                .rating(Integer.MIN_VALUE)
                .tags(Collections.emptyList())
                .address(new HotelAddress())
                .rooms(Collections.singletonList(new HotelRoom().baseRate(Double.MIN_VALUE))),
            // Maximum values
            new Hotel().hotelId(getRandomDocumentKey())
                .category("test")   // No meaningful string max since there is no length limit (other than payload size or term length).
                .lastRenovationDate(new Date(maxEpoch.getYear(), maxEpoch.getMonth(), maxEpoch.getDate(),
                    maxEpoch.getHours(), maxEpoch.getMinutes(), maxEpoch.getSeconds()))
                .location(new GeoPoint(180.0, 90.0))     // North pole, date line from the east
                .parkingIncluded(true)
                .rating(Integer.MAX_VALUE)
                .tags(Collections.singletonList("test"))    // No meaningful string max; see above.
                .address(new HotelAddress().city("Maximum"))
                .rooms(Collections.singletonList(new HotelRoom().baseRate(Double.MAX_VALUE))),
            // Other boundary values #1
            new Hotel().hotelId(getRandomDocumentKey())
                .category(null)
                .lastRenovationDate(null)
                .location(new GeoPoint(0.0, 0.0))     // Equator, meridian
                .parkingIncluded(null)
                .rating(null)
                .tags(Collections.emptyList())
                .address(new HotelAddress().city("Maximum"))
                .rooms(Collections.singletonList(new HotelRoom().baseRate(Double.NEGATIVE_INFINITY))),
            // Other boundary values #2
            new Hotel().hotelId(getRandomDocumentKey())
                .location(null)
                .tags(Collections.emptyList())
                .rooms(Collections.singletonList(new HotelRoom().baseRate(Double.POSITIVE_INFINITY))),
            // Other boundary values #3
            new Hotel().hotelId(getRandomDocumentKey())
                .tags(Collections.emptyList())
                .rooms(Collections.singletonList(new HotelRoom().baseRate(Double.NaN))),
            // Other boundary values #4
            new Hotel().hotelId(getRandomDocumentKey())
                .category(null)
                .tags(Collections.emptyList())
                .rooms(Collections.emptyList()));
    }

    @SuppressWarnings({ "UseOfObsoleteDateTimeApi" })
    private static Hotel canMergeStaticallyTypedDocumentsOriginal(String key) {
        // Define hotels
        return new Hotel().hotelId(key)
            .hotelName("Secret Point Motel")
            .description("The hotel is ideally located on the main commercial artery of the city in the heart of New "
                + "York. A few minutes away is Time's Square and the historic centre of the city, as well as other "
                + "places of interest that make New York one of America's most attractive and cosmopolitan cities.")
            .descriptionFr("L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein "
                + "cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la "
                + "ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus "
                + "attractives et cosmopolites de l'Amérique.")
            .category("Boutique")
            .tags(Arrays.asList("pool", "air conditioning", "concierge"))
            .parkingIncluded(false)
            .smokingAllowed(true)
            .lastRenovationDate(parseDate("2010-06-27T00:00:00Z"))
            .rating(4)
            .location(new GeoPoint(-73.975403, 40.760586))
            .address(new HotelAddress().streetAddress("677 5th Ave")
                .city("New York")
                .stateProvince("NY")
                .country("USA")
                .postalCode("10022"))
            .rooms(Arrays.asList(
                new HotelRoom().description("Budget Room, 1 Queen Bed (Cityside)")
                    .descriptionFr("Chambre Économique, 1 grand lit (côté ville)")
                    .type("Budget Room")
                    .baseRate(9.69)
                    .bedOptions("1 Queen Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(new String[] { "vcr/dvd" }),
                new HotelRoom().description("Budget Room, 1 King Bed (Mountain View)")
                    .descriptionFr("Chambre Économique, 1 très grand lit (Mountain View)")
                    .type("Budget Room")
                    .baseRate(8.09)
                    .bedOptions("1 King Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(new String[] { "vcr/dvd", "jacuzzi tub" })));
    }

    private static Hotel canMergeStaticallyTypedDocumentsUpdated(String key) {
        // Update category, tags, parking included, rating, and rooms. Erase description, last renovation date,
        // location and address.
        return new Hotel().hotelId(key)
            .hotelName("Secret Point Motel")
            .description(null)
            .category("Economy")
            .tags(Arrays.asList("pool", "air conditioning"))
            .parkingIncluded(true)
            .lastRenovationDate(null)
            .rating(3)
            //.location(null)
            .address(new HotelAddress())
            .rooms(Collections.singletonList(new HotelRoom().description(null)
                .type("Budget Room")
                .baseRate(10.5)
                .bedOptions("1 Queen Bed")
                .sleepsCount(2)
                .tags(new String[] { "vcr/dvd", "balcony" })));
    }

    @SuppressWarnings({ "UseOfObsoleteDateTimeApi" })
    private static Hotel canMergeStaticallyTypedDocumentsExpected(String key) {
        // Fields whose values get updated are updated, and whose values get erased remain the same.
        return new Hotel().hotelId(key)
            .hotelName("Secret Point Motel")
            .description("The hotel is ideally located on the main commercial artery of the city in the heart of New "
                + "York. A few minutes away is Time's Square and the historic centre of the city, as well as other "
                + "places of interest that make New York one of America's most attractive and cosmopolitan cities.")
            .descriptionFr("L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein "
                + "cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la "
                + "ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus "
                + "attractives et cosmopolites de l'Amérique.")
            .category("Economy")
            .tags(Arrays.asList("pool", "air conditioning"))
            .parkingIncluded(true)
            .smokingAllowed(true)
            .lastRenovationDate(parseDate("2010-06-27T00:00:00Z"))
            .rating(3)
            .location(new GeoPoint(-73.975403, 40.760586))
            .address(new HotelAddress().streetAddress("677 5th Ave")
                .city("New York")
                .stateProvince("NY")
                .country("USA")
                .postalCode("10022"))
            .rooms(Collections.singletonList(new HotelRoom().description(null)
                .type("Budget Room")
                .baseRate(10.5)
                .bedOptions("1 Queen Bed")
                .sleepsCount(2)
                .tags(new String[] { "vcr/dvd", "balcony" })));
    }

    @SuppressWarnings({ "UseOfObsoleteDateTimeApi" })
    private static LoudHotel canSetExplicitNullsInStaticallyTypedDocumentOriginal(String key) {
        return new LoudHotel().HOTELID(key)
            .HOTELNAME("Secret Point Motel")
            .DESCRIPTION("The hotel is ideally located on the main commercial artery of the city in the heart of New "
                + "York. A few minutes away is Time's Square and the historic centre of the city, as well as other "
                + "places of interest that make New York one of America's most attractive and cosmopolitan cities.")
            .DESCRIPTIONFRENCH("L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein "
                + "cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la "
                + "ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus "
                + "attractives et cosmopolites de l'Amérique.")
            .CATEGORY("Boutique")
            .TAGS(Arrays.asList("pool", "air conditioning", "concierge"))
            .PARKINGINCLUDED(false)
            .SMOKINGALLOWED(false)
            .LASTRENOVATIONDATE(parseDate("1970-01-18T05:00:00Z"))
            .RATING(4)
            .LOCATION(new GeoPoint(-73.975403, 40.760586))
            .ADDRESS(new HotelAddress().streetAddress("677 5th Ave")
                .city("New York")
                .stateProvince("NY")
                .country("USA")
                .postalCode("10022"))
            .ROOMS(Arrays.asList(
                new HotelRoom().description("Budget Room, 1 Queen Bed (Cityside)")
                    .descriptionFr("Chambre Économique, 1 grand lit (côté ville)")
                    .type("Budget Room")
                    .baseRate(9.69)
                    .bedOptions("1 Queen Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(new String[] { "vcr/dvd" }),
                new HotelRoom().description("Budget Room, 1 King Bed (Mountain View)")
                    .descriptionFr("Chambre Économique, 1 très grand lit (Mountain View)")
                    .type("Budget Room")
                    .baseRate(8.09)
                    .bedOptions("1 King Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(new String[] { "vcr/dvd", "jacuzzi tub" })));
    }

    @SuppressWarnings({ "UseOfObsoleteDateTimeApi" })
    private static LoudHotel canSetExplicitNullsInStaticallyTypedDocumentUpdated(String key) {
        return new LoudHotel().HOTELID(key)
            .DESCRIPTION(null)  // This property has JsonInclude.Include.ALWAYS, so this will null out the field.
            .CATEGORY(null)     // This property doesn't have JsonInclude.Include.ALWAYS, so this should have no effect.
            .TAGS(Arrays.asList("pool", "air conditioning"))
            .PARKINGINCLUDED(true)
            .LASTRENOVATIONDATE(parseDate("1970-01-18T05:00:00Z"))
            .RATING(3)
            //.LOCATION(null)     // This property has JsonInclude.Include.ALWAYS, so this will null out the field.
            .ADDRESS(new HotelAddress())
            .ROOMS(Collections.singletonList(new HotelRoom().description(null)
                .type("Budget Room")
                .baseRate(10.5)
                .smokingAllowed(false)
                .tags(new String[] { "vcr/dvd", "balcony" })));
    }

    @SuppressWarnings({ "UseOfObsoleteDateTimeApi" })
    private static LoudHotel canSetExplicitNullsInStaticallyTypedDocumentExpected(String key) {
        return new LoudHotel().HOTELID(key)
            .HOTELNAME("Secret Point Motel")
            .DESCRIPTION(null)
            .DESCRIPTIONFRENCH("L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein "
                + "cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la "
                + "ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus "
                + "attractives et cosmopolites de l'Amérique.")
            .CATEGORY("Boutique")
            .TAGS(Arrays.asList("pool", "air conditioning"))
            .PARKINGINCLUDED(true)
            .SMOKINGALLOWED(false)
            .LASTRENOVATIONDATE(parseDate("1970-01-18T05:00:00Z"))
            .RATING(3)
            //.LOCATION(null)
            .ADDRESS(new HotelAddress().streetAddress("677 5th Ave")
                .city("New York")
                .stateProvince("NY")
                .country("USA")
                .postalCode("10022"))
            .ROOMS(Collections.singletonList(
                // Regardless of NullValueHandling, this should look like the merged doc with unspecified fields as null
                // because we don't support partial updates for complex collections.
                new HotelRoom().description(null)
                    .descriptionFr(null)
                    .type("Budget Room")
                    .baseRate(10.5)
                    .bedOptions(null)
                    .sleepsCount(null)
                    .smokingAllowed(false)
                    .tags(new String[] { "vcr/dvd", "balcony" })));
    }

    private static Map<String, Object> canMergeDynamicDocumentsOriginal(String key) {
        Map<String, Object> originalDoc = new LinkedHashMap<>();
        originalDoc.put("HotelId", key);
        originalDoc.put("HotelName", "Secret Point Motel");
        originalDoc.put("Description", "The hotel is ideally located on the main commercial artery of the city in the "
            + "heart of New York. A few minutes away is Time's Square and the historic centre of the city, as well as "
            + "other places of interest that make New York one of America's most attractive and cosmopolitan cities.");
        originalDoc.put("Description_fr", "L'hôtel est idéalement situé sur la principale artère commerciale de la "
            + "ville en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique "
            + "de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus "
            + "attractives et cosmopolites de l'Amérique.");
        originalDoc.put("Category", "Boutique");
        originalDoc.put("Tags", Arrays.asList("pool", "air conditioning", "concierge"));
        originalDoc.put("ParkingIncluded", false);
        originalDoc.put("SmokingAllowed", true);
        originalDoc.put("LastRenovationDate", OffsetDateTime.parse("2010-06-27T00:00:00Z"));
        originalDoc.put("Rating", 4);
        originalDoc.put("Location", new GeoPoint(-73.965403, 40.760586));

        Map<String, Object> originalAddress = new LinkedHashMap<>();
        originalAddress.put("StreetAddress", "677 5th Ave");
        originalAddress.put("City", "New York");
        originalAddress.put("StateProvince", "NY");
        originalAddress.put("PostalCode", "10022");
        originalAddress.put("Country", "USA");
        originalDoc.put("Address", originalAddress);

        Map<String, Object> originalRoom1 = new LinkedHashMap<>();
        originalRoom1.put("Description", "Budget Room, 1 Queen Bed (Cityside)");
        originalRoom1.put("Description_fr", "Chambre Économique, 1 grand lit (côté ville)");
        originalRoom1.put("Type", "Budget Room");
        originalRoom1.put("BaseRate", 9.69);
        originalRoom1.put("BedOptions", "1 Queen Bed");
        originalRoom1.put("SleepsCount", 2);
        originalRoom1.put("SmokingAllowed", true);
        originalRoom1.put("Tags", Collections.singletonList("vcr/dvd"));

        Map<String, Object> originalRoom2 = new LinkedHashMap<>();
        originalRoom2.put("Description", "Budget Room, 1 King Bed (Mountain View)");
        originalRoom2.put("Description_fr", "Chambre Économique, 1 très grand lit (Mountain View)");
        originalRoom2.put("Type", "Budget Room");
        originalRoom2.put("BaseRate", 8.09);
        originalRoom2.put("BedOptions", "1 King Bed");
        originalRoom2.put("SleepsCount", 2);
        originalRoom2.put("SmokingAllowed", true);
        originalRoom2.put("Tags", Arrays.asList("vcr/dvd", "jacuzzi tub"));

        originalDoc.put("Rooms", Arrays.asList(originalRoom1, originalRoom2));

        return originalDoc;
    }

    private static Map<String, Object> canMergeDynamicDocumentsUpdated(String key) {
        Map<String, Object> updatedDoc = new LinkedHashMap<>();
        updatedDoc.put("HotelId", key);
        updatedDoc.put("Description", null);
        updatedDoc.put("Category", "Economy");
        updatedDoc.put("Tags", Arrays.asList("pool", "air conditioning"));
        updatedDoc.put("ParkingIncluded", true);
        updatedDoc.put("LastRenovationDate", null);
        updatedDoc.put("Rating", 3);
        updatedDoc.put("Location", null);
        updatedDoc.put("Address", new LinkedHashMap<>());

        Map<String, Object> updatedRoom1 = new LinkedHashMap<>();
        updatedRoom1.put("Description", null);
        updatedRoom1.put("Type", "Budget Room");
        updatedRoom1.put("BaseRate", 10.5);
        updatedRoom1.put("BedOptions", "1 Queen Bed");
        updatedRoom1.put("SleepsCount", 2);
        updatedRoom1.put("SmokingAllowed", true);
        updatedRoom1.put("Tags", Arrays.asList("vcr/dvd", "balcony"));
        updatedDoc.put("Rooms", Collections.singletonList(updatedRoom1));

        return updatedDoc;
    }

    private static Map<String, Object> canMergeDynamicDocumentsExpected(String key) {
        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("HotelId", key);
        expectedDoc.put("HotelName", "Secret Point Motel");
        expectedDoc.put("Description", null);
        expectedDoc.put("Description_fr", "L'hôtel est idéalement situé sur la principale artère commerciale de la "
            + "ville en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique "
            + "de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus "
            + "attractives et cosmopolites de l'Amérique.");
        expectedDoc.put("Category", "Economy");
        expectedDoc.put("Tags", Arrays.asList("pool", "air conditioning"));
        expectedDoc.put("ParkingIncluded", true);
        expectedDoc.put("SmokingAllowed", true);
        expectedDoc.put("LastRenovationDate", null);
        expectedDoc.put("Rating", 3);
        expectedDoc.put("Location", null);

        LinkedHashMap<String, Object> expectedAddress = new LinkedHashMap<>();
        expectedAddress.put("StreetAddress", "677 5th Ave");
        expectedAddress.put("City", "New York");
        expectedAddress.put("StateProvince", "NY");
        expectedAddress.put("PostalCode", "10022");
        expectedAddress.put("Country", "USA");
        expectedDoc.put("Address", expectedAddress);

        // This should look like the merged doc with unspecified fields as null because we don't support
        // partial updates for complex collections.
        LinkedHashMap<String, Object> expectedRoom = new LinkedHashMap<>();
        expectedRoom.put("Description", null);
        expectedRoom.put("Description_fr", null);
        expectedRoom.put("Type", "Budget Room");
        expectedRoom.put("BaseRate", 10.5);
        expectedRoom.put("BedOptions", "1 Queen Bed");
        expectedRoom.put("SleepsCount", 2);
        expectedRoom.put("SmokingAllowed", true);
        expectedRoom.put("Tags", Arrays.asList("vcr/dvd", "balcony"));

        List<LinkedHashMap<String, Object>> expectedRooms = new ArrayList<>();
        expectedRooms.add(expectedRoom);
        expectedDoc.put("Rooms", expectedRooms);

        return expectedDoc;
    }
}
