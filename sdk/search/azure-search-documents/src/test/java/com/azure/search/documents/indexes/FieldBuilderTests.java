// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.models.GeoPoint;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Execution(ExecutionMode.CONCURRENT)
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
        assertExceptionMassageAndDataType(exception, SearchFieldDataType.collection(SearchFieldDataType.INT32),
            "getPasscode");
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
            .setStored(true)
            .setHidden(false)
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
    public void supportedFields() {
        List<SearchField> fields = SearchIndexClient.buildSearchFields(AllSupportedFields.class, null);

        assertEquals(25, fields.size());

        Map<String, SearchFieldDataType> fieldToDataType = fields.stream()
            .collect(Collectors.toMap(SearchField::getName, SearchField::getType));

        assertEquals(SearchFieldDataType.INT32, fieldToDataType.get("nullableInt"));
        assertEquals(SearchFieldDataType.INT32, fieldToDataType.get("primitiveInt"));
        assertEquals(SearchFieldDataType.INT64, fieldToDataType.get("nullableLong"));
        assertEquals(SearchFieldDataType.INT64, fieldToDataType.get("primitiveLong"));
        assertEquals(SearchFieldDataType.DOUBLE, fieldToDataType.get("nullableDouble"));
        assertEquals(SearchFieldDataType.DOUBLE, fieldToDataType.get("primitiveDouble"));
        assertEquals(SearchFieldDataType.BOOLEAN, fieldToDataType.get("nullableBoolean"));
        assertEquals(SearchFieldDataType.BOOLEAN, fieldToDataType.get("primitiveBoolean"));
        assertEquals(SearchFieldDataType.STRING, fieldToDataType.get("string"));
        assertEquals(SearchFieldDataType.STRING, fieldToDataType.get("charSequence"));
        assertEquals(SearchFieldDataType.STRING, fieldToDataType.get("nullableChar"));
        assertEquals(SearchFieldDataType.STRING, fieldToDataType.get("primitiveChar"));
        assertEquals(SearchFieldDataType.DATE_TIME_OFFSET, fieldToDataType.get("date"));
        assertEquals(SearchFieldDataType.DATE_TIME_OFFSET, fieldToDataType.get("offsetDateTime"));
        assertEquals(SearchFieldDataType.GEOGRAPHY_POINT, fieldToDataType.get("geoPoint"));
        assertEquals(SearchFieldDataType.collection(SearchFieldDataType.INT32), fieldToDataType.get("intArray"));
        assertEquals(SearchFieldDataType.collection(SearchFieldDataType.INT32), fieldToDataType.get("intList"));
        assertEquals(SearchFieldDataType.collection(SearchFieldDataType.SINGLE), fieldToDataType.get("floatArray"));
        assertEquals(SearchFieldDataType.collection(SearchFieldDataType.SINGLE), fieldToDataType.get("floatList"));
        assertEquals(SearchFieldDataType.INT16, fieldToDataType.get("nullableShort"));
        assertEquals(SearchFieldDataType.INT16, fieldToDataType.get("primitiveShort"));
        assertEquals(SearchFieldDataType.SBYTE, fieldToDataType.get("nullableByte"));
        assertEquals(SearchFieldDataType.SBYTE, fieldToDataType.get("primitiveByte"));
        assertEquals(SearchFieldDataType.collection(SearchFieldDataType.SBYTE), fieldToDataType.get("byteArray"));
        assertEquals(SearchFieldDataType.collection(SearchFieldDataType.SBYTE), fieldToDataType.get("byteList"));

    }

    @SuppressWarnings({"unused", "UseOfObsoleteDateTimeApi"})
    private static final class AllSupportedFields {
        // 1. name = 'nullableInt', OData type = INT32
        private Integer nullableInt;
        public Integer getNullableInt() {
            return nullableInt;
        }

        // 2. name = 'primitiveInt', OData type = INT32
        private int primitiveInt;
        public int getPrimitiveInt() {
            return primitiveInt;
        }

        // 3. name = 'nullableLong', OData type = INT64
        private Long nullableLong;
        public Long getNullableLong() {
            return nullableLong;
        }

        // 4. name = 'primitiveLong', OData type = INT64
        private long primitiveLong;
        public long getPrimitiveLong() {
            return primitiveLong;
        }

        // 5. name = 'nullableDouble', OData type = DOUBLE
        private Double nullableDouble;
        public Double getNullableDouble() {
            return nullableDouble;
        }

        // 6. name = 'primitiveDouble', OData type = DOUBLE
        private double primitiveDouble;
        public double getPrimitiveDouble() {
            return primitiveDouble;
        }

        // 7. name = 'nullableBoolean', OData type = BOOLEAN
        private Boolean nullableBoolean;
        public Boolean getNullableBoolean() {
            return nullableBoolean;
        }

        // 8. name = 'primitiveBoolean', OData type = BOOLEAN
        private boolean primitiveBoolean;
        public boolean isPrimitiveBoolean() {
            return primitiveBoolean;
        }

        // 9. name = 'string', OData type = STRING
        private String string;
        public String getString() {
            return string;
        }

        // 10. name = 'charSequence', OData type = STRING
        private CharSequence charSequence;
        public CharSequence getCharSequence() {
            return charSequence;
        }

        // 11. name = 'nullableChar', OData type = STRING
        private Character nullableChar;
        public Character getNullableChar() {
            return nullableChar;
        }

        // 12. name = 'primitiveChar', OData type = STRING
        private char primitiveChar;
        public char getPrimitiveChar() {
            return primitiveChar;
        }

        // 13. name = 'date', OData type = DATE_TIME_OFFSET
        private Date date;
        public Date getDate() {
            return date;
        }

        // 14. name = 'offsetDateTime', OData type = DATE_TIME_OFFSET
        private OffsetDateTime offsetDateTime;
        public OffsetDateTime getOffsetDateTime() {
            return offsetDateTime;
        }

        // 15. name = 'geoPoint', OData type = GEOGRAPHY_POINT
        private GeoPoint geoPoint;
        public GeoPoint getGeoPoint() {
            return geoPoint;
        }

        // 16. name = 'intArray', OData type = COMPLEX
        private int[] intArray;
        public int[] getIntArray() {
            return intArray;
        }

        // 17. name = 'intList', OData type = COMPLEX
        private List<Integer> intList;
        public List<Integer> getIntList() {
            return intList;
        }

        // 18. name = 'floatList', OData type = COMPLEX
        private List<Float> floatList;

        public List<Float> getFloatList() {
            return floatList;
        }

        // 19. name = 'floatArray', OData type = COMPLEX
        private Float[] floatArray;
        public Float[] getFloatArray() {
            return floatArray;
        }

        // 20. name = 'primitiveShort', OData type = INT16
        private short primitiveShort;
        public short getPrimitiveShort() {
            return primitiveShort;
        }

        // 21. name = 'nullableShort', OData type = INT16
        private Short nullableShort;
        public Short getNullableShort() {
            return nullableShort;
        }

        // 22. name = 'primitiveByte', OData type = SBYTE
        private byte primitiveByte;
        public byte getPrimitiveByte() {
            return primitiveByte;
        }

        // 23. name = 'nullableByte', OData type = SBYTE
        private Byte nullableByte;
        public Byte getNullableByte() {
            return nullableByte;
        }

        // 24. name = 'byteArray', OData type = COMPLEX
        private byte[] byteArray;
        public byte[] getByteArray() {
            return byteArray;
        }

        // 25. name = 'byteList', OData type = COMPLEX
        private List<Byte> byteList;
        public List<Byte> getByteList() {
            return byteList;
        }
    }

    @Test
    public void onlyAnalyzerNameSetsOnlyAnalyzerName() {
        List<SearchField> fields = SearchIndexClient.buildSearchFields(OnlyAnalyzerName.class, null);

        assertEquals(1, fields.size());

        SearchField field = fields.get(0);
        assertEquals("onlyAnalyzer", field.getAnalyzerName().toString());
        assertNull(field.getIndexAnalyzerName());
        assertNull(field.getSearchAnalyzerName());
    }

    @SuppressWarnings("unused")
    public static final class OnlyAnalyzerName {
        @SearchableField(analyzerName = "onlyAnalyzer")
        public String onlyAnalyzer;
    }

    @Test
    public void indexAndSearchAnalyzersSetCorrectly() {
        List<SearchField> fields = SearchIndexClient.buildSearchFields(IndexAndSearchAnalyzerNames.class, null);

        assertEquals(1, fields.size());

        SearchField field = fields.get(0);
        assertNull(field.getAnalyzerName());
        assertEquals("indexAnalyzer", field.getIndexAnalyzerName().toString());
        assertEquals("searchAnalyzer", field.getSearchAnalyzerName().toString());
    }

    @SuppressWarnings("unused")
    public static final class IndexAndSearchAnalyzerNames {
        @SearchableField(indexAnalyzerName = "indexAnalyzer", searchAnalyzerName = "searchAnalyzer")
        public String indexAndSearchAnalyzer;
    }

    @Test
    public void vectorSearchField() {
        List<SearchField> fields = SearchIndexClient.buildSearchFields(VectorSearchField.class, null);

        assertEquals(1, fields.size());

        SearchField field = fields.get(0);
        assertEquals(1536, field.getVectorSearchDimensions());
        assertEquals("myprofile", field.getVectorSearchProfileName());
    }

    @SuppressWarnings("unused")
    public static final class VectorSearchField {
        @SearchableField(vectorSearchDimensions = 1536, vectorSearchProfileName = "myprofile")
        public List<Float> vectorSearchField;
    }

    @Test
    public void vectorFieldMissingDimensions() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            SearchIndexClient.buildSearchFields(VectorFieldMissingDimensions.class, null));

        assertTrue(ex.getMessage().contains("Please specify both vectorSearchDimensions and vectorSearchProfile"));
    }

    @SuppressWarnings("unused")
    public static final class VectorFieldMissingDimensions {
        @SearchableField(vectorSearchProfileName = "myprofile")
        public List<Float> vectorSearchField;
    }

    @Test
    public void vectorFieldMissingProfile() {
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
            SearchIndexClient.buildSearchFields(VectorFieldMissingProfile.class, null));

        assertTrue(ex.getMessage().contains("Please specify both vectorSearchDimensions and vectorSearchProfile"));
    }

    @SuppressWarnings("unused")
    public static final class VectorFieldMissingProfile {
        @SearchableField(vectorSearchDimensions = 1536)
        public List<Float> vectorSearchField;
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
            .setStored(true)
            .setHidden(false)
            .setSearchable(false)
            .setFacetable(false)
            .setFilterable(false);
        SearchField tags = new SearchField("tags", SearchFieldDataType.collection(SearchFieldDataType.STRING))
            .setKey(false)
            .setHidden(false)
            .setStored(true)
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
