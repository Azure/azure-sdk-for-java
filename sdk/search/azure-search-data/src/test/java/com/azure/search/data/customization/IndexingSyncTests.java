// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.core.exception.HttpResponseException;
import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.data.customization.models.GeoPoint;
import com.azure.search.data.generated.models.DocumentIndexResult;
import com.azure.search.data.generated.models.IndexBatch;
import com.azure.search.data.generated.models.IndexingResult;
import com.azure.search.test.environment.models.Book;
import com.azure.search.test.environment.models.Hotel;
import com.azure.search.test.environment.models.HotelAddress;
import com.azure.search.test.environment.models.HotelRoom;
import com.azure.search.service.models.DataType;
import com.azure.search.service.models.Field;
import com.azure.search.service.models.Index;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;


public class IndexingSyncTests extends IndexingTestBase {
    private SearchIndexClient client;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Override
    public void countingDocsOfNewIndexGivesZero() {
        Long actual = client.countDocuments();
        Long expected = 0L;

        Assert.assertEquals(expected, actual);
    }

    @Override
    public void indexDoesNotThrowWhenAllActionsSucceed() {
        String expectedHotelId = "1";
        Long expectedHotelCount = 1L;

        Hotel myHotel = new Hotel().hotelId(expectedHotelId);

        List<IndexingResult> result = client.uploadDocument(myHotel).results();
        this.assertIndexActionSucceeded(expectedHotelId, result.get(0), 201);

        waitFor(2);
        Assert.assertEquals(expectedHotelCount, client.countDocuments());
    }

    @Override
    public void canIndexWithPascalCaseFields() {
        String expectedHotelId = "1";
        Long expectedHotelCount = 1L;

        Hotel myHotel =
            new Hotel().hotelId(expectedHotelId).
                hotelName("My Pascal Hotel").
                description("A Great Pascal Description.").
                category("Category Pascal");

        List<IndexingResult> result = client.uploadDocument(myHotel).results();
        this.assertIndexActionSucceeded(expectedHotelId, result.get(0), 201);

        waitFor(2);
        Assert.assertEquals(expectedHotelCount, client.countDocuments());
    }

    @Override
    public void canDeleteBatchByKeys() {
        client.uploadDocuments(Arrays.asList(
            new Hotel().hotelId("1"),
            new Hotel().hotelId("2")
        ));
        waitFor(2);
        Assert.assertEquals(2, client.countDocuments().intValue());

        IndexBatch deleteBatch = new IndexBatchBuilder()
            .delete("HotelId", Arrays.asList("1", "2"))
            .build();

        DocumentIndexResult documentIndexResult = client.index(deleteBatch);
        waitFor(2);

        Assert.assertEquals(2, documentIndexResult.results().size());
        assertIndexActionSucceeded("1", documentIndexResult.results().get(0), 200);
        assertIndexActionSucceeded("2", documentIndexResult.results().get(1), 200);

        Assert.assertEquals(0, client.countDocuments().intValue());
    }

    @Override
    public void indexDoesNotThrowWhenDeletingDocumentWithExtraFields() {
        Hotel document = new Hotel().hotelId("1").category("Luxury");

        client.uploadDocuments(Arrays.asList(document));
        waitFor(2);
        Assert.assertEquals(1, client.countDocuments().intValue());

        document.category("ignored");
        DocumentIndexResult documentIndexResult = client.deleteDocuments(Arrays.asList(document));
        waitFor(2);

        Assert.assertEquals(1, documentIndexResult.results().size());
        assertIndexActionSucceeded("1", documentIndexResult.results().get(0), 200);
        Assert.assertEquals(0, client.countDocuments().intValue());
    }

    @Override
    public void indexDoesNotThrowWhenDeletingDynamicDocumentWithExtraFields() {
        Document document = new Document();
        document.put("HotelId", "1");
        document.put("Category", "Luxury");
        client.uploadDocuments(Arrays.asList(document));

        waitFor(2);
        Assert.assertEquals(1, client.countDocuments().intValue());

        document.put("Category", "ignored");
        DocumentIndexResult documentIndexResult = client.deleteDocuments(Arrays.asList(document));
        waitFor(2);

        Assert.assertEquals(1, documentIndexResult.results().size());
        assertIndexActionSucceeded("1", documentIndexResult.results().get(0), 200);
        Assert.assertEquals(0, client.countDocuments().intValue());
    }

    public void canIndexStaticallyTypedDocuments() throws ParseException {
        JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        jsonApi.configureTimezone();

        Hotel hotel1 = prepareStaticallyTypedHotel("1");
        Hotel hotel2 = prepareStaticallyTypedHotel("2");
        Hotel hotel3 = prepareStaticallyTypedHotel("3");
        Hotel nonExistingHotel = prepareStaticallyTypedHotel("nonExistingHotel"); // merging with a non existing document
        Hotel randomHotel = prepareStaticallyTypedHotel("randomId"); // deleting a non existing document

        IndexBatch indexBatch = new IndexBatchBuilder()
            .upload(hotel1)
            .delete(randomHotel)
            .merge(nonExistingHotel)
            .mergeOrUpload(hotel3)
            .upload(hotel2)
            .build();

        List<IndexingResult> results =  client.index(indexBatch).results();
        Assert.assertEquals(results.size(), indexBatch.actions().size());

        assertSuccessfulIndexResult(results.get(0), "1", 201);
        assertSuccessfulIndexResult(results.get(1), "randomId", 200);
        assertFailedIndexResult(results.get(2), "nonExistingHotel", 404, "Document not found.");
        assertSuccessfulIndexResult(results.get(3), "3", 201);
        assertSuccessfulIndexResult(results.get(4), "2", 201);

        Hotel actualHotel1 = client.getDocument(hotel1.hotelId()).as(Hotel.class);
        Assert.assertEquals(hotel1, actualHotel1);

        Hotel actualHotel2 = client.getDocument(hotel2.hotelId()).as(Hotel.class);
        Assert.assertEquals(hotel2, actualHotel2);

        Hotel actualHotel3 = client.getDocument(hotel3.hotelId()).as(Hotel.class);
        Assert.assertEquals(hotel3, actualHotel3);
    }

    @Override
    public void canIndexDynamicDocuments() {
        JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        jsonApi.configureTimezone();

        Document hotel1 = prepareDynamicallyTypedHotel("1");
        Document hotel2 = prepareDynamicallyTypedHotel("2");
        Document hotel3 = prepareDynamicallyTypedHotel("3");
        Document nonExistingHotel = prepareDynamicallyTypedHotel("nonExistingHotel"); // deleting a non existing document
        Document randomHotel = prepareDynamicallyTypedHotel("randomId"); // deleting a non existing document

        IndexBatch indexBatch = new IndexBatchBuilder()
            .upload(hotel1)
            .delete(randomHotel)
            .merge(nonExistingHotel)
            .mergeOrUpload(hotel3)
            .upload(hotel2)
            .build();

        List<IndexingResult> results =  client.index(indexBatch).results();
        Assert.assertEquals(results.size(), indexBatch.actions().size());

        assertSuccessfulIndexResult(results.get(0), "1", 201);
        assertSuccessfulIndexResult(results.get(1), "randomId", 200);
        assertFailedIndexResult(results.get(2), "nonExistingHotel", 404, "Document not found.");
        assertSuccessfulIndexResult(results.get(3), "3", 201);
        assertSuccessfulIndexResult(results.get(4), "2", 201);

        Document actualHotel1 = client.getDocument(hotel1.get("HotelId").toString());
        Assert.assertEquals(hotel1, actualHotel1);

        Document actualHotel2 = client.getDocument(hotel2.get("HotelId").toString());
        Assert.assertEquals(hotel2, actualHotel2);

        Document actualHotel3 = client.getDocument(hotel3.get("HotelId").toString());
        Assert.assertEquals(hotel3, actualHotel3);
    }

    @Override
    public void indexWithInvalidDocumentThrowsException() throws Exception {
        thrown.expect(HttpResponseException.class);
        thrown.expectMessage("The request is invalid. Details: actions : 0: Document key cannot be missing or empty.");

        Document toUpload = new Document();
        client.uploadDocument(toUpload);
    }

    @Override
    public void canUseIndexWithReservedName() {
        Index indexWithReservedName = new Index()
            .withName("prototype")
            .withFields(Collections.singletonList(new Field().withName("ID").withType(DataType.EDM_STRING).withKey(Boolean.TRUE)));

        if (!interceptorManager.isPlaybackMode()) {
            searchServiceClient.indexes().create(indexWithReservedName);
        }
        Map<String, Object> indexData = new HashMap<>();
        indexData.put("ID", "1");

        client.setIndexName(indexWithReservedName.name())
            .uploadDocument(indexData);

        Document actual = client.getDocument("1");
        Assert.assertNotNull(actual);
    }

    @Override
    public void canRoundtripBoundaryValues() throws Exception {
        JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        jsonApi.configureTimezone();

        List<Hotel> boundaryConditionDocs = getBoundaryValues();

        client.uploadDocuments(boundaryConditionDocs);

        // Wait 2 secs to allow index request to finish
        Thread.sleep(2000);

        for (Hotel expected : boundaryConditionDocs) {
            Document doc = client.getDocument(expected.hotelId());
            Hotel actual = doc.as(Hotel.class);
            Assert.assertEquals(expected, actual);
        }

    }

    @Override
    public void dynamicDocumentDateTimesRoundTripAsUtc() throws Exception {
        // Book 1's publish date is in UTC format, and book 2's is unspecified.
        List<HashMap<String, Object>> books = Arrays.asList(
            new HashMap<String, Object>() {
                {
                    put(ISBN_FIELD, ISBN1);
                    put(PUBLISH_DATE_FIELD, DATE_UTC);
                }
            },
            new HashMap<String, Object>() {
                {
                    put(ISBN_FIELD, ISBN2);
                    put(PUBLISH_DATE_FIELD, "2010-06-27T00:00:00-00:00");
                }
            }
        );

        // Create 'books' index
        Reader indexData = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(BOOKS_INDEX_JSON));
        Index index = new ObjectMapper().readValue(indexData, Index.class);
        if (!interceptorManager.isPlaybackMode()) {
            searchServiceClient.indexes().create(index);
        }

        // Upload and retrieve book documents
        uploadDocuments(client, BOOKS_INDEX_NAME, books);
        Document actualBook1 = client.getDocument(ISBN1);
        Document actualBook2 = client.getDocument(ISBN2);

        // Verify
        Assert.assertEquals(DATE_UTC, actualBook1.get(PUBLISH_DATE_FIELD));
        Assert.assertEquals(DATE_UTC, actualBook2.get(PUBLISH_DATE_FIELD));
    }

    @Override
    public void staticallyTypedDateTimesRoundTripAsUtc() throws Exception {
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

        // Create 'books' index
        Reader indexData = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(BOOKS_INDEX_JSON));
        Index index = new ObjectMapper().readValue(indexData, Index.class);
        if (!interceptorManager.isPlaybackMode()) {
            searchServiceClient.indexes().create(index);
        }

        // Upload and retrieve book documents
        uploadDocuments(client, BOOKS_INDEX_NAME, books);
        Document actualBook1 = client.getDocument(ISBN1);
        Document actualBook2 = client.getDocument(ISBN2);

        // Verify
        Assert.assertEquals(books.get(0).publishDate(), actualBook1.as(Book.class).publishDate());
        Assert.assertEquals(books.get(1).publishDate(), actualBook2.as(Book.class).publishDate());
    }

    @Override
    public void canMergeStaticallyTypedDocuments() throws ParseException {
        // Define commonly used values
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String hotelId = "1";
        String hotelName = "Secret Point Motel";
        String description = "The hotel is ideally located on the main commercial artery of the city in the heart of New York. A few minutes away is Time's Square and the historic centre of the city, as well as other places of interest that make New York one of America's most attractive and cosmopolitan cities.";
        String descriptionFr = "L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus attractives et cosmopolites de l'Amérique.";
        GeoPoint location = GeoPoint.createWithDefaultCrs(40.760586, -73.975403);
        HotelAddress address = new HotelAddress()
            .streetAddress("677 5th Ave")
            .city("New York")
            .stateProvince("NY")
            .country("USA")
            .postalCode("10022");
        Date lastRenovationDate = dateFormat.parse("2010-06-27T00:00:00Z");
        List<HotelRoom> updatedRooms = Arrays.asList(
            new HotelRoom()
                .description(null)
                .type("Budget Room")
                .baseRate(10.5)
                .bedOptions("1 Queen Bed")
                .sleepsCount(2)
                .tags(Arrays.asList("vcr/dvd",
                    "balcony"))
        );
        String updatedHotelCategory = "Economy";
        List<String> updatedTags = Arrays.asList("pool",
            "air conditioning");

        // Define hotels
        Hotel originalDoc = new Hotel()
            .hotelId(hotelId)
            .hotelName(hotelName)
            .description(description)
            .descriptionFr(descriptionFr)
            .category("Boutique")
            .tags(Arrays.asList("pool",
                "air conditioning",
                "concierge"))
            .parkingIncluded(false)
            .smokingAllowed(true)
            .lastRenovationDate(lastRenovationDate)
            .rating(4)
            .location(location)
            .address(address)
            .rooms(Arrays.asList(
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
                    .tags(Arrays.asList("vcr/dvd",
                        "jacuzzi tub"))
            ));
        // Update category, tags, parking included, rating, and rooms. Erase description, last renovation date, location and address.
        Hotel updatedDoc = new Hotel()
            .hotelId(hotelId)
            .hotelName(hotelName)
            .description(null)
            .category(updatedHotelCategory)
            .tags(updatedTags)
            .parkingIncluded(true)
            .lastRenovationDate(null)
            .rating(3)
            .location(null)
            .address(new HotelAddress())
            .rooms(updatedRooms);
        // Fields whose values get updated are updated, and whose values get erased remain the same.
        Hotel expectedDoc = new Hotel()
            .hotelId(hotelId)
            .hotelName(hotelName)
            .description(description)
            .descriptionFr(descriptionFr)
            .category(updatedHotelCategory)
            .tags(updatedTags)
            .parkingIncluded(true)
            .smokingAllowed(true)
            .lastRenovationDate(lastRenovationDate)
            .rating(3)
            .location(location)
            .address(address)
            .rooms(updatedRooms);

        client.uploadDocument(originalDoc);

        client.mergeDocument(updatedDoc);
        Assert.assertEquals(expectedDoc, client.getDocument(hotelId).as(Hotel.class));

        client.mergeDocument(originalDoc);
        Assert.assertEquals(originalDoc, client.getDocument(hotelId).as(Hotel.class));
    }

    @Override
    public void mergeDocumentWithoutExistingKeyThrowsIndexingException() throws Exception {
        Hotel hotel = prepareStaticallyTypedHotel("1");

        List<IndexingResult> indexingResults = client.mergeDocument(hotel).results();
        assertFailedIndexResult(indexingResults.get(0), "1", HttpResponseStatus.NOT_FOUND.code(), "Document not found.");
        Assert.assertEquals(1, indexingResults.size());
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildClient();
    }
}
