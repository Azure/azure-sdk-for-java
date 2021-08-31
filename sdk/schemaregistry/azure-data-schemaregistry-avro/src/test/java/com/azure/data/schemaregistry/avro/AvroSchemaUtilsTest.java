// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.avro;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link AvroSchemaUtils}.
 */
public class AvroSchemaUtilsTest {

    public static Stream<Arguments> getPrimitiveTypes() {
        return Arrays.stream(Schema.Type.values())
            .map(value -> {
                // This is fine because there are types in Schema.Type that are not primitive and Schema.create only
                // gets schemas for primitive types.
                final Schema schema;
                try {
                    schema = Schema.create(value);
                } catch (AvroRuntimeException e) {
                    return null;
                }
                return Arguments.of(value, schema);
            }).filter(Objects::nonNull);
    }

    @MethodSource
    @ParameterizedTest
    public void getPrimitiveTypes(Schema.Type type, Schema expectedSchema) {
        // Arrange
        final Map<Schema.Type, Schema> primitiveTypes = AvroSchemaUtils.getPrimitiveSchemas();

        // Act & Assert
        assertTrue(primitiveTypes.containsKey(type));

        final Schema actual = primitiveTypes.get(type);
        assertEquals(expectedSchema, actual);
    }

    /**
     * Asserts that we can't modify the primitive types.
     */
    @Test
    public void cannotModifyPrimitives() {
        // Arrange
        final Schema testTypesSchema = Schema.createEnum("TestType", "Different test types",
            "org.example", Arrays.asList("UNIT", "INTEGRATION", "REGRESSION", "PERFORMANCE"));

        final Map<Schema.Type, Schema> primitives = AvroSchemaUtils.getPrimitiveSchemas();

        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> primitives.put(Schema.Type.RECORD, testTypesSchema));
        assertThrows(UnsupportedOperationException.class, () -> primitives.remove(Schema.Type.BOOLEAN));
    }
}
