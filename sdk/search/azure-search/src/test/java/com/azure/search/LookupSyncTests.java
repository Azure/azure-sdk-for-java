// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.search.models.GeoPoint;
import com.azure.search.models.IndexBatch;
import com.azure.search.test.environment.models.Hotel;
import com.azure.search.test.environment.models.HotelAddress;
import com.azure.search.test.environment.models.HotelRoom;
import com.azure.search.test.environment.models.ModelWithPrimitiveCollections;
import com.microsoft.azure.storage.core.Base64;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.IGNORE_DEFAULTS;

public class LookupSyncTests extends LookupTestBase {
    private SearchIndexClient client;

    @Test
    public void canGetStaticallyTypedDocument() throws ParseException {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        Hotel expected = prepareExpectedHotel();
        uploadDocument(client, expected);

        Document result = client.getDocument(expected.hotelId());
        Hotel actual = convertToType(result, Hotel.class);
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
    }

    @Test
    public void canGetStaticallyTypedDocumentWithNullOrEmptyValues() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        Hotel expected = prepareEmptyHotel();
        uploadDocument(client, expected);

        Document result = client.getDocument(expected.hotelId());
        Hotel actual = convertToType(result, Hotel.class);
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
    }

    @Test
    public void canGetStaticallyTypedDocumentWithPascalCaseFields() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        Hotel expected = preparePascalCaseFieldsHotel();
        uploadDocument(client, expected);

        Document result = client.getDocument(expected.hotelId());
        Hotel actual = convertToType(result, Hotel.class);
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
    }

    @Test
    public void canRoundtripStaticallyTypedPrimitiveCollections() {
        String indexName = setupIndexWithDataTypes();
        client = getSearchIndexClientBuilder(indexName).buildClient();

        ModelWithPrimitiveCollections expected = preparePrimitivesModel();
        uploadDocument(client, expected);

        Document result = client.getDocument(expected.key());
        ModelWithPrimitiveCollections actual = convertToType(result, ModelWithPrimitiveCollections.class);
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
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
        Document result = client.getDocument(indexedDoc.hotelId(), selectedFields, generateRequestOptions());
        Hotel actual = convertToType(result, Hotel.class);
        assertReflectionEquals(expected, actual, IGNORE_DEFAULTS);
    }

    @Test
    public void canGetDynamicDocumentWithNullOrEmptyValues() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        Document expectedDoc = new Document() {
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
                    new Document() {
                        {
                            put("BaseRate", null);
                            put("BedOptions", null);
                            put("SleepsCount", null);
                            put("SmokingAllowed", null);
                            put("Tags", Collections.emptyList());
                        }
                    }
                ));
            }
        };

        uploadDocument(client, expectedDoc);
        // Select only the fields set in the test case so we don't get superfluous data back.
        List<String> selectedFields = Arrays.asList("HotelId", "HotelName", "Tags", "ParkingIncluded", "LastRenovationDate", "Rating", "Location", "Address", "Rooms/BaseRate", "Rooms/BedOptions", "Rooms/SleepsCount", "Rooms/SmokingAllowed", "Rooms/Tags");

        Document actualDoc = client.getDocument("1", selectedFields, generateRequestOptions());
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void getDynamicDocumentWithEmptyObjectsReturnsObjectsFullOfNulls() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        Document originalDoc = new Document() {
            {
                put("HotelId", "1");
                put("Address", new Document());
            }
        };

        Document expectedDoc = new Document() {
            {
                put("HotelId", "1");
                put("Address", new Document() {
                    {
                        put("StreetAddress", null);
                        put("City", null);
                        put("StateProvince", null);
                        put("Country", null);
                        put("PostalCode", null);
                    }
                });
            }
        };

        uploadDocument(client, originalDoc);
        // Select only the fields set in the test case so we don't get superfluous data back.
        List<String> selectedFields = Arrays.asList("HotelId", "Address");

        Document actualDoc = client.getDocument("1", selectedFields, generateRequestOptions());
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void emptyDynamicallyTypedPrimitiveCollectionsRoundtripAsObjectArrays() {
        String indexName = setupIndexWithDataTypes();
        client = getSearchIndexClientBuilder(indexName).buildClient();

        String docKey = "3";

        Document originalDoc = new Document() {
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
        };

        Document expectedDoc = new Document() {
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
        };

        uploadDocument(client, originalDoc);

        Document actualDoc = client.getDocument(docKey);
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void emptyDynamicObjectsInCollectionExpandedOnGetWhenCollectionFieldSelected() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        Document originalDoc = new Document() {
            {
                put("HotelId", "1");
                put("Rooms", Arrays.asList(
                    new Document(),
                    new Document() {
                        {
                            put("BaseRate", null);
                            put("BedOptions", null);
                            put("SleepsCount", null);
                            put("SmokingAllowed", null);
                            put("Tags", Collections.emptyList());
                        }
                    }
                ));
            }
        };

        Document expectedDoc = new Document() {
            {
                put("HotelId", "1");
                put("Rooms", Arrays.asList(
                    new Document() {
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
                    },
                    new Document() {
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
                ));
            }
        };

        uploadDocument(client, originalDoc);
        List<String> selectedFields = Arrays.asList("HotelId", "Rooms");

        Document actualDoc = client.getDocument("1", selectedFields, generateRequestOptions());
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Override
    public void getDynamicDocumentCannotAlwaysDetermineCorrectType() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        List<Document> rooms = new ArrayList<>();
        rooms.add(new Document(Collections.singletonMap("baseRate", NaN)));

        Document indexedDoc = new Document();
        indexedDoc.put("HotelId", "1");
        indexedDoc.put("HotelName", "2015-02-11T12:58:00Z");
        indexedDoc.put("Location", GeoPoint.create(40.760586, -73.975403)); // Test that we don't confuse Geo-JSON & complex types.
        indexedDoc.put("Rooms", rooms);

        Document expectedDoc = new Document();
        expectedDoc.put("HotelId", "1");
        expectedDoc.put("HotelName", OffsetDateTime.of(2015, 2, 11, 12, 58, 0, 9, ZoneOffset.UTC));
        expectedDoc.put("Location", GeoPoint.create(40.760586, -73.975403));
        expectedDoc.put("Rooms", Collections.singleton(new Document(Collections.singletonMap("BaseRate", "NaN"))));

        client.index(new IndexBatch<>().addUploadAction(expectedDoc));

        // Select only the fields set in the test case so we don't get superfluous data back.
        Assertions.assertEquals(client.getDocument("1", new ArrayList<>(indexedDoc.keySet()), null), expectedDoc);
    }

    @Override
    public void canGetDocumentWithBase64EncodedKey() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        String complexKey = Base64.encode(new byte[]{1, 2, 3, 4, 5});

        Document expectedDoc = new Document();
        expectedDoc.put("HotelId", complexKey);

        client.index(new IndexBatch<>().addUploadAction(expectedDoc));
        Assertions.assertEquals(client.getDocument(complexKey, new ArrayList<>(expectedDoc.keySet()), null), expectedDoc);
    }

    @Override
    public void roundTrippingDateTimeOffsetNormalizesToUtc() throws ParseException {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

        Document indexedDoc = new Document();
        indexedDoc.put("HotelId", "1");
        indexedDoc.put("LastRenovationDate", dateFormat.parse("2010-06-27T00:00:00-08:00"));

        Document expectedDoc = new Document();
        expectedDoc.put("HotelId", "1");
        expectedDoc.put("LastRenovationDate", dateFormat.parse("2010-06-27T08:00:00Z"));

        client.index(new IndexBatch<>().addUploadAction(expectedDoc));
        Assertions.assertEquals(client.getDocument("1"), expectedDoc);
    }

    @Test
    public void emptyDynamicObjectsOmittedFromCollectionOnGetWhenSubFieldsSelected() {
        createHotelIndex();
        client = getSearchIndexClientBuilder(INDEX_NAME).buildClient();

        Document originalDoc = new Document() {
            {
                put("HotelId", "1");
                put("Rooms", Arrays.asList(
                    new Document(),
                    new Document() {
                        {
                            put("BaseRate", null);
                            put("BedOptions", null);
                            put("SleepsCount", null);
                            put("SmokingAllowed", null);
                            put("Tags", Collections.emptyList());
                        }
                    }
                ));
            }
        };

        Document expectedDoc = new Document() {
            {
                put("HotelId", "1");
                put("Rooms", Collections.singletonList(
                    new Document() {
                        {
                            put("BaseRate", null);
                            put("BedOptions", null);
                            put("SleepsCount", null);
                            put("SmokingAllowed", null);
                            put("Tags", Collections.emptyList());
                        }
                    }
                ));
            }
        };

        uploadDocument(client, originalDoc);
        List<String> selectedFields = Arrays.asList("HotelId", "Rooms/BaseRate", "Rooms/BedOptions", "Rooms/SleepsCount", "Rooms/SmokingAllowed", "Rooms/Tags");

        Document actualDoc = client.getDocument("1", selectedFields, generateRequestOptions());
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Test
    public void dynamicallyTypedPrimitiveCollectionsDoNotAllRoundtripCorrectly() {
        String indexName = setupIndexWithDataTypes();
        client = getSearchIndexClientBuilder(indexName).buildClient();

        String docKey = "1";
        OffsetDateTime dateTime = OffsetDateTime.parse("2019-08-13T14:30:00Z");
        GeoPoint geoPoint = GeoPoint.create(1.0, 100.0);

        Document indexedDoc = new Document() {
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
        };

        // This is the expected document when querying the document later
        Document expectedDoc = new Document() {
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
        };

        uploadDocument(client, indexedDoc);

        Document actualDoc = client.getDocument(docKey);
        Assert.assertEquals(expectedDoc, actualDoc);
    }
}
