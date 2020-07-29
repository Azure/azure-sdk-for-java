// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.search.documents.TestHelpers;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
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

        List<SearchField> expectedFields = Collections.singletonList(new SearchField("tags",
            SearchFieldDataType.collection(SearchFieldDataType.STRING))
            .setSearchable(true)
            .setKey(false)
            .setFilterable(false)
            .setSortable(false)
            .setFacetable(false)
            .setSynonymMapNames(Arrays.asList("asynonymMaps", "maps")));

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
        SearchField homeAddress = new SearchField("homeAddress", SearchFieldDataType.COMPLEX)
            .setFields(buildHotelInAddress());
        SearchField billingAddress = new SearchField("billingAddress", SearchFieldDataType.COMPLEX)
            .setFields(buildHotelInAddress());
        return Arrays.asList(homeAddress, billingAddress);
    }

    private List<SearchField> buildHotelInAddress() {
        SearchField hotel = new SearchField("hotel", SearchFieldDataType.COMPLEX);
        return Collections.singletonList(hotel);
    }

    private List<SearchField> buildHotelWithArrayModel() {
        SearchField hotelId = new SearchField("hotelId", SearchFieldDataType.STRING)
            .setKey(true)
            .setSortable(true)
            .setSearchable(false)
            .setFacetable(false)
            .setFilterable(false);
        SearchField tags = new SearchField("tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
            .setKey(false)
            .setSearchable(true)
            .setSortable(false)
            .setFilterable(false)
            .setFacetable(false);
        return Arrays.asList(hotelId, tags);
    }

    private List<SearchField> buildHotelAddressField() {
        SearchField streetAddress = new SearchField("streetAddress", SearchFieldDataType.STRING).setFacetable(true)
            .setKey(true);
        SearchField city = new SearchField("city", SearchFieldDataType.STRING).setFilterable(true);
        SearchField stateProvince = new SearchField("stateProvince", SearchFieldDataType.STRING);
        SearchField country =
            new SearchField("country", SearchFieldDataType.STRING).setSynonymMapNames(Arrays.asList("America -> USA",
                "USA -> US"));
        SearchField postalCode = new SearchField("postalCode", SearchFieldDataType.STRING);
        return Arrays.asList(streetAddress, city, stateProvince, country, postalCode);
    }

    private List<SearchField> buildHotelRoomField() {
        SearchField description = new SearchField("description", SearchFieldDataType.STRING);
        SearchField descriptionFr = new SearchField("descriptionFr", SearchFieldDataType.STRING);
        SearchField type = new SearchField("type", SearchFieldDataType.STRING);
        SearchField baseRate = new SearchField("baseRate", SearchFieldDataType.DOUBLE);
        SearchField bedOptions = new SearchField("bedOptions", SearchFieldDataType.STRING);
        SearchField sleepsCount = new SearchField("sleepsCount", SearchFieldDataType.INT32);
        SearchField smokingAllowed = new SearchField("smokingAllowed", SearchFieldDataType.BOOLEAN);
        SearchField tags = new SearchField("tags", SearchFieldDataType.collection(SearchFieldDataType.STRING));
        return Arrays.asList(description, descriptionFr, type, baseRate, bedOptions, sleepsCount, smokingAllowed, tags);
    }

    private List<SearchField> sortByFieldName(List<SearchField> fields) {
        fields.sort(Comparator.comparing(SearchField::getName));
        return fields;
    }
}
