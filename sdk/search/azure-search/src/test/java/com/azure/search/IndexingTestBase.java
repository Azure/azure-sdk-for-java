// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.search.models.GeoPoint;
import com.azure.search.models.IndexingResult;
import com.azure.search.test.environment.models.Hotel;
import com.azure.search.test.environment.models.HotelAddress;
import com.azure.search.test.environment.models.HotelRoom;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class IndexingTestBase extends SearchIndexClientTestBase {
    static final String INDEX_NAME = "hotels";
    static final String BOOKS_INDEX_NAME = "books";
    static final String BOOKS_INDEX_JSON = "BooksIndexData.json";

    @Test
    public abstract void countingDocsOfNewIndexGivesZero();

    @Test
    public abstract void indexDoesNotThrowWhenAllActionsSucceed();

    @Test
    public abstract void canIndexWithPascalCaseFields();

    @Test
    public abstract void canIndexStaticallyTypedDocuments() throws ParseException;

    @Test
    public abstract void canIndexDynamicDocuments();

    @Test
    public abstract void canDeleteBatchByKeys();

    @Test
    public abstract void indexDoesNotThrowWhenDeletingDocumentWithExtraFields();

    @Test
    public abstract void indexDoesNotThrowWhenDeletingDynamicDocumentWithExtraFields();

    Hotel prepareStaticallyTypedHotel(String hotelId) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
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
            .lastRenovationDate(dateFormat.parse("2010-06-27T00:00:00Z"))
            .rating(5)
            .location(GeoPoint.create(47.678581, -122.131577))
            .address(
                new HotelAddress()
                    .streetAddress("1 Microsoft Way")
                    .city("Redmond")
                    .stateProvince("Washington")
                    .postalCode("98052")
                    .country("United States")
            );
    }

    Document prepareDynamicallyTypedHotel(String hotelId) {

        Document room1 = new Document();
        room1.put("Description", "Budget Room, 1 Queen Bed");
        room1.put("Description_fr", null);
        room1.put("Type", "Budget Room");
        room1.put("BaseRate", 149.99);
        room1.put("BedOptions", "1 Queen Bed");
        room1.put("SleepsCount", 2);
        room1.put("SmokingAllowed", true);
        room1.put("Tags", Arrays.asList("vcr/dvd", "great view"));

        Document room2 = new Document();
        room2.put("Description", "Budget Room, 1 King Bed");
        room2.put("Description_fr", null);
        room2.put("Type", "Budget Room");
        room2.put("BaseRate", 249.99);
        room2.put("BedOptions", "1 King Bed");
        room2.put("SleepsCount", 2);
        room2.put("SmokingAllowed", true);
        room2.put("Tags", Arrays.asList("vcr/dvd", "seaside view"));

        List<Document> rooms = Arrays.asList(room1, room2);

        Document address = new Document();
        address.put("StreetAddress", "One Microsoft way");
        address.put("City", "Redmond");
        address.put("StateProvince", "Washington");
        address.put("PostalCode", "98052");
        address.put("Country", "US");

        Document location = new Document();
        location.put("type", "Point");
        location.put("coordinates", Arrays.asList(-122.131577, 47.678581));
        location.put("crs", null);

        Document hotel = new Document();
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
        Assert.assertEquals(result.getKey(), key);
        Assert.assertEquals(result.getStatusCode(), statusCode);
        Assert.assertTrue(result.isSucceeded());
    }

    void assertFailedIndexResult(IndexingResult result, String key, int statusCode, String errorMessage) {
        Assert.assertEquals(result.getKey(), key);
        Assert.assertEquals(result.getStatusCode(), statusCode);
        Assert.assertEquals(result.getErrorMessage(), errorMessage);
        Assert.assertFalse(result.isSucceeded());
    }

    @Test
    public abstract void indexWithInvalidDocumentThrowsException();

    @Test
    public abstract void canUseIndexWithReservedName();

    @Test
    public abstract void canRoundtripBoundaryValues() throws ParseException;

    @Test
    public abstract void dynamicDocumentDateTimesRoundTripAsUtc();

    @Test
    public abstract void staticallyTypedDateTimesRoundTripAsUtc();

    @Test
    public abstract void mergeDocumentWithoutExistingKeyThrowsIndexingException() throws ParseException;

    @Test
    public abstract void canMergeStaticallyTypedDocuments() throws ParseException;

    @Test
    public abstract void canSetExplicitNullsInStaticallyTypedDocument() throws ParseException;

    @Test
    public abstract void canMergeDynamicDocuments();

    @Test
    public abstract void canIndexAndAccessResponse();

    void assertIndexActionSucceeded(String key, IndexingResult result, int expectedStatusCode) {
        Assert.assertEquals(key, result.getKey());
        Assert.assertTrue(result.isSucceeded());
        Assert.assertNull(result.getErrorMessage());
        Assert.assertEquals(expectedStatusCode, result.getStatusCode());
    }

    List<Hotel> getBoundaryValues() throws ParseException {
        return Arrays.asList(
            // Minimum values
            new Hotel()
                .hotelId("1")
                .category("")
                .lastRenovationDate(DATE_FORMAT.parse("0001-01-01T00:00:00Z"))
                .location(GeoPoint.create(-90, -180))   // South pole, date line from the west
                .parkingIncluded(false)
                .rating(Integer.MIN_VALUE)
                .tags(Collections.emptyList())
                .address(new HotelAddress())
                .rooms(Collections.singletonList(
                    new HotelRoom()
                        .baseRate(Double.MIN_VALUE)
                )),
            // Maximum values
            new Hotel()
                .hotelId("2")
                .category("test")   // No meaningful string max since there is no length limit (other than payload size or term length).
                .lastRenovationDate(DATE_FORMAT.parse("9999-12-31T11:59:59Z"))
                .location(GeoPoint.create(90, 180))     // North pole, date line from the east
                .parkingIncluded(true)
                .rating(Integer.MAX_VALUE)
                .tags(Collections.singletonList("test"))    // No meaningful string max; see above.
                .address(new HotelAddress()
                    .city("Maximum"))
                .rooms(Collections.singletonList(
                    new HotelRoom()
                        .baseRate(Double.MAX_VALUE)
                )),
            // Other boundary values #1
            new Hotel()
                .hotelId("3")
                .category(null)
                .lastRenovationDate(null)
                .location(GeoPoint.create(0, 0))     // Equator, meridian
                .parkingIncluded(null)
                .rating(null)
                .tags(Collections.emptyList())
                .address(new HotelAddress()
                    .city("Maximum"))
                .rooms(Collections.singletonList(
                    new HotelRoom()
                        .baseRate(Double.NEGATIVE_INFINITY)
                )),
            // Other boundary values #2
            new Hotel()
                .hotelId("4")
                .location(null)
                .tags(Collections.emptyList())
                .rooms(Collections.singletonList(
                    new HotelRoom()
                        .baseRate(Double.POSITIVE_INFINITY)
                )),
            // Other boundary values #3
            new Hotel()
                .hotelId("5")
                .tags(Collections.emptyList())
                .rooms(Collections.singletonList(
                    new HotelRoom()
                        .baseRate(Double.NaN)
                )),
            // Other boundary values #4
            new Hotel()
                .hotelId("6")
                .category(null)
                .tags(Collections.emptyList())
                .rooms(Collections.emptyList()));
    }
}
