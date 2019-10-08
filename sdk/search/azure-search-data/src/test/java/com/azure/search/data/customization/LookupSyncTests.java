// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.search.data.customization.models.GeoPoint;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.test.environment.models.Hotel;
import com.azure.search.test.environment.models.HotelAddress;
import com.azure.search.test.environment.models.HotelRoom;
import com.azure.search.test.environment.models.ModelWithPrimitiveCollections;
import org.junit.Assert;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;

public class LookupSyncTests extends LookupTestBase {
    private SearchIndexClient client;

    @Override
    public void canGetStaticallyTypedDocument() throws ParseException {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildClient();

        Hotel expected = prepareExpectedHotel();
        uploadDocument(client, expected);

        Document result = client.getDocument(expected.hotelId());
        Hotel actual = result.as(Hotel.class);
        Assert.assertEquals(expected, actual);
    }

    @Override
    public void canGetStaticallyTypedDocumentWithNullOrEmptyValues() {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildClient();

        Hotel expected = prepareEmptyHotel();
        uploadDocument(client, expected);

        Document result = client.getDocument(expected.hotelId());
        Hotel actual = result.as(Hotel.class);
        Assert.assertEquals(expected, actual);
    }

    @Override
    public void canGetStaticallyTypedDocumentWithPascalCaseFields() {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildClient();

        Hotel expected = preparePascalCaseFieldsHotel();
        uploadDocument(client, expected);

        Document result = client.getDocument(expected.hotelId());
        Hotel actual = result.as(Hotel.class);
        Assert.assertEquals(expected, actual);
    }

    @Override
    public void canRoundtripStaticallyTypedPrimitiveCollections() throws ParseException {
        setupIndexFromJsonFile(MODEL_WITH_DATA_TYPES_INDEX_JSON);
        client = getClientBuilder(DATA_TYPES_INDEX_NAME).buildClient();

        ModelWithPrimitiveCollections expected = preparePrimitivesModel();
        uploadDocument(client, expected);

        Document result = client.getDocument(expected.key);
        ModelWithPrimitiveCollections actual = result.as(ModelWithPrimitiveCollections.class);
        Assert.assertEquals(expected, actual);
    }

    @Override
    public void getStaticallyTypedDocumentSetsUnselectedFieldsToNull() throws ParseException {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildClient();

        Hotel indexedDoc = prepareSelectedFieldsHotel();
        Hotel expected = new Hotel()
            .hotelName("Countryside Hotel")
            .description("Save up to 50% off traditional hotels.  Free WiFi, great location near downtown, full kitchen, washer & dryer, 24/7 support, bowling alley, fitness center and more.")
            .address(new HotelAddress().city("Durham"))
            .rooms(Arrays.asList(new HotelRoom().baseRate(2.44), new HotelRoom().baseRate(7.69)));

        uploadDocument(client, indexedDoc);

        List<String> selectedFields = Arrays.asList("Description", "HotelName", "Address/City", "Rooms/BaseRate");
        Document result = client.getDocument(indexedDoc.hotelId(), selectedFields, new SearchRequestOptions());
        Hotel actual = result.as(Hotel.class);
        Assert.assertEquals(expected, actual);
    }

    @Override
    public void canGetDynamicDocumentWithNullOrEmptyValues() {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildClient();

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
                put("Rooms", Arrays.asList(
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

        Document actualDoc = client.getDocument("1", selectedFields, new SearchRequestOptions());
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Override
    public void getDynamicDocumentWithEmptyObjectsReturnsObjectsFullOfNulls() {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildClient();

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

        Document actualDoc = client.getDocument("1", selectedFields, new SearchRequestOptions());
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Override
    public void emptyDynamicallyTypedPrimitiveCollectionsRoundtripAsObjectArrays() {
        setupIndexFromJsonFile(MODEL_WITH_DATA_TYPES_INDEX_JSON);
        client = getClientBuilder(DATA_TYPES_INDEX_NAME).buildClient();

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

    @Override
    public void emptyDynamicObjectsInCollectionExpandedOnGetWhenCollectionFieldSelected() {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildClient();

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

        Document actualDoc = client.getDocument("1", selectedFields, new SearchRequestOptions());
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Override
    public void emptyDynamicObjectsOmittedFromCollectionOnGetWhenSubFieldsSelected() {
        createHotelIndex();
        client = getClientBuilder(INDEX_NAME).buildClient();

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

        Document actualDoc = client.getDocument("1", selectedFields, new SearchRequestOptions());
        Assert.assertEquals(expectedDoc, actualDoc);
    }

    @Override
    public void dynamicallyTypedPrimitiveCollectionsDoNotAllRoundtripCorrectly() throws ParseException {
        setupIndexFromJsonFile(MODEL_WITH_DATA_TYPES_INDEX_JSON);
        client = getClientBuilder(DATA_TYPES_INDEX_NAME).buildClient();

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
