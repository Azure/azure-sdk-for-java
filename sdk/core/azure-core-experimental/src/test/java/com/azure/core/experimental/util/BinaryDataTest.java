package com.azure.core.experimental.util;

import com.azure.core.serializer.json.jackson.JacksonJsonSerializer;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class BinaryDataTest {
    private static final JacksonJsonSerializer DEFAULT_SERIALIZER = new JacksonJsonSerializerBuilder().build();

    @MethodSource()
    @ParameterizedTest
    public void anyTypeToObject(Object actualValue, Object expectedValue) {

        BinaryData data = BinaryData.getBinaryData(actualValue, DEFAULT_SERIALIZER);

        assertEquals(expectedValue, data.getDataAsObject(expectedValue.getClass(), DEFAULT_SERIALIZER));
    }

    @MethodSource()
    @ParameterizedTest
    public void anyTypeToByteArray(Object actualValue, byte[] expectedValue) {
        BinaryData data = BinaryData.getBinaryData(actualValue, DEFAULT_SERIALIZER);
        assertArrayEquals(expectedValue, data.getData());
    }

    @Test
    public void constructorString() {
        final String expected = "Doe";
        BinaryData data = new BinaryData(expected);
        assertArrayEquals(expected.getBytes(), data.getData());
        assertEquals(expected, data.getDataAsString());
    }

    @Test
    public void constructorByteArray() {
        final byte[] expected = "Doe".getBytes(StandardCharsets.UTF_8);
        BinaryData data = new BinaryData(expected);
        assertArrayEquals(expected, data.getData());
    }

    static Stream<Arguments> anyTypeToByteArray() {
        return Stream.of(
            Arguments.of(new Person().setName("John Doe"), "{\"name\":\"John Doe\",\"age\":0}".getBytes(StandardCharsets.UTF_8)),
            Arguments.of(new Person().setName("John Doe").setAge(50), "{\"name\":\"John Doe\",\"age\":50}".getBytes(StandardCharsets.UTF_8))
        );
    }

    static Stream<Arguments> anyTypeToObject() {
        return Stream.of(
            Arguments.of("10", "10"),
            Arguments.of(Long.valueOf("10"), Long.valueOf("10")) ,
            Arguments.of(Double.valueOf("10.1"), Double.valueOf("10.1")),
            Arguments.of(Boolean.TRUE, Boolean.TRUE),
            Arguments.of(new Person().setName("John Doe").setAge(50), new Person().setName("John Doe").setAge(50))
        );
    }
}
