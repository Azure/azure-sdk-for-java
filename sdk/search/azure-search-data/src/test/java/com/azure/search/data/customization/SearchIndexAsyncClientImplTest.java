// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.data.customization.models.GeoPoint;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SearchIndexAsyncClientImplTest extends SearchIndexClientTestBase {

    private static final CharSequence ERROR_MESSAGE_INVALID_FIELDS_REQUEST =
        "Invalid expression: Could not find a property named 'ThisFieldDoesNotExist' on type 'search.document'.";
    private static final String INDEX_NAME = "hotels";
    private SearchIndexAsyncClient asyncClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
        asyncClient = builderSetup().indexName(INDEX_NAME).buildAsyncClient();
    }

    @Test
    public void canGetDynamicDocument() {
        JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        Map<String, Object> addressDoc = new HashMap<String, Object>();
        addressDoc.put("StreetAddress", "677 5th Ave");
        addressDoc.put("City", "New York");
        addressDoc.put("StateProvince", "NY");
        addressDoc.put("Country", "USA");
        addressDoc.put("PostalCode", "10022");

        ArrayList<String> room1Tags = new ArrayList<String>();
        room1Tags.add("vcr/dvd");

        HashMap<String, Object> room1Doc = new HashMap<String, Object>();
        room1Doc.put("Description", "Budget Room, 1 Queen Bed (Cityside)");
        room1Doc.put("Description_fr", "Chambre Économique, 1 grand lit (côté ville)");
        room1Doc.put("Type", "Budget Room");
        room1Doc.put("BaseRate", 9.69);
        room1Doc.put("BedOptions", "1 Queen Bed");
        room1Doc.put("SleepsCount", 2);
        room1Doc.put("SmokingAllowed", true);
        room1Doc.put("Tags", room1Tags);

        ArrayList<String> room2Tags = new ArrayList<String>();
        room2Tags.add("vcr/dvd");
        room2Tags.add("jacuzzi tub");

        HashMap<String, Object> room2Doc = new HashMap<String, Object>();
        room2Doc.put("Description", "Budget Room, 1 King Bed (Mountain View)");
        room2Doc.put("Description_fr", "Chambre Économique, 1 très grand lit (Mountain View)");
        room2Doc.put("Type", "Budget Room");
        room2Doc.put("BaseRate", 8.09);
        room2Doc.put("BedOptions", "1 King Bed");
        room2Doc.put("SleepsCount", 2);
        room2Doc.put("SmokingAllowed", true);
        room2Doc.put("Tags", room2Tags);

        ArrayList<HashMap<String, Object>> rooms = new ArrayList<HashMap<String, Object>>();
        rooms.add(room1Doc);
        rooms.add(room2Doc);

        ArrayList<String> tags = new ArrayList<String>();
        tags.add("pool");
        tags.add("air conditioning");
        tags.add("concierge");

        HashMap<String, Object> expectedDoc = new HashMap<String, Object>();
        expectedDoc.put("HotelId", "1");
        expectedDoc.put("HotelName", "Secret Point Motel");
        expectedDoc.put(
            "Description",
            "The hotel is ideally located on the main commercial artery of the city in the heart of New York. A few "
                + "minutes away is Time's Square and the historic centre of the city, as well as other places of "
                + "interest that make New York one of America's most attractive and cosmopolitan cities.");
        expectedDoc.put(
            "Description_fr",
            "L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein cœur de New York."
                + " A quelques minutes se trouve la place du temps et le centre historique de la ville, ainsi que "
                + "d'autres lieux d'intérêt qui font de New York l'une des villes les plus attractives et cosmopolites "
                + "de l'Amérique.");
        expectedDoc.put("Category", "Boutique");
        expectedDoc.put("Tags", tags);
        expectedDoc.put("ParkingIncluded", false);
        expectedDoc.put("SmokingAllowed", true);
        expectedDoc.put("LastRenovationDate", "1970-01-18T00:00:00Z");
        expectedDoc.put("Rating", 3);
        expectedDoc.put("Address", addressDoc);
        expectedDoc.put("Rooms", rooms);
        expectedDoc.put(
            "Location",
            jsonApi.convertObjectToType(GeoPoint.create(40.760586, -73.975403), Map.class));

        uploadDocument(asyncClient, INDEX_NAME, expectedDoc);

        Mono<Document> futureDoc = asyncClient.getDocument("1");

        StepVerifier
            .create(futureDoc)
            .assertNext(result -> Assert.assertEquals(expectedDoc, result))
            .verifyComplete();
    }

    @Test
    public void getDocumentThrowsWhenDocumentNotFound() {
        Mono<Document> futureDoc = asyncClient.getDocument("1000000001");
        StepVerifier
            .create(futureDoc)
            .verifyErrorSatisfies(error -> assertEquals(ResourceNotFoundException.class, error.getClass()));
    }

    @Test
    public void getDocumentThrowsWhenRequestIsMalformed() {

        HashMap<String, Object> hotelDoc = new HashMap<String, Object>();
        hotelDoc.put("HotelId", "2");
        hotelDoc.put("Description", "Surprisingly expensive");

        ArrayList<String> selectedFields = new ArrayList<String>();
        selectedFields.add("HotelId");
        selectedFields.add("ThisFieldDoesNotExist");

        uploadDocument(asyncClient, INDEX_NAME, hotelDoc);

        Mono futureDoc = asyncClient.getDocument("2", selectedFields, null);

        StepVerifier
            .create(futureDoc)
            .verifyErrorSatisfies(error -> {
                assertEquals(HttpResponseException.class, error.getClass());
                assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error).response().statusCode());
                assertTrue(error.getMessage().contains(ERROR_MESSAGE_INVALID_FIELDS_REQUEST));
            });
    }
}
