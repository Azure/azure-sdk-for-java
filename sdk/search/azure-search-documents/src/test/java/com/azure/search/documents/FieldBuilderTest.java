// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.search.documents.models.AnalyzerName;
import com.azure.search.documents.models.ComplexField;
import com.azure.search.documents.models.DataType;
import com.azure.search.documents.models.Field;
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
        List<Field> actualFields = sortByFieldName(FieldBuilder.build(Hotel.class));
        List<Field> expectedFields = sortByFieldName(buildHotelFields());
        assertListFieldEquals(expectedFields, actualFields);
    }

    @Test
    public void hotelSearchableThrowException() {
        Exception exception = assertThrows(RuntimeException.class, () ->
            FieldBuilder.build(HotelSearchException.class));
        assertExceptionMassageAndDataType(exception, DataType.EDM_INT32, "hotelId");
    }

    @Test
    public void hotelListFieldSearchableThrowException() {
        Exception exception = assertThrows(RuntimeException.class, () ->
            FieldBuilder.build(HotelSearchableExceptionOnList.class));
        assertExceptionMassageAndDataType(exception, DataType.collection(DataType.EDM_INT32), "passcode");
    }

    @Test
    public void hotelCircularDependencies() {
        List<Field> actualFields = sortByFieldName(FieldBuilder.build(HotelCircularDependencies.class));
        List<Field> expectedFields = sortByFieldName(buildHotelCircularDependenciesModel());
        assertListFieldEquals(expectedFields, actualFields);
    }

    @Test
    public void hotelWithEmptySynonymMaps() {
        // We cannot put null in the annotation. So no need to test null case.
        List<Field> actualFields = FieldBuilder.build(HotelWithEmptyInSynonymMaps.class);
        List<Field> expectedFields = Collections.singletonList(new SearchableField("tags", true)
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

    private void assertListFieldEquals(List<Field> expected, List<Field> actual) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            TestHelpers.assertObjectEquals(expected.get(i), actual.get(i));
        }
    }

    private void assertExceptionMassageAndDataType(Exception exception, DataType dataType, String msg) {
        assertTrue(exception.getMessage().contains(msg));
        if (dataType != null) {
            assertTrue(exception.getMessage().contains(dataType.toString()));
        }
    }

    private List<Field> buildHotelCircularDependenciesModel() {
        Field homeAddress = new ComplexField("homeAddress", false).setFields(buildHotelInAddress()).build();
        Field billingAddress = new ComplexField("billingAddress", false).setFields(buildHotelInAddress()).build();
        return Arrays.asList(homeAddress, billingAddress);
    }

    private List<Field> buildHotelInAddress() {
        Field hotel = new ComplexField("hotel", false).build();
        return Collections.singletonList(hotel);
    }

    private List<Field> buildHotelFields() {
        Field hotelId = new SimpleField("hotelId", DataType.EDM_STRING, false).setSortable(true)
            .setKey(true).build();
        Field hotelName = new SearchableField("hotelName", false).setAnalyzer(AnalyzerName.fromString("en.lucene"))
            .setSortable(true).build();
        Field description = new SimpleField("description", DataType.EDM_STRING, false).build();
        Field category = new SimpleField("category", DataType.EDM_STRING, false).build();
        Field tags = new SearchableField("tags", true).build();
        Field parkingIncluded = new SimpleField("parkingIncluded", DataType.EDM_BOOLEAN, false).build();
        Field smokingAllowed = new SimpleField("smokingAllowed", DataType.EDM_BOOLEAN, false).build();
        Field lastRenovationDate = new SimpleField("lastRenovationDate", DataType.EDM_DATE_TIME_OFFSET, false).build();
        Field rating = new SimpleField("rating", DataType.EDM_INT32, false).build();
        Field location = new SimpleField("location", DataType.EDM_GEOGRAPHY_POINT, false).build();
        Field address = new ComplexField("address", false)
            .setFields(buildHotelAddressField()).build();
        Field rooms = new ComplexField("rooms", true).setFields(buildHotelRoomField()).build();

        return Arrays.asList(hotelId, hotelName, description, category, tags, parkingIncluded, smokingAllowed,
            lastRenovationDate, rating, location, address, rooms);
    }

    private List<Field> buildHotelAddressField() {
        Field streetAddress = new SimpleField("streetAddress", DataType.EDM_STRING, false).setFacetable(true)
            .setKey(true).build();
        Field city = new SearchableField("city", false).setFilterable(true).build();
        Field stateProvince = new SearchableField("stateProvince", false).build();
        Field country = new SearchableField("country", false)
            .setSynonymMapNames(Arrays.asList("America -> USA", "USA -> US")).build();
        Field postalCode = new SimpleField("postalCode", DataType.EDM_STRING, false).build();
        return Arrays.asList(streetAddress, city, stateProvince, country, postalCode);
    }

    private List<Field> buildHotelRoomField() {
        Field description = new SimpleField("description", DataType.EDM_STRING, false).build();
        Field descriptionFr = new SimpleField("descriptionFr", DataType.EDM_STRING, false).build();
        Field type = new SimpleField("type", DataType.EDM_STRING, false).build();
        Field baseRate = new SimpleField("baseRate", DataType.EDM_DOUBLE, false).build();
        Field bedOptions = new SimpleField("bedOptions", DataType.EDM_STRING, false).build();
        Field sleepsCount = new SimpleField("sleepsCount", DataType.EDM_INT32, false).build();
        Field smokingAllowed = new SimpleField("smokingAllowed", DataType.EDM_BOOLEAN, false).build();
        Field tags = new SimpleField("tags", DataType.EDM_STRING, true).build();
        return Arrays.asList(description, descriptionFr, type, baseRate, bedOptions, sleepsCount, smokingAllowed, tags);
    }

    private List<Field> sortByFieldName(List<Field> fields) {
        fields.sort(Comparator.comparing(Field::getName));
        return fields;
    }
}
