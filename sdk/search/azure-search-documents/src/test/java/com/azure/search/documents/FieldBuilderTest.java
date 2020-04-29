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
import com.azure.search.documents.test.environment.models.HotelSearchException;
import com.azure.search.documents.test.environment.models.HotelSearchableExceptionOnList;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FieldBuilderTest {
    @Test
    public void hotelComparison() {
        List<Field> actualFields = sortByFieldName(FieldBuilder.build(Hotel.class));
        List<Field> expectedFields = sortByFieldName(buildHotelFieldsFromModel());
        assertEquals(expectedFields.size(), actualFields.size());
        for (int i = 0; i < expectedFields.size(); i++) {
            TestHelpers.assertObjectEquals(expectedFields.get(i), actualFields.get(i));
        }
    }

    @Test
    public void hotelSearchableThrowException() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            FieldBuilder.build(HotelSearchException.class);
        });
        assertTrue(exception.getMessage().contains("hotelId"));
        assertTrue(exception.getMessage().contains(DataType.EDM_INT32.toString()));
    }

    @Test
    public void hotelListFieldSearchableThrowException() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            FieldBuilder.build(HotelSearchableExceptionOnList.class);
        });
        assertTrue(exception.getMessage().contains("passcode"));
        assertTrue(exception.getMessage().contains(DataType.collection(DataType.EDM_INT32).toString()));
    }

    private List<Field> buildHotelFieldsFromModel() {
        Field hotelId = new SimpleField("hotelId", DataType.EDM_STRING).setSortable(true)
            .setKey(true).build();
        Field hotelName = new SearchableField("hotelName", false).setAnalyzer(AnalyzerName.fromString("en.lucene"))
            .setSortable(true).build();
        Field description = new SimpleField("description", DataType.EDM_STRING).build();
        Field category = new SimpleField("category", DataType.EDM_STRING).build();
        Field tags = new SearchableField("tags", true).build();
        Field parkingIncluded = new SimpleField("parkingIncluded", DataType.EDM_BOOLEAN).build();
        Field smokingAllowed = new SimpleField("smokingAllowed", DataType.EDM_BOOLEAN).build();
        Field lastRenovationDate = new SimpleField("lastRenovationDate", DataType.EDM_DATE_TIME_OFFSET).build();
        Field rating = new SimpleField("rating", DataType.EDM_INT32).build();
        Field location = new SimpleField("location", DataType.EDM_GEOGRAPHY_POINT).build();
        Field address = new ComplexField("address", false)
            .setSubFields(buildHotelAddressField()).build();
        Field rooms = new ComplexField("rooms", true).setSubFields(buildHotelRoomField()).build();

        return Arrays.asList(hotelId, hotelName, description, category, tags, parkingIncluded, smokingAllowed,
            lastRenovationDate, rating, location, address, rooms);
    }

    private List<Field> buildHotelAddressField() {
        Field streetAddress = new SimpleField("streetAddress", DataType.EDM_STRING).setFacetable(true)
            .setKey(true).build();
        Field city = new SearchableField("city", false).setFilterable(true).build();
        Field stateProvince = new SearchableField("stateProvince", false).build();
        Field country = new SearchableField("country", false)
            .setSynonymMaps(Arrays.asList("America -> USA", "USA -> US")).build();
        Field postalCode = new SimpleField("postalCode", DataType.EDM_STRING).build();
        return Arrays.asList(streetAddress, city, stateProvince, country, postalCode);
    }

    private List<Field> buildHotelRoomField() {
        Field description = new SimpleField("description", DataType.EDM_STRING).build();
        Field descriptionFr = new SimpleField("descriptionFr", DataType.EDM_STRING).build();
        Field type = new SimpleField("type", DataType.EDM_STRING).build();
        Field baseRate = new SimpleField("baseRate", DataType.EDM_DOUBLE).build();
        Field bedOptions = new SimpleField("bedOptions", DataType.EDM_STRING).build();
        Field sleepsCount = new SimpleField("sleepsCount", DataType.EDM_INT32).build();
        Field smokingAllowed = new SimpleField("smokingAllowed", DataType.EDM_BOOLEAN).build();
        Field tags = new SimpleField("tags", DataType.collection(DataType.EDM_STRING)).build();
        return Arrays.asList(description, descriptionFr, type, baseRate, bedOptions, sleepsCount, smokingAllowed, tags);
    }

    private List<Field> sortByFieldName(List<Field> fields) {
        return fields.stream().sorted((o1, o2) -> o1.getName().compareTo(o2.getName())).collect(Collectors.toList());
    }
}
