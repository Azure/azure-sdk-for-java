// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.DateTimeRfc1123;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class JacksonAdapterTests {
    @Test
    public void emptyMap() throws IOException {
        final Map<String, String> map = new HashMap<>();
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{}", serializer.serialize(map, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithNullKey() throws IOException {
        final Map<String, String> map = new HashMap<>();
        map.put(null, null);
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{}", serializer.serialize(map, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndNullValue() throws IOException {
        final MapHolder mapHolder = new MapHolder();
        mapHolder.map(new HashMap<>());
        mapHolder.map().put("", null);

        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{\"map\":{\"\":null}}", serializer.serialize(mapHolder, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndEmptyValue() throws IOException {
        final MapHolder mapHolder = new MapHolder();
        mapHolder.map = new HashMap<>();
        mapHolder.map.put("", "");
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{\"map\":{\"\":\"\"}}", serializer.serialize(mapHolder, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndNonEmptyValue() throws IOException {
        final Map<String, String> map = new HashMap<>();
        map.put("", "test");
        final JacksonAdapter serializer = new JacksonAdapter();
        assertEquals("{\"\":\"test\"}", serializer.serialize(map, SerializerEncoding.JSON));
    }

    private static class MapHolder {

        @JsonInclude(content = JsonInclude.Include.ALWAYS)
        private Map<String, String> map = new HashMap<>();

        public Map<String, String> map() {
            return map;
        }

        public void map(Map<String, String> map) {
            this.map = map;
        }

    }

    @JacksonXmlRootElement(localName = "XmlString")
    private static class XmlString {

        @JsonProperty("Value")
        private String value;

        public String getValue() {
            return value;
        }

    }

    @ParameterizedTest
    @MethodSource("serializeCollectionSupplier")
    public void testSerializeList(List<?> values, CollectionFormat format, String expectedSerializedString) {
        String actualSerializedString = JacksonAdapter.createDefaultSerializerAdapter()
            .serializeList(values, format);
        assertEquals(expectedSerializedString, actualSerializedString);
    }

    @ParameterizedTest
    @MethodSource("serializeCollectionSupplier")
    public void testSerializeIterable(Iterable<?> values, CollectionFormat format, String expectedSerializedString) {
        String actualSerializedString = JacksonAdapter.createDefaultSerializerAdapter()
            .serializeIterable(values, format);
        assertEquals(expectedSerializedString, actualSerializedString);
    }

    @ParameterizedTest
    @MethodSource("deserializeJsonSupplier")
    public void deserializeJson(String json, OffsetDateTime expected) throws IOException {
        DateTimeWrapper wrapper = JacksonAdapter.createDefaultSerializerAdapter()
            .deserialize(json, DateTimeWrapper.class, SerializerEncoding.JSON);

        assertEquals(expected, wrapper.getOffsetDateTime());
    }

    private static Stream<Arguments> serializeCollectionSupplier() {
        return Stream.of(
            Arguments.of(Arrays.asList("foo", "bar", "baz"), CollectionFormat.CSV, "foo,bar,baz"),
            Arguments.of(Arrays.asList("foo", null, "baz"), CollectionFormat.CSV, "foo,,baz"),
            Arguments.of(Arrays.asList(null, "bar", null, null), CollectionFormat.CSV, ",bar,,"),
            Arguments.of(Arrays.asList(1, 2, 3), CollectionFormat.CSV, "1,2,3"),
            Arguments.of(Arrays.asList(1, 2, 3), CollectionFormat.PIPES, "1|2|3"),
            Arguments.of(Arrays.asList(1, 2, 3), CollectionFormat.SSV, "1 2 3"),
            Arguments.of(Arrays.asList("foo", "bar", "baz"), CollectionFormat.MULTI, "foo&bar&baz")
        );
    }

    private static Stream<Arguments> deserializeJsonSupplier() {
        final String jsonFormat = "{\"OffsetDateTime\":\"%s\"}";
        OffsetDateTime minValue = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime unixEpoch = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        return Stream.of(
            Arguments.of(String.format(jsonFormat, "0001-01-01T00:00:00"), minValue),
            Arguments.of(String.format(jsonFormat, "0001-01-01T00:00:00Z"), minValue),
            Arguments.of(String.format(jsonFormat, "1970-01-01T00:00:00"), unixEpoch),
            Arguments.of(String.format(jsonFormat, "1970-01-01T00:00:00Z"), unixEpoch)
        );
    }

    @ParameterizedTest
    @MethodSource("deserializeXmlSupplier")
    public void deserializeXml(String xml, OffsetDateTime expected) throws IOException {
        DateTimeWrapper wrapper = JacksonAdapter.createDefaultSerializerAdapter()
            .deserialize(xml, DateTimeWrapper.class, SerializerEncoding.XML);

        assertEquals(expected, wrapper.getOffsetDateTime());
    }

    private static Stream<Arguments> deserializeXmlSupplier() {
        final String xmlFormat = "<Wrapper><OffsetDateTime>%s</OffsetDateTime></Wrapper>";
        OffsetDateTime minValue = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime unixEpoch = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        return Stream.of(
            Arguments.of(String.format(xmlFormat, "0001-01-01T00:00:00"), minValue),
            Arguments.of(String.format(xmlFormat, "0001-01-01T00:00:00Z"), minValue),
            Arguments.of(String.format(xmlFormat, "1970-01-01T00:00:00"), unixEpoch),
            Arguments.of(String.format(xmlFormat, "1970-01-01T00:00:00Z"), unixEpoch)
        );
    }

    @JacksonXmlRootElement(localName = "Wrapper")
    private static class DateTimeWrapper {
        @JsonProperty(value = "OffsetDateTime", required = true)
        private OffsetDateTime offsetDateTime;

        public DateTimeWrapper setOffsetDateTime(OffsetDateTime offsetDateTime) {
            this.offsetDateTime = offsetDateTime;
            return this;
        }

        public OffsetDateTime getOffsetDateTime() {
            return offsetDateTime;
        }
    }

    @Test
    public void stronglyTypedHeadersClassIsDeserialized() throws IOException {
        final String expectedDate = DateTimeRfc1123.toRfc1123String(OffsetDateTime.now());

        HttpHeaders rawHeaders = new HttpHeaders().set("Date", expectedDate);

        StronglyTypedHeaders stronglyTypedHeaders = JacksonAdapter.createDefaultSerializerAdapter()
            .deserialize(rawHeaders, StronglyTypedHeaders.class);

        assertEquals(expectedDate, DateTimeRfc1123.toRfc1123String(stronglyTypedHeaders.getDate()));
    }

    @Test
    public void stronglyTypedHeadersClassThrowsEagerly() {
        HttpHeaders rawHeaders = new HttpHeaders().set("Date", "invalid-rfc1123-date");

        assertThrows(DateTimeParseException.class, () -> JacksonAdapter.createDefaultSerializerAdapter()
            .deserialize(rawHeaders, StronglyTypedHeaders.class));
    }

    @Test
    public void invalidStronglyTypedHeadersClassThrowsCorrectException() throws IOException {
        try {
            JacksonAdapter.createDefaultSerializerAdapter().deserialize(new HttpHeaders(),
                InvalidStronglyTypedHeaders.class);

            fail("An exception should have been thrown.");
        } catch (RuntimeException ex) {
            assertTrue(ex.getCause() instanceof JsonProcessingException, "Exception cause type was "
                + ex.getCause().getClass().getName() + " instead of the expected JsonProcessingException type.");
        }
    }

    @ParameterizedTest
    @MethodSource("quoteRemovalSupplier")
    public void quoteRemoval(String str, String expected) {
        assertEquals(expected, JacksonAdapter.removeLeadingAndTrailingQuotes(str));
    }

    private static Stream<Arguments> quoteRemovalSupplier() {
        return Stream.of(
            Arguments.of("", ""),
            Arguments.of("\"\"", ""),
            Arguments.of("\"\"\"\"\"\"\"\"", ""),
            Arguments.of("\"\"hello\"\"", "hello"),
            Arguments.of("\"\"they said \"hello\" to you\"\"", "they said \"hello\" to you")
        );
    }

    public static final class StronglyTypedHeaders {
        private final DateTimeRfc1123 date;

        public StronglyTypedHeaders(HttpHeaders rawHeaders) {
            String dateString = rawHeaders.getValue("Date");
            this.date = (dateString == null) ? null : new DateTimeRfc1123(dateString);
        }

        OffsetDateTime getDate() {
            return (date == null) ? null : date.getDateTime();
        }
    }

    public static final class InvalidStronglyTypedHeaders {
        public InvalidStronglyTypedHeaders(HttpHeaders httpHeaders) throws Exception {
            throw new Exception();
        }
    }
}
