// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.http.rest.Response;
import com.azure.core.models.GeoPoint;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.test.environment.models.Hotel;
import com.azure.search.documents.test.environment.models.HotelAddress;
import com.azure.search.documents.test.environment.models.HotelRoom;
import com.azure.search.documents.test.environment.models.ModelWithPrimitiveCollections;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;

import static com.azure.search.documents.TestHelpers.assertMapEquals;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.createSharedSearchIndexClient;
import static com.azure.search.documents.TestHelpers.setupSharedIndex;
import static com.azure.search.documents.TestHelpers.uploadDocument;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LookupTests extends SearchTestBase {
    private static final String HOTEL_INDEX_NAME = "azsearch-lookup-shared-hotel-instance";
    private static final String TYPE_INDEX_NAME = "azsearch-lookup-shared-type-instance";

    private static SearchIndexClient searchIndexClient;

    @BeforeAll
    public static void setupClass() {
        TestBase.setupClass();

        if (TEST_MODE == TestMode.PLAYBACK) {
            return;
        }

        searchIndexClient = setupSharedIndex(HOTEL_INDEX_NAME, HOTELS_TESTS_INDEX_DATA_JSON, null);
        setupIndexWithDataTypes();
    }

    @AfterAll
    public static void cleanupClass() {
        if (TEST_MODE != TestMode.PLAYBACK) {
            searchIndexClient.deleteIndex(HOTEL_INDEX_NAME);
            searchIndexClient.deleteIndex(TYPE_INDEX_NAME);
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
    public void canGetStaticallyTypedDocumentSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        Hotel expected = prepareExpectedHotel(getRandomDocumentKey());
        uploadDocument(client, expected);

        Hotel actual = client.getDocument(expected.hotelId(), Hotel.class);
        assertObjectEquals(expected, actual, true, "boundingBox");
    }

    @Test
    public void canGetStaticallyTypedDocumentAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        Hotel expected = prepareExpectedHotel(getRandomDocumentKey());
        uploadDocument(asyncClient, expected);

        getAndValidateDocumentAsync(asyncClient, expected.hotelId(), Hotel.class, expected,
            (ignored, actual) -> assertObjectEquals(expected, actual, true, "boundingBox"));
    }

    @Test
    public void canGetStaticallyTypedDocumentWithNullOrEmptyValuesSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        Hotel expected = prepareEmptyHotel(getRandomDocumentKey());
        uploadDocument(client, expected);

        Hotel actual = client.getDocument(expected.hotelId(), Hotel.class);
        assertObjectEquals(expected, actual, true);
    }

    @Test
    public void canGetStaticallyTypedDocumentWithNullOrEmptyValuesAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        Hotel expected = prepareEmptyHotel(getRandomDocumentKey());
        uploadDocument(asyncClient, expected);

        getAndValidateDocumentAsync(asyncClient, expected.hotelId(), Hotel.class, expected,
            (ignored, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void canGetStaticallyTypedDocumentWithPascalCaseFieldsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        Hotel expected = preparePascalCaseFieldsHotel(getRandomDocumentKey());
        uploadDocument(client, expected);

        Hotel actual = client.getDocument(expected.hotelId(), Hotel.class);
        assertObjectEquals(expected, actual, true);
    }

    @Test
    public void canGetStaticallyTypedDocumentWithPascalCaseFieldsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        Hotel expected = preparePascalCaseFieldsHotel(getRandomDocumentKey());
        uploadDocument(asyncClient, expected);

        getAndValidateDocumentAsync(asyncClient, expected.hotelId(), Hotel.class, expected,
            (ignored, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void canRoundTripStaticallyTypedPrimitiveCollectionsSync() {
        SearchClient client = getClient(TYPE_INDEX_NAME);

        ModelWithPrimitiveCollections expected = preparePrimitivesModel(getRandomDocumentKey());
        uploadDocument(client, expected);

        ModelWithPrimitiveCollections actual = client.getDocument(expected.key(), ModelWithPrimitiveCollections.class);
        assertObjectEquals(expected, actual, true, "boundingBox");
    }

    @Test
    public void canRoundTripStaticallyTypedPrimitiveCollectionsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(TYPE_INDEX_NAME);

        ModelWithPrimitiveCollections expected = preparePrimitivesModel(getRandomDocumentKey());
        uploadDocument(asyncClient, expected);

        getAndValidateDocumentAsync(asyncClient, expected.key(), ModelWithPrimitiveCollections.class, expected,
            (ignored, actual) -> assertObjectEquals(expected, actual, true, "boundingBox"));
    }

    @Test
    public void getStaticallyTypedDocumentSetsUnselectedFieldsToNullSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        Hotel indexedDoc = prepareSelectedFieldsHotel(getRandomDocumentKey());
        Hotel expected = new Hotel()
            .hotelName("Countryside Hotel")
            .description("Save up to 50% off traditional hotels.  Free WiFi, great location near downtown, "
                + "full kitchen, washer & dryer, 24/7 support, bowling alley, fitness center and more.")
            .address(new HotelAddress().city("Durham"))
            .rooms(Arrays.asList(new HotelRoom().baseRate(2.44), new HotelRoom().baseRate(7.69)));

        uploadDocument(client, indexedDoc);

        List<String> selectedFields = Arrays.asList("Description", "HotelName", "Address/City", "Rooms/BaseRate");
        Response<Hotel> actual = client.getDocumentWithResponse(indexedDoc.hotelId(), Hotel.class,
            selectedFields, Context.NONE);
        assertObjectEquals(expected, actual.getValue(), true);
    }

    @Test
    public void getStaticallyTypedDocumentSetsUnselectedFieldsToNullAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        Hotel indexedDoc = prepareSelectedFieldsHotel(getRandomDocumentKey());
        Hotel expected = new Hotel()
            .hotelName("Countryside Hotel")
            .description("Save up to 50% off traditional hotels.  Free WiFi, great location near downtown, "
                + "full kitchen, washer & dryer, 24/7 support, bowling alley, fitness center and more.")
            .address(new HotelAddress().city("Durham"))
            .rooms(Arrays.asList(new HotelRoom().baseRate(2.44), new HotelRoom().baseRate(7.69)));

        uploadDocument(asyncClient, indexedDoc);

        List<String> selectedFields = Arrays.asList("Description", "HotelName", "Address/City", "Rooms/BaseRate");
        getAndValidateDocumentAsync(asyncClient, indexedDoc.hotelId(), Hotel.class, selectedFields, expected,
            (ignored, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void canGetDynamicDocumentWithNullOrEmptyValuesSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", hotelId);
        expectedDoc.put("HotelName", null);
        expectedDoc.put("Tags", Collections.emptyList());
        expectedDoc.put("ParkingIncluded", null);
        expectedDoc.put("LastRenovationDate", null);
        expectedDoc.put("Rating", null);
        expectedDoc.put("Location", null);
        expectedDoc.put("Address", null);

        SearchDocument room = new SearchDocument();
        room.put("BaseRate", null);
        room.put("BedOptions", null);
        room.put("SleepsCount", null);
        room.put("SmokingAllowed", null);
        room.put("Tags", Collections.emptyList());

        expectedDoc.put("Rooms", Collections.singletonList(room));

        uploadDocument(client, expectedDoc);
        // Select only the fields set in the test case.
        List<String> selectedFields = Arrays.asList("HotelId", "HotelName", "Tags", "ParkingIncluded",
            "LastRenovationDate", "Rating", "Location", "Address", "Rooms/BaseRate", "Rooms/BedOptions",
            "Rooms/SleepsCount", "Rooms/SmokingAllowed", "Rooms/Tags");

        Response<SearchDocument> response = client.getDocumentWithResponse(hotelId, SearchDocument.class,
            selectedFields, Context.NONE);
        assertObjectEquals(expectedDoc, response.getValue(), true);
    }

    @Test
    public void canGetDynamicDocumentWithNullOrEmptyValuesAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", hotelId);
        expectedDoc.put("HotelName", null);
        expectedDoc.put("Tags", Collections.emptyList());
        expectedDoc.put("ParkingIncluded", null);
        expectedDoc.put("LastRenovationDate", null);
        expectedDoc.put("Rating", null);
        expectedDoc.put("Location", null);
        expectedDoc.put("Address", null);

        SearchDocument room = new SearchDocument();
        room.put("BaseRate", null);
        room.put("BedOptions", null);
        room.put("SleepsCount", null);
        room.put("SmokingAllowed", null);
        room.put("Tags", Collections.emptyList());

        expectedDoc.put("Rooms", Collections.singletonList(room));

        uploadDocument(asyncClient, expectedDoc);
        // Select only the fields set in the test case.
        List<String> selectedFields = Arrays.asList("HotelId", "HotelName", "Tags", "ParkingIncluded",
            "LastRenovationDate", "Rating", "Location", "Address", "Rooms/BaseRate", "Rooms/BedOptions",
            "Rooms/SleepsCount", "Rooms/SmokingAllowed", "Rooms/Tags");

        getAndValidateDocumentAsync(asyncClient, hotelId, SearchDocument.class, selectedFields, expectedDoc,
            (ignored, actual) -> assertObjectEquals(expectedDoc, actual, true));
    }

    @Test
    public void getDynamicDocumentWithEmptyObjectsReturnsObjectsFullOfNullsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        SearchDocument originalDoc = new SearchDocument();
        originalDoc.put("HotelId", hotelId);
        originalDoc.put("Address", new SearchDocument());

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", hotelId);

        SearchDocument address = new SearchDocument();
        address.put("StreetAddress", null);
        address.put("City", null);
        address.put("StateProvince", null);
        address.put("Country", null);
        address.put("PostalCode", null);
        expectedDoc.put("Address", address);

        uploadDocument(client, originalDoc);
        // Select only the fields set in the test case.
        List<String> selectedFields = Arrays.asList("HotelId", "Address");

        Response<SearchDocument> response = client.getDocumentWithResponse(hotelId, SearchDocument.class,
            selectedFields, Context.NONE);
        assertObjectEquals(expectedDoc, response.getValue(), true);
    }

    @Test
    public void getDynamicDocumentWithEmptyObjectsReturnsObjectsFullOfNullsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        SearchDocument originalDoc = new SearchDocument();
        originalDoc.put("HotelId", hotelId);
        originalDoc.put("Address", new SearchDocument());

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", hotelId);

        SearchDocument address = new SearchDocument();
        address.put("StreetAddress", null);
        address.put("City", null);
        address.put("StateProvince", null);
        address.put("Country", null);
        address.put("PostalCode", null);
        expectedDoc.put("Address", address);

        uploadDocument(asyncClient, originalDoc);
        // Select only the fields set in the test case.
        List<String> selectedFields = Arrays.asList("HotelId", "Address");

        getAndValidateDocumentAsync(asyncClient, hotelId, SearchDocument.class, selectedFields, expectedDoc,
            (ignored, actual) -> assertObjectEquals(expectedDoc, actual, true));
    }

    @Test
    public void emptyDynamicallyTypedPrimitiveCollectionsRoundTripAsObjectArraysSync() {
        SearchClient client = getClient(TYPE_INDEX_NAME);

        String docKey = getRandomDocumentKey();

        SearchDocument originalDoc = new SearchDocument();
        originalDoc.put("Key", docKey);
        originalDoc.put("Dates", new Object[]{});
        originalDoc.put("Doubles", new Double[]{});
        originalDoc.put("Bools", new boolean[]{});
        originalDoc.put("Longs", new Long[]{});
        originalDoc.put("Strings", new String[]{});
        originalDoc.put("Ints", new int[]{});
        originalDoc.put("Points", new Object[]{});

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("Key", docKey);
        expectedDoc.put("Doubles", Collections.emptyList());
        expectedDoc.put("Bools", Collections.emptyList());
        expectedDoc.put("Longs", Collections.emptyList());
        expectedDoc.put("Strings", Collections.emptyList());
        expectedDoc.put("Ints", Collections.emptyList());
        expectedDoc.put("Points", Collections.emptyList());
        expectedDoc.put("Dates", Collections.emptyList());

        uploadDocument(client, originalDoc);

        SearchDocument actualDoc = client.getDocument(docKey, SearchDocument.class);
        assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void emptyDynamicallyTypedPrimitiveCollectionsRoundTripAsObjectArraysAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(TYPE_INDEX_NAME);

        String docKey = getRandomDocumentKey();

        SearchDocument originalDoc = new SearchDocument();
        originalDoc.put("Key", docKey);
        originalDoc.put("Dates", new Object[]{});
        originalDoc.put("Doubles", new Double[]{});
        originalDoc.put("Bools", new boolean[]{});
        originalDoc.put("Longs", new Long[]{});
        originalDoc.put("Strings", new String[]{});
        originalDoc.put("Ints", new int[]{});
        originalDoc.put("Points", new Object[]{});

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("Key", docKey);
        expectedDoc.put("Doubles", Collections.emptyList());
        expectedDoc.put("Bools", Collections.emptyList());
        expectedDoc.put("Longs", Collections.emptyList());
        expectedDoc.put("Strings", Collections.emptyList());
        expectedDoc.put("Ints", Collections.emptyList());
        expectedDoc.put("Points", Collections.emptyList());
        expectedDoc.put("Dates", Collections.emptyList());

        uploadDocument(asyncClient, originalDoc);

        getAndValidateDocumentAsync(asyncClient, docKey, SearchDocument.class, expectedDoc, Assertions::assertEquals);
    }

    @Test
    public void emptyDynamicObjectsInCollectionExpandedOnGetWhenCollectionFieldSelectedSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        SearchDocument originalDoc = new SearchDocument();
        originalDoc.put("HotelId", hotelId);

        SearchDocument originalRoom = new SearchDocument();
        originalRoom.put("BaseRate", null);
        originalRoom.put("BedOptions", null);
        originalRoom.put("SleepsCount", null);
        originalRoom.put("SmokingAllowed", null);
        originalRoom.put("Tags", Collections.emptyList());
        originalDoc.put("Rooms", Arrays.asList(new SearchDocument(), originalRoom));

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", hotelId);

        SearchDocument expectedRoom1 = new SearchDocument();
        expectedRoom1.put("Description", null);
        expectedRoom1.put("Description_fr", null);
        expectedRoom1.put("Type", null);
        expectedRoom1.put("BaseRate", null);
        expectedRoom1.put("BedOptions", null);
        expectedRoom1.put("SleepsCount", null);
        expectedRoom1.put("SmokingAllowed", null);
        expectedRoom1.put("Tags", Collections.emptyList());

        SearchDocument expectedRoom2 = new SearchDocument();
        expectedRoom2.put("Description", null);
        expectedRoom2.put("Description_fr", null);
        expectedRoom2.put("Type", null);
        expectedRoom2.put("BaseRate", null);
        expectedRoom2.put("BedOptions", null);
        expectedRoom2.put("SleepsCount", null);
        expectedRoom2.put("SmokingAllowed", null);
        expectedRoom2.put("Tags", Collections.emptyList());

        expectedDoc.put("Rooms", Arrays.asList(expectedRoom1, expectedRoom2));

        uploadDocument(client, originalDoc);
        List<String> selectedFields = Arrays.asList("HotelId", "Rooms");

        Response<SearchDocument> response = client.getDocumentWithResponse(hotelId, SearchDocument.class,
            selectedFields, Context.NONE);
        assertObjectEquals(expectedDoc, response.getValue(), true);
    }

    @Test
    public void emptyDynamicObjectsInCollectionExpandedOnGetWhenCollectionFieldSelectedAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        SearchDocument originalDoc = new SearchDocument();
        originalDoc.put("HotelId", hotelId);

        SearchDocument originalRoom = new SearchDocument();
        originalRoom.put("BaseRate", null);
        originalRoom.put("BedOptions", null);
        originalRoom.put("SleepsCount", null);
        originalRoom.put("SmokingAllowed", null);
        originalRoom.put("Tags", Collections.emptyList());
        originalDoc.put("Rooms", Arrays.asList(new SearchDocument(), originalRoom));

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", hotelId);

        SearchDocument expectedRoom1 = new SearchDocument();
        expectedRoom1.put("Description", null);
        expectedRoom1.put("Description_fr", null);
        expectedRoom1.put("Type", null);
        expectedRoom1.put("BaseRate", null);
        expectedRoom1.put("BedOptions", null);
        expectedRoom1.put("SleepsCount", null);
        expectedRoom1.put("SmokingAllowed", null);
        expectedRoom1.put("Tags", Collections.emptyList());

        SearchDocument expectedRoom2 = new SearchDocument();
        expectedRoom2.put("Description", null);
        expectedRoom2.put("Description_fr", null);
        expectedRoom2.put("Type", null);
        expectedRoom2.put("BaseRate", null);
        expectedRoom2.put("BedOptions", null);
        expectedRoom2.put("SleepsCount", null);
        expectedRoom2.put("SmokingAllowed", null);
        expectedRoom2.put("Tags", Collections.emptyList());

        expectedDoc.put("Rooms", Arrays.asList(expectedRoom1, expectedRoom2));

        uploadDocument(asyncClient, originalDoc);
        List<String> selectedFields = Arrays.asList("HotelId", "Rooms");

        getAndValidateDocumentAsync(asyncClient, hotelId, SearchDocument.class, selectedFields, expectedDoc,
            (expected, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void getDynamicDocumentCannotAlwaysDetermineCorrectTypeSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        SearchDocument indexedDoc = new SearchDocument();
        indexedDoc.put("HotelId", hotelId);
        indexedDoc.put("LastRenovationDate", "2017-01-13T14:03:00.7552052-07:00");
        // Test that we don't confuse Geo-JSON & complex types.
        indexedDoc.put("Location", new GeoPoint(-73.975403, 40.760586));
        indexedDoc.put("Rooms", Collections.singletonList(new SearchDocument(Collections.singletonMap("BaseRate", NaN))));

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", hotelId);
        expectedDoc.put("LastRenovationDate", OffsetDateTime.of(2017, 1, 13, 21, 3, 0, 755000000, ZoneOffset.UTC));
        expectedDoc.put("Location", new GeoPoint(-73.975403, 40.760586));
        expectedDoc.put("Rooms", Collections.singletonList(new SearchDocument(Collections.singletonMap("BaseRate", "NaN"))));

        client.indexDocuments(new IndexDocumentsBatch<>().addUploadActions(Collections.singletonList(indexedDoc)));

        // Select only the fields set in the test case.
        List<String> selectedFields = Arrays.asList("HotelId", "LastRenovationDate", "Location", "Rooms/BaseRate");
        assertMapEquals(expectedDoc, client.getDocumentWithResponse(hotelId, SearchDocument.class, selectedFields,
            Context.NONE).getValue(), true, "boundingBox", "properties");
    }

    @Test
    public void getDynamicDocumentCannotAlwaysDetermineCorrectTypeAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        SearchDocument indexedDoc = new SearchDocument();
        indexedDoc.put("HotelId", hotelId);
        indexedDoc.put("LastRenovationDate", "2017-01-13T14:03:00.7552052-07:00");
        // Test that we don't confuse Geo-JSON & complex types.
        indexedDoc.put("Location", new GeoPoint(-73.975403, 40.760586));
        indexedDoc.put("Rooms", Collections.singletonList(new SearchDocument(Collections.singletonMap("BaseRate", NaN))));

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", hotelId);
        expectedDoc.put("LastRenovationDate", OffsetDateTime.of(2017, 1, 13, 21, 3, 0, 755000000, ZoneOffset.UTC));
        expectedDoc.put("Location", new GeoPoint(-73.975403, 40.760586));
        expectedDoc.put("Rooms", Collections.singletonList(new SearchDocument(Collections.singletonMap("BaseRate", "NaN"))));

        asyncClient.indexDocuments(new IndexDocumentsBatch<>().addUploadActions(Collections.singletonList(indexedDoc)))
            .block();

        // Select only the fields set in the test case.
        List<String> selectedFields = Arrays.asList("HotelId", "LastRenovationDate", "Location", "Rooms/BaseRate");
        getAndValidateDocumentAsync(asyncClient, hotelId, SearchDocument.class, selectedFields, expectedDoc,
            (expected, actual) -> assertMapEquals(expected, actual, true, "boundingBox", "properties"));
    }

    @Test
    public void canGetDocumentWithBase64EncodedKeySync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String complexKey = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4, 5});

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", complexKey);

        client.indexDocuments(new IndexDocumentsBatch<>().addUploadActions(Collections.singletonList(expectedDoc)));
        assertEquals(client.getDocumentWithResponse(complexKey, SearchDocument.class,
            new ArrayList<>(expectedDoc.keySet()), Context.NONE).getValue(), expectedDoc);
    }

    @Test
    public void canGetDocumentWithBase64EncodedKeyAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String complexKey = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4, 5});

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", complexKey);

        asyncClient.indexDocuments(new IndexDocumentsBatch<>().addUploadActions(Collections.singletonList(expectedDoc)))
            .block();

        getAndValidateDocumentAsync(asyncClient, complexKey, SearchDocument.class, new ArrayList<>(expectedDoc.keySet()),
            expectedDoc, Assertions::assertEquals);
    }

    @Test
    public void roundTrippingDateTimeOffsetNormalizesToUtcSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        SearchDocument indexedDoc = new SearchDocument();
        indexedDoc.put("HotelId", hotelId);
        indexedDoc.put("LastRenovationDate", OffsetDateTime.parse("2010-06-27T00:00:00-08:00"));

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", hotelId);

        expectedDoc.put("LastRenovationDate", OffsetDateTime.parse("2010-06-27T08:00Z"));

        client.indexDocuments(new IndexDocumentsBatch<>().addUploadActions(Collections.singletonList(indexedDoc)));
        SearchDocument actualDoc = client.getDocumentWithResponse(hotelId, SearchDocument.class,
            new ArrayList<>(expectedDoc.keySet()), Context.NONE).getValue();
        assertMapEquals(expectedDoc, actualDoc, false);
    }

    @Test
    public void roundTrippingDateTimeOffsetNormalizesToUtcAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        SearchDocument indexedDoc = new SearchDocument();
        indexedDoc.put("HotelId", hotelId);
        indexedDoc.put("LastRenovationDate", OffsetDateTime.parse("2010-06-27T00:00:00-08:00"));

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", hotelId);

        expectedDoc.put("LastRenovationDate", OffsetDateTime.parse("2010-06-27T08:00Z"));

        asyncClient.indexDocuments(new IndexDocumentsBatch<>().addUploadActions(Collections.singletonList(indexedDoc)))
            .block();

        getAndValidateDocumentAsync(asyncClient, hotelId, SearchDocument.class, new ArrayList<>(expectedDoc.keySet()),
            expectedDoc, (expected, actual) -> assertMapEquals(expected, actual, false));
    }

    @Test
    public void emptyDynamicObjectsOmittedFromCollectionOnGetWhenSubFieldsSelectedSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        SearchDocument originalDoc = new SearchDocument();
        originalDoc.put("HotelId", hotelId);

        SearchDocument originalRoom = new SearchDocument();
        originalRoom.put("BaseRate", null);
        originalRoom.put("BedOptions", null);
        originalRoom.put("SleepsCount", null);
        originalRoom.put("SmokingAllowed", null);
        originalRoom.put("Tags", Collections.emptyList());
        originalDoc.put("Rooms", Arrays.asList(new SearchDocument(), originalRoom));

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", hotelId);

        SearchDocument expectedRoom = new SearchDocument();
        expectedRoom.put("BaseRate", null);
        expectedRoom.put("BedOptions", null);
        expectedRoom.put("SleepsCount", null);
        expectedRoom.put("SmokingAllowed", null);
        expectedRoom.put("Tags", Collections.emptyList());
        expectedDoc.put("Rooms", Collections.singletonList(expectedRoom));

        uploadDocument(client, originalDoc);
        List<String> selectedFields = Arrays.asList("HotelId", "Rooms/BaseRate", "Rooms/BedOptions",
            "Rooms/SleepsCount", "Rooms/SmokingAllowed", "Rooms/Tags");

        Response<SearchDocument> response = client.getDocumentWithResponse(hotelId, SearchDocument.class,
            selectedFields, Context.NONE);
        assertObjectEquals(expectedDoc, response.getValue(), true);
    }

    @Test
    public void emptyDynamicObjectsOmittedFromCollectionOnGetWhenSubFieldsSelectedAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        SearchDocument originalDoc = new SearchDocument();
        originalDoc.put("HotelId", hotelId);

        SearchDocument originalRoom = new SearchDocument();
        originalRoom.put("BaseRate", null);
        originalRoom.put("BedOptions", null);
        originalRoom.put("SleepsCount", null);
        originalRoom.put("SmokingAllowed", null);
        originalRoom.put("Tags", Collections.emptyList());
        originalDoc.put("Rooms", Arrays.asList(new SearchDocument(), originalRoom));

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", hotelId);

        SearchDocument expectedRoom = new SearchDocument();
        expectedRoom.put("BaseRate", null);
        expectedRoom.put("BedOptions", null);
        expectedRoom.put("SleepsCount", null);
        expectedRoom.put("SmokingAllowed", null);
        expectedRoom.put("Tags", Collections.emptyList());
        expectedDoc.put("Rooms", Collections.singletonList(expectedRoom));

        uploadDocument(asyncClient, originalDoc);
        List<String> selectedFields = Arrays.asList("HotelId", "Rooms/BaseRate", "Rooms/BedOptions",
            "Rooms/SleepsCount", "Rooms/SmokingAllowed", "Rooms/Tags");

        getAndValidateDocumentAsync(asyncClient, hotelId, SearchDocument.class, selectedFields, expectedDoc,
            (expected, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void dynamicallyTypedPrimitiveCollectionsDoNotAllRoundTripCorrectlySync() {
        SearchClient client = getClient(TYPE_INDEX_NAME);

        String docKey = getRandomDocumentKey();
        OffsetDateTime dateTime = OffsetDateTime.parse("2019-08-13T14:30:00Z");
        GeoPoint geoPoint = new GeoPoint(100.0, 1.0);

        SearchDocument indexedDoc = new SearchDocument();
        indexedDoc.put("Key", docKey);
        indexedDoc.put("Dates", new OffsetDateTime[]{dateTime});
        indexedDoc.put("Doubles", new Double[]{0.0, 5.8, POSITIVE_INFINITY, NEGATIVE_INFINITY, NaN});
        indexedDoc.put("Bools", new Boolean[]{true, false});
        indexedDoc.put("Longs", new Long[]{9999999999999999L, 832372345832523L});
        indexedDoc.put("Strings", new String[]{"hello", "bye"});
        indexedDoc.put("Ints", new int[]{1, 2, 3, 4, -13, 5, 0});
        indexedDoc.put("Points", new GeoPoint[]{geoPoint});

        // This is the expected document when querying the document later
        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("Key", docKey);
        expectedDoc.put("Doubles", Arrays.asList(0.0, 5.8, "INF", "-INF", "NaN"));
        expectedDoc.put("Bools", Arrays.asList(true, false));
        expectedDoc.put("Longs", Arrays.asList(9999999999999999L, 832372345832523L));
        expectedDoc.put("Strings", Arrays.asList("hello", "bye"));
        expectedDoc.put("Ints", Arrays.asList(1, 2, 3, 4, -13, 5, 0));
        //expectedDoc.put("Points", Collections.singletonList(geoPoint));
        expectedDoc.put("Dates", Collections.singletonList(dateTime));

        uploadDocument(client, indexedDoc);

        SearchDocument actualDoc = client.getDocument(docKey, SearchDocument.class);

        assertMapEquals(expectedDoc, actualDoc, true, "properties");
    }

    @Test
    public void dynamicallyTypedPrimitiveCollectionsDoNotAllRoundTripCorrectlyAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(TYPE_INDEX_NAME);

        String docKey = getRandomDocumentKey();
        OffsetDateTime dateTime = OffsetDateTime.parse("2019-08-13T14:30:00Z");
        GeoPoint geoPoint = new GeoPoint(100.0, 1.0);

        SearchDocument indexedDoc = new SearchDocument();
        indexedDoc.put("Key", docKey);
        indexedDoc.put("Dates", new OffsetDateTime[]{dateTime});
        indexedDoc.put("Doubles", new Double[]{0.0, 5.8, POSITIVE_INFINITY, NEGATIVE_INFINITY, NaN});
        indexedDoc.put("Bools", new Boolean[]{true, false});
        indexedDoc.put("Longs", new Long[]{9999999999999999L, 832372345832523L});
        indexedDoc.put("Strings", new String[]{"hello", "bye"});
        indexedDoc.put("Ints", new int[]{1, 2, 3, 4, -13, 5, 0});
        indexedDoc.put("Points", new GeoPoint[]{geoPoint});

        // This is the expected document when querying the document later
        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("Key", docKey);
        expectedDoc.put("Doubles", Arrays.asList(0.0, 5.8, "INF", "-INF", "NaN"));
        expectedDoc.put("Bools", Arrays.asList(true, false));
        expectedDoc.put("Longs", Arrays.asList(9999999999999999L, 832372345832523L));
        expectedDoc.put("Strings", Arrays.asList("hello", "bye"));
        expectedDoc.put("Ints", Arrays.asList(1, 2, 3, 4, -13, 5, 0));
        //expectedDoc.put("Points", Collections.singletonList(geoPoint));
        expectedDoc.put("Dates", Collections.singletonList(dateTime));

        uploadDocument(asyncClient, indexedDoc);

        getAndValidateDocumentAsync(asyncClient, docKey, SearchDocument.class, expectedDoc,
            (expected, actual) -> assertMapEquals(expected, actual, true, "properties"));
    }

    @SuppressWarnings({"deprecation", "UseOfObsoleteDateTimeApi"})
    static Hotel prepareExpectedHotel(String key) {
        Date expectDate = Date.from(Instant.ofEpochMilli(1277582400000L));
        return new Hotel().hotelId(key)
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
            .lastRenovationDate(new Date(expectDate.getYear(),
                expectDate.getMonth(), expectDate.getDate(), expectDate.getHours(),
                expectDate.getMinutes(), expectDate.getSeconds()))
            .rating(5)
            .location(new GeoPoint(-122.131577, 47.678581))
            .rooms(new ArrayList<>());
    }

    private static <T> void getAndValidateDocumentAsync(SearchAsyncClient asyncClient, String key, Class<T> type,
        T expected, BiConsumer<T, T> comparator) {
        StepVerifier.create(asyncClient.getDocument(key, type))
            .assertNext(actual -> comparator.accept(expected, actual))
            .verifyComplete();
    }

    private static <T> void getAndValidateDocumentAsync(SearchAsyncClient asyncClient, String key, Class<T> type,
        List<String> selectedFields, T expected, BiConsumer<T, T> comparator) {
        StepVerifier.create(asyncClient.getDocumentWithResponse(key, type, selectedFields))
            .assertNext(actual -> comparator.accept(expected, actual.getValue()))
            .verifyComplete();
    }

    static Hotel prepareEmptyHotel(String key) {
        return new Hotel().hotelId(key)
            .tags(new ArrayList<>())
            .rooms(Collections.singletonList(new HotelRoom().tags(new String[0])));
    }

    static Hotel preparePascalCaseFieldsHotel(String key) {
        return new Hotel()
            .hotelId(key)
            .hotelName("Lord of the Rings")
            .description("J.R.R")
            .descriptionFr("Tolkien");
    }

    @SuppressWarnings({"deprecation", "UseOfObsoleteDateTimeApi"})
    static Hotel prepareSelectedFieldsHotel(String key) {
        // Since Date doesn't have time zone information to make this test durable against time zones create the Date
        // from an OffsetDateTime.
        OffsetDateTime dateTime = OffsetDateTime.parse("2010-06-26T17:00:00.000+00:00")
            .atZoneSameInstant(ZoneId.systemDefault())
            .toOffsetDateTime();

        return new Hotel()
            .hotelId(key)
            .hotelName("Countryside Hotel")
            .description("Save up to 50% off traditional hotels.  Free WiFi, great location near downtown, full kitchen, washer & dryer, 24/7 support, bowling alley, fitness center and more.")
            .descriptionFr("Économisez jusqu'à 50% sur les hôtels traditionnels.  WiFi gratuit, très bien situé près du centre-ville, cuisine complète, laveuse & sécheuse, support 24/7, bowling, centre de fitness et plus encore.")
            .category("Budget")
            .tags(Arrays.asList("24-hour front desk service", "coffee in lobby", "restaurant"))
            .parkingIncluded(false)
            .smokingAllowed(true)
            .lastRenovationDate(new Date(dateTime.getYear() - 1900, dateTime.getMonth().ordinal(), dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute()))
            .rating(3)
            .location(new GeoPoint(-78.940483, 35.904160))
            .address(new HotelAddress().streetAddress("6910 Fayetteville Rd").city("Durham").stateProvince("NC").country("USA").postalCode("27713"))
            .rooms(Arrays.asList(
                new HotelRoom()
                    .description("Suite, 1 King Bed (Amenities)")
                    .descriptionFr("Suite, 1 très grand lit (Services)")
                    .type("Suite")
                    .baseRate(2.44)
                    .bedOptions("1 King Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(new String[]{"coffee maker"}),
                new HotelRoom()
                    .description("Budget Room, 1 Queen Bed (Amenities)")
                    .descriptionFr("Chambre Économique, 1 grand lit (Services)")
                    .type("Budget Room")
                    .baseRate(7.69)
                    .bedOptions("1 Queen Bed")
                    .sleepsCount(2)
                    .smokingAllowed(false)
                    .tags(new String[]{"coffee maker"})));
    }

    static ModelWithPrimitiveCollections preparePrimitivesModel(String key) {
        return new ModelWithPrimitiveCollections()
            .key(key)
            .bools(new Boolean[]{true, false})
            .dates(new OffsetDateTime[]{
                OffsetDateTime.parse("2019-04-14T14:24:00Z"),
                OffsetDateTime.parse("1999-12-31T23:59:59Z")})
            .doubles(new Double[]{NEGATIVE_INFINITY, 0.0, 2.78, NaN, 3.25, POSITIVE_INFINITY})
            .ints(new int[]{1, 2, 3, 4, -13, 5, 0})
            .longs(new Long[]{-9_999_999_999_999_999L, 832_372_345_832_523L})
            .points(new GeoPoint[]{new GeoPoint(-67.0, 49.0), new GeoPoint(21.0, 47.0)})
            .strings(new String[]{"hello", "2019-04-14T14:56:00-07:00"});
    }

    static void setupIndexWithDataTypes() {
        SearchIndex index = new SearchIndex(TYPE_INDEX_NAME)
            .setFields(Arrays.asList(
                new SearchField("Key", SearchFieldDataType.STRING)
                    .setKey(true)
                    .setHidden(false),
                new SearchField("Bools", SearchFieldDataType.collection(SearchFieldDataType.BOOLEAN))
                    .setHidden(false),
                new SearchField("Dates", SearchFieldDataType.collection(SearchFieldDataType.DATE_TIME_OFFSET))
                    .setHidden(false),
                new SearchField("Doubles", SearchFieldDataType.collection(SearchFieldDataType.DOUBLE))
                    .setHidden(false),
                new SearchField("Points", SearchFieldDataType.collection(SearchFieldDataType.GEOGRAPHY_POINT))
                    .setHidden(false),
                new SearchField("Ints", SearchFieldDataType.collection(SearchFieldDataType.INT32))
                    .setHidden(false),
                new SearchField("Longs", SearchFieldDataType.collection(SearchFieldDataType.INT64))
                    .setHidden(false),
                new SearchField("Strings", SearchFieldDataType.collection(SearchFieldDataType.STRING))
                    .setHidden(false)
            ));

        createSharedSearchIndexClient().createOrUpdateIndex(index);
    }
}
