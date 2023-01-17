// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.http.rest.Response;
import com.azure.core.models.GeoPoint;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SearchIndexAsyncClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.models.IndexBatchException;
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
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.azure.search.documents.TestHelpers.ISO8601_FORMAT;
import static com.azure.search.documents.TestHelpers.assertHttpResponseException;
import static com.azure.search.documents.TestHelpers.assertMapEquals;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.verifyHttpResponseError;
import static com.azure.search.documents.TestHelpers.waitForIndexing;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexingTests extends SearchTestBase {
    private static final String BOOKS_INDEX_JSON = "BooksIndexData.json";

    private final List<String> indexesToDelete = new ArrayList<>();

    @Override
    protected void afterTest() {
        super.afterTest();

        SearchIndexClient serviceClient = getSearchIndexClientBuilder().buildClient();
        for (String index : indexesToDelete) {
            serviceClient.deleteIndex(index);
        }
    }

    private SearchClient setupClient(Supplier<String> indexSupplier) {
        String indexName = indexSupplier.get();
        indexesToDelete.add(indexName);

        return getSearchClientBuilder(indexName).buildClient();
    }

    private SearchAsyncClient setupAsyncClient(Supplier<String> indexSupplier) {
        String indexName = indexSupplier.get();
        indexesToDelete.add(indexName);

        return getSearchClientBuilder(indexName).buildAsyncClient();
    }

    @Test
    public void countingDocsOfNewIndexGivesZeroSync() {
        SearchClient client = setupClient(this::createHotelIndex);

        assertEquals(0L, client.getDocumentCount());
    }

    @Test
    public void countingDocsOfNewIndexGivesZeroAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(this::createHotelIndex);

        validateDocumentCountAsync(0, asyncClient);
    }

    @Test
    public void indexDoesNotThrowWhenAllActionsSucceedSync() {
        SearchClient client = setupClient(this::createHotelIndex);

        String expectedHotelId = "1";
        List<Hotel> hotels = Collections.singletonList(new Hotel().hotelId(expectedHotelId));

        List<IndexingResult> result = client.uploadDocuments(hotels).getResults();
        assertIndexActionSucceeded(expectedHotelId, result.get(0), 201);

        waitForIndexing();
        assertEquals(1L, client.getDocumentCount());
    }

    @Test
    public void indexDoesNotThrowWhenAllActionsSucceedAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(this::createHotelIndex);

        String expectedHotelId = "1";
        List<Hotel> hotels = Collections.singletonList(new Hotel().hotelId(expectedHotelId));

        StepVerifier.create(asyncClient.uploadDocuments(hotels))
            .assertNext(result -> assertIndexActionSucceeded(expectedHotelId, result.getResults().get(0), 201))
            .verifyComplete();

        waitForIndexing();

        validateDocumentCountAsync(1, asyncClient);
    }

    @Test
    public void canIndexWithPascalCaseFieldsSync() {
        SearchClient client = setupClient(() -> setupIndexFromJsonFile(BOOKS_INDEX_JSON));

        List<Book> books = new ArrayList<>();
        books.add(new Book()
            .ISBN("132")
            .title("Lord of the Rings")
            .author(new Author()
                .firstName("J.R.R")
                .lastName("Tolkien"))
        );

        List<IndexingResult> result = client.uploadDocuments(books).getResults();
        assertIndexActionSucceeded("132", result.get(0), 201);

        waitForIndexing();
        assertEquals(1L, client.getDocumentCount());
    }

    @Test
    public void canIndexWithPascalCaseFieldsAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(() -> setupIndexFromJsonFile(BOOKS_INDEX_JSON));

        List<Book> books = new ArrayList<>();
        books.add(new Book()
            .ISBN("132")
            .title("Lord of the Rings")
            .author(new Author()
                .firstName("J.R.R")
                .lastName("Tolkien")));

        StepVerifier.create(asyncClient.uploadDocuments(books))
            .assertNext(result -> assertIndexActionSucceeded("132", result.getResults().get(0), 201))
            .verifyComplete();

        waitForIndexing();

        validateDocumentCountAsync(1, asyncClient);
    }

    @Test
    public void canDeleteBatchByKeysSync() {
        SearchClient client = setupClient(this::createHotelIndex);

        client.uploadDocuments(Arrays.asList(new Hotel().hotelId("1"), new Hotel().hotelId("2")));
        waitForIndexing();
        assertEquals(2, client.getDocumentCount());

        IndexDocumentsBatch<Hotel> deleteBatch = new IndexDocumentsBatch<Hotel>()
            .addDeleteActions("HotelId", Arrays.asList("1", "2"));

        IndexDocumentsResult documentIndexResult = client.indexDocuments(deleteBatch);
        waitForIndexing();

        assertEquals(2, documentIndexResult.getResults().size());
        assertIndexActionSucceeded("1", documentIndexResult.getResults().get(0), 200);
        assertIndexActionSucceeded("2", documentIndexResult.getResults().get(1), 200);

        assertEquals(0, client.getDocumentCount());
    }

    @Test
    public void canDeleteBatchByKeysAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(this::createHotelIndex);

        asyncClient.uploadDocuments(Arrays.asList(new Hotel().hotelId("1"), new Hotel().hotelId("2"))).block();
        waitForIndexing();

        validateDocumentCountAsync(2, asyncClient);

        IndexDocumentsBatch<Hotel> deleteBatch = new IndexDocumentsBatch<Hotel>()
            .addDeleteActions("HotelId", Arrays.asList("1", "2"));

        StepVerifier.create(asyncClient.indexDocuments(deleteBatch))
            .assertNext(result -> {
                assertEquals(2, result.getResults().size());
                assertIndexActionSucceeded("1", result.getResults().get(0), 200);
                assertIndexActionSucceeded("2", result.getResults().get(1), 200);
            })
            .verifyComplete();

        waitForIndexing();

        validateDocumentCountAsync(0, asyncClient);
    }

    @Test
    public void indexDoesNotThrowWhenDeletingDocumentWithExtraFieldsSync() {
        SearchClient client = setupClient(this::createHotelIndex);

        Hotel hotel = new Hotel()
            .hotelId("1")
            .category("Luxury");
        List<Hotel> hotels = Collections.singletonList(hotel);

        client.uploadDocuments(hotels);
        waitForIndexing();
        assertEquals(1, client.getDocumentCount());

        hotel.category("ignored");
        IndexDocumentsResult documentIndexResult = client.deleteDocuments(hotels);
        waitForIndexing();

        assertEquals(1, documentIndexResult.getResults().size());
        assertIndexActionSucceeded("1", documentIndexResult.getResults().get(0), 200);
        assertEquals(0, client.getDocumentCount());
    }

    @Test
    public void indexDoesNotThrowWhenDeletingDocumentWithExtraFieldsAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(this::createHotelIndex);

        Hotel hotel = new Hotel()
            .hotelId("1")
            .category("Luxury");
        List<Hotel> hotels = Collections.singletonList(hotel);

        asyncClient.uploadDocuments(hotels).block();
        waitForIndexing();

        validateDocumentCountAsync(1, asyncClient);

        hotel.category("ignored");

        StepVerifier.create(asyncClient.deleteDocuments(hotels))
            .assertNext(result -> {
                assertEquals(1, result.getResults().size());
                assertIndexActionSucceeded("1", result.getResults().get(0), 200);
            })
            .verifyComplete();
        waitForIndexing();

        validateDocumentCountAsync(0, asyncClient);
    }

    @Test
    public void indexDoesNotThrowWhenDeletingDynamicDocumentWithExtraFieldsSync() {
        SearchClient client = setupClient(this::createHotelIndex);

        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("HotelId", "1");
        searchDocument.put("Category", "Luxury");
        List<SearchDocument> docs = Collections.singletonList(searchDocument);

        client.uploadDocuments(docs);

        waitForIndexing();
        assertEquals(1, client.getDocumentCount());

        searchDocument.put("Category", "ignored");
        IndexDocumentsResult documentIndexResult = client.deleteDocuments(docs);
        waitForIndexing();

        assertEquals(1, documentIndexResult.getResults().size());
        assertIndexActionSucceeded("1", documentIndexResult.getResults().get(0), 200);
        assertEquals(0, client.getDocumentCount());
    }

    @Test
    public void indexDoesNotThrowWhenDeletingDynamicDocumentWithExtraFieldsAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(this::createHotelIndex);

        SearchDocument searchDocument = new SearchDocument();
        searchDocument.put("HotelId", "1");
        searchDocument.put("Category", "Luxury");
        List<SearchDocument> docs = Collections.singletonList(searchDocument);

        asyncClient.uploadDocuments(docs).block();
        waitForIndexing();

        validateDocumentCountAsync(1, asyncClient);

        searchDocument.put("Category", "ignored");

        StepVerifier.create(asyncClient.deleteDocuments(docs))
            .assertNext(result -> {
                assertEquals(1, result.getResults().size());
                assertIndexActionSucceeded("1", result.getResults().get(0), 200);
            })
            .verifyComplete();
        waitForIndexing();

        validateDocumentCountAsync(0, asyncClient);
    }

    @Test
    public void canIndexStaticallyTypedDocumentsSync() {
        SearchClient client = setupClient(this::createHotelIndex);

        Hotel hotel1 = prepareStaticallyTypedHotel("1");
        Hotel hotel2 = prepareStaticallyTypedHotel("2");
        Hotel hotel3 = prepareStaticallyTypedHotel("3");
        Hotel nonExistingHotel = prepareStaticallyTypedHotel("nonExistingHotel"); // merging with a non-existing document
        Hotel randomHotel = prepareStaticallyTypedHotel("randomId"); // deleting a non existing document

        IndexDocumentsBatch<Hotel> batch = new IndexDocumentsBatch<Hotel>()
            .addUploadActions(Collections.singletonList(hotel1))
            .addDeleteActions(Collections.singletonList(randomHotel))
            .addMergeActions(Collections.singletonList(nonExistingHotel))
            .addMergeOrUploadActions(Collections.singletonList(hotel3))
            .addUploadActions(Collections.singletonList(hotel2));

        IndexBatchException ex = assertThrows(IndexBatchException.class, () -> client.indexDocumentsWithResponse(batch,
            new IndexDocumentsOptions().setThrowOnAnyError(true), Context.NONE));

        List<IndexingResult> results = ex.getIndexingResults();
        assertEquals(results.size(), batch.getActions().size());

        assertSuccessfulIndexResult(results.get(0), "1", 201);
        assertSuccessfulIndexResult(results.get(1), "randomId", 200);
        assertFailedIndexResult(results.get(2), "nonExistingHotel", 404);
        assertSuccessfulIndexResult(results.get(3), "3", 201);
        assertSuccessfulIndexResult(results.get(4), "2", 201);

        for (Hotel hotel : Arrays.asList(hotel1, hotel2, hotel3)) {
            Hotel actual = client.getDocument(hotel.hotelId(), Hotel.class);
            assertObjectEquals(hotel, actual, true);
        }
    }

    @Test
    public void canIndexStaticallyTypedDocumentsAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(this::createHotelIndex);

        Hotel hotel1 = prepareStaticallyTypedHotel("1");
        Hotel hotel2 = prepareStaticallyTypedHotel("2");
        Hotel hotel3 = prepareStaticallyTypedHotel("3");
        Hotel nonExistingHotel = prepareStaticallyTypedHotel("nonExistingHotel"); // merging with a non-existing document
        Hotel randomHotel = prepareStaticallyTypedHotel("randomId"); // deleting a non existing document

        IndexDocumentsBatch<Hotel> batch = new IndexDocumentsBatch<Hotel>()
            .addUploadActions(Collections.singletonList(hotel1))
            .addDeleteActions(Collections.singletonList(randomHotel))
            .addMergeActions(Collections.singletonList(nonExistingHotel))
            .addMergeOrUploadActions(Collections.singletonList(hotel3))
            .addUploadActions(Collections.singletonList(hotel2));

        StepVerifier.create(asyncClient.indexDocumentsWithResponse(batch,
                new IndexDocumentsOptions().setThrowOnAnyError(true)))
            .verifyErrorSatisfies(throwable -> {
                IndexBatchException ex = assertInstanceOf(IndexBatchException.class, throwable);

                List<IndexingResult> results = ex.getIndexingResults();
                assertEquals(results.size(), batch.getActions().size());

                assertSuccessfulIndexResult(results.get(0), "1", 201);
                assertSuccessfulIndexResult(results.get(1), "randomId", 200);
                assertFailedIndexResult(results.get(2), "nonExistingHotel", 404);
                assertSuccessfulIndexResult(results.get(3), "3", 201);
                assertSuccessfulIndexResult(results.get(4), "2", 201);
            });

        for (Hotel hotel : Arrays.asList(hotel1, hotel2, hotel3)) {
            getAndValidateDocumentAsync(asyncClient, hotel.hotelId(), Hotel.class, hotel,
                (expected, actual) -> assertObjectEquals(expected, actual, true));
        }
    }


    @Test
    public void canIndexDynamicDocumentsNotThrowSync() {
        SearchClient client = setupClient(this::createHotelIndex);

        SearchDocument hotel1 = prepareDynamicallyTypedHotel("1");
        SearchDocument hotel2 = prepareDynamicallyTypedHotel("2");
        SearchDocument hotel3 = prepareDynamicallyTypedHotel("3");
        SearchDocument nonExistingHotel = prepareDynamicallyTypedHotel("nonExistingHotel"); // deleting a non existing document
        SearchDocument randomHotel = prepareDynamicallyTypedHotel("randomId"); // deleting a non existing document

        IndexDocumentsBatch<SearchDocument> batch = new IndexDocumentsBatch<SearchDocument>()
            .addUploadActions(Collections.singletonList(hotel1))
            .addDeleteActions(Collections.singletonList(randomHotel))
            .addMergeActions(Collections.singletonList(nonExistingHotel))
            .addMergeOrUploadActions(Collections.singletonList(hotel3))
            .addUploadActions(Collections.singletonList(hotel2));

        Response<IndexDocumentsResult> resultResponse = client.indexDocumentsWithResponse(batch,
            new IndexDocumentsOptions().setThrowOnAnyError(false), Context.NONE);
        List<IndexingResult> results = resultResponse.getValue().getResults();
        assertEquals(resultResponse.getStatusCode(), 207);
        assertSuccessfulIndexResult(results.get(0), "1", 201);
        assertSuccessfulIndexResult(results.get(1), "randomId", 200);
        assertFailedIndexResult(results.get(2), "nonExistingHotel", 404);
        assertSuccessfulIndexResult(results.get(3), "3", 201);
        assertSuccessfulIndexResult(results.get(4), "2", 201);

        for (SearchDocument hotel : Arrays.asList(hotel1, hotel2, hotel3)) {
            SearchDocument actual = client.getDocument(hotel.get("HotelId").toString(), SearchDocument.class);
            assertMapEquals(hotel, actual, false);
        }
    }

    @Test
    public void canIndexDynamicDocumentsNotThrowAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(this::createHotelIndex);

        SearchDocument hotel1 = prepareDynamicallyTypedHotel("1");
        SearchDocument hotel2 = prepareDynamicallyTypedHotel("2");
        SearchDocument hotel3 = prepareDynamicallyTypedHotel("3");
        SearchDocument nonExistingHotel = prepareDynamicallyTypedHotel("nonExistingHotel"); // deleting a non existing document
        SearchDocument randomHotel = prepareDynamicallyTypedHotel("randomId"); // deleting a non existing document

        IndexDocumentsBatch<SearchDocument> batch = new IndexDocumentsBatch<SearchDocument>()
            .addUploadActions(Collections.singletonList(hotel1))
            .addDeleteActions(Collections.singletonList(randomHotel))
            .addMergeActions(Collections.singletonList(nonExistingHotel))
            .addMergeOrUploadActions(Collections.singletonList(hotel3))
            .addUploadActions(Collections.singletonList(hotel2));

        StepVerifier.create(asyncClient.indexDocumentsWithResponse(batch,
                new IndexDocumentsOptions().setThrowOnAnyError(false)))
            .assertNext(resultResponse -> {
                List<IndexingResult> results = resultResponse.getValue().getResults();
                assertEquals(resultResponse.getStatusCode(), 207);
                assertSuccessfulIndexResult(results.get(0), "1", 201);
                assertSuccessfulIndexResult(results.get(1), "randomId", 200);
                assertFailedIndexResult(results.get(2), "nonExistingHotel", 404);
                assertSuccessfulIndexResult(results.get(3), "3", 201);
                assertSuccessfulIndexResult(results.get(4), "2", 201);
            })
            .verifyComplete();

        for (SearchDocument hotel : Arrays.asList(hotel1, hotel2, hotel3)) {
            getAndValidateDocumentAsync(asyncClient, hotel.get("HotelId").toString(), SearchDocument.class, hotel,
                (expected, actual) -> assertMapEquals(expected, actual, false));
        }
    }

    @Test
    public void canIndexDynamicDocumentsThrowOnErrorSync() {
        SearchClient client = setupClient(this::createHotelIndex);

        SearchDocument hotel1 = prepareDynamicallyTypedHotel("1");
        SearchDocument hotel2 = prepareDynamicallyTypedHotel("2");
        SearchDocument hotel3 = prepareDynamicallyTypedHotel("3");
        SearchDocument nonExistingHotel = prepareDynamicallyTypedHotel("nonExistingHotel"); // deleting a non existing document
        SearchDocument randomHotel = prepareDynamicallyTypedHotel("randomId"); // deleting a non existing document

        IndexDocumentsBatch<SearchDocument> batch = new IndexDocumentsBatch<SearchDocument>()
            .addUploadActions(Collections.singletonList(hotel1))
            .addDeleteActions(Collections.singletonList(randomHotel))
            .addMergeActions(Collections.singletonList(nonExistingHotel))
            .addMergeOrUploadActions(Collections.singletonList(hotel3))
            .addUploadActions(Collections.singletonList(hotel2));

        IndexBatchException ex = assertThrows(IndexBatchException.class, () -> client.indexDocuments(batch));
        List<IndexingResult> results = ex.getIndexingResults();
        assertEquals(results.size(), batch.getActions().size());

        assertSuccessfulIndexResult(results.get(0), "1", 201);
        assertSuccessfulIndexResult(results.get(1), "randomId", 200);
        assertFailedIndexResult(results.get(2), "nonExistingHotel", 404);
        assertSuccessfulIndexResult(results.get(3), "3", 201);
        assertSuccessfulIndexResult(results.get(4), "2", 201);

        for (SearchDocument hotel : Arrays.asList(hotel1, hotel2, hotel3)) {
            SearchDocument actual = client.getDocument(hotel.get("HotelId").toString(), SearchDocument.class);
            assertMapEquals(hotel, actual, false);
        }
    }

    @Test
    public void canIndexDynamicDocumentsThrowOnErrorAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(this::createHotelIndex);

        SearchDocument hotel1 = prepareDynamicallyTypedHotel("1");
        SearchDocument hotel2 = prepareDynamicallyTypedHotel("2");
        SearchDocument hotel3 = prepareDynamicallyTypedHotel("3");
        SearchDocument nonExistingHotel = prepareDynamicallyTypedHotel("nonExistingHotel"); // deleting a non existing document
        SearchDocument randomHotel = prepareDynamicallyTypedHotel("randomId"); // deleting a non existing document

        IndexDocumentsBatch<SearchDocument> batch = new IndexDocumentsBatch<SearchDocument>()
            .addUploadActions(Collections.singletonList(hotel1))
            .addDeleteActions(Collections.singletonList(randomHotel))
            .addMergeActions(Collections.singletonList(nonExistingHotel))
            .addMergeOrUploadActions(Collections.singletonList(hotel3))
            .addUploadActions(Collections.singletonList(hotel2));

        StepVerifier.create(asyncClient.indexDocuments(batch))
            .verifyErrorSatisfies(throwable -> {
                IndexBatchException ex = assertInstanceOf(IndexBatchException.class, throwable);

                List<IndexingResult> results = ex.getIndexingResults();
                assertEquals(results.size(), batch.getActions().size());

                assertSuccessfulIndexResult(results.get(0), "1", 201);
                assertSuccessfulIndexResult(results.get(1), "randomId", 200);
                assertFailedIndexResult(results.get(2), "nonExistingHotel", 404);
                assertSuccessfulIndexResult(results.get(3), "3", 201);
                assertSuccessfulIndexResult(results.get(4), "2", 201);
            });

        for (SearchDocument hotel : Arrays.asList(hotel1, hotel2, hotel3)) {
            getAndValidateDocumentAsync(asyncClient, hotel.get("HotelId").toString(), SearchDocument.class, hotel,
                (expected, actual) -> assertMapEquals(expected, actual, false));
        }
    }

    @Test
    public void indexWithInvalidDocumentThrowsExceptionSync() {
        SearchClient client = setupClient(this::createHotelIndex);

        List<SearchDocument> docs = Collections.singletonList(new SearchDocument());

        assertHttpResponseException(() -> client.uploadDocuments(docs), HttpURLConnection.HTTP_BAD_REQUEST, null);
    }

    @Test
    public void indexWithInvalidDocumentThrowsExceptionAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(this::createHotelIndex);

        List<SearchDocument> docs = Collections.singletonList(new SearchDocument());

        StepVerifier.create(asyncClient.uploadDocuments(docs))
            .verifyErrorSatisfies(throwable -> verifyHttpResponseError(throwable, HttpURLConnection.HTTP_BAD_REQUEST,
                null));
    }

    @Test
    public void canUseIndexWithReservedNameSync() {
        String indexName = "prototype";
        SearchIndex indexWithReservedName = new SearchIndex(indexName)
            .setFields(new SearchField("ID", SearchFieldDataType.STRING).setKey(Boolean.TRUE));

        SearchIndexClient searchIndexClient = getSearchIndexClientBuilder().buildClient();
        searchIndexClient.createOrUpdateIndex(indexWithReservedName);
        indexesToDelete.add(indexWithReservedName.getName());

        SearchClient client = getSearchClientBuilder(indexName).buildClient();

        client.uploadDocuments(Collections.singletonList(Collections.singletonMap("ID", "1")));

        SearchDocument actual = client.getDocument("1", SearchDocument.class);
        assertNotNull(actual);
    }

    @Test
    public void canUseIndexWithReservedNameAsync() {
        String indexName = "prototype";
        SearchIndex indexWithReservedName = new SearchIndex(indexName)
            .setFields(new SearchField("ID", SearchFieldDataType.STRING).setKey(Boolean.TRUE));

        SearchIndexAsyncClient searchIndexAsyncClient = getSearchIndexClientBuilder().buildAsyncClient();
        searchIndexAsyncClient.createOrUpdateIndex(indexWithReservedName).block();
        indexesToDelete.add(indexWithReservedName.getName());

        SearchAsyncClient asyncClient = getSearchClientBuilder(indexName).buildAsyncClient();

        asyncClient.uploadDocuments(Collections.singletonList(Collections.singletonMap("ID", "1"))).block();

        StepVerifier.create(asyncClient.getDocument("1", SearchDocument.class))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    public void canRoundtripBoundaryValuesSync() {
        SearchClient client = setupClient(this::createHotelIndex);

        List<Hotel> boundaryConditionDocs = getBoundaryValues();

        client.uploadDocuments(boundaryConditionDocs);
        waitForIndexing();

        for (Hotel expected : boundaryConditionDocs) {
            Hotel actual = client.getDocument(expected.hotelId(), Hotel.class);

            assertObjectEquals(expected, actual, true);
        }
    }

    @Test
    public void canRoundtripBoundaryValuesAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(this::createHotelIndex);

        List<Hotel> boundaryConditionDocs = getBoundaryValues();

        asyncClient.uploadDocuments(boundaryConditionDocs).block();
        waitForIndexing();

        for (Hotel expected : boundaryConditionDocs) {
            getAndValidateDocumentAsync(asyncClient, expected.hotelId(), Hotel.class, expected,
                (ignored, actual) -> assertObjectEquals(expected, actual, true));
        }
    }

    @Test
    public void dynamicDocumentDateTimesRoundTripAsUtcSync() {
        SearchClient client = setupClient(() -> setupIndexFromJsonFile(BOOKS_INDEX_JSON));

        OffsetDateTime utcTime = OffsetDateTime.of(LocalDateTime.of(2010, 1, 1, 0, 0, 0), ZoneOffset.UTC);
        // UTC-8
        OffsetDateTime utcTimeMinusEight = OffsetDateTime.of(LocalDateTime.of(2010, 1, 1, 0, 0, 0),
            ZoneOffset.ofHours(-8));

        Map<String, Object> book1 = new HashMap<>();
        book1.put("ISBN", "1");
        book1.put("PublishDate", utcTime);

        Map<String, Object> book2 = new HashMap<>();
        book2.put("ISBN", "2");
        book2.put("PublishDate", utcTimeMinusEight);

        client.uploadDocuments(Arrays.asList(book1, book2));
        waitForIndexing();

        SearchDocument actualBook1 = client.getDocument("1", SearchDocument.class);
        assertEquals(utcTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), actualBook1.get("PublishDate"));

        // Azure Cognitive Search normalizes to UTC, so we compare instants
        SearchDocument actualBook2 = client.getDocument("2", SearchDocument.class);
        assertEquals(utcTimeMinusEight.withOffsetSameInstant(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), actualBook2.get("PublishDate"));
    }

    @Test
    public void dynamicDocumentDateTimesRoundTripAsUtcAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(() -> setupIndexFromJsonFile(BOOKS_INDEX_JSON));

        OffsetDateTime utcTime = OffsetDateTime.of(LocalDateTime.of(2010, 1, 1, 0, 0, 0), ZoneOffset.UTC);
        // UTC-8
        OffsetDateTime utcTimeMinusEight = OffsetDateTime.of(LocalDateTime.of(2010, 1, 1, 0, 0, 0),
            ZoneOffset.ofHours(-8));

        SearchDocument book1 = new SearchDocument();
        book1.put("ISBN", "1");
        book1.put("PublishDate", utcTime);

        SearchDocument book2 = new SearchDocument();
        book2.put("ISBN", "2");
        book2.put("PublishDate", utcTimeMinusEight);

        asyncClient.uploadDocuments(Arrays.asList(book1, book2)).block();
        waitForIndexing();

        getAndValidateDocumentAsync(asyncClient, "1", SearchDocument.class, book1,
            (expected, actual) -> assertEquals(utcTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                actual.get("PublishDate")));

        // Azure Cognitive Search normalizes to UTC, so we compare instants
        getAndValidateDocumentAsync(asyncClient, "2", SearchDocument.class, book2,
            (expected, actual) -> assertEquals(utcTimeMinusEight.withOffsetSameInstant(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME), actual.get("PublishDate")));
    }

    @Test
    public void staticallyTypedDateTimesRoundTripAsUtcSync() {
        SearchClient client = setupClient(() -> setupIndexFromJsonFile(BOOKS_INDEX_JSON));

        List<Book> books = Arrays.asList(
            new Book()
                .ISBN("1")
                .publishDate(OffsetDateTime.of(
                    LocalDateTime.of(2010, 1, 1, 0, 0, 0),
                    ZoneOffset.UTC)),
            new Book()
                .ISBN("2")
                .publishDate(OffsetDateTime.of(
                    LocalDateTime.of(2010, 1, 1, 0, 0, 0),
                    ZoneOffset.ofHours(-8))));

        client.uploadDocuments(books);

        Book actualBook1 = client.getDocument("1", Book.class);
        assertEquals(books.get(0).publishDate(), actualBook1.publishDate());

        // Azure Cognitive Search normalizes to UTC, so we compare instants
        Book actualBook2 = client.getDocument("2", Book.class);
        assertEquals(books.get(1).publishDate().withOffsetSameInstant(ZoneOffset.UTC),
            actualBook2.publishDate().withOffsetSameInstant(ZoneOffset.UTC));
    }

    @Test
    public void staticallyTypedDateTimesRoundTripAsUtcAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(() -> setupIndexFromJsonFile(BOOKS_INDEX_JSON));

        List<Book> books = Arrays.asList(
            new Book()
                .ISBN("1")
                .publishDate(OffsetDateTime.of(
                    LocalDateTime.of(2010, 1, 1, 0, 0, 0),
                    ZoneOffset.UTC)),
            new Book()
                .ISBN("2")
                .publishDate(OffsetDateTime.of(
                    LocalDateTime.of(2010, 1, 1, 0, 0, 0),
                    ZoneOffset.ofHours(-8))));

        asyncClient.uploadDocuments(books).block();

        getAndValidateDocumentAsync(asyncClient, "1", Book.class, null,
            (expected, actual) -> assertEquals(books.get(0).publishDate(), actual.publishDate()));

        // Azure Cognitive Search normalizes to UTC, so we compare instants
        getAndValidateDocumentAsync(asyncClient, "2", Book.class, null,
            (expected, actual) -> assertEquals(books.get(1).publishDate().withOffsetSameInstant(ZoneOffset.UTC),
                actual.publishDate().withOffsetSameInstant(ZoneOffset.UTC)));
    }

    @Test
    public void canMergeStaticallyTypedDocumentsSync() {
        SearchClient client = setupClient(this::createHotelIndex);

        // Define hotels
        Hotel originalDoc = canMergeStaticallyTypedDocumentsOriginal();

        // Update category, tags, parking included, rating, and rooms. Erase description, last renovation date, location and address.
        Hotel updatedDoc = canMergeStaticallyTypedDocumentsUpdated();

        // Fields whose values get updated are updated, and whose values get erased remain the same.
        Hotel expectedDoc = canMergeStaticallyTypedDocumentsExpected();

        List<Hotel> originalDocs = Collections.singletonList(originalDoc);
        client.uploadDocuments(originalDocs);

        client.mergeDocuments(Collections.singletonList(updatedDoc));
        assertObjectEquals(expectedDoc, client.getDocument("1", Hotel.class), true);

        client.mergeDocuments(originalDocs);
        assertObjectEquals(originalDoc, client.getDocument("1", Hotel.class), true);
    }

    @Test
    public void canMergeStaticallyTypedDocumentsAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(this::createHotelIndex);

        // Define hotels
        Hotel originalDoc = canMergeStaticallyTypedDocumentsOriginal();

        // Update category, tags, parking included, rating, and rooms. Erase description, last renovation date, location and address.
        Hotel updatedDoc = canMergeStaticallyTypedDocumentsUpdated();

        // Fields whose values get updated are updated, and whose values get erased remain the same.
        Hotel expectedDoc = canMergeStaticallyTypedDocumentsExpected();

        List<Hotel> originalDocs = Collections.singletonList(originalDoc);
        asyncClient.uploadDocuments(originalDocs).block();

        asyncClient.mergeDocuments(Collections.singletonList(updatedDoc)).block();

        getAndValidateDocumentAsync(asyncClient, "1", Hotel.class, expectedDoc,
            (expected, actual) -> assertObjectEquals(expected, actual, true));

        asyncClient.mergeDocuments(originalDocs).block();

        getAndValidateDocumentAsync(asyncClient, "1", Hotel.class, originalDoc,
            (expected, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void mergeDocumentWithoutExistingKeyThrowsIndexingExceptionSync() {
        SearchClient client = setupClient(this::createHotelIndex);

        IndexBatchException ex = assertThrows(IndexBatchException.class,
            () -> client.mergeDocuments(Collections.singletonList(prepareStaticallyTypedHotel("1"))));

        List<IndexingResult> results = ex.getIndexingResults();
        assertFailedIndexResult(results.get(0), "1", HttpResponseStatus.NOT_FOUND.code());
        assertEquals(1, results.size());
    }

    @Test
    public void mergeDocumentWithoutExistingKeyThrowsIndexingExceptionAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(this::createHotelIndex);

        StepVerifier.create(asyncClient.mergeDocuments(Collections.singletonList(prepareStaticallyTypedHotel("1"))))
            .verifyErrorSatisfies(throwable -> {
                IndexBatchException ex = assertInstanceOf(IndexBatchException.class, throwable);

                List<IndexingResult> results = ex.getIndexingResults();
                assertFailedIndexResult(results.get(0), "1", HttpResponseStatus.NOT_FOUND.code());
                assertEquals(1, results.size());
            });
    }

    @Test
    public void canSetExplicitNullsInStaticallyTypedDocumentSync() {
        SearchClient client = setupClient(this::createHotelIndex);

        LoudHotel originalDoc = canSetExplicitNullsInStaticallyTypedDocumentOriginal();
        LoudHotel updatedDoc = canSetExplicitNullsInStaticallyTypedDocumentUpdated();
        LoudHotel expectedDoc = canSetExplicitNullsInStaticallyTypedDocumentExpected();

        List<LoudHotel> originalDocs = Collections.singletonList(originalDoc);
        client.uploadDocuments(originalDocs);
        waitForIndexing();

        client.mergeDocuments(Collections.singletonList(updatedDoc));
        waitForIndexing();

        LoudHotel actualDoc1 = client.getDocument("1", LoudHotel.class);
        assertObjectEquals(expectedDoc, actualDoc1, true);

        client.uploadDocuments(originalDocs);
        waitForIndexing();

        LoudHotel actualDoc2 = client.getDocument("1", LoudHotel.class);
        assertObjectEquals(originalDoc, actualDoc2, true);
    }

    @Test
    public void canSetExplicitNullsInStaticallyTypedDocumentAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(this::createHotelIndex);

        LoudHotel originalDoc = canSetExplicitNullsInStaticallyTypedDocumentOriginal();
        LoudHotel updatedDoc = canSetExplicitNullsInStaticallyTypedDocumentUpdated();
        LoudHotel expectedDoc = canSetExplicitNullsInStaticallyTypedDocumentExpected();

        List<LoudHotel> originalDocs = Collections.singletonList(originalDoc);
        asyncClient.uploadDocuments(originalDocs).block();
        waitForIndexing();

        asyncClient.mergeDocuments(Collections.singletonList(updatedDoc)).block();
        waitForIndexing();

        getAndValidateDocumentAsync(asyncClient, "1", LoudHotel.class, expectedDoc,
            (expected, actual) -> assertObjectEquals(expected, actual, true));

        asyncClient.uploadDocuments(originalDocs).block();
        waitForIndexing();

        getAndValidateDocumentAsync(asyncClient, "1", LoudHotel.class, originalDoc,
            (expected, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void canMergeDynamicDocumentsSync() {
        SearchClient client = setupClient(this::createHotelIndex);

        SearchDocument originalDoc = canMergeDynamicDocumentsOriginal();
        SearchDocument updatedDoc = canMergeDynamicDocumentsUpdated();
        SearchDocument expectedDoc = canMergeDynamicDocumentsExpected();

        List<SearchDocument> originalDocs = Collections.singletonList(originalDoc);
        client.mergeOrUploadDocuments(originalDocs);
        waitForIndexing();

        client.mergeDocuments(Collections.singletonList(updatedDoc));
        waitForIndexing();

        SearchDocument actualDoc = client.getDocument("1", SearchDocument.class);
        assertObjectEquals(expectedDoc, actualDoc, true);

        client.mergeOrUploadDocuments(originalDocs);
        waitForIndexing();

        actualDoc = client.getDocument("1", SearchDocument.class);
        assertMapEquals(originalDoc, actualDoc, false, "properties");
    }

    @Test
    public void canMergeDynamicDocumentsAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(this::createHotelIndex);

        SearchDocument originalDoc = canMergeDynamicDocumentsOriginal();
        SearchDocument updatedDoc = canMergeDynamicDocumentsUpdated();
        SearchDocument expectedDoc = canMergeDynamicDocumentsExpected();

        List<SearchDocument> originalDocs = Collections.singletonList(originalDoc);
        asyncClient.mergeOrUploadDocuments(originalDocs).block();
        waitForIndexing();

        asyncClient.mergeDocuments(Collections.singletonList(updatedDoc)).block();
        waitForIndexing();

        getAndValidateDocumentAsync(asyncClient, "1", SearchDocument.class, expectedDoc,
            (expected, actual) -> assertObjectEquals(expected, actual, true));

        asyncClient.mergeOrUploadDocuments(originalDocs).block();
        waitForIndexing();

        getAndValidateDocumentAsync(asyncClient, "1", SearchDocument.class, originalDoc,
            (expected, actual) -> assertObjectEquals(expected, actual, true, "properties"));
    }

    @Test
    public void canIndexAndAccessResponseSync() {
        SearchClient client = setupClient(this::createHotelIndex);

        List<Hotel> hotelsToUpload = Arrays.asList(
            new Hotel().hotelId("1"),
            new Hotel().hotelId("2"));

        List<Hotel> hotelsToMerge = Collections.singletonList(new Hotel().hotelId("1").rating(5));

        List<Hotel> hotelsToMergeOrUpload = Arrays.asList(
            new Hotel().hotelId("3").rating(4),
            new Hotel().hotelId("4").rating(1));

        List<Hotel> hotelsToDelete = Collections.singletonList(new Hotel().hotelId("4"));

        IndexDocumentsBatch<Hotel> batch = new IndexDocumentsBatch<Hotel>()
            .addUploadActions(hotelsToUpload)
            .addMergeOrUploadActions(hotelsToMergeOrUpload);

        validateIndexResponseSync(client.uploadDocumentsWithResponse(hotelsToUpload, null, Context.NONE), 2);
        waitForIndexing();

        validateIndexResponseSync(client.mergeDocumentsWithResponse(hotelsToMerge, null, Context.NONE), 1);
        waitForIndexing();

        validateIndexResponseSync(client.mergeOrUploadDocumentsWithResponse(hotelsToMergeOrUpload, null, Context.NONE),
            2);
        waitForIndexing();

        validateIndexResponseSync(client.deleteDocumentsWithResponse(hotelsToDelete, null, Context.NONE), 1);
        waitForIndexing();

        validateIndexResponseSync(client.indexDocumentsWithResponse(batch, null, Context.NONE), 4);
        waitForIndexing();

        assertEquals(4, client.getDocument("3", SearchDocument.class).get("Rating"));

        assertEquals(4L, client.getDocumentCount());
    }

    @Test
    public void canIndexAndAccessResponseAsync() {
        SearchAsyncClient asyncClient = setupAsyncClient(this::createHotelIndex);

        List<Hotel> hotelsToUpload = Arrays.asList(
            new Hotel().hotelId("1"),
            new Hotel().hotelId("2"));

        List<Hotel> hotelsToMerge = Collections.singletonList(new Hotel().hotelId("1").rating(5));

        List<Hotel> hotelsToMergeOrUpload = Arrays.asList(
            new Hotel().hotelId("3").rating(4),
            new Hotel().hotelId("4").rating(1));

        List<Hotel> hotelsToDelete = Collections.singletonList(new Hotel().hotelId("4"));

        IndexDocumentsBatch<Hotel> batch = new IndexDocumentsBatch<Hotel>()
            .addUploadActions(hotelsToUpload)
            .addMergeOrUploadActions(hotelsToMergeOrUpload);

        validateIndexResponseAsync(asyncClient.uploadDocumentsWithResponse(hotelsToUpload, null), 2);

        waitForIndexing();

        validateIndexResponseAsync(asyncClient.mergeDocumentsWithResponse(hotelsToMerge, null), 1);

        waitForIndexing();

        validateIndexResponseAsync(asyncClient.mergeOrUploadDocumentsWithResponse(hotelsToMergeOrUpload, null), 2);

        waitForIndexing();

        validateIndexResponseAsync(asyncClient.deleteDocumentsWithResponse(hotelsToDelete, null), 1);

        waitForIndexing();

        validateIndexResponseAsync(asyncClient.indexDocumentsWithResponse(batch, null), 4);

        waitForIndexing();

        getAndValidateDocumentAsync(asyncClient, "3", SearchDocument.class, null,
            (expected, actual) -> assertEquals(4, actual.get("Rating")));

        validateDocumentCountAsync(4, asyncClient);
    }

    private static void validateDocumentCountAsync(long count, SearchAsyncClient asyncClient) {
        StepVerifier.create(asyncClient.getDocumentCount())
            .assertNext(actual -> assertEquals(count, actual))
            .verifyComplete();
    }

    private static <T> void getAndValidateDocumentAsync(SearchAsyncClient asyncClient, String key, Class<T> type,
        T expected, BiConsumer<T, T> comparator) {
        StepVerifier.create(asyncClient.getDocument(key, type))
            .assertNext(actual -> comparator.accept(expected, actual))
            .verifyComplete();
    }

    private static void validateIndexResponseSync(Response<IndexDocumentsResult> response, int resultCount) {
        assertEquals(200, response.getStatusCode());
        assertEquals(resultCount, response.getValue().getResults().size());
    }

    private static void validateIndexResponseAsync(Mono<Response<IndexDocumentsResult>> indexDocumentsWithResponse,
        int resultCount) {
        StepVerifier.create(indexDocumentsWithResponse)
            .assertNext(response -> {
                assertEquals(200, response.getStatusCode());
                assertEquals(resultCount, response.getValue().getResults().size());
            })
            .verifyComplete();
    }

    @SuppressWarnings({"UseOfObsoleteDateTimeApi"})
    static Hotel prepareStaticallyTypedHotel(String hotelId) {
        return new Hotel()
            .hotelId(hotelId)
            .hotelName("Fancy Stay")
            .description("Best hotel in town if you like luxury hotels. They have an amazing infinity pool, a spa, and a really helpful concierge. The location is perfect -- right downtown, close to all the tourist attractions. We highly recommend this hotel.")
            .descriptionFr("Meilleur hôtel en ville si vous aimez les hôtels de luxe. Ils ont une magnifique piscine à débordement, un spa et un concierge très utile. L'emplacement est parfait – en plein centre, à proximité de toutes les attractions touristiques. Nous recommandons fortement cet hôtel.")
            .category("Luxury")
            .tags(Arrays.asList("pool",
                "view",
                "wifi",
                "concierge"))
            .parkingIncluded(false)
            .smokingAllowed(false)
            .lastRenovationDate(parseDate("2010-06-27T00:00:00Z"))
            .rating(5)
            .location(new GeoPoint(-122.131577, 47.678581))
            .address(
                new HotelAddress()
                    .streetAddress("1 Microsoft Way")
                    .city("Redmond")
                    .stateProvince("Washington")
                    .postalCode("98052")
                    .country("United States")
            );
    }

    SearchDocument prepareDynamicallyTypedHotel(String hotelId) {

        SearchDocument room1 = new SearchDocument();
        room1.put("Description", "Budget Room, 1 Queen Bed");
        room1.put("Description_fr", null);
        room1.put("Type", "Budget Room");
        room1.put("BaseRate", 149.99);
        room1.put("BedOptions", "1 Queen Bed");
        room1.put("SleepsCount", 2);
        room1.put("SmokingAllowed", true);
        room1.put("Tags", Arrays.asList("vcr/dvd", "great view"));

        SearchDocument room2 = new SearchDocument();
        room2.put("Description", "Budget Room, 1 King Bed");
        room2.put("Description_fr", null);
        room2.put("Type", "Budget Room");
        room2.put("BaseRate", 249.99);
        room2.put("BedOptions", "1 King Bed");
        room2.put("SleepsCount", 2);
        room2.put("SmokingAllowed", true);
        room2.put("Tags", Arrays.asList("vcr/dvd", "seaside view"));

        List<SearchDocument> rooms = Arrays.asList(room1, room2);

        SearchDocument address = new SearchDocument();
        address.put("StreetAddress", "One Microsoft way");
        address.put("City", "Redmond");
        address.put("StateProvince", "Washington");
        address.put("PostalCode", "98052");
        address.put("Country", "US");

        // TODO (alzimmer): Determine if this should be used to create the hotel document.
        SearchDocument location = new SearchDocument();
        location.put("type", "Point");
        location.put("coordinates", Arrays.asList(-122.131577, 47.678581));
        location.put("crs", null);

        SearchDocument hotel = new SearchDocument();
        hotel.put("HotelId", hotelId);
        hotel.put("HotelName", "Fancy Stay Hotel");
        hotel.put("Description", "Best hotel in town if you like luxury hotels. They have an amazing infinity pool, a spa, and a really helpful concierge. The location is perfect -- right downtown, close to all the tourist attractions. We highly recommend this hotel.");
        hotel.put("Description_fr", null);
        hotel.put("Address", address);
        hotel.put("Location", null);
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
        assertEquals(result.getErrorMessage(), "Document not found.");
        assertFalse(result.isSucceeded());
    }

    static void assertIndexActionSucceeded(String key, IndexingResult result, int expectedStatusCode) {
        assertEquals(key, result.getKey());
        assertTrue(result.isSucceeded());
        assertNull(result.getErrorMessage());
        assertEquals(expectedStatusCode, result.getStatusCode());
    }

    @SuppressWarnings({"UseOfObsoleteDateTimeApi", "deprecation"})
    List<Hotel> getBoundaryValues() {
        Date maxEpoch = Date.from(Instant.ofEpochMilli(253402300799000L));
        Date minEpoch = Date.from(Instant.ofEpochMilli(-2208988800000L));
        return Arrays.asList(
            // Minimum values
            new Hotel()
                .hotelId("1")
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
            new Hotel()
                .hotelId("2")
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
            new Hotel()
                .hotelId("3")
                .category(null)
                .lastRenovationDate(null)
                .location(new GeoPoint(0.0, 0.0))     // Equator, meridian
                .parkingIncluded(null)
                .rating(null)
                .tags(Collections.emptyList())
                .address(new HotelAddress().city("Maximum"))
                .rooms(Collections.singletonList(new HotelRoom().baseRate(Double.NEGATIVE_INFINITY))),
            // Other boundary values #2
            new Hotel()
                .hotelId("4")
                .location(null)
                .tags(Collections.emptyList())
                .rooms(Collections.singletonList(new HotelRoom().baseRate(Double.POSITIVE_INFINITY))),
            // Other boundary values #3
            new Hotel()
                .hotelId("5")
                .tags(Collections.emptyList())
                .rooms(Collections.singletonList(new HotelRoom().baseRate(Double.NaN))),
            // Other boundary values #4
            new Hotel()
                .hotelId("6")
                .category(null)
                .tags(Collections.emptyList())
                .rooms(Collections.emptyList()));
    }

    @SuppressWarnings({"UseOfObsoleteDateTimeApi"})
    private static Hotel canMergeStaticallyTypedDocumentsOriginal() {
        // Define hotels
        return new Hotel()
            .hotelId("1")
            .hotelName("Secret Point Motel")
            .description("The hotel is ideally located on the main commercial artery of the city in the heart of New York. A few minutes away is Time's Square and the historic centre of the city, as well as other places of interest that make New York one of America's most attractive and cosmopolitan cities.")
            .descriptionFr("L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus attractives et cosmopolites de l'Amérique.")
            .category("Boutique")
            .tags(Arrays.asList("pool", "air conditioning", "concierge"))
            .parkingIncluded(false)
            .smokingAllowed(true)
            .lastRenovationDate(parseDate("2010-06-27T00:00:00Z"))
            .rating(4)
            .location(new GeoPoint(-73.975403, 40.760586))
            .address(new HotelAddress()
                .streetAddress("677 5th Ave")
                .city("New York")
                .stateProvince("NY")
                .country("USA")
                .postalCode("10022"))
            .rooms(Arrays.asList(
                new HotelRoom()
                    .description("Budget Room, 1 Queen Bed (Cityside)")
                    .descriptionFr("Chambre Économique, 1 grand lit (côté ville)")
                    .type("Budget Room")
                    .baseRate(9.69)
                    .bedOptions("1 Queen Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(new String[]{"vcr/dvd"}),
                new HotelRoom()
                    .description("Budget Room, 1 King Bed (Mountain View)")
                    .descriptionFr("Chambre Économique, 1 très grand lit (Mountain View)")
                    .type("Budget Room")
                    .baseRate(8.09)
                    .bedOptions("1 King Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(new String[]{"vcr/dvd", "jacuzzi tub"})
            ));
    }

    private static Hotel canMergeStaticallyTypedDocumentsUpdated() {
        // Update category, tags, parking included, rating, and rooms. Erase description, last renovation date, location and address.
        return new Hotel()
            .hotelId("1")
            .hotelName("Secret Point Motel")
            .description(null)
            .category("Economy")
            .tags(Arrays.asList("pool", "air conditioning"))
            .parkingIncluded(true)
            .lastRenovationDate(null)
            .rating(3)
            //.location(null)
            .address(new HotelAddress())
            .rooms(Collections.singletonList(
                new HotelRoom()
                    .description(null)
                    .type("Budget Room")
                    .baseRate(10.5)
                    .bedOptions("1 Queen Bed")
                    .sleepsCount(2)
                    .tags(new String[]{"vcr/dvd", "balcony"})
            ));
    }

    @SuppressWarnings({"UseOfObsoleteDateTimeApi"})
    private static Hotel canMergeStaticallyTypedDocumentsExpected() {
        // Fields whose values get updated are updated, and whose values get erased remain the same.
        return new Hotel()
            .hotelId("1")
            .hotelName("Secret Point Motel")
            .description("The hotel is ideally located on the main commercial artery of the city in the heart of New York. A few minutes away is Time's Square and the historic centre of the city, as well as other places of interest that make New York one of America's most attractive and cosmopolitan cities.")
            .descriptionFr("L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus attractives et cosmopolites de l'Amérique.")
            .category("Economy")
            .tags(Arrays.asList("pool", "air conditioning"))
            .parkingIncluded(true)
            .smokingAllowed(true)
            .lastRenovationDate(parseDate("2010-06-27T00:00:00Z"))
            .rating(3)
            .location(new GeoPoint(-73.975403, 40.760586))
            .address(new HotelAddress()
                .streetAddress("677 5th Ave")
                .city("New York")
                .stateProvince("NY")
                .country("USA")
                .postalCode("10022"))
            .rooms(Collections.singletonList(
                new HotelRoom()
                    .description(null)
                    .type("Budget Room")
                    .baseRate(10.5)
                    .bedOptions("1 Queen Bed")
                    .sleepsCount(2)
                    .tags(new String[]{"vcr/dvd", "balcony"})
            ));
    }

    @SuppressWarnings({"UseOfObsoleteDateTimeApi"})
    private static LoudHotel canSetExplicitNullsInStaticallyTypedDocumentOriginal() {
        return new LoudHotel()
            .HOTELID("1")
            .HOTELNAME("Secret Point Motel")
            .DESCRIPTION("The hotel is ideally located on the main commercial artery of the city in the heart of New York. A few minutes away is Time's Square and the historic centre of the city, as well as other places of interest that make New York one of America's most attractive and cosmopolitan cities.")
            .DESCRIPTIONFRENCH("L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus attractives et cosmopolites de l'Amérique.")
            .CATEGORY("Boutique")
            .TAGS(Arrays.asList("pool", "air conditioning", "concierge"))
            .PARKINGINCLUDED(false)
            .SMOKINGALLOWED(false)
            .LASTRENOVATIONDATE(parseDate("1970-01-18T05:00:00Z"))
            .RATING(4)
            .LOCATION(new GeoPoint(-73.975403, 40.760586))
            .ADDRESS(new HotelAddress()
                .streetAddress("677 5th Ave")
                .city("New York")
                .stateProvince("NY")
                .country("USA")
                .postalCode("10022")
            ).ROOMS(Arrays.asList(
                new HotelRoom()
                    .description("Budget Room, 1 Queen Bed (Cityside)")
                    .descriptionFr("Chambre Économique, 1 grand lit (côté ville)")
                    .type("Budget Room")
                    .baseRate(9.69)
                    .bedOptions("1 Queen Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(new String[]{"vcr/dvd"}),
                new HotelRoom()
                    .description("Budget Room, 1 King Bed (Mountain View)")
                    .descriptionFr("Chambre Économique, 1 très grand lit (Mountain View)")
                    .type("Budget Room")
                    .baseRate(8.09)
                    .bedOptions("1 King Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(new String[]{"vcr/dvd", "jacuzzi tub"})
            ));
    }

    @SuppressWarnings({"UseOfObsoleteDateTimeApi"})
    private static LoudHotel canSetExplicitNullsInStaticallyTypedDocumentUpdated() {
        return new LoudHotel()
            .HOTELID("1")
            .DESCRIPTION(null)  // This property has JsonInclude.Include.ALWAYS, so this will null out the field.
            .CATEGORY(null)     // This property doesn't have JsonInclude.Include.ALWAYS, so this should have no effect.
            .TAGS(Arrays.asList("pool", "air conditioning"))
            .PARKINGINCLUDED(true)
            .LASTRENOVATIONDATE(parseDate("1970-01-18T05:00:00Z"))
            .RATING(3)
            //.LOCATION(null)     // This property has JsonInclude.Include.ALWAYS, so this will null out the field.
            .ADDRESS(new HotelAddress())
            .ROOMS(Collections.singletonList(
                new HotelRoom()
                    .description(null)
                    .type("Budget Room")
                    .baseRate(10.5)
                    .smokingAllowed(false)
                    .tags(new String[]{"vcr/dvd", "balcony"})
            ));
    }

    @SuppressWarnings({"UseOfObsoleteDateTimeApi"})
    private static LoudHotel canSetExplicitNullsInStaticallyTypedDocumentExpected() {
        return new LoudHotel()
            .HOTELID("1")
            .HOTELNAME("Secret Point Motel")
            .DESCRIPTION(null)
            .DESCRIPTIONFRENCH("L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus attractives et cosmopolites de l'Amérique.")
            .CATEGORY("Boutique")
            .TAGS(Arrays.asList("pool", "air conditioning"))
            .PARKINGINCLUDED(true)
            .SMOKINGALLOWED(false)
            .LASTRENOVATIONDATE(parseDate("1970-01-18T05:00:00Z"))
            .RATING(3)
            //.LOCATION(null)
            .ADDRESS(new HotelAddress()
                .streetAddress("677 5th Ave")
                .city("New York")
                .stateProvince("NY")
                .country("USA")
                .postalCode("10022")
            ).ROOMS(Collections.singletonList(
                // Regardless of NullValueHandling, this should look like the merged doc with unspecified fields as null
                // because we don't support partial updates for complex collections.
                new HotelRoom()
                    .description(null)
                    .descriptionFr(null)
                    .type("Budget Room")
                    .baseRate(10.5)
                    .bedOptions(null)
                    .sleepsCount(null)
                    .smokingAllowed(false)
                    .tags(new String[]{"vcr/dvd", "balcony"})
            ));
    }

    private static SearchDocument canMergeDynamicDocumentsOriginal() {
        SearchDocument originalDoc = new SearchDocument();
        originalDoc.put("HotelId", "1");
        originalDoc.put("HotelName", "Secret Point Motel");
        originalDoc.put("Description", "The hotel is ideally located on the main commercial artery of the city in the heart of New York. A few minutes away is Time's Square and the historic centre of the city, as well as other places of interest that make New York one of America's most attractive and cosmopolitan cities.");
        originalDoc.put("Description_fr", "L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus attractives et cosmopolites de l'Amérique.");
        originalDoc.put("Category", "Boutique");
        originalDoc.put("Tags", Arrays.asList("pool", "air conditioning", "concierge"));
        originalDoc.put("ParkingIncluded", false);
        originalDoc.put("SmokingAllowed", true);
        originalDoc.put("LastRenovationDate", OffsetDateTime.parse("2010-06-27T00:00:00Z"));
        originalDoc.put("Rating", 4);
        originalDoc.put("Location", new GeoPoint(-73.965403, 40.760586));

        SearchDocument originalAddress = new SearchDocument();
        originalAddress.put("StreetAddress", "677 5th Ave");
        originalAddress.put("City", "New York");
        originalAddress.put("StateProvince", "NY");
        originalAddress.put("PostalCode", "10022");
        originalAddress.put("Country", "USA");
        originalDoc.put("Address", originalAddress);

        SearchDocument originalRoom1 = new SearchDocument();
        originalRoom1.put("Description", "Budget Room, 1 Queen Bed (Cityside)");
        originalRoom1.put("Description_fr", "Chambre Économique, 1 grand lit (côté ville)");
        originalRoom1.put("Type", "Budget Room");
        originalRoom1.put("BaseRate", 9.69);
        originalRoom1.put("BedOptions", "1 Queen Bed");
        originalRoom1.put("SleepsCount", 2);
        originalRoom1.put("SmokingAllowed", true);
        originalRoom1.put("Tags", Collections.singletonList("vcr/dvd"));

        SearchDocument originalRoom2 = new SearchDocument();
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

    private static SearchDocument canMergeDynamicDocumentsUpdated() {
        SearchDocument updatedDoc = new SearchDocument();
        updatedDoc.put("HotelId", "1");
        updatedDoc.put("Description", null);
        updatedDoc.put("Category", "Economy");
        updatedDoc.put("Tags", Arrays.asList("pool", "air conditioning"));
        updatedDoc.put("ParkingIncluded", true);
        updatedDoc.put("LastRenovationDate", null);
        updatedDoc.put("Rating", 3);
        updatedDoc.put("Location", null);
        updatedDoc.put("Address", new SearchDocument());

        SearchDocument updatedRoom1 = new SearchDocument();
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

    private static SearchDocument canMergeDynamicDocumentsExpected() {
        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", "1");
        expectedDoc.put("HotelName", "Secret Point Motel");
        expectedDoc.put("Description", null);
        expectedDoc.put("Description_fr", "L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus attractives et cosmopolites de l'Amérique.");
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

    @SuppressWarnings({"UseOfObsoleteDateTimeApi"})
    private static Date parseDate(String dateString) {
        DateFormat dateFormat = new SimpleDateFormat(ISO8601_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            return dateFormat.parse(dateString);
        } catch (ParseException ex) {
            throw new RuntimeException(ex);
        }
    }
}
