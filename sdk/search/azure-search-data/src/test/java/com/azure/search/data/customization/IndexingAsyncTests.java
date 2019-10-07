// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.core.exception.HttpResponseException;
import com.azure.search.data.customization.models.GeoPoint;
import com.azure.search.data.generated.models.DocumentIndexResult;
import com.azure.search.data.generated.models.IndexingResult;
import com.azure.search.test.environment.models.Hotel;
import com.azure.search.test.environment.models.HotelRoom;
import com.azure.search.test.environment.models.HotelAddress;
import com.azure.search.test.environment.models.Book;
import com.azure.search.test.environment.models.LoudHotel;
import com.azure.search.service.models.DataType;
import com.azure.search.service.models.Field;
import com.azure.search.service.models.Index;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IndexingAsyncTests extends IndexingTestBase {
    private SearchIndexAsyncClient client;

    @Override
    public void countingDocsOfNewIndexGivesZero() {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildAsyncClient();
        Mono<Long> result = client.getDocumentCount();
        Long expected = 0L;

        StepVerifier.create(result)
            .expectNext(expected)
            .verifyComplete();
    }

    @Override
    public void indexDoesNotThrowWhenAllActionsSucceed() {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildAsyncClient();

        String expectedHotelId = "1";
        Long expectedHotelCount = 1L;

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(new Hotel()
            .hotelId(expectedHotelId)
        );
        Mono<DocumentIndexResult> asyncResult = client.uploadDocuments(hotels);

        StepVerifier.create(asyncResult).assertNext(res -> {
            List<IndexingResult> result = res.results();
            this.assertIndexActionSucceeded(expectedHotelId, result.get(0), 201);
        }).verifyComplete();

        waitForIndexing();

        StepVerifier.create(client.getDocumentCount()).
            expectNext(expectedHotelCount).
            verifyComplete();
    }

    @Override
    public void canIndexWithPascalCaseFields() {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildAsyncClient();

        String expectedHotelId = "1";
        Long expectedHotelCount = 1L;

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(new Hotel()
            .hotelId(expectedHotelId)
            .hotelName("My Pascal Hotel")
            .description("A Great Pascal Description.")
            .category("Category Pascal")
        );

        Mono<DocumentIndexResult> asyncResult = client.uploadDocuments(hotels);

        StepVerifier.create(asyncResult).assertNext(res -> {
            List<IndexingResult> result = res.results();
            this.assertIndexActionSucceeded(expectedHotelId, result.get(0), 201);
        }).verifyComplete();

        waitForIndexing();

        StepVerifier.create(client.getDocumentCount()).
            expectNext(expectedHotelCount).
            verifyComplete();
    }

    @Override
    public void canIndexStaticallyTypedDocuments() throws ParseException {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildAsyncClient();

        Hotel hotel1 = prepareStaticallyTypedHotel("1");
        Hotel hotel2 = prepareStaticallyTypedHotel("2");
        Hotel hotel3 = prepareStaticallyTypedHotel("3");
        Hotel nonExistingHotel = prepareStaticallyTypedHotel("nonExistingHotel"); // merging with a non existing document
        Hotel randomHotel = prepareStaticallyTypedHotel("randomId"); // deleting a non existing document

        IndexBatch<Hotel> batch = new IndexBatch<Hotel>()
            .addUploadAction(hotel1)
            .addDeleteAction(randomHotel)
            .addMergeAction(nonExistingHotel)
            .addMergeOrUploadAction(hotel3)
            .addUploadAction(hotel2);

        Mono<DocumentIndexResult> response = client.index(batch);

        StepVerifier.create(response)
            .verifyErrorSatisfies(err -> {
                Assert.assertEquals(IndexBatchException.class, err.getClass());

                List<IndexingResult> indexingResults = ((IndexBatchException) err).getIndexingResults();

                Assert.assertEquals(batch.actions().size(), indexingResults.size());

                assertSuccessfulIndexResult(indexingResults.get(0), "1", 201);
                assertSuccessfulIndexResult(indexingResults.get(1), "randomId", 200);
                assertFailedIndexResult(indexingResults.get(2), "nonExistingHotel", 404, "Document not found.");
                assertSuccessfulIndexResult(indexingResults.get(3), "3", 201);
                assertSuccessfulIndexResult(indexingResults.get(4), "2", 201);
            });

        StepVerifier.create(client.getDocument(hotel1.hotelId()))
            .assertNext(result -> {
                Hotel actual = result.as(Hotel.class);
                Assert.assertEquals(hotel1, actual);
            })
            .verifyComplete();

        StepVerifier.create(client.getDocument(hotel2.hotelId()))
            .assertNext(result -> {
                Hotel actual = result.as(Hotel.class);
                Assert.assertEquals(hotel2, actual);
            })
            .verifyComplete();

        StepVerifier.create(client.getDocument(hotel3.hotelId()))
            .assertNext(result -> {
                Hotel actual = result.as(Hotel.class);
                Assert.assertEquals(hotel3, actual);
            })
            .verifyComplete();
    }

    @Override
    public void canIndexDynamicDocuments() {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildAsyncClient();

        Document hotel1 = prepareDynamicallyTypedHotel("1");
        Document hotel2 = prepareDynamicallyTypedHotel("2");
        Document hotel3 = prepareDynamicallyTypedHotel("3");
        Document nonExistingHotel = prepareDynamicallyTypedHotel("nonExistingHotel"); // merging with a non existing document
        Document randomHotel = prepareDynamicallyTypedHotel("randomId"); // deleting a non existing document

        IndexBatch<Document> batch = new IndexBatch<Document>()
            .addUploadAction(hotel1)
            .addDeleteAction(randomHotel)
            .addMergeAction(nonExistingHotel)
            .addMergeOrUploadAction(hotel3)
            .addUploadAction(hotel2);

        Mono<DocumentIndexResult> response = client.index(batch);

        StepVerifier.create(response)
            .verifyErrorSatisfies(err -> {
                Assert.assertEquals(IndexBatchException.class, err.getClass());

                List<IndexingResult> results = ((IndexBatchException) err).getIndexingResults();

                assertSuccessfulIndexResult(results.get(0), "1", 201);
                assertSuccessfulIndexResult(results.get(1), "randomId", 200);
                assertFailedIndexResult(results.get(2), "nonExistingHotel", 404, "Document not found.");
                assertSuccessfulIndexResult(results.get(3), "3", 201);
                assertSuccessfulIndexResult(results.get(4), "2", 201);
                Assert.assertEquals(batch.actions().size(), results.size());
            });

        StepVerifier.create(client.getDocument(hotel1.get("HotelId").toString()))
            .expectNext(hotel1)
            .verifyComplete();

        StepVerifier.create(client.getDocument(hotel2.get("HotelId").toString()))
            .expectNext(hotel2)
            .verifyComplete();

        StepVerifier.create(client.getDocument(hotel3.get("HotelId").toString()))
            .expectNext(hotel3)
            .verifyComplete();
    }

    @Override
    public void indexWithInvalidDocumentThrowsException() {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildAsyncClient();

        List<Document> docs = new ArrayList<>();
        docs.add(new Document());
        Mono<DocumentIndexResult> indexResult = client.uploadDocuments(docs);

        StepVerifier
            .create(indexResult)
            .verifyErrorSatisfies(error -> {
                Assert.assertEquals(HttpResponseException.class, error.getClass());
                Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error).response().statusCode());
                Assert.assertTrue(error.getMessage().contains("The request is invalid. Details: actions : 0: Document key cannot be missing or empty."));
            });
    }

    @Override
    public void canUseIndexWithReservedName() {
        Index indexWithReservedName = new Index()
            .withName("prototype")
            .withFields(Collections.singletonList(new Field()
                .withName("ID")
                .withType(DataType.EDM_STRING)
                .withKey(Boolean.TRUE)
            ));

        if (!interceptorManager.isPlaybackMode()) {
            getSearchServiceClient().indexes().create(indexWithReservedName);
        }

        client = getClientBuilder(indexWithReservedName.name()).buildAsyncClient();

        List<Map<String, Object>> docs = new ArrayList<>();

        Map<String, Object> doc = new HashMap<>();
        doc.put("ID", "1");
        docs.add(doc);

        client.uploadDocuments(docs)
            .block();

        StepVerifier
            .create(client.getDocument("1"))
            .assertNext(Assert::assertNotNull)
            .verifyComplete();
    }

    @Override
    public void canRoundtripBoundaryValues() throws Exception {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildAsyncClient();

        List<Hotel> boundaryConditionDocs = getBoundaryValues();

        client.uploadDocuments(boundaryConditionDocs).block();

        waitForIndexing();

        for (Hotel expected : boundaryConditionDocs) {
            StepVerifier.create(client.getDocument(expected.hotelId()))
                .assertNext(d -> {
                    Hotel actual = d.as(Hotel.class);
                    Assert.assertEquals(expected, actual);
                })
                .verifyComplete();
        }

    }

    @Override
    public void dynamicDocumentDateTimesRoundTripAsUtc() throws IOException {
        setupIndexFromJsonFile(BOOKS_INDEX_JSON);
        client = getClientBuilder(BOOKS_INDEX_NAME).buildAsyncClient();

        // Book 1's publish date is in UTC format, and book 2's is unspecified.
        List<Map<String, Object>> books = new ArrayList<>();

        Map<String, Object> book1 = new HashMap<>();
        book1.put(ISBN_FIELD, ISBN1);
        book1.put(PUBLISH_DATE_FIELD, DATE_UTC);
        books.add(book1);

        Map<String, Object> book2 = new HashMap<>();
        book2.put(ISBN_FIELD, ISBN2);
        book2.put(PUBLISH_DATE_FIELD, "2010-06-27T00:00:00-00:00");
        books.add(book2);

        // Upload and retrieve book documents
        client.uploadDocuments(books)
            .block();
        waitForIndexing();

        Mono<Document> actualBook1 = client.getDocument(ISBN1);
        Mono<Document> actualBook2 = client.getDocument(ISBN2);

        // Verify
        StepVerifier
            .create(actualBook1)
            .assertNext(res -> Assert.assertEquals(DATE_UTC, res.get(PUBLISH_DATE_FIELD)))
            .verifyComplete();
        StepVerifier
            .create(actualBook2)
            .assertNext(res -> Assert.assertEquals(DATE_UTC, res.get(PUBLISH_DATE_FIELD)))
            .verifyComplete();
    }

    @Override
    public void mergeDocumentWithoutExistingKeyThrowsIndexingException() throws Exception {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildAsyncClient();

        List<Hotel> hotels = new ArrayList<>();
        hotels.add(prepareStaticallyTypedHotel("1"));

        Mono<DocumentIndexResult> documentIndexResult = client.mergeDocuments(hotels);

        StepVerifier
            .create(documentIndexResult)
            .verifyErrorSatisfies(err -> {
                Assert.assertEquals(IndexBatchException.class, err.getClass());

                List<IndexingResult> indexingResults = ((IndexBatchException) err).getIndexingResults();
                assertFailedIndexResult(indexingResults.get(0), "1", HttpResponseStatus.NOT_FOUND.code(), "Document not found.");
                Assert.assertEquals(1, indexingResults.size());
            });
    }

    @Override
    public void staticallyTypedDateTimesRoundTripAsUtc() throws Exception {
        setupIndexFromJsonFile(BOOKS_INDEX_JSON);
        client = getClientBuilder(BOOKS_INDEX_NAME).buildAsyncClient();

        // Book 1's publish date is in UTC format, and book 2's is unspecified.
        DateFormat dateFormatUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        DateFormat dateFormatUnspecifiedTimezone = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<Book> books = Arrays.asList(
            new Book()
                .ISBN(ISBN1)
                .publishDate(dateFormatUtc.parse(DATE_UTC)),
            new Book()
                .ISBN(ISBN2)
                .publishDate(dateFormatUnspecifiedTimezone.parse("2010-06-27 00:00:00"))
        );
        // Upload and retrieve book documents
        client.uploadDocuments(books).block();
        Mono<Document> actualBook1 = client.getDocument(ISBN1);
        Mono<Document> actualBook2 = client.getDocument(ISBN2);

        // Verify
        StepVerifier
            .create(actualBook1)
            .assertNext(res -> Assert.assertEquals(books.get(0).publishDate(), res.as(Book.class).publishDate()))
            .verifyComplete();
        StepVerifier
            .create(actualBook2)
            .assertNext(res -> Assert.assertEquals(books.get(1).publishDate(), res.as(Book.class).publishDate()))
            .verifyComplete();
    }

    @Override
    public void canMergeStaticallyTypedDocuments() throws ParseException {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildAsyncClient();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        // Define hotels
        Hotel originalDoc = new Hotel()
            .hotelId("1")
            .hotelName("Secret Point Motel")
            .description("The hotel is ideally located on the main commercial artery of the city in the heart of New York. A few minutes away is Time's Square and the historic centre of the city, as well as other places of interest that make New York one of America's most attractive and cosmopolitan cities.")
            .descriptionFr("L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus attractives et cosmopolites de l'Amérique.")
            .category("Boutique")
            .tags(Arrays.asList("pool", "air conditioning", "concierge"))
            .parkingIncluded(false)
            .smokingAllowed(true)
            .lastRenovationDate(dateFormat.parse("2010-06-27T00:00:00Z"))
            .rating(4)
            .location(GeoPoint.create(40.760586, -73.975403))
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
                    .tags(Collections.singletonList("vcr/dvd")),
                new HotelRoom()
                    .description("Budget Room, 1 King Bed (Mountain View)")
                    .descriptionFr("Chambre Économique, 1 très grand lit (Mountain View)")
                    .type("Budget Room")
                    .baseRate(8.09)
                    .bedOptions("1 King Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(Arrays.asList("vcr/dvd", "jacuzzi tub"))
            ));

        // Update category, tags, parking included, rating, and rooms. Erase description, last renovation date, location and address.
        Hotel updatedDoc = new Hotel()
            .hotelId("1")
            .hotelName("Secret Point Motel")
            .description(null)
            .category("Economy")
            .tags(Arrays.asList("pool", "air conditioning"))
            .parkingIncluded(true)
            .lastRenovationDate(null)
            .rating(3)
            .location(null)
            .address(new HotelAddress())
            .rooms(Collections.singletonList(
                new HotelRoom()
                    .description(null)
                    .type("Budget Room")
                    .baseRate(10.5)
                    .bedOptions("1 Queen Bed")
                    .sleepsCount(2)
                    .tags(Arrays.asList("vcr/dvd", "balcony"))
            ));

        // Fields whose values get updated are updated, and whose values get erased remain the same.
        Hotel expectedDoc = new Hotel()
            .hotelId("1")
            .hotelName("Secret Point Motel")
            .description("The hotel is ideally located on the main commercial artery of the city in the heart of New York. A few minutes away is Time's Square and the historic centre of the city, as well as other places of interest that make New York one of America's most attractive and cosmopolitan cities.")
            .descriptionFr("L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus attractives et cosmopolites de l'Amérique.")
            .category("Economy")
            .tags(Arrays.asList("pool", "air conditioning"))
            .parkingIncluded(true)
            .smokingAllowed(true)
            .lastRenovationDate(dateFormat.parse("2010-06-27T00:00:00Z"))
            .rating(3)
            .location(GeoPoint.create(40.760586, -73.975403))
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
                    .tags(Arrays.asList("vcr/dvd", "balcony"))
            ));

        List<Hotel> originalDocs = new ArrayList<>();
        originalDocs.add(originalDoc);

        client.uploadDocuments(originalDocs).block();

        List<Hotel> updatedDocs = new ArrayList<>();
        updatedDocs.add(updatedDoc);
        client.mergeDocuments(updatedDocs).block();

        StepVerifier.create(client.getDocument("1"))
            .assertNext(result -> Assert.assertEquals(expectedDoc, result.as(Hotel.class)))
            .verifyComplete();

        client.mergeDocuments(originalDocs).block();

        // Verify
        StepVerifier.create(client.getDocument("1"))
            .assertNext(result -> Assert.assertEquals(originalDoc, result.as(Hotel.class)))
            .verifyComplete();
    }

    @Override
    public void canDeleteBatchByKeys() {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildAsyncClient();

        client.uploadDocuments(Arrays.asList(
            new Hotel().hotelId("1"),
            new Hotel().hotelId("2")
        )).block();
        waitForIndexing();

        StepVerifier.create(client.getDocumentCount())
            .expectNext(2L)
            .verifyComplete();

        IndexBatch<Hotel> deleteBatch = new IndexBatch<Hotel>()
            .addDeleteAction("HotelId", "1", "2");
        Mono<DocumentIndexResult> documentIndexResult = client.index(deleteBatch);

        StepVerifier.create(documentIndexResult)
            .assertNext(res -> {
                Assert.assertEquals(2, res.results().size());
                assertIndexActionSucceeded("1", res.results().get(0), 200);
                assertIndexActionSucceeded("2", res.results().get(1), 200);
            })
            .verifyComplete();
        waitForIndexing();

        StepVerifier.create(client.getDocumentCount())
            .expectNext(0L)
            .verifyComplete();
    }

    @Override
    public void indexDoesNotThrowWhenDeletingDocumentWithExtraFields() {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildAsyncClient();

        List<Hotel> hotels = new ArrayList<>();
        Hotel hotel = new Hotel()
            .hotelId("1")
            .category("Luxury");
        hotels.add(hotel);

        client.uploadDocuments(hotels).block();
        waitForIndexing();

        StepVerifier.create(client.getDocumentCount())
            .expectNext(1L)
            .verifyComplete();

        hotel.category("ignored");
        Mono<DocumentIndexResult> documentIndexResult = client.deleteDocuments(hotels);

        StepVerifier.create(documentIndexResult)
            .assertNext(res -> {
                Assert.assertEquals(1, res.results().size());
                assertIndexActionSucceeded("1", res.results().get(0), 200);
            })
            .verifyComplete();
        waitForIndexing();

        StepVerifier.create(client.getDocumentCount())
            .expectNext(0L)
            .verifyComplete();
    }

    @Override
    public void indexDoesNotThrowWhenDeletingDynamicDocumentWithExtraFields() {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildAsyncClient();

        List<Document> docs = new ArrayList<>();

        Document document = new Document();
        document.put("HotelId", "1");
        document.put("Category", "Luxury");
        docs.add(document);

        client.uploadDocuments(docs).block();
        waitForIndexing();

        StepVerifier.create(client.getDocumentCount())
            .expectNext(1L)
            .verifyComplete();

        document.put("Category", "ignored");
        Mono<DocumentIndexResult> documentIndexResult = client.deleteDocuments(docs);

        StepVerifier.create(documentIndexResult)
            .assertNext(res -> {
                Assert.assertEquals(1, res.results().size());
                assertIndexActionSucceeded("1", res.results().get(0), 200);
            })
            .verifyComplete();
        waitForIndexing();

        StepVerifier.create(client.getDocumentCount())
            .expectNext(0L)
            .verifyComplete();
    }

    @Override
    public void canSetExplicitNullsInStaticallyTypedDocument() throws ParseException {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildAsyncClient();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        LoudHotel originalDoc = new LoudHotel()
            .HOTELID("1")
            .HOTELNAME("Secret Point Motel")
            .DESCRIPTION("The hotel is ideally located on the main commercial artery of the city in the heart of New York. A few minutes away is Time's Square and the historic centre of the city, as well as other places of interest that make New York one of America's most attractive and cosmopolitan cities.")
            .DESCRIPTIONFRENCH("L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus attractives et cosmopolites de l'Amérique.")
            .CATEGORY("Boutique")
            .TAGS(Arrays.asList("pool", "air conditioning", "concierge"))
            .PARKINGINCLUDED(false)
            .SMOKINGALLOWED(false)
            .LASTRENOVATIONDATE(dateFormat.parse("1970-01-18T-05:00:00Z"))
            .RATING(4)
            .LOCATION(GeoPoint.create(40.760586, -73.975403))
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
                    .tags(Arrays.asList("vcr/dvd")),
                new HotelRoom()
                    .description("Budget Room, 1 King Bed (Mountain View)")
                    .descriptionFr("Chambre Économique, 1 très grand lit (Mountain View)")
                    .type("Budget Room")
                    .baseRate(8.09)
                    .bedOptions("1 King Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(Arrays.asList("vcr/dvd", "jacuzzi tub"))
            ));

        LoudHotel updatedDoc = new LoudHotel()
            .HOTELID("1")
            .DESCRIPTION(null)  // This property has JsonInclude.Include.ALWAYS, so this will null out the field.
            .CATEGORY(null)     // This property doesn't have JsonInclude.Include.ALWAYS, so this should have no effect.
            .TAGS(Arrays.asList("pool", "air conditioning"))
            .PARKINGINCLUDED(true)
            .LASTRENOVATIONDATE(dateFormat.parse("1970-01-18T-05:00:00Z"))
            .RATING(3)
            .LOCATION(null)     // This property has JsonInclude.Include.ALWAYS, so this will null out the field.
            .ADDRESS(new HotelAddress())
            .ROOMS(Arrays.asList(
                new HotelRoom()
                    .description(null)
                    .type("Budget Room")
                    .baseRate(10.5)
                    .smokingAllowed(false)
                    .tags(Arrays.asList("vcr/dvd", "balcony"))
            ));

        LoudHotel expectedDoc = new LoudHotel()
            .HOTELID("1")
            .HOTELNAME("Secret Point Motel")
            .DESCRIPTION(null)
            .DESCRIPTIONFRENCH("L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus attractives et cosmopolites de l'Amérique.")
            .CATEGORY("Boutique")
            .TAGS(Arrays.asList("pool", "air conditioning"))
            .PARKINGINCLUDED(true)
            .SMOKINGALLOWED(false)
            .LASTRENOVATIONDATE(dateFormat.parse("1970-01-18T-05:00:00Z"))
            .RATING(3)
            .LOCATION(null)
            .ADDRESS(new HotelAddress()
                .streetAddress("677 5th Ave")
                .city("New York")
                .stateProvince("NY")
                .country("USA")
                .postalCode("10022")
            ).ROOMS(Arrays.asList(
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
                    .tags(Arrays.asList("vcr/dvd", "balcony"))
            ));

        List<LoudHotel> originalDocs = new ArrayList<>();
        originalDocs.add(originalDoc);
        client.uploadDocuments(originalDocs).block();
        waitForIndexing();

        List<LoudHotel> updatedDocs = new ArrayList<>();
        updatedDocs.add(updatedDoc);
        client.mergeDocuments(updatedDocs).block();
        waitForIndexing();

        Mono<Document> mono = client.getDocument("1");

        StepVerifier.create(mono)
            .expectNextMatches(result -> {
                LoudHotel actual = result.as(LoudHotel.class);
                return actual.equals(expectedDoc);
            })
            .verifyComplete();

        client.uploadDocuments(originalDocs).block();
        waitForIndexing();

        mono = client.getDocument("1");
        StepVerifier.create(mono)
            .expectNextMatches(result -> {
                LoudHotel actual = result.as(LoudHotel.class);
                return actual.equals(originalDoc);
            })
            .verifyComplete();
    }

    @Override
    public void canMergeDynamicDocuments() {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildAsyncClient();

        Document originalDoc = new Document();
        originalDoc.put("HotelId", "1");
        originalDoc.put("HotelName", "Secret Point Motel");
        originalDoc.put("Description", "The hotel is ideally located on the main commercial artery of the city in the heart of New York. A few minutes away is Time's Square and the historic centre of the city, as well as other places of interest that make New York one of America's most attractive and cosmopolitan cities.");
        originalDoc.put("Description_fr", "L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus attractives et cosmopolites de l'Amérique.");
        originalDoc.put("Category", "Boutique");
        originalDoc.put("Tags", Arrays.asList("pool", "air conditioning", "concierge"));
        originalDoc.put("ParkingIncluded", false);
        originalDoc.put("SmokingAllowed", true);
        originalDoc.put("LastRenovationDate", "2010-06-27T00:00:00Z");
        originalDoc.put("Rating", 4);

        Map<String, Object> originalCrsProperties = new LinkedHashMap<>();
        originalCrsProperties.put("name", "EPSG:4326");

        Map<String, Object> originalCrs = new LinkedHashMap<>();
        originalCrs.put("type", "name");
        originalCrs.put("properties", originalCrsProperties);

        Map<String, Object> originalPoint = new LinkedHashMap<>();
        originalPoint.put("type", "Point");
        originalPoint.put("coordinates", Arrays.asList(-73.975403, 40.760586));
        originalPoint.put("crs", originalCrs);
        originalDoc.put("Location", originalPoint);

        Document originalAddress = new Document();
        originalAddress.put("StreetAddress", "677 5th Ave");
        originalAddress.put("City", "New York");
        originalAddress.put("StateProvince", "NY");
        originalAddress.put("PostalCode", "10022");
        originalAddress.put("Country", "USA");
        originalDoc.put("Address", originalAddress);

        Document originalRoom1 = new Document();
        originalRoom1.put("Description", "Budget Room, 1 Queen Bed (Cityside)");
        originalRoom1.put("Description_fr", "Chambre Économique, 1 grand lit (côté ville)");
        originalRoom1.put("Type", "Budget Room");
        originalRoom1.put("BaseRate", 9.69);
        originalRoom1.put("BedOptions", "1 Queen Bed");
        originalRoom1.put("SleepsCount", 2);
        originalRoom1.put("SmokingAllowed", true);
        originalRoom1.put("Tags", Arrays.asList("vcr/dvd"));

        Document originalRoom2 = new Document();
        originalRoom2.put("Description", "Budget Room, 1 King Bed (Mountain View)");
        originalRoom2.put("Description_fr", "Chambre Économique, 1 très grand lit (Mountain View)");
        originalRoom2.put("Type", "Budget Room");
        originalRoom2.put("BaseRate", 8.09);
        originalRoom2.put("BedOptions", "1 King Bed");
        originalRoom2.put("SleepsCount", 2);
        originalRoom2.put("SmokingAllowed", true);
        originalRoom2.put("Tags", Arrays.asList("vcr/dvd", "jacuzzi tub"));

        originalDoc.put("Rooms", Arrays.asList(originalRoom1, originalRoom2));

        Document updatedDoc = new Document();
        updatedDoc.put("HotelId", "1");
        updatedDoc.put("Description", null);
        updatedDoc.put("Category", "Economy");
        updatedDoc.put("Tags", Arrays.asList("pool", "air conditioning"));
        updatedDoc.put("ParkingIncluded", true);
        updatedDoc.put("LastRenovationDate", null);
        updatedDoc.put("Rating", 3);
        updatedDoc.put("Location", null);
        updatedDoc.put("Address", new Document());

        Document updatedRoom1 = new Document();
        updatedRoom1.put("Description", null);
        updatedRoom1.put("Type", "Budget Room");
        updatedRoom1.put("BaseRate", 10.5);
        updatedRoom1.put("BedOptions", "1 Queen Bed");
        updatedRoom1.put("SleepsCount", 2);
        updatedRoom1.put("SmokingAllowed", true);
        updatedRoom1.put("Tags", Arrays.asList("vcr/dvd", "balcony"));
        updatedDoc.put("Rooms", Arrays.asList(updatedRoom1));

        Document expectedDoc = new Document();
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

        List<Document> originalDocs = new ArrayList<>();
        originalDocs.add(originalDoc);
        client.mergeOrUploadDocuments(originalDocs).block();
        waitForIndexing();

        List<Document> updatedDocs = new ArrayList<>();
        updatedDocs.add(updatedDoc);
        client.mergeDocuments(updatedDocs).block();
        waitForIndexing();

        Mono<Document> actualMono = client.getDocument("1");
        StepVerifier.create(actualMono)
            .expectNext(expectedDoc)
            .verifyComplete();

        client.mergeOrUploadDocuments(originalDocs).block();
        waitForIndexing();

        actualMono = client.getDocument("1");
        StepVerifier.create(actualMono)
            .expectNext(originalDoc)
            .verifyComplete();
    }
}
