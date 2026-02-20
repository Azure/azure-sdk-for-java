// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.indexes;

import com.azure.core.models.GeoPoint;
import com.azure.search.documents.TestHelpers;
import com.azure.search.documents.indexes.models.LexicalNormalizerName;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.testingmodels.HotelAnalyzerException;
import com.azure.search.documents.testingmodels.HotelCircularDependencies;
import com.azure.search.documents.testingmodels.HotelRenameProperty;
import com.azure.search.documents.testingmodels.HotelSearchException;
import com.azure.search.documents.testingmodels.HotelSearchableExceptionOnList;
import com.azure.search.documents.testingmodels.HotelTwoDimensional;
import com.azure.search.documents.testingmodels.HotelWithArray;
import com.azure.search.documents.testingmodels.HotelWithEmptyInSynonymMaps;
import com.azure.search.documents.testingmodels.HotelWithIgnoredFields;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> SearchIndexClient.buildSearchFields(HotelSearchException.class));
        assertExceptionMassageAndDataType(exception, SearchFieldDataType.INT32, "getHotelId");
    }

    @Test
    public void hotelListFieldSearchableThrowException() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> SearchIndexClient.buildSearchFields(HotelSearchableExceptionOnList.class));
        assertExceptionMassageAndDataType(exception, SearchFieldDataType.collection(SearchFieldDataType.INT32),
            "getPasscode");
    }

    @Test
    public void hotelCircularDependencies() {
        List<SearchField> actualFields
            = sortByFieldName(SearchIndexClient.buildSearchFields(HotelCircularDependencies.class));
        List<SearchField> expectedFields = sortByFieldName(buildHotelCircularDependenciesModel());
        assertListFieldEquals(expectedFields, actualFields);
    }

    @Test
    @Disabled("Temporarily disabled")
    public void hotelWithEmptySynonymMaps() {
        // We cannot put null in the annotation. So no need to test null case.
        List<SearchField> actualFields = SearchIndexClient.buildSearchFields(HotelWithEmptyInSynonymMaps.class);

        List<SearchField> expectedFields = Collections.singletonList(
            new SearchField("tags", SearchFieldDataType.collection(SearchFieldDataType.STRING)).setSearchable(true)
                .setKey(false)
                .setStored(true)
                .setRetrievable(true)
                .setFilterable(false)
                .setSortable(false)
                .setFacetable(false)
                .setSynonymMapNames(Arrays.asList("asynonymMaps", "maps")));

        assertListFieldEquals(expectedFields, actualFields);
    }

    @Test
    public void hotelWithTwoDimensionalType() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> SearchIndexClient.buildSearchFields(HotelTwoDimensional.class));
        assertExceptionMassageAndDataType(exception, null, "cannot be a nested array or Iterable.");
    }

    @Test
    public void hotelAnalyzerException() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> SearchIndexClient.buildSearchFields(HotelAnalyzerException.class));
        assertExceptionMassageAndDataType(exception, null, "either analyzer or both searchAnalyzer and indexAnalyzer");
    }

    @Test
    @Disabled("Temporarily disabled")
    public void hotelWithArrayType() {
        List<SearchField> actualFields = sortByFieldName(SearchIndexClient.buildSearchFields(HotelWithArray.class));
        List<SearchField> expectedFields = sortByFieldName(buildHotelWithArrayModel());
        assertListFieldEquals(expectedFields, actualFields);
    }

    @Test
    public void propertyRename() {
        List<SearchField> actualFields
            = sortByFieldName(SearchIndexClient.buildSearchFields(HotelRenameProperty.class));
        List<String> expectedFieldNames = Arrays.asList("HotelName", "HotelId", "Description");
        Collections.sort(expectedFieldNames);
        assertEquals(expectedFieldNames.get(0), actualFields.get(0).getName());
        assertEquals(expectedFieldNames.get(1), actualFields.get(1).getName());
        assertEquals(expectedFieldNames.get(2), actualFields.get(2).getName());
    }

    @Test
    public void ignoredPropertyName() {
        List<SearchField> actualFields = SearchIndexClient.buildSearchFields(HotelWithIgnoredFields.class);
        assertEquals(1, actualFields.size());
        assertEquals("NotIgnoredName", actualFields.get(0).getName());
    }

    @Test
    public void supportedFields() {
        List<SearchField> fields = SearchIndexClient.buildSearchFields(AllSupportedFields.class);

        assertEquals(25, fields.size());

        Map<String, SearchFieldDataType> fieldToDataType
            = fields.stream().collect(Collectors.toMap(SearchField::getName, SearchField::getType));

        assertEquals(SearchFieldDataType.INT32, fieldToDataType.get("NullableInt"));
        assertEquals(SearchFieldDataType.INT32, fieldToDataType.get("PrimitiveInt"));
        assertEquals(SearchFieldDataType.INT64, fieldToDataType.get("NullableLong"));
        assertEquals(SearchFieldDataType.INT64, fieldToDataType.get("PrimitiveLong"));
        assertEquals(SearchFieldDataType.DOUBLE, fieldToDataType.get("NullableDouble"));
        assertEquals(SearchFieldDataType.DOUBLE, fieldToDataType.get("PrimitiveDouble"));
        assertEquals(SearchFieldDataType.BOOLEAN, fieldToDataType.get("NullableBoolean"));
        assertEquals(SearchFieldDataType.BOOLEAN, fieldToDataType.get("PrimitiveBoolean"));
        assertEquals(SearchFieldDataType.STRING, fieldToDataType.get("String"));
        assertEquals(SearchFieldDataType.STRING, fieldToDataType.get("CharSequence"));
        assertEquals(SearchFieldDataType.STRING, fieldToDataType.get("NullableChar"));
        assertEquals(SearchFieldDataType.STRING, fieldToDataType.get("PrimitiveChar"));
        assertEquals(SearchFieldDataType.DATE_TIME_OFFSET, fieldToDataType.get("Date"));
        assertEquals(SearchFieldDataType.DATE_TIME_OFFSET, fieldToDataType.get("OffsetDateTime"));
        assertEquals(SearchFieldDataType.GEOGRAPHY_POINT, fieldToDataType.get("GeoPoint"));
        assertEquals(SearchFieldDataType.collection(SearchFieldDataType.INT32), fieldToDataType.get("IntArray"));
        assertEquals(SearchFieldDataType.collection(SearchFieldDataType.INT32), fieldToDataType.get("IntList"));
        assertEquals(SearchFieldDataType.collection(SearchFieldDataType.SINGLE), fieldToDataType.get("FloatArray"));
        assertEquals(SearchFieldDataType.collection(SearchFieldDataType.SINGLE), fieldToDataType.get("FloatList"));
        assertEquals(SearchFieldDataType.INT16, fieldToDataType.get("NullableShort"));
        assertEquals(SearchFieldDataType.INT16, fieldToDataType.get("PrimitiveShort"));
        assertEquals(SearchFieldDataType.SBYTE, fieldToDataType.get("NullableByte"));
        assertEquals(SearchFieldDataType.SBYTE, fieldToDataType.get("PrimitiveByte"));
        assertEquals(SearchFieldDataType.collection(SearchFieldDataType.SBYTE), fieldToDataType.get("ByteArray"));
        assertEquals(SearchFieldDataType.collection(SearchFieldDataType.SBYTE), fieldToDataType.get("ByteList"));

    }

    @SuppressWarnings({ "unused", "UseOfObsoleteDateTimeApi" })
    private static final class AllSupportedFields {
        // 1. name = 'NullableInt', OData type = INT32
        @BasicField(name = "NullableInt")
        private Integer nullableInt;

        public Integer getNullableInt() {
            return nullableInt;
        }

        // 2. name = 'PrimitiveInt', OData type = INT32
        @BasicField(name = "PrimitiveInt")
        private int primitiveInt;

        public int getPrimitiveInt() {
            return primitiveInt;
        }

        // 3. name = 'NullableLong', OData type = INT64
        @BasicField(name = "NullableLong")
        private Long nullableLong;

        public Long getNullableLong() {
            return nullableLong;
        }

        // 4. name = 'PrimitiveLong', OData type = INT64
        @BasicField(name = "PrimitiveLong")
        private long primitiveLong;

        public long getPrimitiveLong() {
            return primitiveLong;
        }

        // 5. name = 'NullableDouble', OData type = DOUBLE
        @BasicField(name = "NullableDouble")
        private Double nullableDouble;

        public Double getNullableDouble() {
            return nullableDouble;
        }

        // 6. name = 'PrimitiveDouble', OData type = DOUBLE
        @BasicField(name = "PrimitiveDouble")
        private double primitiveDouble;

        public double getPrimitiveDouble() {
            return primitiveDouble;
        }

        // 7. name = 'NullableBoolean', OData type = BOOLEAN
        @BasicField(name = "NullableBoolean")
        private Boolean nullableBoolean;

        public Boolean getNullableBoolean() {
            return nullableBoolean;
        }

        // 8. name = 'PrimitiveBoolean', OData type = BOOLEAN
        @BasicField(name = "PrimitiveBoolean")
        private boolean primitiveBoolean;

        public boolean isPrimitiveBoolean() {
            return primitiveBoolean;
        }

        // 9. name = 'String', OData type = STRING
        @BasicField(name = "String")
        private String string;

        public String getString() {
            return string;
        }

        // 10. name = 'CharSequence', OData type = STRING
        @BasicField(name = "CharSequence")
        private CharSequence charSequence;

        public CharSequence getCharSequence() {
            return charSequence;
        }

        // 11. name = 'NullableChar', OData type = STRING
        @BasicField(name = "NullableChar")
        private Character nullableChar;

        public Character getNullableChar() {
            return nullableChar;
        }

        // 12. name = 'PrimitiveChar', OData type = STRING
        @BasicField(name = "PrimitiveChar")
        private char primitiveChar;

        public char getPrimitiveChar() {
            return primitiveChar;
        }

        // 13. name = 'Date', OData type = DATE_TIME_OFFSET
        @BasicField(name = "Date")
        private Date date;

        public Date getDate() {
            return date;
        }

        // 14. name = 'OffsetDateTime', OData type = DATE_TIME_OFFSET
        @BasicField(name = "OffsetDateTime")
        private OffsetDateTime offsetDateTime;

        public OffsetDateTime getOffsetDateTime() {
            return offsetDateTime;
        }

        // 15. name = 'GeoPoint', OData type = GEOGRAPHY_POINT
        @BasicField(name = "GeoPoint")
        private GeoPoint geoPoint;

        public GeoPoint getGeoPoint() {
            return geoPoint;
        }

        // 16. name = 'IntArray', OData type = COMPLEX
        @BasicField(name = "IntArray")
        private int[] intArray;

        public int[] getIntArray() {
            return intArray;
        }

        // 17. name = 'IntList', OData type = COMPLEX
        @BasicField(name = "IntList")
        private List<Integer> intList;

        public List<Integer> getIntList() {
            return intList;
        }

        // 18. name = 'FloatList', OData type = COMPLEX
        @BasicField(name = "FloatList")
        private List<Float> floatList;

        public List<Float> getFloatList() {
            return floatList;
        }

        // 19. name = 'FloatArray', OData type = COMPLEX
        @BasicField(name = "FloatArray")
        private Float[] floatArray;

        public Float[] getFloatArray() {
            return floatArray;
        }

        // 20. name = 'PrimitiveShort', OData type = INT16
        @BasicField(name = "PrimitiveShort")
        private short primitiveShort;

        public short getPrimitiveShort() {
            return primitiveShort;
        }

        // 21. name = 'NullableShort', OData type = INT16
        @BasicField(name = "NullableShort")
        private Short nullableShort;

        public Short getNullableShort() {
            return nullableShort;
        }

        // 22. name = 'PrimitiveByte', OData type = SBYTE
        @BasicField(name = "PrimitiveByte")
        private byte primitiveByte;

        public byte getPrimitiveByte() {
            return primitiveByte;
        }

        // 23. name = 'NullableByte', OData type = SBYTE
        @BasicField(name = "NullableByte")
        private Byte nullableByte;

        public Byte getNullableByte() {
            return nullableByte;
        }

        // 24. name = 'ByteArray', OData type = COMPLEX
        @BasicField(name = "ByteArray")
        private byte[] byteArray;

        public byte[] getByteArray() {
            return byteArray;
        }

        // 25. name = 'ByteList', OData type = COMPLEX
        @BasicField(name = "ByteList")
        private List<Byte> byteList;

        public List<Byte> getByteList() {
            return byteList;
        }
    }

    @Test
    public void validNormalizerField() {
        List<SearchField> fields = SearchIndexClient.buildSearchFields(ValidNormalizer.class);

        assertEquals(1, fields.size());

        SearchField normalizerField = fields.get(0);
        assertEquals(LexicalNormalizerName.STANDARD, normalizerField.getNormalizerName());
    }

    @SuppressWarnings("unused")
    public static final class ValidNormalizer {
        @BasicField(name = "ValidNormalizer", normalizerName = "standard", isFilterable = BasicField.BooleanHelper.TRUE)
        public String validNormalizer;
    }

    @ParameterizedTest
    @ValueSource(classes = { NonStringNormalizer.class, MissingFunctionalityNormalizer.class })
    public void invalidNormalizerField(Class<?> type) {
        IllegalStateException ex
            = assertThrows(IllegalStateException.class, () -> SearchIndexClient.buildSearchFields(type));

        assertTrue(ex.getMessage().contains("A field with a normalizer name"));
    }

    @SuppressWarnings("unused")
    public static final class NonStringNormalizer {
        @BasicField(name = "WrongTypeForNormalizer", normalizerName = "standard")
        public int wrongTypeForNormalizer;
    }

    @SuppressWarnings("unused")
    public static final class MissingFunctionalityNormalizer {
        @BasicField(name = "RightTypeWrongFunctionality", normalizerName = "standard")
        public String rightTypeWrongFunctionality;
    }

    @Test
    public void onlyAnalyzerNameSetsOnlyAnalyzerName() {
        List<SearchField> fields = SearchIndexClient.buildSearchFields(OnlyAnalyzerName.class);

        assertEquals(1, fields.size());

        SearchField field = fields.get(0);
        assertEquals("onlyAnalyzer", field.getAnalyzerName().toString());
        assertNull(field.getIndexAnalyzerName());
        assertNull(field.getSearchAnalyzerName());
    }

    @SuppressWarnings("unused")
    public static final class OnlyAnalyzerName {
        @BasicField(name = "OnlyAnalyzer", analyzerName = "onlyAnalyzer")
        public String onlyAnalyzer;
    }

    @Test
    public void indexAndSearchAnalyzersSetCorrectly() {
        List<SearchField> fields = SearchIndexClient.buildSearchFields(IndexAndSearchAnalyzerNames.class);

        assertEquals(1, fields.size());

        SearchField field = fields.get(0);
        assertNull(field.getAnalyzerName());
        assertEquals("indexAnalyzer", field.getIndexAnalyzerName().toString());
        assertEquals("searchAnalyzer", field.getSearchAnalyzerName().toString());
    }

    @SuppressWarnings("unused")
    public static final class IndexAndSearchAnalyzerNames {
        @BasicField(
            name = "indexAndSearchAnalyzer",
            indexAnalyzerName = "indexAnalyzer",
            searchAnalyzerName = "searchAnalyzer")
        public String indexAndSearchAnalyzer;
    }

    @Test
    public void vectorSearchField() {
        List<SearchField> fields = SearchIndexClient.buildSearchFields(VectorSearchField.class);

        assertEquals(1, fields.size());

        SearchField field = fields.get(0);
        assertEquals(1536, field.getVectorSearchDimensions());
        assertEquals("myprofile", field.getVectorSearchProfileName());
    }

    @SuppressWarnings("unused")
    public static final class VectorSearchField {
        @BasicField(name = "vectorSearchField", vectorSearchDimensions = 1536, vectorSearchProfileName = "myprofile")
        public List<Float> vectorSearchField;
    }

    @Test
    public void vectorFieldMissingDimensions() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> SearchIndexClient.buildSearchFields(VectorFieldMissingDimensions.class));

        assertTrue(ex.getMessage().contains("Please specify both vectorSearchDimensions and vectorSearchProfile"));
    }

    @SuppressWarnings("unused")
    public static final class VectorFieldMissingDimensions {
        @BasicField(name = "vectorSearchField", vectorSearchProfileName = "myprofile")
        public List<Float> vectorSearchField;
    }

    @Test
    public void vectorFieldMissingProfile() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> SearchIndexClient.buildSearchFields(VectorFieldMissingProfile.class));

        assertTrue(ex.getMessage().contains("Please specify both vectorSearchDimensions and vectorSearchProfile"));
    }

    @SuppressWarnings("unused")
    public static final class VectorFieldMissingProfile {
        @BasicField(name = "vectorSearchField", vectorSearchDimensions = 1536)
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
        SearchField homeAddress
            = new SearchField("HomeAddress", SearchFieldDataType.COMPLEX).setFields(buildHotelInAddress());
        SearchField billingAddress
            = new SearchField("BillingAddress", SearchFieldDataType.COMPLEX).setFields(buildHotelInAddress());
        return Arrays.asList(homeAddress, billingAddress);
    }

    private List<SearchField> buildHotelInAddress() {
        return Collections.singletonList(new SearchField("Hotel", SearchFieldDataType.COMPLEX));
    }

    private List<SearchField> buildHotelWithArrayModel() {
        SearchField hotelId = new SearchField("HotelId", SearchFieldDataType.STRING).setKey(true)
            .setSortable(true)
            .setStored(true)
            .setRetrievable(true)
            .setSearchable(false)
            .setFacetable(false)
            .setFilterable(false);
        SearchField tags
            = new SearchField("Tags", SearchFieldDataType.collection(SearchFieldDataType.STRING)).setKey(false)
                .setRetrievable(true)
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
