// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.search.documents.TestHelpers;
import com.azure.search.documents.indexes.models.ComplexFieldBuilder;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchableFieldBuilder;
import com.azure.search.documents.indexes.models.SimpleFieldBuilder;
import com.azure.search.documents.test.environment.models.HotelAnalyzerException;
import com.azure.search.documents.test.environment.models.HotelCircularDependencies;
import com.azure.search.documents.test.environment.models.HotelSearchException;
import com.azure.search.documents.test.environment.models.HotelSearchableExceptionOnList;
import com.azure.search.documents.test.environment.models.HotelTwoDimensional;
import com.azure.search.documents.test.environment.models.HotelWithArray;
import com.azure.search.documents.test.environment.models.HotelWithEmptyInSynonymMaps;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FieldBuilderTests {

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
        List<SearchField> expectedFields = Collections.singletonList(new SearchableFieldBuilder("tags", true)
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

    @Test
    public void hotelWithArrayType() {
        List<SearchField> actualFields = sortByFieldName(FieldBuilder.build(HotelWithArray.class));
        List<SearchField> expectedFields = sortByFieldName(buildHotelWithArrayModel());
        assertListFieldEquals(expectedFields, actualFields);
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
        SearchField homeAddress = new ComplexFieldBuilder("homeAddress", false).setFields(buildHotelInAddress()).build();
        SearchField billingAddress = new ComplexFieldBuilder("billingAddress", false).setFields(buildHotelInAddress()).build();
        return Arrays.asList(homeAddress, billingAddress);
    }

    private List<SearchField> buildHotelInAddress() {
        SearchField hotel = new ComplexFieldBuilder("hotel", false).build();
        return Collections.singletonList(hotel);
    }

    private List<SearchField> buildHotelWithArrayModel() {
        SearchField hotelId = new SimpleFieldBuilder("hotelId", SearchFieldDataType.STRING, false).setKey(true)
            .setSortable(true).build();
        SearchField tags = new SearchableFieldBuilder("tags", true).build();
        return Arrays.asList(hotelId, tags);
    }

    private List<SearchField> buildHotelAddressField() {
        SearchField streetAddress = new SimpleFieldBuilder("streetAddress", SearchFieldDataType.STRING, false).setFacetable(true)
            .setKey(true).build();
        SearchField city = new SearchableFieldBuilder("city", false).setFilterable(true).build();
        SearchField stateProvince = new SearchableFieldBuilder("stateProvince", false).build();
        SearchField country = new SearchableFieldBuilder("country", false)
            .setSynonymMapNames(Arrays.asList("America -> USA", "USA -> US")).build();
        SearchField postalCode = new SimpleFieldBuilder("postalCode", SearchFieldDataType.STRING, false).build();
        return Arrays.asList(streetAddress, city, stateProvince, country, postalCode);
    }

    private List<SearchField> buildHotelRoomField() {
        SearchField description = new SimpleFieldBuilder("description", SearchFieldDataType.STRING, false).build();
        SearchField descriptionFr = new SimpleFieldBuilder("descriptionFr", SearchFieldDataType.STRING, false).build();
        SearchField type = new SimpleFieldBuilder("type", SearchFieldDataType.STRING, false).build();
        SearchField baseRate = new SimpleFieldBuilder("baseRate", SearchFieldDataType.DOUBLE, false).build();
        SearchField bedOptions = new SimpleFieldBuilder("bedOptions", SearchFieldDataType.STRING, false).build();
        SearchField sleepsCount = new SimpleFieldBuilder("sleepsCount", SearchFieldDataType.INT32, false).build();
        SearchField smokingAllowed = new SimpleFieldBuilder("smokingAllowed", SearchFieldDataType.BOOLEAN, false).build();
        SearchField tags = new SimpleFieldBuilder("tags", SearchFieldDataType.STRING, true).build();
        return Arrays.asList(description, descriptionFr, type, baseRate, bedOptions, sleepsCount, smokingAllowed, tags);
    }

    private List<SearchField> sortByFieldName(List<SearchField> fields) {
        fields.sort(Comparator.comparing(SearchField::getName));
        return fields;
    }
}
