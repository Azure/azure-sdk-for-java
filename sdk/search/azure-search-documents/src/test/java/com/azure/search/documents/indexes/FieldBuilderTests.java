// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.search.documents.TestHelpers;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.test.environment.models.HotelAnalyzerException;
import com.azure.search.documents.test.environment.models.HotelCircularDependencies;
import com.azure.search.documents.test.environment.models.HotelRenameProperty;
import com.azure.search.documents.test.environment.models.HotelSearchException;
import com.azure.search.documents.test.environment.models.HotelSearchableExceptionOnList;
import com.azure.search.documents.test.environment.models.HotelTwoDimensional;
import com.azure.search.documents.test.environment.models.HotelWithArray;
import com.azure.search.documents.test.environment.models.HotelWithEmptyInSynonymMaps;
import com.azure.search.documents.test.environment.models.HotelWithIgnoredFields;
import com.azure.search.documents.test.environment.models.HotelWithUnsupportedField;
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
            SearchIndexClient.buildSearchFields(HotelSearchException.class, null));
        assertExceptionMassageAndDataType(exception, SearchFieldDataType.INT32, "getHotelId");
    }

    @Test
    public void hotelListFieldSearchableThrowException() {
        Exception exception = assertThrows(RuntimeException.class, () ->
            SearchIndexClient.buildSearchFields(HotelSearchableExceptionOnList.class, null));
        assertExceptionMassageAndDataType(exception, SearchFieldDataType.collection(SearchFieldDataType.INT32), "getPasscode");
    }

    @Test
    public void hotelCircularDependencies() {
        List<SearchField> actualFields = sortByFieldName(
            SearchIndexClient.buildSearchFields(HotelCircularDependencies.class, null));
        List<SearchField> expectedFields = sortByFieldName(buildHotelCircularDependenciesModel());
        assertListFieldEquals(expectedFields, actualFields);
    }

    @Test
    public void hotelWithEmptySynonymMaps() {
        // We cannot put null in the annotation. So no need to test null case.
        List<SearchField> actualFields = SearchIndexClient.buildSearchFields(HotelWithEmptyInSynonymMaps.class, null);

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
        Exception exception = assertThrows(RuntimeException.class, () ->
            SearchIndexClient.buildSearchFields(HotelTwoDimensional.class, null));
        assertExceptionMassageAndDataType(exception, null, "single-dimensional");
    }

    @Test
    public void hotelAnalyzerException() {
        Exception exception = assertThrows(RuntimeException.class, () ->
            SearchIndexClient.buildSearchFields(HotelAnalyzerException.class, null));
        assertExceptionMassageAndDataType(exception, null,
            "either analyzer or both searchAnalyzer and indexAnalyzer");
    }

    @Test
    public void hotelWithArrayType() {
        List<SearchField> actualFields = sortByFieldName(
            SearchIndexClient.buildSearchFields(HotelWithArray.class, null));
        List<SearchField> expectedFields = sortByFieldName(buildHotelWithArrayModel());
        assertListFieldEquals(expectedFields, actualFields);
    }

    @Test
    public void propertyRename() {
        List<SearchField> actualFields = sortByFieldName(
            SearchIndexClient.buildSearchFields(HotelRenameProperty.class, null));
        List<String> expectedFieldNames = Arrays.asList("HotelName", "hotelId", "description");
        Collections.sort(expectedFieldNames);
        assertEquals(expectedFieldNames.get(0), actualFields.get(0).getName());
        assertEquals(expectedFieldNames.get(1), actualFields.get(1).getName());
        assertEquals(expectedFieldNames.get(2), actualFields.get(2).getName());
    }

    @Test
    public void ignoredPropertyName() {
        List<SearchField> actualFields = SearchIndexClient.buildSearchFields(HotelWithIgnoredFields.class, null);
        assertEquals(1, actualFields.size());
        assertEquals("notIgnoredName", actualFields.get(0).getName());
    }

    @Test
    public void unsupportedFields() {
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
            SearchIndexClient.buildSearchFields(HotelWithUnsupportedField.class, null));
        System.out.println(exception.getMessage());
        assertExceptionMassageAndDataType(exception, null, "is not supported");
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

    private List<SearchField> sortByFieldName(List<SearchField> fields) {
        fields.sort(Comparator.comparing(SearchField::getName));
        return fields;
    }
}
