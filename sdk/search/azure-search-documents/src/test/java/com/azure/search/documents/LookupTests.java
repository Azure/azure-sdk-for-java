// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.models.GeoPoint;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.json.JsonReader;
import com.azure.json.ReadValueCallback;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.models.IndexActionType;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.models.LookupDocument;
import com.azure.search.documents.testingmodels.Hotel;
import com.azure.search.documents.testingmodels.HotelAddress;
import com.azure.search.documents.testingmodels.HotelRoom;
import com.azure.search.documents.testingmodels.ModelWithPrimitiveCollections;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static com.azure.search.documents.TestHelpers.assertMapEquals;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static com.azure.search.documents.TestHelpers.convertFromMapStringObject;
import static com.azure.search.documents.TestHelpers.createIndexAction;
import static com.azure.search.documents.TestHelpers.createSharedSearchIndexClient;
import static com.azure.search.documents.TestHelpers.setupSharedIndex;
import static com.azure.search.documents.TestHelpers.uploadDocument;
import static com.azure.search.documents.TestHelpers.uploadDocumentRaw;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;

@Execution(ExecutionMode.CONCURRENT)
public class LookupTests extends SearchTestBase {
    private static final String HOTEL_INDEX_NAME = "azsearch-lookup-shared-hotel-instance";
    private static final String TYPE_INDEX_NAME = "azsearch-lookup-shared-type-instance";

    private static SearchIndexClient searchIndexClient;

    @BeforeAll
    public static void setupClass() {
        TestProxyTestBase.setupClass();

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

        getAndValidateDocument(client, expected.hotelId(), Hotel::fromJson, expected,
            (ignored, actual) -> assertObjectEquals(expected, actual, true, "boundingBox"));
    }

    @Test
    public void canGetStaticallyTypedDocumentAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        Hotel expected = prepareExpectedHotel(getRandomDocumentKey());
        uploadDocument(asyncClient, expected);

        getAndValidateDocumentAsync(asyncClient, expected.hotelId(), Hotel::fromJson, expected,
            (ignored, actual) -> assertObjectEquals(expected, actual, true, "boundingBox"));
    }

    @Test
    public void canGetStaticallyTypedDocumentWithNullOrEmptyValuesSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        Hotel expected = prepareEmptyHotel(getRandomDocumentKey());
        uploadDocument(client, expected);

        getAndValidateDocument(client, expected.hotelId(), Hotel::fromJson, expected,
            (ignored, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void canGetStaticallyTypedDocumentWithNullOrEmptyValuesAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        Hotel expected = prepareEmptyHotel(getRandomDocumentKey());
        uploadDocument(asyncClient, expected);

        getAndValidateDocumentAsync(asyncClient, expected.hotelId(), Hotel::fromJson, expected,
            (ignored, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void canGetStaticallyTypedDocumentWithPascalCaseFieldsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        Hotel expected = preparePascalCaseFieldsHotel(getRandomDocumentKey());
        uploadDocument(client, expected);

        getAndValidateDocument(client, expected.hotelId(), Hotel::fromJson, expected,
            (ignored, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void canGetStaticallyTypedDocumentWithPascalCaseFieldsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        Hotel expected = preparePascalCaseFieldsHotel(getRandomDocumentKey());
        uploadDocument(asyncClient, expected);

        getAndValidateDocumentAsync(asyncClient, expected.hotelId(), Hotel::fromJson, expected,
            (ignored, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void canRoundTripStaticallyTypedPrimitiveCollectionsSync() {
        SearchClient client = getClient(TYPE_INDEX_NAME);

        ModelWithPrimitiveCollections expected = preparePrimitivesModel(getRandomDocumentKey());
        uploadDocument(client, expected);

        getAndValidateDocument(client, expected.key(), ModelWithPrimitiveCollections::fromJson, expected,
            (ignored, actual) -> assertObjectEquals(expected, actual, true, "boundingBox"));
    }

    @Test
    public void canRoundTripStaticallyTypedPrimitiveCollectionsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(TYPE_INDEX_NAME);

        ModelWithPrimitiveCollections expected = preparePrimitivesModel(getRandomDocumentKey());
        uploadDocument(asyncClient, expected);

        getAndValidateDocumentAsync(asyncClient, expected.key(), ModelWithPrimitiveCollections::fromJson, expected,
            (ignored, actual) -> assertObjectEquals(expected, actual, true, "boundingBox"));
    }

    @Test
    public void getStaticallyTypedDocumentSetsUnselectedFieldsToNullSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        Hotel indexedDoc = prepareSelectedFieldsHotel(getRandomDocumentKey());
        Hotel expected = new Hotel().hotelName("Countryside Hotel")
            .description("Save up to 50% off traditional hotels.  Free WiFi, great location near downtown, "
                + "full kitchen, washer & dryer, 24/7 support, bowling alley, fitness center and more.")
            .address(new HotelAddress().city("Durham"))
            .rooms(Arrays.asList(new HotelRoom().baseRate(2.44), new HotelRoom().baseRate(7.69)));

        uploadDocument(client, indexedDoc);

        List<String> selectedFields = Arrays.asList("Description", "HotelName", "Address/City", "Rooms/BaseRate");
        getAndValidateDocument(client, indexedDoc.hotelId(), Hotel::fromJson, selectedFields, expected,
            (ignored, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void getStaticallyTypedDocumentSetsUnselectedFieldsToNullAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        Hotel indexedDoc = prepareSelectedFieldsHotel(getRandomDocumentKey());
        Hotel expected = new Hotel().hotelName("Countryside Hotel")
            .description("Save up to 50% off traditional hotels.  Free WiFi, great location near downtown, "
                + "full kitchen, washer & dryer, 24/7 support, bowling alley, fitness center and more.")
            .address(new HotelAddress().city("Durham"))
            .rooms(Arrays.asList(new HotelRoom().baseRate(2.44), new HotelRoom().baseRate(7.69)));

        uploadDocument(asyncClient, indexedDoc);

        List<String> selectedFields = Arrays.asList("Description", "HotelName", "Address/City", "Rooms/BaseRate");
        getAndValidateDocumentAsync(asyncClient, indexedDoc.hotelId(), Hotel::fromJson, selectedFields, expected,
            (ignored, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void canGetDynamicDocumentWithNullOrEmptyValuesSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("HotelId", hotelId);
        expectedDoc.put("HotelName", null);
        expectedDoc.put("Tags", Collections.emptyList());
        expectedDoc.put("ParkingIncluded", null);
        expectedDoc.put("LastRenovationDate", null);
        expectedDoc.put("Rating", null);
        expectedDoc.put("Location", null);
        expectedDoc.put("Address", null);

        Map<String, Object> room = new LinkedHashMap<>();
        room.put("BaseRate", null);
        room.put("BedOptions", null);
        room.put("SleepsCount", null);
        room.put("SmokingAllowed", null);
        room.put("Tags", Collections.emptyList());

        expectedDoc.put("Rooms", Collections.singletonList(room));

        uploadDocumentRaw(client, expectedDoc);
        // Select only the fields set in the test case.
        List<String> selectedFields = Arrays.asList("HotelId", "HotelName", "Tags", "ParkingIncluded",
            "LastRenovationDate", "Rating", "Location", "Address", "Rooms/BaseRate", "Rooms/BedOptions",
            "Rooms/SleepsCount", "Rooms/SmokingAllowed", "Rooms/Tags");

        getAndValidateDocument(client, hotelId, selectedFields, expectedDoc,
            (expected, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void canGetDynamicDocumentWithNullOrEmptyValuesAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("HotelId", hotelId);
        expectedDoc.put("HotelName", null);
        expectedDoc.put("Tags", Collections.emptyList());
        expectedDoc.put("ParkingIncluded", null);
        expectedDoc.put("LastRenovationDate", null);
        expectedDoc.put("Rating", null);
        expectedDoc.put("Location", null);
        expectedDoc.put("Address", null);

        Map<String, Object> room = new LinkedHashMap<>();
        room.put("BaseRate", null);
        room.put("BedOptions", null);
        room.put("SleepsCount", null);
        room.put("SmokingAllowed", null);
        room.put("Tags", Collections.emptyList());

        expectedDoc.put("Rooms", Collections.singletonList(room));

        uploadDocumentRaw(asyncClient, expectedDoc);
        // Select only the fields set in the test case.
        List<String> selectedFields = Arrays.asList("HotelId", "HotelName", "Tags", "ParkingIncluded",
            "LastRenovationDate", "Rating", "Location", "Address", "Rooms/BaseRate", "Rooms/BedOptions",
            "Rooms/SleepsCount", "Rooms/SmokingAllowed", "Rooms/Tags");

        getAndValidateDocumentAsync(asyncClient, hotelId, selectedFields, expectedDoc,
            (expected, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void getDynamicDocumentWithEmptyObjectsReturnsObjectsFullOfNullsSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Map<String, Object> originalDoc = new LinkedHashMap<>();
        originalDoc.put("HotelId", hotelId);
        originalDoc.put("Address", new LinkedHashMap<String, Object>());

        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("HotelId", hotelId);

        Map<String, Object> address = new LinkedHashMap<>();
        address.put("StreetAddress", null);
        address.put("City", null);
        address.put("StateProvince", null);
        address.put("Country", null);
        address.put("PostalCode", null);
        expectedDoc.put("Address", address);

        uploadDocumentRaw(client, originalDoc);
        // Select only the fields set in the test case.
        List<String> selectedFields = Arrays.asList("HotelId", "Address");

        getAndValidateDocument(client, hotelId, selectedFields, expectedDoc,
            (expected, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void getDynamicDocumentWithEmptyObjectsReturnsObjectsFullOfNullsAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Map<String, Object> originalDoc = new LinkedHashMap<>();
        originalDoc.put("HotelId", hotelId);
        originalDoc.put("Address", new LinkedHashMap<String, Object>());

        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("HotelId", hotelId);

        Map<String, Object> address = new LinkedHashMap<>();
        address.put("StreetAddress", null);
        address.put("City", null);
        address.put("StateProvince", null);
        address.put("Country", null);
        address.put("PostalCode", null);
        expectedDoc.put("Address", address);

        uploadDocumentRaw(asyncClient, originalDoc);
        // Select only the fields set in the test case.
        List<String> selectedFields = Arrays.asList("HotelId", "Address");

        getAndValidateDocumentAsync(asyncClient, hotelId, selectedFields, expectedDoc,
            (expected, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void emptyDynamicallyTypedPrimitiveCollectionsRoundTripAsObjectArraysSync() {
        SearchClient client = getClient(TYPE_INDEX_NAME);

        String docKey = getRandomDocumentKey();

        Map<String, Object> originalDoc = new LinkedHashMap<>();
        originalDoc.put("Key", docKey);
        originalDoc.put("Dates", new Object[0]);
        originalDoc.put("Doubles", new Double[0]);
        originalDoc.put("Bools", new Boolean[0]);
        originalDoc.put("Longs", new Long[0]);
        originalDoc.put("Strings", new String[0]);
        originalDoc.put("Ints", new Integer[0]);
        originalDoc.put("Points", new Object[0]);

        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("Key", docKey);
        expectedDoc.put("Doubles", Collections.emptyList());
        expectedDoc.put("Bools", Collections.emptyList());
        expectedDoc.put("Longs", Collections.emptyList());
        expectedDoc.put("Strings", Collections.emptyList());
        expectedDoc.put("Ints", Collections.emptyList());
        expectedDoc.put("Points", Collections.emptyList());
        expectedDoc.put("Dates", Collections.emptyList());

        uploadDocumentRaw(client, originalDoc);

        getAndValidateDocument(client, docKey, expectedDoc, Assertions::assertEquals);
    }

    @Test
    public void emptyDynamicallyTypedPrimitiveCollectionsRoundTripAsObjectArraysAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(TYPE_INDEX_NAME);

        String docKey = getRandomDocumentKey();

        Map<String, Object> originalDoc = new LinkedHashMap<>();
        originalDoc.put("Key", docKey);
        originalDoc.put("Dates", new Object[0]);
        originalDoc.put("Doubles", new Double[0]);
        originalDoc.put("Bools", new Boolean[0]);
        originalDoc.put("Longs", new Long[0]);
        originalDoc.put("Strings", new String[0]);
        originalDoc.put("Ints", new Integer[0]);
        originalDoc.put("Points", new Object[0]);

        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("Key", docKey);
        expectedDoc.put("Doubles", Collections.emptyList());
        expectedDoc.put("Bools", Collections.emptyList());
        expectedDoc.put("Longs", Collections.emptyList());
        expectedDoc.put("Strings", Collections.emptyList());
        expectedDoc.put("Ints", Collections.emptyList());
        expectedDoc.put("Points", Collections.emptyList());
        expectedDoc.put("Dates", Collections.emptyList());

        uploadDocumentRaw(asyncClient, originalDoc);

        getAndValidateDocumentAsync(asyncClient, docKey, expectedDoc, Assertions::assertEquals);
    }

    @Test
    public void emptyDynamicObjectsInCollectionExpandedOnGetWhenCollectionFieldSelectedSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Map<String, Object> originalDoc = new LinkedHashMap<>();
        originalDoc.put("HotelId", hotelId);

        Map<String, Object> originalRoom = new LinkedHashMap<>();
        originalRoom.put("BaseRate", null);
        originalRoom.put("BedOptions", null);
        originalRoom.put("SleepsCount", null);
        originalRoom.put("SmokingAllowed", null);
        originalRoom.put("Tags", Collections.emptyList());
        originalDoc.put("Rooms", Arrays.asList(new LinkedHashMap<String, Object>(), originalRoom));

        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("HotelId", hotelId);

        Map<String, Object> expectedRoom1 = new LinkedHashMap<>();
        expectedRoom1.put("Description", null);
        expectedRoom1.put("Description_fr", null);
        expectedRoom1.put("Type", null);
        expectedRoom1.put("BaseRate", null);
        expectedRoom1.put("BedOptions", null);
        expectedRoom1.put("SleepsCount", null);
        expectedRoom1.put("SmokingAllowed", null);
        expectedRoom1.put("Tags", Collections.emptyList());

        Map<String, Object> expectedRoom2 = new LinkedHashMap<>();
        expectedRoom2.put("Description", null);
        expectedRoom2.put("Description_fr", null);
        expectedRoom2.put("Type", null);
        expectedRoom2.put("BaseRate", null);
        expectedRoom2.put("BedOptions", null);
        expectedRoom2.put("SleepsCount", null);
        expectedRoom2.put("SmokingAllowed", null);
        expectedRoom2.put("Tags", Collections.emptyList());

        expectedDoc.put("Rooms", Arrays.asList(expectedRoom1, expectedRoom2));

        uploadDocumentRaw(client, originalDoc);
        List<String> selectedFields = Arrays.asList("HotelId", "Rooms");

        getAndValidateDocument(client, hotelId, selectedFields, expectedDoc,
            (expected, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void emptyDynamicObjectsInCollectionExpandedOnGetWhenCollectionFieldSelectedAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Map<String, Object> originalDoc = new LinkedHashMap<>();
        originalDoc.put("HotelId", hotelId);

        Map<String, Object> originalRoom = new LinkedHashMap<>();
        originalRoom.put("BaseRate", null);
        originalRoom.put("BedOptions", null);
        originalRoom.put("SleepsCount", null);
        originalRoom.put("SmokingAllowed", null);
        originalRoom.put("Tags", Collections.emptyList());
        originalDoc.put("Rooms", Arrays.asList(new LinkedHashMap<String, Object>(), originalRoom));

        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("HotelId", hotelId);

        Map<String, Object> expectedRoom1 = new LinkedHashMap<>();
        expectedRoom1.put("Description", null);
        expectedRoom1.put("Description_fr", null);
        expectedRoom1.put("Type", null);
        expectedRoom1.put("BaseRate", null);
        expectedRoom1.put("BedOptions", null);
        expectedRoom1.put("SleepsCount", null);
        expectedRoom1.put("SmokingAllowed", null);
        expectedRoom1.put("Tags", Collections.emptyList());

        Map<String, Object> expectedRoom2 = new LinkedHashMap<>();
        expectedRoom2.put("Description", null);
        expectedRoom2.put("Description_fr", null);
        expectedRoom2.put("Type", null);
        expectedRoom2.put("BaseRate", null);
        expectedRoom2.put("BedOptions", null);
        expectedRoom2.put("SleepsCount", null);
        expectedRoom2.put("SmokingAllowed", null);
        expectedRoom2.put("Tags", Collections.emptyList());

        expectedDoc.put("Rooms", Arrays.asList(expectedRoom1, expectedRoom2));

        uploadDocumentRaw(asyncClient, originalDoc);
        List<String> selectedFields = Arrays.asList("HotelId", "Rooms");

        getAndValidateDocumentAsync(asyncClient, hotelId, selectedFields, expectedDoc,
            (expected, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void getDynamicDocumentCannotAlwaysDetermineCorrectTypeSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Map<String, Object> indexedDoc = new LinkedHashMap<>();
        indexedDoc.put("HotelId", hotelId);
        indexedDoc.put("LastRenovationDate", "2017-01-13T14:03:00.7552052-07:00");
        // Test that we don't confuse Geo-JSON & complex types.
        indexedDoc.put("Location", new GeoPoint(-73.975403, 40.760586));
        indexedDoc.put("Rooms", Collections.singletonList(Collections.singletonMap("BaseRate", NaN)));

        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("HotelId", hotelId);
        expectedDoc.put("LastRenovationDate", OffsetDateTime.of(2017, 1, 13, 21, 3, 0, 755000000, ZoneOffset.UTC));
        expectedDoc.put("Location", new GeoPoint(-73.975403, 40.760586));
        expectedDoc.put("Rooms", Collections.singletonList(Collections.singletonMap("BaseRate", "NaN")));

        client.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, indexedDoc)));

        // Select only the fields set in the test case.
        List<String> selectedFields = Arrays.asList("HotelId", "LastRenovationDate", "Location", "Rooms/BaseRate");
        getAndValidateDocument(client, hotelId, selectedFields, expectedDoc,
            (expected, actual) -> assertMapEquals(expected, actual, true, "boundingBox", "properties"));
    }

    @Test
    public void getDynamicDocumentCannotAlwaysDetermineCorrectTypeAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Map<String, Object> indexedDoc = new LinkedHashMap<>();
        indexedDoc.put("HotelId", hotelId);
        indexedDoc.put("LastRenovationDate", "2017-01-13T14:03:00.7552052-07:00");
        // Test that we don't confuse Geo-JSON & complex types.
        indexedDoc.put("Location", new GeoPoint(-73.975403, 40.760586));
        indexedDoc.put("Rooms", Collections.singletonList(Collections.singletonMap("BaseRate", NaN)));

        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("HotelId", hotelId);
        expectedDoc.put("LastRenovationDate", OffsetDateTime.of(2017, 1, 13, 21, 3, 0, 755000000, ZoneOffset.UTC));
        expectedDoc.put("Location", new GeoPoint(-73.975403, 40.760586));
        expectedDoc.put("Rooms", Collections.singletonList(Collections.singletonMap("BaseRate", "NaN")));

        asyncClient.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, indexedDoc)))
            .block();

        // Select only the fields set in the test case.
        List<String> selectedFields = Arrays.asList("HotelId", "LastRenovationDate", "Location", "Rooms/BaseRate");
        getAndValidateDocumentAsync(asyncClient, hotelId, selectedFields, expectedDoc,
            (expected, actual) -> assertMapEquals(expected, actual, true, "boundingBox", "properties"));
    }

    @Test
    public void canGetDocumentWithBase64EncodedKeySync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String complexKey = Base64.getEncoder().encodeToString(new byte[] { 1, 2, 3, 4, 5 });

        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("HotelId", complexKey);

        client.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, expectedDoc)));
        getAndValidateDocument(client, complexKey, expectedDoc.keySet(), expectedDoc, Assertions::assertEquals);
    }

    @Test
    public void canGetDocumentWithBase64EncodedKeyAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String complexKey = Base64.getEncoder().encodeToString(new byte[] { 1, 2, 3, 4, 5 });

        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("HotelId", complexKey);

        asyncClient.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, expectedDoc)))
            .block();

        getAndValidateDocumentAsync(asyncClient, complexKey, expectedDoc.keySet(), expectedDoc,
            Assertions::assertEquals);
    }

    @Test
    public void roundTrippingDateTimeOffsetNormalizesToUtcSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Map<String, Object> indexedDoc = new LinkedHashMap<>();
        indexedDoc.put("HotelId", hotelId);
        indexedDoc.put("LastRenovationDate", OffsetDateTime.parse("2010-06-27T00:00:00-08:00"));

        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("HotelId", hotelId);

        expectedDoc.put("LastRenovationDate", OffsetDateTime.parse("2010-06-27T08:00Z"));

        client.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, indexedDoc)));
        getAndValidateDocument(client, hotelId, expectedDoc.keySet(), expectedDoc,
            (expected, actual) -> assertMapEquals(expected, actual, false));
    }

    @Test
    public void roundTrippingDateTimeOffsetNormalizesToUtcAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Map<String, Object> indexedDoc = new LinkedHashMap<>();
        indexedDoc.put("HotelId", hotelId);
        indexedDoc.put("LastRenovationDate", OffsetDateTime.parse("2010-06-27T00:00:00-08:00"));

        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("HotelId", hotelId);

        expectedDoc.put("LastRenovationDate", OffsetDateTime.parse("2010-06-27T08:00Z"));

        asyncClient.indexDocuments(new IndexDocumentsBatch(createIndexAction(IndexActionType.UPLOAD, indexedDoc)))
            .block();

        getAndValidateDocumentAsync(asyncClient, hotelId, expectedDoc.keySet(), expectedDoc,
            (expected, actual) -> assertMapEquals(expected, actual, false));
    }

    @Test
    public void emptyDynamicObjectsOmittedFromCollectionOnGetWhenSubFieldsSelectedSync() {
        SearchClient client = getClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Map<String, Object> originalDoc = new LinkedHashMap<>();
        originalDoc.put("HotelId", hotelId);

        Map<String, Object> originalRoom = new LinkedHashMap<>();
        originalRoom.put("BaseRate", null);
        originalRoom.put("BedOptions", null);
        originalRoom.put("SleepsCount", null);
        originalRoom.put("SmokingAllowed", null);
        originalRoom.put("Tags", Collections.emptyList());
        originalDoc.put("Rooms", Arrays.asList(new LinkedHashMap<String, Object>(), originalRoom));

        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("HotelId", hotelId);

        Map<String, Object> expectedRoom = new LinkedHashMap<>();
        expectedRoom.put("BaseRate", null);
        expectedRoom.put("BedOptions", null);
        expectedRoom.put("SleepsCount", null);
        expectedRoom.put("SmokingAllowed", null);
        expectedRoom.put("Tags", Collections.emptyList());
        expectedDoc.put("Rooms", Collections.singletonList(expectedRoom));

        uploadDocumentRaw(client, originalDoc);
        List<String> selectedFields = Arrays.asList("HotelId", "Rooms/BaseRate", "Rooms/BedOptions",
            "Rooms/SleepsCount", "Rooms/SmokingAllowed", "Rooms/Tags");

        getAndValidateDocument(client, hotelId, selectedFields, expectedDoc,
            (expected, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void emptyDynamicObjectsOmittedFromCollectionOnGetWhenSubFieldsSelectedAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(HOTEL_INDEX_NAME);

        String hotelId = getRandomDocumentKey();
        Map<String, Object> originalDoc = new LinkedHashMap<>();
        originalDoc.put("HotelId", hotelId);

        Map<String, Object> originalRoom = new LinkedHashMap<>();
        originalRoom.put("BaseRate", null);
        originalRoom.put("BedOptions", null);
        originalRoom.put("SleepsCount", null);
        originalRoom.put("SmokingAllowed", null);
        originalRoom.put("Tags", Collections.emptyList());
        originalDoc.put("Rooms", Arrays.asList(new LinkedHashMap<String, Object>(), originalRoom));

        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("HotelId", hotelId);

        Map<String, Object> expectedRoom = new LinkedHashMap<>();
        expectedRoom.put("BaseRate", null);
        expectedRoom.put("BedOptions", null);
        expectedRoom.put("SleepsCount", null);
        expectedRoom.put("SmokingAllowed", null);
        expectedRoom.put("Tags", Collections.emptyList());
        expectedDoc.put("Rooms", Collections.singletonList(expectedRoom));

        uploadDocumentRaw(asyncClient, originalDoc);
        List<String> selectedFields = Arrays.asList("HotelId", "Rooms/BaseRate", "Rooms/BedOptions",
            "Rooms/SleepsCount", "Rooms/SmokingAllowed", "Rooms/Tags");

        getAndValidateDocumentAsync(asyncClient, hotelId, selectedFields, expectedDoc,
            (expected, actual) -> assertObjectEquals(expected, actual, true));
    }

    @Test
    public void dynamicallyTypedPrimitiveCollectionsDoNotAllRoundTripCorrectlySync() {
        SearchClient client = getClient(TYPE_INDEX_NAME);

        String docKey = getRandomDocumentKey();
        OffsetDateTime dateTime = OffsetDateTime.parse("2019-08-13T14:30:00Z");
        GeoPoint geoPoint = new GeoPoint(100.0, 1.0);

        Map<String, Object> indexedDoc = new LinkedHashMap<>();
        indexedDoc.put("Key", docKey);
        indexedDoc.put("Dates", new OffsetDateTime[] { dateTime });
        indexedDoc.put("Doubles", new Double[] { 0.0, 5.8, POSITIVE_INFINITY, NEGATIVE_INFINITY, NaN });
        indexedDoc.put("Bools", new Boolean[] { true, false });
        indexedDoc.put("Longs", new Long[] { 9999999999999999L, 832372345832523L });
        indexedDoc.put("Strings", new String[] { "hello", "bye" });
        indexedDoc.put("Ints", new Integer[] { 1, 2, 3, 4, -13, 5, 0 });
        indexedDoc.put("Points", new GeoPoint[] { geoPoint });

        // This is the expected document when querying the document later
        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("Key", docKey);
        expectedDoc.put("Doubles", Arrays.asList(0.0, 5.8, "INF", "-INF", "NaN"));
        expectedDoc.put("Bools", Arrays.asList(true, false));
        expectedDoc.put("Longs", Arrays.asList(9999999999999999L, 832372345832523L));
        expectedDoc.put("Strings", Arrays.asList("hello", "bye"));
        expectedDoc.put("Ints", Arrays.asList(1, 2, 3, 4, -13, 5, 0));
        expectedDoc.put("Points", Collections.singletonList(geoPoint));
        expectedDoc.put("Dates", Collections.singletonList(dateTime));

        uploadDocumentRaw(client, indexedDoc);

        getAndValidateDocument(client, docKey, expectedDoc,
            (expected, actual) -> assertMapEquals(expected, actual, true, "properties"));
    }

    @Test
    public void dynamicallyTypedPrimitiveCollectionsDoNotAllRoundTripCorrectlyAsync() {
        SearchAsyncClient asyncClient = getAsyncClient(TYPE_INDEX_NAME);

        String docKey = getRandomDocumentKey();
        OffsetDateTime dateTime = OffsetDateTime.parse("2019-08-13T14:30:00Z");
        GeoPoint geoPoint = new GeoPoint(100.0, 1.0);

        Map<String, Object> indexedDoc = new LinkedHashMap<>();
        indexedDoc.put("Key", docKey);
        indexedDoc.put("Dates", new OffsetDateTime[] { dateTime });
        indexedDoc.put("Doubles", new Double[] { 0.0, 5.8, POSITIVE_INFINITY, NEGATIVE_INFINITY, NaN });
        indexedDoc.put("Bools", new Boolean[] { true, false });
        indexedDoc.put("Longs", new Long[] { 9999999999999999L, 832372345832523L });
        indexedDoc.put("Strings", new String[] { "hello", "bye" });
        indexedDoc.put("Ints", new Integer[] { 1, 2, 3, 4, -13, 5, 0 });
        indexedDoc.put("Points", new GeoPoint[] { geoPoint });

        // This is the expected document when querying the document later
        Map<String, Object> expectedDoc = new LinkedHashMap<>();
        expectedDoc.put("Key", docKey);
        expectedDoc.put("Doubles", Arrays.asList(0.0, 5.8, "INF", "-INF", "NaN"));
        expectedDoc.put("Bools", Arrays.asList(true, false));
        expectedDoc.put("Longs", Arrays.asList(9999999999999999L, 832372345832523L));
        expectedDoc.put("Strings", Arrays.asList("hello", "bye"));
        expectedDoc.put("Ints", Arrays.asList(1, 2, 3, 4, -13, 5, 0));
        expectedDoc.put("Points", Collections.singletonList(geoPoint));
        expectedDoc.put("Dates", Collections.singletonList(dateTime));

        uploadDocumentRaw(asyncClient, indexedDoc);

        getAndValidateDocumentAsync(asyncClient, docKey, expectedDoc,
            (expected, actual) -> assertMapEquals(expected, actual, true, "properties"));
    }

    static Hotel prepareExpectedHotel(String key) {
        return new Hotel().hotelId(key)
            .hotelName("Fancy Stay")
            .description("Best hotel in town if you like luxury hotels. They have an amazing infinity pool, a spa, and "
                + "a really helpful concierge. The location is perfect -- right downtown, close to all the tourist "
                + "attractions. We highly recommend this hotel.")
            .descriptionFr("Meilleur hôtel en ville si vous aimez les hôtels de luxe. Ils ont une magnifique piscine à "
                + "débordement, un spa et un concierge très utile. L'emplacement est parfait – en plein centre, à "
                + "proximité de toutes les attractions touristiques. Nous recommandons fortement cet hôtel.")
            .category("Luxury")
            .tags(Arrays.asList("pool", "view", "wifi", "concierge"))
            .parkingIncluded(false)
            .smokingAllowed(false)
            .lastRenovationDate(OffsetDateTime.ofInstant(Instant.ofEpochMilli(1277582400000L), ZoneOffset.UTC))
            .rating(5)
            .location(new GeoPoint(-122.131577, 47.678581))
            .rooms(new ArrayList<>());
    }

    private static void getAndValidateDocumentAsync(SearchAsyncClient asyncClient, String key,
        Map<String, Object> expected, BiConsumer<Map<String, Object>, Map<String, Object>> comparator) {
        StepVerifier.create(asyncClient.getDocument(key))
            .assertNext(actual -> comparator.accept(expected, actual.getAdditionalProperties()))
            .verifyComplete();
    }

    private static <T> void getAndValidateDocumentAsync(SearchAsyncClient asyncClient, String key,
        ReadValueCallback<JsonReader, T> converter, T expected, BiConsumer<T, T> comparator) {
        StepVerifier
            .create(asyncClient.getDocument(key)
                .map(doc -> convertFromMapStringObject(doc.getAdditionalProperties(), converter)))
            .assertNext(actual -> comparator.accept(expected, actual))
            .verifyComplete();
    }

    private static void getAndValidateDocumentAsync(SearchAsyncClient asyncClient, String key,
        Collection<String> selectedFields, Map<String, Object> expected,
        BiConsumer<Map<String, Object>, Map<String, Object>> comparator) {
        StepVerifier
            .create(asyncClient
                .getDocumentWithResponse(key,
                    new RequestOptions().addQueryParam("$select", String.join(",", selectedFields)))
                .map(response -> response.getValue().toObject(LookupDocument.class).getAdditionalProperties()))
            .assertNext(actual -> comparator.accept(expected, actual))
            .verifyComplete();
    }

    private static <T> void getAndValidateDocumentAsync(SearchAsyncClient asyncClient, String key,
        ReadValueCallback<JsonReader, T> converter, Collection<String> selectedFields, T expected,
        BiConsumer<T, T> comparator) {
        StepVerifier
            .create(asyncClient
                .getDocumentWithResponse(key,
                    new RequestOptions().addQueryParam("$select", String.join(",", selectedFields)))
                .map(response -> convertFromMapStringObject(
                    response.getValue().toObject(LookupDocument.class).getAdditionalProperties(), converter)))
            .assertNext(actual -> comparator.accept(expected, actual))
            .verifyComplete();
    }

    private static void getAndValidateDocument(SearchClient client, String key, Map<String, Object> expected,
        BiConsumer<Map<String, Object>, Map<String, Object>> comparator) {
        comparator.accept(expected, client.getDocument(key).getAdditionalProperties());
    }

    private static <T> void getAndValidateDocument(SearchClient client, String key,
        ReadValueCallback<JsonReader, T> converter, T expected, BiConsumer<T, T> comparator) {
        comparator.accept(expected,
            convertFromMapStringObject(client.getDocument(key).getAdditionalProperties(), converter));
    }

    private static void getAndValidateDocument(SearchClient client, String key, Collection<String> selectedFields,
        Map<String, Object> expected, BiConsumer<Map<String, Object>, Map<String, Object>> comparator) {
        Map<String, Object> actual = client
            .getDocumentWithResponse(key,
                new RequestOptions().addQueryParam("$select", String.join(",", selectedFields)))
            .getValue()
            .toObject(LookupDocument.class)
            .getAdditionalProperties();
        comparator.accept(expected, actual);
    }

    private static <T> void getAndValidateDocument(SearchClient client, String key,
        ReadValueCallback<JsonReader, T> converter, Collection<String> selectedFields, T expected,
        BiConsumer<T, T> comparator) {
        Map<String, Object> actual = client
            .getDocumentWithResponse(key,
                new RequestOptions().addQueryParam("$select", String.join(",", selectedFields)))
            .getValue()
            .toObject(LookupDocument.class)
            .getAdditionalProperties();
        comparator.accept(expected, convertFromMapStringObject(actual, converter));
    }

    static Hotel prepareEmptyHotel(String key) {
        return new Hotel().hotelId(key)
            .tags(new ArrayList<>())
            .rooms(Collections.singletonList(new HotelRoom().tags(new String[0])));
    }

    static Hotel preparePascalCaseFieldsHotel(String key) {
        return new Hotel().hotelId(key).hotelName("Lord of the Rings").description("J.R.R").descriptionFr("Tolkien");
    }

    static Hotel prepareSelectedFieldsHotel(String key) {
        // Since Date doesn't have time zone information to make this test durable against time zones create the Date
        // from an OffsetDateTime.
        OffsetDateTime dateTime = OffsetDateTime.parse("2010-06-26T17:00:00.000+00:00")
            .atZoneSameInstant(ZoneId.systemDefault())
            .toOffsetDateTime();

        return new Hotel().hotelId(key)
            .hotelName("Countryside Hotel")
            .description("Save up to 50% off traditional hotels.  Free WiFi, great location near downtown, full "
                + "kitchen, washer & dryer, 24/7 support, bowling alley, fitness center and more.")
            .descriptionFr("Économisez jusqu'à 50% sur les hôtels traditionnels.  WiFi gratuit, très bien situé près "
                + "du centre-ville, cuisine complète, laveuse & sécheuse, support 24/7, bowling, centre de fitness et "
                + "plus encore.")
            .category("Budget")
            .tags(Arrays.asList("24-hour front desk service", "coffee in lobby", "restaurant"))
            .parkingIncluded(false)
            .smokingAllowed(true)
            .lastRenovationDate(dateTime)
            .rating(3)
            .location(new GeoPoint(-78.940483, 35.904160))
            .address(new HotelAddress().streetAddress("6910 Fayetteville Rd")
                .city("Durham")
                .stateProvince("NC")
                .country("USA")
                .postalCode("27713"))
            .rooms(Arrays.asList(
                new HotelRoom().description("Suite, 1 King Bed (Amenities)")
                    .descriptionFr("Suite, 1 très grand lit (Services)")
                    .type("Suite")
                    .baseRate(2.44)
                    .bedOptions("1 King Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(new String[] { "coffee maker" }),
                new HotelRoom().description("Budget Room, 1 Queen Bed (Amenities)")
                    .descriptionFr("Chambre Économique, 1 grand lit (Services)")
                    .type("Budget Room")
                    .baseRate(7.69)
                    .bedOptions("1 Queen Bed")
                    .sleepsCount(2)
                    .smokingAllowed(false)
                    .tags(new String[] { "coffee maker" })));
    }

    static ModelWithPrimitiveCollections preparePrimitivesModel(String key) {
        return new ModelWithPrimitiveCollections().key(key)
            .bools(new Boolean[] { true, false })
            .dates(new OffsetDateTime[] {
                OffsetDateTime.parse("2019-04-14T14:24:00Z"),
                OffsetDateTime.parse("1999-12-31T23:59:59Z") })
            .doubles(new Double[] { NEGATIVE_INFINITY, 0.0, 2.78, NaN, 3.25, POSITIVE_INFINITY })
            .ints(new int[] { 1, 2, 3, 4, -13, 5, 0 })
            .longs(new Long[] { -9_999_999_999_999_999L, 832_372_345_832_523L })
            .points(new GeoPoint[] { new GeoPoint(-67.0, 49.0), new GeoPoint(21.0, 47.0) })
            .strings(new String[] { "hello", "2019-04-14T14:56:00-07:00" });
    }

    static void setupIndexWithDataTypes() {
        SearchIndex index = new SearchIndex(TYPE_INDEX_NAME,
            new SearchField("Key", SearchFieldDataType.STRING).setKey(true).setRetrievable(true),
            new SearchField("Bools", SearchFieldDataType.collection(SearchFieldDataType.BOOLEAN)).setRetrievable(true),
            new SearchField("Dates", SearchFieldDataType.collection(SearchFieldDataType.DATE_TIME_OFFSET))
                .setRetrievable(true),
            new SearchField("Doubles", SearchFieldDataType.collection(SearchFieldDataType.DOUBLE)).setRetrievable(true),
            new SearchField("Points", SearchFieldDataType.collection(SearchFieldDataType.GEOGRAPHY_POINT))
                .setRetrievable(true),
            new SearchField("Ints", SearchFieldDataType.collection(SearchFieldDataType.INT32)).setRetrievable(true),
            new SearchField("Longs", SearchFieldDataType.collection(SearchFieldDataType.INT64)).setRetrievable(true),
            new SearchField("Strings", SearchFieldDataType.collection(SearchFieldDataType.STRING))
                .setRetrievable(true));

        createSharedSearchIndexClient().createOrUpdateIndex(index);
    }
}
