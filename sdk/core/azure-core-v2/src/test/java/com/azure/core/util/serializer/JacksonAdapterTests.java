// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.HttpMethod;
import com.azure.core.v2.implementation.AccessibleByteArrayOutputStream;
import com.azure.core.v2.models.GeoObjectType;
import com.azure.core.v2.models.JsonPatchDocument;
import io.clientcore.core.implementation.util.DateTimeRfc1123;
import io.clientcore.core.implementation.util.UrlBuilder;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.azure.core.CoreTestUtils.assertArraysEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class JacksonAdapterTests {
    private static final JacksonAdapter ADAPTER = new JacksonAdapter();

    @Test
    public void emptyMap() throws IOException {
        final Map<String, String> map = new HashMap<>();
        assertEquals("{}", ADAPTER.serialize(map, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithNullKey() throws IOException {
        final Map<String, String> map = new HashMap<>();
        map.put(null, null);
        assertEquals("{}", ADAPTER.serialize(map, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndNullValue() throws IOException {
        final MapHolder mapHolder = new MapHolder();
        mapHolder.map(new HashMap<>());
        mapHolder.map().put("", null);

        assertEquals("{\"map\":{\"\":null}}", ADAPTER.serialize(mapHolder, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndEmptyValue() throws IOException {
        final MapHolder mapHolder = new MapHolder();
        mapHolder.map = new HashMap<>();
        mapHolder.map.put("", "");

        assertEquals("{\"map\":{\"\":\"\"}}", ADAPTER.serialize(mapHolder, SerializerEncoding.JSON));
    }

    @Test
    public void mapWithEmptyKeyAndNonEmptyValue() throws IOException {
        final Map<String, String> map = new HashMap<>();
        map.put("", "test");

        assertEquals("{\"\":\"test\"}", ADAPTER.serialize(map, SerializerEncoding.JSON));
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
        String actualSerializedString = JacksonAdapter.createDefaultSerializerAdapter().serializeList(values, format);
        assertEquals(expectedSerializedString, actualSerializedString);
    }

    @ParameterizedTest
    @MethodSource("serializeCollectionSupplier")
    public void testSerializeIterable(Iterable<?> values, CollectionFormat format, String expectedSerializedString) {
        String actualSerializedString
            = JacksonAdapter.createDefaultSerializerAdapter().serializeIterable(values, format);
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
        return Stream.of(Arguments.of(Arrays.asList("foo", "bar", "baz"), CollectionFormat.CSV, "foo,bar,baz"),
            Arguments.of(Arrays.asList("foo", null, "baz"), CollectionFormat.CSV, "foo,,baz"),
            Arguments.of(Arrays.asList(null, "bar", null, null), CollectionFormat.CSV, ",bar,,"),
            Arguments.of(Arrays.asList(1, 2, 3), CollectionFormat.CSV, "1,2,3"),
            Arguments.of(Arrays.asList(1, 2, 3), CollectionFormat.PIPES, "1|2|3"),
            Arguments.of(Arrays.asList(1, 2, 3), CollectionFormat.SSV, "1 2 3"),
            Arguments.of(Arrays.asList("foo", "bar", "baz"), CollectionFormat.MULTI, "foo&bar&baz"));
    }

    private static Stream<Arguments> deserializeJsonSupplier() {
        final String jsonFormat = "{\"OffsetDateTime\":\"%s\"}";
        OffsetDateTime minValue = OffsetDateTime.of(1, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime unixEpoch = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

        return Stream.of(Arguments.of(String.format(jsonFormat, "0001-01-01T00:00:00"), minValue),
            Arguments.of(String.format(jsonFormat, "0001-01-01T00:00:00Z"), minValue),
            Arguments.of(String.format(jsonFormat, "1970-01-01T00:00:00"), unixEpoch),
            Arguments.of(String.format(jsonFormat, "1970-01-01T00:00:00Z"), unixEpoch));
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

        return Stream.of(Arguments.of(String.format(xmlFormat, "0001-01-01T00:00:00"), minValue),
            Arguments.of(String.format(xmlFormat, "0001-01-01T00:00:00Z"), minValue),
            Arguments.of(String.format(xmlFormat, "1970-01-01T00:00:00"), unixEpoch),
            Arguments.of(String.format(xmlFormat, "1970-01-01T00:00:00Z"), unixEpoch));
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

        HttpHeaders rawHeaders = new HttpHeaders().set(HttpHeaderName.DATE, expectedDate);

        StronglyTypedHeaders stronglyTypedHeaders
            = JacksonAdapter.createDefaultSerializerAdapter().deserialize(rawHeaders, StronglyTypedHeaders.class);

        assertEquals(expectedDate, DateTimeRfc1123.toRfc1123String(stronglyTypedHeaders.getDate()));
    }

    @Test
    public void stronglyTypedHeadersClassThrowsEagerly() {
        HttpHeaders rawHeaders = new HttpHeaders().set(HttpHeaderName.DATE, "invalid-rfc1123-date");

        assertThrows(DateTimeParseException.class,
            () -> JacksonAdapter.createDefaultSerializerAdapter().deserialize(rawHeaders, StronglyTypedHeaders.class));
    }

    @Test
    public void invalidStronglyTypedHeadersClassThrowsCorrectException() throws IOException {
        try {
            JacksonAdapter.createDefaultSerializerAdapter()
                .deserialize(new HttpHeaders(), InvalidStronglyTypedHeaders.class);

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
        return Stream.of(Arguments.of("", ""), Arguments.of("\"\"", ""), Arguments.of("\"\"\"\"\"\"\"\"", ""),
            Arguments.of("\"\"hello\"\"", "hello"),
            Arguments.of("\"\"they said \"hello\" to you\"\"", "they said \"hello\" to you"));
    }

    @ParameterizedTest
    @MethodSource("textSerializationSupplier")
    public void textToStringSerialization(Object value, String expected) throws IOException {
        assertEquals(expected, ADAPTER.serialize(value, SerializerEncoding.TEXT));
    }

    @ParameterizedTest
    @MethodSource("textSerializationSupplier")
    public void textToBytesSerialization(Object value, String expected) throws IOException {
        byte[] actual = ADAPTER.serializeToBytes(value, SerializerEncoding.TEXT);

        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected, new String(actual, StandardCharsets.UTF_8));
        }
    }

    @ParameterizedTest
    @MethodSource("textSerializationSupplier")
    public void textToOutputStreamSerialization(Object value, String expected) throws IOException {
        AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
        ADAPTER.serialize(value, SerializerEncoding.TEXT, outputStream);

        if (expected == null) {
            assertEquals(0, outputStream.count());
        } else {
            assertEquals(expected, outputStream.toString(StandardCharsets.UTF_8));
        }
    }

    private static Stream<Arguments> textSerializationSupplier() {
        Map<String, String> map = Collections.singletonMap("key", "value");

        return Stream.of(Arguments.of(1, "1"), Arguments.of(1L, "1"), Arguments.of(1.0F, "1.0"),
            Arguments.of(1.0D, "1.0"), Arguments.of("1", "1"), Arguments.of(HttpMethod.GET, "GET"),
            Arguments.of(GeoObjectType.POINT, "Point"), Arguments.of(map, String.valueOf(map)),
            Arguments.of(null, null));
    }

    @ParameterizedTest
    @MethodSource("textDeserializationSupplier")
    public void stringToTextDeserialization(byte[] stringBytes, Class<?> type, Object expected) throws IOException {
        Object actual
            = ADAPTER.deserialize(new String(stringBytes, StandardCharsets.UTF_8), type, SerializerEncoding.TEXT);

        if (type == byte[].class) {
            assertArraysEqual((byte[]) expected, (byte[]) actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    @ParameterizedTest
    @MethodSource("textDeserializationSupplier")
    public void bytesToTextDeserialization(byte[] bytes, Class<?> type, Object expected) throws IOException {
        Object actual = ADAPTER.deserialize(bytes, type, SerializerEncoding.TEXT);

        if (type == byte[].class) {
            assertArraysEqual((byte[]) expected, (byte[]) actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    @ParameterizedTest
    @MethodSource("textDeserializationSupplier")
    public void inputStreamToTextDeserialization(byte[] inputStreamBytes, Class<?> type, Object expected)
        throws IOException {
        Object actual = ADAPTER.deserialize(new ByteArrayInputStream(inputStreamBytes), type, SerializerEncoding.TEXT);

        if (type == byte[].class) {
            assertArraysEqual((byte[]) expected, (byte[]) actual);
        } else {
            assertEquals(expected, actual);
        }
    }

    private static Stream<Arguments> textDeserializationSupplier() throws MalformedURLException {
        byte[] helloBytes = "hello".getBytes(StandardCharsets.UTF_8);
        String urlUri = "https://azure.com";
        byte[] urlUriBytes = urlUri.getBytes(StandardCharsets.UTF_8);
        OffsetDateTime offsetDateTime = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
        DateTimeRfc1123 dateTimeRfc1123 = new DateTimeRfc1123(offsetDateTime);
        LocalDate localDate = LocalDate.now(ZoneOffset.UTC);
        UUID uuid = UUID.randomUUID();
        HttpMethod httpMethod = HttpMethod.GET;
        GeoObjectType geoObjectType = GeoObjectType.POINT;

        return Stream.of(Arguments.of(helloBytes, String.class, "hello"),
            Arguments.of(helloBytes, CharSequence.class, "hello"),
            Arguments.of("1".getBytes(StandardCharsets.UTF_8), int.class, 1),
            Arguments.of("1".getBytes(StandardCharsets.UTF_8), Integer.class, 1),
            Arguments.of("1".getBytes(StandardCharsets.UTF_8), byte.class, (byte) 49),
            Arguments.of("1".getBytes(StandardCharsets.UTF_8), Byte.class, (byte) 49),
            Arguments.of("1".getBytes(StandardCharsets.UTF_8), long.class, 1L),
            Arguments.of("1".getBytes(StandardCharsets.UTF_8), Long.class, 1L),
            Arguments.of("1".getBytes(StandardCharsets.UTF_8), short.class, (short) 1),
            Arguments.of("1".getBytes(StandardCharsets.UTF_8), Short.class, (short) 1),
            Arguments.of("1.0".getBytes(StandardCharsets.UTF_8), double.class, 1.0D),
            Arguments.of("1.0".getBytes(StandardCharsets.UTF_8), Double.class, 1.0D),
            Arguments.of("1.0".getBytes(StandardCharsets.UTF_8), float.class, 1.0F),
            Arguments.of("1.0".getBytes(StandardCharsets.UTF_8), Float.class, 1.0F),
            Arguments.of("1".getBytes(StandardCharsets.UTF_8), char.class, '1'),
            Arguments.of("1".getBytes(StandardCharsets.UTF_8), Character.class, '1'),
            Arguments.of("1".getBytes(StandardCharsets.UTF_8), byte[].class, "1".getBytes(StandardCharsets.UTF_8)),
            Arguments.of("true".getBytes(StandardCharsets.UTF_8), boolean.class, true),
            Arguments.of("true".getBytes(StandardCharsets.UTF_8), Boolean.class, true),
            Arguments.of(urlUriBytes, URL.class, UrlBuilder.parse(urlUri).toUrl()),
            Arguments.of(urlUriBytes, URI.class, URI.create(urlUri)),
            Arguments.of(getObjectBytes(offsetDateTime), OffsetDateTime.class, offsetDateTime),
            Arguments.of(getObjectBytes(dateTimeRfc1123), DateTimeRfc1123.class, dateTimeRfc1123),
            Arguments.of(getObjectBytes(localDate), LocalDate.class, localDate),
            Arguments.of(getObjectBytes(uuid), UUID.class, uuid),
            Arguments.of(getObjectBytes(httpMethod), HttpMethod.class, httpMethod),
            Arguments.of(getObjectBytes(geoObjectType), GeoObjectType.class, geoObjectType));
    }

    @ParameterizedTest
    @MethodSource("textUnsupportedDeserializationSupplier")
    public void unsupportedTextTypesDeserialization(Class<?> unsupportedType,
        Class<? extends Throwable> exceptionType) {
        assertThrows(exceptionType, () -> ADAPTER.deserialize(":////", unsupportedType, SerializerEncoding.TEXT));
    }

    private static Stream<Arguments> textUnsupportedDeserializationSupplier() {
        return Stream.of(Arguments.of(InputStream.class, IllegalStateException.class),
            Arguments.of(JsonPatchDocument.class, IllegalStateException.class),
            Arguments.of(URL.class, IOException.class), // Thrown when the String isn't a valid URL
            Arguments.of(URI.class, IllegalArgumentException.class) // Thrown when the String isn't a valid URI
        );
    }

    public static final class StronglyTypedHeaders {
        private final DateTimeRfc1123 date;

        public StronglyTypedHeaders(HttpHeaders rawHeaders) {
            String dateString = rawHeaders.getValue(HttpHeaderName.DATE);
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

    private static byte[] getObjectBytes(Object value) {
        return String.valueOf(value).getBytes(StandardCharsets.UTF_8);
    }
}
