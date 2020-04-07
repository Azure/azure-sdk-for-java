// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.models.DataType;
import com.azure.search.documents.models.Field;
import com.azure.search.documents.models.GeoPoint;
import com.azure.search.documents.models.Index;
import com.azure.search.documents.models.IndexDocumentsBatch;
import com.azure.search.documents.test.environment.models.Hotel;
import com.azure.search.documents.test.environment.models.HotelAddress;
import com.azure.search.documents.test.environment.models.HotelRoom;
import com.azure.search.documents.test.environment.models.ModelWithPrimitiveCollections;
import java.util.HashMap;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static com.azure.search.documents.TestHelpers.assertObjectEquals;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LookupSyncTests extends SearchIndexClientTestBase {
    private static final String INDEX_NAME = "hotels";

    private SearchIndexClient client;

    @Test
    public void canGetStaticallyTypedDocument() throws ParseException {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        Hotel expected = prepareExpectedHotel();
        uploadDocument(client, expected);

        SearchDocument result = client.getDocument(expected.hotelId());
        Hotel actual = convertToType(result, Hotel.class);
        assertObjectEquals(expected, actual, true);
    }

    @Test
    public void canGetStaticallyTypedDocumentWithNullOrEmptyValues() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        Hotel expected = prepareEmptyHotel();
        uploadDocument(client, expected);

        SearchDocument result = client.getDocument(expected.hotelId());
        Hotel actual = convertToType(result, Hotel.class);
        assertObjectEquals(expected, actual, true);
    }

    @Test
    public void canGetStaticallyTypedDocumentWithPascalCaseFields() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        Hotel expected = preparePascalCaseFieldsHotel();
        uploadDocument(client, expected);

        SearchDocument result = client.getDocument(expected.hotelId());
        Hotel actual = convertToType(result, Hotel.class);
        assertObjectEquals(expected, actual, true);
    }

    @Test
    public void canRoundtripStaticallyTypedPrimitiveCollections() {
        String indexName = setupIndexWithDataTypes();
        client = getSearchIndexClientBuilder(indexName).buildClient();

        ModelWithPrimitiveCollections expected = preparePrimitivesModel();
        uploadDocument(client, expected);

        SearchDocument result = client.getDocument(expected.key());
        ModelWithPrimitiveCollections actual = convertToType(result, ModelWithPrimitiveCollections.class);
        assertObjectEquals(expected, actual, true);
    }

    @Test
    public void getStaticallyTypedDocumentSetsUnselectedFieldsToNull() throws ParseException {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        Hotel indexedDoc = prepareSelectedFieldsHotel();
        Hotel expected = new Hotel()
            .hotelName("Countryside Hotel")
            .description("Save up to 50% off traditional hotels.  Free WiFi, great location near downtown, full kitchen, washer & dryer, 24/7 support, bowling alley, fitness center and more.")
            .address(new HotelAddress().city("Durham"))
            .rooms(Arrays.asList(new HotelRoom().baseRate(2.44), new HotelRoom().baseRate(7.69)));

        uploadDocument(client, indexedDoc);

        List<String> selectedFields = Arrays.asList("Description", "HotelName", "Address/City", "Rooms/BaseRate");
        Response<SearchDocument> response = client.getDocumentWithResponse(indexedDoc.hotelId(), selectedFields, generateRequestOptions(), Context.NONE);
        Hotel actual = convertToType(response.getValue(), Hotel.class);
        assertObjectEquals(expected, actual, true);
    }

    @Test
    public void canGetDynamicDocumentWithNullOrEmptyValues() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        SearchDocument expectedDoc = new SearchDocument(new HashMap<String, Object>() {
            {
                put("HotelId", "1");
                put("HotelName", null);
                put("Tags", Collections.emptyList());
                put("ParkingIncluded", null);
                put("LastRenovationDate", null);
                put("Rating", null);
                put("Location", null);
                put("Address", null);
                put("Rooms", Collections.singletonList(
                    new SearchDocument(new HashMap<String, Object>() {
                        {
                            put("BaseRate", null);
                            put("BedOptions", null);
                            put("SleepsCount", null);
                            put("SmokingAllowed", null);
                            put("Tags", Collections.emptyList());
                        }
                    }
                )));
            }
        });

        uploadDocument(client, expectedDoc);
        // Select only the fields set in the test case so we don't get superfluous data back.
        List<String> selectedFields = Arrays.asList("HotelId", "HotelName", "Tags", "ParkingIncluded", "LastRenovationDate", "Rating", "Location", "Address", "Rooms/BaseRate", "Rooms/BedOptions", "Rooms/SleepsCount", "Rooms/SmokingAllowed", "Rooms/Tags");

        Response<SearchDocument> response = client.getDocumentWithResponse("1", selectedFields, generateRequestOptions(), Context.NONE);
        assertEquals(expectedDoc, response.getValue());
    }

    @Test
    public void getDynamicDocumentWithEmptyObjectsReturnsObjectsFullOfNulls() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        SearchDocument originalDoc = new SearchDocument(new HashMap<String, Object>() {
            {
                put("HotelId", "1");
                put("Address", new SearchDocument());
            }
        });

        SearchDocument expectedDoc = new SearchDocument(new HashMap<String, Object>() {
            {
                put("HotelId", "1");
                put("Address", new SearchDocument(new HashMap<String, Object>() {
                    {
                        put("StreetAddress", null);
                        put("City", null);
                        put("StateProvince", null);
                        put("Country", null);
                        put("PostalCode", null);
                    }
                }));
            }
        });

        uploadDocument(client, originalDoc);
        // Select only the fields set in the test case so we don't get superfluous data back.
        List<String> selectedFields = Arrays.asList("HotelId", "Address");

        Response<SearchDocument> response = client.getDocumentWithResponse("1", selectedFields, generateRequestOptions(), Context.NONE);
        assertEquals(expectedDoc, response.getValue());
    }

    @Test
    public void emptyDynamicallyTypedPrimitiveCollectionsRoundtripAsObjectArrays() {
        String indexName = setupIndexWithDataTypes();
        client = getSearchIndexClientBuilder(indexName).buildClient();

        String docKey = "3";

        SearchDocument originalDoc = new SearchDocument(new HashMap<String, Object>() {
            {
                put("Key", docKey);
                put("Dates", new Object[]{});
                put("Doubles", new Double[]{});
                put("Bools", new boolean[]{});
                put("Longs", new Long[]{});
                put("Strings", new String[]{});
                put("Ints", new int[]{});
                put("Points", new Object[]{});
            }
        });

        SearchDocument expectedDoc = new SearchDocument(new HashMap<String, Object>() {
            {
                put("Key", docKey);
                put("Doubles", Collections.emptyList());
                put("Bools", Collections.emptyList());
                put("Longs", Collections.emptyList());
                put("Strings", Collections.emptyList());
                put("Ints", Collections.emptyList());
                put("Points", Collections.emptyList());
                put("Dates", Collections.emptyList());
            }
        });

        uploadDocument(client, originalDoc);

        SearchDocument actualDoc = client.getDocument(docKey);
        assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void emptyDynamicObjectsInCollectionExpandedOnGetWhenCollectionFieldSelected() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        SearchDocument originalDoc = new SearchDocument(new HashMap<String, Object>() {
            {
                put("HotelId", "1");
                put("Rooms", Arrays.asList(
                    new SearchDocument(),
                    new SearchDocument(new HashMap<String, Object>() {
                        {
                            put("BaseRate", null);
                            put("BedOptions", null);
                            put("SleepsCount", null);
                            put("SmokingAllowed", null);
                            put("Tags", Collections.emptyList());
                        }
                    }
                )));
            }
        });

        SearchDocument expectedDoc = new SearchDocument(new HashMap<String, Object>() {
            {
                put("HotelId", "1");
                put("Rooms", Arrays.asList(
                    new SearchDocument(new HashMap<String, Object>() {
                        {
                            put("Description", null);
                            put("Description_fr", null);
                            put("Type", null);
                            put("BaseRate", null);
                            put("BedOptions", null);
                            put("SleepsCount", null);
                            put("SmokingAllowed", null);
                            put("Tags", Collections.emptyList());
                        }
                    }),
                    new SearchDocument(new HashMap<String, Object>() {
                        {
                            put("Description", null);
                            put("Description_fr", null);
                            put("Type", null);
                            put("BaseRate", null);
                            put("BedOptions", null);
                            put("SleepsCount", null);
                            put("SmokingAllowed", null);
                            put("Tags", Collections.emptyList());
                        }
                    }
                )));
            }
        });

        uploadDocument(client, originalDoc);
        List<String> selectedFields = Arrays.asList("HotelId", "Rooms");

        Response<SearchDocument> response = client.getDocumentWithResponse("1", selectedFields, generateRequestOptions(), Context.NONE);
        assertEquals(expectedDoc, response.getValue());
    }

    @Test
    public void getDynamicDocumentCannotAlwaysDetermineCorrectType() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        SearchDocument indexedDoc = new SearchDocument();
        indexedDoc.put("HotelId", "1");
        indexedDoc.put("HotelName", "2015-02-11T12:58:00Z");
        indexedDoc.put("Location", GeoPoint.create(40.760586, -73.975403)); // Test that we don't confuse Geo-JSON & complex types.
        indexedDoc.put("Rooms", Collections.singletonList(new SearchDocument(Collections.singletonMap("BaseRate", NaN))));

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", "1");
        expectedDoc.put("HotelName", OffsetDateTime.of(2015, 2, 11, 12, 58, 0, 0, ZoneOffset.UTC));
        expectedDoc.put("Location", GeoPoint.create(40.760586, -73.975403));
        expectedDoc.put("Rooms", Collections.singletonList(new SearchDocument(Collections.singletonMap("BaseRate", "NaN"))));

        client.indexDocuments(new IndexDocumentsBatch<>().addUploadActions(indexedDoc));

        // Select only the fields set in the test case so we don't get superfluous data back.
        List<String> selectedFields = Arrays.asList("HotelId", "HotelName", "Location", "Rooms/BaseRate");
        assertEquals(expectedDoc, client.getDocumentWithResponse("1", selectedFields, null, Context.NONE).getValue());
    }

    @Test
    public void canGetDocumentWithBase64EncodedKey() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        String complexKey = Base64.getEncoder().encodeToString(new byte[]{1, 2, 3, 4, 5});

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", complexKey);

        client.indexDocuments(new IndexDocumentsBatch<>().addUploadActions(expectedDoc));
        assertEquals(client.getDocumentWithResponse(complexKey, new ArrayList<>(expectedDoc.keySet()), null, Context.NONE).getValue(), expectedDoc);
    }

    @Test
    public void roundTrippingDateTimeOffsetNormalizesToUtc() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        SearchDocument indexedDoc = new SearchDocument();
        indexedDoc.put("HotelId", "1");
        indexedDoc.put("LastRenovationDate", OffsetDateTime.parse("2010-06-27T00:00:00-08:00", DateTimeFormatter.ISO_DATE_TIME));

        SearchDocument expectedDoc = new SearchDocument();
        expectedDoc.put("HotelId", "1");
        expectedDoc.put("LastRenovationDate", OffsetDateTime.parse("2010-06-27T08:00:00Z", DateTimeFormatter.ISO_DATE_TIME));

        client.indexDocuments(new IndexDocumentsBatch<>().addUploadActions(indexedDoc));
        assertEquals(client.getDocumentWithResponse("1", new ArrayList<>(expectedDoc.keySet()), null, Context.NONE).getValue(), expectedDoc);
    }

    @Test
    public void emptyDynamicObjectsOmittedFromCollectionOnGetWhenSubFieldsSelected() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        SearchDocument originalDoc = new SearchDocument(new HashMap<String, Object>() {
            {
                put("HotelId", "1");
                put("Rooms", Arrays.asList(
                    new SearchDocument(),
                    new SearchDocument(new HashMap<String, Object>() {
                        {
                            put("BaseRate", null);
                            put("BedOptions", null);
                            put("SleepsCount", null);
                            put("SmokingAllowed", null);
                            put("Tags", Collections.emptyList());
                        }
                    }
                )));
            }
        });

        SearchDocument expectedDoc = new SearchDocument(new HashMap<String, Object>() {
            {
                put("HotelId", "1");
                put("Rooms", Collections.singletonList(
                    new SearchDocument(new HashMap<String, Object>() {
                        {
                            put("BaseRate", null);
                            put("BedOptions", null);
                            put("SleepsCount", null);
                            put("SmokingAllowed", null);
                            put("Tags", Collections.emptyList());
                        }
                    }
                )));
            }
        });

        uploadDocument(client, originalDoc);
        List<String> selectedFields = Arrays.asList("HotelId", "Rooms/BaseRate", "Rooms/BedOptions", "Rooms/SleepsCount", "Rooms/SmokingAllowed", "Rooms/Tags");

        Response<SearchDocument> response = client.getDocumentWithResponse("1", selectedFields, generateRequestOptions(), Context.NONE);
        assertEquals(expectedDoc, response.getValue());
    }

    @Test
    public void dynamicallyTypedPrimitiveCollectionsDoNotAllRoundtripCorrectly() {
        String indexName = setupIndexWithDataTypes();
        client = getSearchIndexClientBuilder(indexName).buildClient();

        String docKey = "1";
        OffsetDateTime dateTime = OffsetDateTime.parse("2019-08-13T14:30:00Z");
        GeoPoint geoPoint = GeoPoint.create(1.0, 100.0);

        SearchDocument indexedDoc = new SearchDocument(new HashMap<String, Object>() {
            {
                put("Key", docKey);
                put("Dates", new OffsetDateTime[]{dateTime});
                put("Doubles", new Double[]{0.0, 5.8, POSITIVE_INFINITY, NEGATIVE_INFINITY, NaN});
                put("Bools", new Boolean[]{true, false});
                put("Longs", new Long[]{9999999999999999L, 832372345832523L});
                put("Strings", new String[]{"hello", "bye"});
                put("Ints", new int[]{1, 2, 3, 4, -13, 5, 0});
                put("Points", new GeoPoint[]{geoPoint});
            }
        });

        // This is the expected document when querying the document later
        SearchDocument expectedDoc = new SearchDocument(new HashMap<String, Object>() {
            {
                put("Key", docKey);
                put("Doubles", Arrays.asList(0.0, 5.8, "INF", "-INF", "NaN"));
                put("Bools", Arrays.asList(true, false));
                put("Longs", Arrays.asList(9999999999999999L, 832372345832523L));
                put("Strings", Arrays.asList("hello", "bye"));
                put("Ints", Arrays.asList(1, 2, 3, 4, -13, 5, 0));
                put("Points", Collections.singletonList(geoPoint));
                put("Dates", Collections.singletonList(dateTime));
            }
        });

        uploadDocument(client, indexedDoc);

        SearchDocument actualDoc = client.getDocument(docKey);
        assertEquals(expectedDoc, actualDoc);
    }

    Hotel prepareExpectedHotel() throws ParseException {
        return new Hotel().hotelId("1")
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
            .lastRenovationDate(DATE_FORMAT.parse("2010-06-27T00:00:00Z"))
            .rating(5)
            .location(GeoPoint.create(47.678581, -122.131577))
            .rooms(new ArrayList<>());
    }

    Hotel prepareEmptyHotel() {
        return new Hotel().hotelId("1")
            .tags(new ArrayList<>())
            .rooms(Collections.singletonList(
                new HotelRoom().tags(new ArrayList<>())
            ));
    }

    Hotel preparePascalCaseFieldsHotel() {
        return new Hotel().hotelId("123").hotelName("Lord of the Rings").description("J.R.R").descriptionFr("Tolkien");
    }

    Hotel prepareSelectedFieldsHotel() throws ParseException {
        return new Hotel()
            .hotelId("2")
            .hotelName("Countryside Hotel")
            .description("Save up to 50% off traditional hotels.  Free WiFi, great location near downtown, full kitchen, washer & dryer, 24/7 support, bowling alley, fitness center and more.")
            .descriptionFr("Économisez jusqu'à 50% sur les hôtels traditionnels.  WiFi gratuit, très bien situé près du centre-ville, cuisine complète, laveuse & sécheuse, support 24/7, bowling, centre de fitness et plus encore.")
            .category("Budget")
            .tags(Arrays.asList("24-hour front desk service", "coffee in lobby", "restaurant"))
            .parkingIncluded(false)
            .smokingAllowed(true)
            .lastRenovationDate(DATE_FORMAT.parse("2010-06-27T00:00:00Z"))
            .rating(3)
            .location(GeoPoint.create(35.904160, -78.940483))
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
                    .tags(Collections.singletonList("coffee maker")),
                new HotelRoom()
                    .description("Budget Room, 1 Queen Bed (Amenities)")
                    .descriptionFr("Chambre Économique, 1 grand lit (Services)")
                    .type("Budget Room")
                    .baseRate(7.69)
                    .bedOptions("1 Queen Bed")
                    .sleepsCount(2)
                    .smokingAllowed(false)
                    .tags(Collections.singletonList("coffee maker"))));
    }

    ModelWithPrimitiveCollections preparePrimitivesModel() {
        return new ModelWithPrimitiveCollections()
            .key("1")
            .bools(new Boolean[]{true, false})
            .dates(new OffsetDateTime[]{
                OffsetDateTime.parse("2019-04-14T14:24:00Z"),
                OffsetDateTime.parse("1999-12-31T23:59:59Z")})
            .doubles(new Double[]{NEGATIVE_INFINITY, 0.0, 2.78, NaN, 3.14, POSITIVE_INFINITY})
            .ints(new int[]{1, 2, 3, 4, -13, 5, 0})
            .longs(new Long[]{-9_999_999_999_999_999L, 832_372_345_832_523L})
            .points(new GeoPoint[]{
                GeoPoint.create(49.0, -67.0),
                GeoPoint.create(47.0, 21.0)})
            .strings(new String[]{"hello", "2019-04-14T14:56:00-07:00"});
    }

    String setupIndexWithDataTypes() {
        Index index = new Index()
            .setName("data-types-tests-index")
            .setFields(Arrays.asList(
                new Field()
                    .setName("Key")
                    .setType(DataType.EDM_STRING)
                    .setKey(true)
                    .setRetrievable(true),
                new Field()
                    .setName("Bools")
                    .setType(DataType.collection(DataType.EDM_BOOLEAN))
                    .setRetrievable(true),
                new Field()
                    .setName("Dates")
                    .setType(DataType.collection(DataType.EDM_DATE_TIME_OFFSET))
                    .setRetrievable(true),
                new Field()
                    .setName("Doubles")
                    .setType(DataType.collection(DataType.EDM_DOUBLE))
                    .setRetrievable(true),
                new Field()
                    .setName("Points")
                    .setType(DataType.collection(DataType.EDM_GEOGRAPHY_POINT))
                    .setRetrievable(true),
                new Field()
                    .setName("Ints")
                    .setType(DataType.collection(DataType.EDM_INT32))
                    .setRetrievable(true),
                new Field()
                    .setName("Longs")
                    .setType(DataType.collection(DataType.EDM_INT64))
                    .setRetrievable(true),
                new Field()
                    .setName("Strings")
                    .setType(DataType.collection(DataType.EDM_STRING))
                    .setRetrievable(true)
            ));

        setupIndex(index);

        return index.getName();
    }
}
