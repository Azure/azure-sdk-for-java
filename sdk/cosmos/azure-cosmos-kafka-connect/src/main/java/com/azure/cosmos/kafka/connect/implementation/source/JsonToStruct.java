// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.implementation.Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.type.MapType;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static org.apache.kafka.connect.data.Values.convertToByte;
import static org.apache.kafka.connect.data.Values.convertToDouble;
import static org.apache.kafka.connect.data.Values.convertToFloat;
import static org.apache.kafka.connect.data.Values.convertToInteger;
import static org.apache.kafka.connect.data.Values.convertToLong;
import static org.apache.kafka.connect.data.Values.convertToShort;

public class JsonToStruct {
    public static final MapType JACKSON_MAP_TYPE = Utils
        .getSimpleObjectMapper()
        .getTypeFactory()
        .constructMapType(LinkedHashMap.class, String.class, Object.class);

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonToStruct.class);
    private static final String SCHEMA_NAME_TEMPLATE = "inferred_name_%s";

    public static SchemaAndValue recordToUnifiedSchema(final String entityType, final String jsonValue) {
        checkNotNull(entityType, "Argument 'entityType' should not be null");

        Struct struct = new Struct(UnifiedMetadataSchemaConstants.SCHEMA)
            .put(UnifiedMetadataSchemaConstants.ENTITY_TYPE_NAME, entityType)
            .put(UnifiedMetadataSchemaConstants.JSON_VALUE_NAME, jsonValue);

        return new SchemaAndValue(UnifiedMetadataSchemaConstants.SCHEMA, struct);
    }

    public static SchemaAndValue recordToSchemaAndValue(final JsonNode node) {
        Schema nodeSchema = inferSchema(node);
        Struct struct = new Struct(nodeSchema);

        if (nodeSchema != null) {
            nodeSchema.fields().forEach(field -> {
                JsonNode fieldValue = node.get(field.name());
                if (fieldValue != null) {
                    SchemaAndValue schemaAndValue = toSchemaAndValue(field.schema(), fieldValue);
                    struct.put(field, schemaAndValue.value());
                } else {
                    boolean optionalField = field.schema().isOptional();
                    Object defaultValue = field.schema().defaultValue();
                    if (optionalField || defaultValue != null) {
                        struct.put(field, defaultValue);
                    } else {
                        LOGGER.error("Missing value for field {}", field.name());
                    }
                }
            });
        }
        return new SchemaAndValue(nodeSchema, struct);
    }

    private static Schema inferSchema(JsonNode jsonNode) {
        switch (jsonNode.getNodeType()) {
            case NULL:
                return Schema.OPTIONAL_STRING_SCHEMA;
            case BOOLEAN:
                return Schema.BOOLEAN_SCHEMA;
            case NUMBER:
                if (jsonNode.isIntegralNumber()) {
                    return Schema.INT64_SCHEMA;
                } else {
                    return Schema.FLOAT64_SCHEMA;
                }
            case ARRAY:
                List<JsonNode> jsonValues = new ArrayList<>();
                SchemaBuilder arrayBuilder;
                jsonNode.forEach(jn -> jsonValues.add(jn));

                Schema firstItemSchema = jsonValues.isEmpty() ? Schema.OPTIONAL_STRING_SCHEMA
                    : inferSchema(jsonValues.get(0));
                if (jsonValues.isEmpty() || jsonValues.stream()
                    .anyMatch(jv -> !Objects.equals(inferSchema(jv), firstItemSchema))) {
                    // If array is emtpy or it contains elements with different schema types
                    arrayBuilder = SchemaBuilder.array(Schema.OPTIONAL_STRING_SCHEMA);
                    arrayBuilder.name(generateName(arrayBuilder));
                    return arrayBuilder.optional().build();
                }
                arrayBuilder = SchemaBuilder.array(inferSchema(jsonValues.get(0)));
                arrayBuilder.name(generateName(arrayBuilder));
                return arrayBuilder.optional().build();
            case OBJECT:
                SchemaBuilder structBuilder = SchemaBuilder.struct();
                Iterator<Map.Entry<String, JsonNode>> it = jsonNode.fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    structBuilder.field(entry.getKey(), inferSchema(entry.getValue()));
                }
                structBuilder.name(generateName(structBuilder));
                return structBuilder.build();
            case STRING:
                return Schema.STRING_SCHEMA;
            case BINARY:
            case MISSING:
            case POJO:
            default:
                return null;
        }
    }

    // Generate Unique Schema Name
    private static String generateName(final SchemaBuilder builder) {
        return format(SCHEMA_NAME_TEMPLATE, Objects.hashCode(builder.build())).replace("-", "_");
    }

    private static SchemaAndValue toSchemaAndValue(final Schema schema, final JsonNode node) {
        SchemaAndValue schemaAndValue = new SchemaAndValue(schema, node);
        if (schema.isOptional() && node.isNull()) {
            return new SchemaAndValue(schema, null);
        }
        switch (schema.type()) {
            case INT8:
            case INT16:
            case INT32:
            case INT64:
            case FLOAT32:
            case FLOAT64:
                schemaAndValue = numberToSchemaAndValue(schema, node);
                break;
            case BOOLEAN:
                schemaAndValue = new SchemaAndValue(schema, node.asBoolean());
                break;
            case STRING:
                schemaAndValue = stringToSchemaAndValue(schema, node);
                break;
            case BYTES:
                schemaAndValue = new SchemaAndValue(schema, node);
                break;
            case ARRAY:
                schemaAndValue = arrayToSchemaAndValue(schema, node);
                break;
            case MAP:
                schemaAndValue = new SchemaAndValue(schema, node);
                break;
            case STRUCT:
                schemaAndValue = recordToSchemaAndValue(node);
                break;
            default:
                LOGGER.error("Unsupported Schema type: {}", schema.type());
        }
        return schemaAndValue;
    }

    private static SchemaAndValue stringToSchemaAndValue(final Schema schema, final JsonNode nodeValue) {
        String value;
        if (nodeValue.isTextual()) {
            value = nodeValue.asText();
        } else {
            value = nodeValue.toString();
        }
        return new SchemaAndValue(schema, value);
    }

    private static SchemaAndValue arrayToSchemaAndValue(final Schema schema, final JsonNode nodeValue) {
        if (!nodeValue.isArray()) {
            LOGGER.error("Unexpected array value for schema {}", schema);
        }
        List<Object> values = new ArrayList<>();
        nodeValue.forEach(v ->
            values.add(toSchemaAndValue(schema.valueSchema(), v).value())
        );
        return new SchemaAndValue(schema, values);
    }

    private static SchemaAndValue numberToSchemaAndValue(final Schema schema, final JsonNode nodeValue) {
        Object value = null;
        if (nodeValue.isNumber()) {
            if (nodeValue.isInt()) {
                value = nodeValue.intValue();
            } else if (nodeValue.isDouble()) {
                value = nodeValue.doubleValue();
            } else if (nodeValue.isLong()) {
                value = nodeValue.longValue();
            }
        } else {
            LOGGER.error("Unexpected value for schema {}", schema);
        }

        switch (schema.type()) {
            case INT8:
                value = convertToByte(schema, value);
                break;
            case INT16:
                value = convertToShort(schema, value);
                break;
            case INT32:
                value = convertToInteger(schema, value);
                break;
            case INT64:
                value = convertToLong(schema, value);
                break;
            case FLOAT32:
                value = convertToFloat(schema, value);
                break;
            case FLOAT64:
                value = convertToDouble(schema, value);
                break;
            default:
                LOGGER.error("Unsupported Schema type: {}", schema.type());
        }
        return new SchemaAndValue(schema, value);
    }
}
