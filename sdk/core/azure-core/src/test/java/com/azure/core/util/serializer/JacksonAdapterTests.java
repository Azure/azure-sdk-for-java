// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.util.Configuration;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @ParameterizedTest
    @MethodSource("loadAccessHelperSupplier")
    public void loadAccessHelper(Configuration configuration, boolean shouldAccessHelperBeSet,
        Object expected) {
        Function<Callable<Object>, Object> accessHelper = JacksonAdapter.loadAccessHelper(configuration);

        if (shouldAccessHelperBeSet) {
            assertNotNull(accessHelper);
            Object actual = accessHelper.apply(() -> expected);

            assertEquals(expected, actual);
        } else {
            assertNull(accessHelper);
        }
    }

    private static Stream<Arguments> loadAccessHelperSupplier() throws NoSuchMethodException {
        String accessHelperConfigName = "AZURE_JACKSON_ADAPTER_ACCESS_HELPER";

        return Stream.of(
            // NONE configuration has no values.
            Arguments.of(Configuration.NONE, false, null),

            // Empty configuration.
            Arguments.of(new Configuration(), false, null),

            // No value set for configuration property.
            Arguments.of(new Configuration().put(accessHelperConfigName, ""), false, null),

            // Access helper is a class name only.
            Arguments.of(new Configuration().put(accessHelperConfigName, JacksonAdapterTests.class.getName()), false,
                null),

            // Access helper isn't a real class.
            Arguments.of(new Configuration().put(accessHelperConfigName, "com.azure.fakeclass"), false, null),

            // Access helper isn't a real method on the class.
            Arguments.of(new Configuration().put(accessHelperConfigName,
                JacksonAdapterTests.class.getName() + ".notARealMethod"), false, null),

            // Access helper class isn't publicly accessible.
            Arguments.of(new Configuration().put(accessHelperConfigName,
                    getFullMethodString(PrivateAccessHelperClass.class, "accessHelper", Callable.class)), false, null),

            // Access helper method isn't publicly accessible.
            Arguments.of(new Configuration().put(accessHelperConfigName,
                    getFullMethodString(PublicAccessHelperClass.class, "nonPublicAccessHelper", Callable.class)),
                false, null),

            // Access helper method isn't static.
            Arguments.of(new Configuration().put(accessHelperConfigName,
                    getFullMethodString(PublicAccessHelperClass.class, "nonStaticAccessHelper", Callable.class)),
                false, null),

            // Access helper method doesn't return Object.
            Arguments.of(new Configuration().put(accessHelperConfigName,
                getFullMethodString(PublicAccessHelperClass.class, "invalidAccessHelperReturnType",
                    Callable.class)), false, null),

            // Access helper method doesn't take Callable<Object> as the only parameter.
            Arguments.of(new Configuration().put(accessHelperConfigName,
                getFullMethodString(PublicAccessHelperClass.class, "invalidAccessHelperParameters",
                    Supplier.class)), false, null),
            Arguments.of(new Configuration().put(accessHelperConfigName,
                getFullMethodString(PublicAccessHelperClass.class, "invalidAccessHelperParameters2",
                    Callable.class, int.class)), false, null),

            // Properly configured access helper.
            Arguments.of(new Configuration().put(accessHelperConfigName,
                    getFullMethodString(PublicAccessHelperClass.class, "accessHelper", Callable.class)), true,
                "Hello world!")
        );
    }

    private static String getFullMethodString(Class<?> clazz, String methodName, Class<?>... params)
        throws NoSuchMethodException {
        return clazz.getName() + "." + clazz.getDeclaredMethod(methodName, params).getName();
    }

    private static final class PrivateAccessHelperClass {
        static Object accessHelper(Callable<Object> callable) throws Exception {
            return callable.call();
        }
    }

    public final class PublicAccessHelperClass {
        static Object nonPublicAccessHelper(Callable<Object> callable) throws Exception {
            return callable.call();
        }

        public Object nonStaticAccessHelper(Callable<Object> callable) throws Exception {
            return callable.call();
        }

        public static String invalidAccessHelperReturnType(Callable<Object> callable) throws Exception {
            return (String) callable.call();
        }

        public static Object invalidAccessHelperParameters(Supplier<Object> supplier) {
            return supplier.get();
        }

        public static Object invalidAccessHelperParameters2(Callable<Object> callable, int anotherParam)
            throws Exception {
            return callable.call();
        }

        public static Object accessHelper(Callable<Object> callable) throws Exception {
            return callable.call();
        }
    }
}
