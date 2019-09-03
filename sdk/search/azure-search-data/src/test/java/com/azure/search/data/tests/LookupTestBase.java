// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.tests;

import com.azure.search.data.common.jsonwrapper.jacksonwrapper.JacksonDeserializer;
import com.azure.search.data.common.jsonwrapper.JsonWrapper;
import com.azure.search.data.common.jsonwrapper.api.Config;
import com.azure.search.data.common.jsonwrapper.api.JsonApi;
import com.azure.search.data.env.SearchIndexClientTestBase;
import com.azure.search.data.generated.models.IndexAction;
import com.azure.search.data.generated.models.IndexActionType;
import com.azure.search.data.models.GeographyPoint;
import com.azure.search.data.models.Hotel;
import com.azure.search.data.models.HotelAddress;
import com.azure.search.data.models.HotelRoom;
import com.azure.search.data.models.ModelWithPrimitiveCollections;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.Double.*;

public abstract class LookupTestBase extends SearchIndexClientTestBase {

    protected static final String INDEX_NAME = "hotels";
    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    protected void beforeTest() {
        super.beforeTest();
        initializeClient();
    }

    protected Hotel prepareExpectedHotel() throws ParseException {
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
            .location(
                new GeographyPoint().type("Point").coordinates(Arrays.asList(-122.131577, 47.678581)))
            .rooms(new ArrayList<>());
    }

    protected Hotel prepareEmptyHotel() {
        return new Hotel().hotelId("1")
            .tags(new ArrayList<>())
            .rooms(Arrays.asList(
                new HotelRoom().tags(new ArrayList<>())
            ));
    }

    protected Hotel preparePascalCaseFieldsHotel() {
        return new Hotel().hotelId("123").hotelName("Lord of the Rings").description("J.R.R").descriptionFr("Tolkien");
    }

    protected Hotel prepareSelectedFieldsHotel() throws ParseException {
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
            .location(new GeographyPoint().type("Point").coordinates(Arrays.asList(35.904160, -78.940483)))
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
                    .tags(Arrays.asList("coffee maker")),
                new HotelRoom()
                    .description("Budget Room, 1 Queen Bed (Amenities)")
                    .descriptionFr("Chambre Économique, 1 grand lit (Services)")
                    .type("Budget Room")
                    .baseRate(7.69)
                    .bedOptions("1 Queen Bed")
                    .sleepsCount(2)
                    .smokingAllowed(false)
                    .tags(Arrays.asList("coffee maker"))));
    }

    protected ModelWithPrimitiveCollections preparePrimitivesModel() throws ParseException {
        return new ModelWithPrimitiveCollections()
            .key("1")
            .bools(new Boolean[]{true, false})
            .dates(new Date[]{DATE_FORMAT.parse("2019-04-14T14:24:00Z"), DATE_FORMAT.parse("1999-12-31T23:59:59Z")})
            .doubles(new Double[]{NEGATIVE_INFINITY, 0.0, 2.78, NaN, 3.14, POSITIVE_INFINITY})
            .ints(new int[]{1, 2, 3, 4, -13, 5, 0})
            .longs(new Long[]{-9_999_999_999_999_999L, 832_372_345_832_523L})
            //TODO (Nava) remove uncomment to test GeoPoint oce issue resolved
            /*.points(new GeographyPoint[]{
                new GeographyPoint().type("Point").coordinates(Arrays.asList(49.0, -67.0)),
                new GeographyPoint().type("Point").coordinates(Arrays.asList(47.0, 21.0))})*/
            .points(new GeographyPoint[]{})
            .strings(new String[]{ "hello", "2019-04-14T14:56:00-07:00"});
    }

    protected <T> void uploadDocuments(T uploadDoc) throws Exception {
        JsonApi jsonApi = JsonWrapper.newInstance(JacksonDeserializer.class);
        jsonApi.configure(Config.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonApi.configureTimezone();
        Map<String, Object> document = jsonApi.convertObjectToType(uploadDoc, Map.class);
        List<IndexAction> indexActions = new LinkedList<>();
        indexActions.add(new IndexAction()
            .actionType(IndexActionType.UPLOAD)
            .additionalProperties(document));

        indexDocuments(indexActions);

        // Wait 2 secs to allow index request to finish
        Thread.sleep(2000);
    }

    @Test
    public abstract void canGetStaticallyTypedDocument() throws Exception;

    @Test
    public abstract void canGetStaticallyTypedDocumentWithNullOrEmptyValues() throws Exception;

    @Test
    public abstract void canGetStaticallyTypedDocumentWithPascalCaseFields() throws Exception;

    @Test
    public abstract void canRoundtripStaticallyTypedPrimitiveCollections() throws Exception;

    @Test
    public abstract void getStaticallyTypedDocumentSetsUnselectedFieldsToNull() throws Exception;

    protected abstract void indexDocuments(List<IndexAction> indexActions);

    protected abstract void initializeClient();
}
