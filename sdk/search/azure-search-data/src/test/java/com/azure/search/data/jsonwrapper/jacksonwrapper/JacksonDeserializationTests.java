// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.data.jsonwrapper.jacksonwrapper;

import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.Config;
import com.azure.search.data.common.jsonwrapper.api.Type;
import com.azure.search.data.customization.models.GeoPoint;
import com.azure.search.data.jsonwrapper.JsonDeserializationTests;
import com.azure.search.data.models.Entry;
import com.azure.search.data.models.Hotel;
import com.azure.search.data.models.HotelAddress;
import com.azure.search.data.models.HotelRoom;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JacksonDeserializationTests extends JsonDeserializationTests {

    String jsonString;
    List<Hotel> hotels;

    @Before
    public void initialize() throws Exception {
        // createDeserializer
        jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        Assert.assertNotNull(jsonApi);

        jsonApi.configure(Config.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonApi.configureTimezone();
        jsonString = loadJsonString();
        initializeHotels();
    }

    private void initializeHotels() throws ParseException {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        hotels = new ArrayList<>();
        hotels.add(new Hotel().hotelId("1")
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
            .location(GeoPoint.create(-122.131577, 47.678581)));

        hotels.add(new Hotel().hotelId("2")
            .hotelName("Secret Point Motel")
            .description("The hotel is ideally located on the main commercial artery of the city in the heart of New York. A few minutes away is Time's Square and the historic centre of the city, as well as other places of interest that make New York one of America's most attractive and cosmopolitan cities.")
            .descriptionFr("L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus attractives et cosmopolites de l'Amérique.")
            .category("Boutique")
            .tags(Arrays.asList("pool",
                "air conditioning",
                "concierge"))
            .parkingIncluded(false)
            .lastRenovationDate(dateFormat.parse("1970-01-18T00:00:00Z"))
            .rating(4)
            .address(new HotelAddress()
                .streetAddress("677 5th Ave")
                .city("New York")
                .stateProvince("NY")
                .postalCode("10022")
                .country("USA"))
            .location(GeoPoint.create(-73.975403, 40.760586))
            .rooms(Arrays.asList(new HotelRoom().description("Budget Room, 1 Queen Bed (Cityside)")
                    .descriptionFr("Chambre Économique, 1 grand lit (côté ville)")
                    .type("Budget Room")
                    .baseRate(9.69)
                    .bedOptions("1 Queen Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(Arrays.asList("vcr/dvd")),
                new HotelRoom().description("Budget Room, 1 King Bed (Mountain View)")
                    .descriptionFr("Chambre Économique, 1 très grand lit (Mountain View)")
                    .type("Budget Room")
                    .baseRate(8.09)
                    .bedOptions("1 King Bed")
                    .sleepsCount(2)
                    .smokingAllowed(true)
                    .tags(Arrays.asList("vcr/dvd", "jacuzzi tub")))));

    }

    private String loadJsonString() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("SmallHotelsDataArray.json");
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String text;
        while ((text = bufferedReader.readLine()) != null) {
            stringBuilder.append(text);
        }

        return stringBuilder.toString();
    }

    @Test
    public void deserializeCustomObject() {
        Type<List<Hotel>> listType = new Type<List<Hotel>>() { };
        List<Hotel> results = jsonApi.readStringToList(jsonString, listType);
        Assert.assertNotNull(results);

        Assert.assertEquals(hotels.size(), results.size());

        Iterator<Hotel> expectedIterator = hotels.iterator();
        Iterator<Hotel> actualIterator = results.iterator();
        while (expectedIterator.hasNext() && actualIterator.hasNext()) {
            Hotel actual = actualIterator.next();
            Hotel expected = expectedIterator.next();
            Assert.assertEquals(actual, expected);
        }
    }

    @Test
    public void deserializeDate() throws ParseException {
        String json = "{ \"date\" : \"1970-01-18T00:00:00Z\"}";

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date date = dateFormat.parse("1970-01-18T00:00:00Z");

        Entry entry = jsonApi.readString(json, Entry.class);
        Assert.assertEquals(date, entry.date());
    }

    @Test
    public void convertMap2Object() {
        Map<String, Object> document = new HashMap<>();
        document.put("HotelId", 1);
        document.put("HotelName", "Fancy Stay");

        Hotel expected = new Hotel().hotelId("1").hotelName("Fancy Stay");
        Hotel actual = jsonApi.convertObjectToType(document, Hotel.class);
        Assert.assertEquals(expected, actual);
    }
}
