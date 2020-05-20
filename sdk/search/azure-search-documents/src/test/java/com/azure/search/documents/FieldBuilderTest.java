// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.models.ComplexSearchField;
import com.azure.search.documents.models.LexicalAnalyzerName;
import com.azure.search.documents.models.SearchField;
import com.azure.search.documents.models.SearchFieldDataType;
import com.azure.search.documents.models.SearchableField;
import com.azure.search.documents.models.SimpleField;
import com.azure.search.documents.test.environment.models.Hotel;
import com.azure.search.documents.test.environment.models.HotelAnalyzerException;
import com.azure.search.documents.test.environment.models.HotelCircularDependencies;
import com.azure.search.documents.test.environment.models.HotelSearchException;
import com.azure.search.documents.test.environment.models.HotelSearchableExceptionOnList;
import com.azure.search.documents.test.environment.models.HotelTwoDimensional;
import com.azure.search.documents.test.environment.models.HotelWithEmptyInSynonymMaps;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FieldBuilderTest {
    @Test
    public void hotelComparison() {
        List<SearchField> actualFields = sortByFieldName(FieldBuilder.build(Hotel.class));
        List<SearchField> expectedFields = sortByFieldName(buildHotelFields());
        assertListFieldEquals(expectedFields, actualFields);
    }

    @Test
    public void hotelSearchableThrowException() {
        Exception exception = assertThrows(RuntimeException.class, () ->
            FieldBuilder.build(HotelSearchException.class));
        assertExceptionMassageAndDataType(exception, SearchFieldDataType.INT32, "hotelId");
    }

    @Test
    public void hotelListFieldSearchableThrowException() {
        Exception exception = assertThrows(RuntimeException.class, () ->
            FieldBuilder.build(HotelSearchableExceptionOnList.class));
        assertExceptionMassageAndDataType(exception, SearchFieldDataType.collection(SearchFieldDataType.INT32), "passcode");
    }

    @Test
    public void hotelCircularDependencies() {
        List<SearchField> actualFields = sortByFieldName(FieldBuilder.build(HotelCircularDependencies.class));
        List<SearchField> expectedFields = sortByFieldName(buildHotelCircularDependenciesModel());
        assertListFieldEquals(expectedFields, actualFields);
    }

    @Test
    public void hotelWithEmptySynonymMaps() {
        // We cannot put null in the annotation. So no need to test null case.
        List<SearchField> actualFields = FieldBuilder.build(HotelWithEmptyInSynonymMaps.class);
        List<SearchField> expectedFields = Collections.singletonList(new SearchableField("tags", true)
            .setSynonymMapNames(Arrays.asList("asynonymMaps", "maps")).build());
        assertListFieldEquals(expectedFields, actualFields);
    }

    @Test
    public void hotelWithTwoDimensionalType() {
        Exception exception = assertThrows(RuntimeException.class, () -> FieldBuilder.build(HotelTwoDimensional.class));
        assertExceptionMassageAndDataType(exception, null, "single-dimensional");
    }

    @Test
    public void hotelAnalyzerException() {
        Exception exception = assertThrows(RuntimeException.class, () ->
            FieldBuilder.build(HotelAnalyzerException.class));
        assertExceptionMassageAndDataType(exception, null,
            "either analyzer or both searchAnalyzer and indexAnalyzer");
    }

    private void assertListFieldEquals(List<SearchField> expected, List<SearchField> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            TestHelpers.assertObjectEquals(expected.get(i), actual.get(i));
        }
    }

    private void assertExceptionMassageAndDataType(Exception exception, SearchFieldDataType dataType, String msg) {
        assertTrue(exception.getMessage().contains(msg));
        if (dataType != null) {
            assertTrue(exception.getMessage().contains(dataType.toString()));
        }
    }

    private List<SearchField> buildHotelCircularDependenciesModel() {
        SearchField homeAddress = new ComplexSearchField("homeAddress", false).setFields(buildHotelInAddress()).build();
        SearchField billingAddress = new ComplexSearchField("billingAddress", false).setFields(buildHotelInAddress()).build();
        return Arrays.asList(homeAddress, billingAddress);
    }

    private List<SearchField> buildHotelInAddress() {
        SearchField hotel = new ComplexSearchField("hotel", false).build();
        return Collections.singletonList(hotel);
    }

    private List<SearchField> buildHotelFields() {
        SearchField hotelId = new SimpleField("hotelId", SearchFieldDataType.STRING, false).setSortable(true)
            .setKey(true).build();
        SearchField hotelName = new SearchableField("hotelName", false).setAnalyzer(LexicalAnalyzerName.fromString("en.lucene"))
            .setSortable(true).build();
        SearchField description = new SimpleField("description", SearchFieldDataType.STRING, false).build();
        SearchField category = new SimpleField("category", SearchFieldDataType.STRING, false).build();
        SearchField tags = new SearchableField("tags", true).build();
        SearchField parkingIncluded = new SimpleField("parkingIncluded", SearchFieldDataType.BOOLEAN, false).build();
        SearchField smokingAllowed = new SimpleField("smokingAllowed", SearchFieldDataType.BOOLEAN, false).build();
        SearchField lastRenovationDate = new SimpleField("lastRenovationDate", SearchFieldDataType.DATE_TIME_OFFSET, false).build();
        SearchField rating = new SimpleField("rating", SearchFieldDataType.INT32, false).build();
        SearchField location = new SimpleField("location", SearchFieldDataType.GEOGRAPHY_POINT, false).build();
        SearchField address = new ComplexSearchField("address", false)
            .setFields(buildHotelAddressField()).build();
        SearchField rooms = new ComplexSearchField("rooms", true).setFields(buildHotelRoomField()).build();

        return Arrays.asList(hotelId, hotelName, description, category, tags, parkingIncluded, smokingAllowed,
            lastRenovationDate, rating, location, address, rooms);
    }

    private List<SearchField> buildHotelAddressField() {
        SearchField streetAddress = new SimpleField("streetAddress", SearchFieldDataType.STRING, false).setFacetable(true)
            .setKey(true).build();
        SearchField city = new SearchableField("city", false).setFilterable(true).build();
        SearchField stateProvince = new SearchableField("stateProvince", false).build();
        SearchField country = new SearchableField("country", false)
            .setSynonymMapNames(Arrays.asList("America -> USA", "USA -> US")).build();
        SearchField postalCode = new SimpleField("postalCode", SearchFieldDataType.STRING, false).build();
        return Arrays.asList(streetAddress, city, stateProvince, country, postalCode);
    }

    private List<SearchField> buildHotelRoomField() {
        SearchField description = new SimpleField("description", SearchFieldDataType.STRING, false).build();
        SearchField descriptionFr = new SimpleField("descriptionFr", SearchFieldDataType.STRING, false).build();
        SearchField type = new SimpleField("type", SearchFieldDataType.STRING, false).build();
        SearchField baseRate = new SimpleField("baseRate", SearchFieldDataType.DOUBLE, false).build();
        SearchField bedOptions = new SimpleField("bedOptions", SearchFieldDataType.STRING, false).build();
        SearchField sleepsCount = new SimpleField("sleepsCount", SearchFieldDataType.INT32, false).build();
        SearchField smokingAllowed = new SimpleField("smokingAllowed", SearchFieldDataType.BOOLEAN, false).build();
        SearchField tags = new SimpleField("tags", SearchFieldDataType.STRING, true).build();
        return Arrays.asList(description, descriptionFr, type, baseRate, bedOptions, sleepsCount, smokingAllowed, tags);
    }

    private List<SearchField> sortByFieldName(List<SearchField> fields) {
        fields.sort(Comparator.comparing(SearchField::getName));
        return fields;
    }
}
