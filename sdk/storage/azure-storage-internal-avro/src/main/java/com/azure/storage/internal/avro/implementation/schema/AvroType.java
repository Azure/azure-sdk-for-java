// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation.schema;

import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.util.LinkedList;
import java.util.List;

import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.ARRAY;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.BOOLEAN;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.BYTES;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.DOUBLE;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.ENUM;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.FIXED;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.FLOAT;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.INT;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.LONG;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.MAP;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.NULL;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.PRIMITIVE_TYPES;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.RECORD;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.STRING;
import static com.azure.storage.internal.avro.implementation.AvroConstants.Types.UNION;

/**
 * A class that represents an Avro type.
 * AvroTypes function as a type that stores all the data a schema may need.
 * @see AvroType#getType(JsonNode)
 */
public class AvroType {

    private final String type;

    /**
     * Creates a new instance of an AvroType.
     * @param type The type associated with the AvroType.
     */
    AvroType(String type) {
        this.type = type;
    }

    /**
     * @return the type.
     */
    String getType() {
        return type;
    }

    /**
     * An avro primitive type.
     * No additional data is required to parse a primitive.
     */
    static class AvroPrimitiveType extends AvroType {

        /**
         * Creates a new instance of an AvroPrimitiveType.
         * @param type The type associated with the AvroType.
         * @see AvroConstants.Types#PRIMITIVE_TYPES
         */
        AvroPrimitiveType(String type) {
            super(type);
        }
    }

    /**
     * An avro record type.
     * A record is defined by an array of fields.
     * @see AvroRecordField
     */
    static class AvroRecordType extends AvroType {

        private final String name;
        private final List<AvroRecordField> fields;

        /**
         * Creates a new instance of an AvroRecordType.
         * @param name The name of the record.
         * @param fields The fields in the record.
         */
        AvroRecordType(String name, List<AvroRecordField> fields) {
            super(RECORD);
            this.name = name;
            this.fields = fields;
        }

        /**
         * @return the name.
         */
        String getName() {
            return name;
        }

        /**
         * @return the fields.
         */
        List<AvroRecordField> getFields() {
            return fields;
        }
    }

    /**
     * An avro enum type.
     * An enum is defined by an array of symbols.
     */
    static class AvroEnumType extends AvroType {

        private final String name;
        private final List<String> symbols;

        /**
         * Creates a new instance of an AvroEnumType.
         * @param name The name of the enum.
         * @param symbols The symbols associated with the enum.
         */
        AvroEnumType(String name, List<String> symbols) {
            super(ENUM);
            this.name = name;
            this.symbols = symbols;
        }

        /**
         * @return the name.
         */
        String getName() {
            return name;
        }

        /**
         * @return the symbols.
         */
        List<String> getSymbols() {
            return symbols;
        }
    }

    /**
     * An avro array type.
     * An array is defined by the type of the items in it.
     */
    static class AvroArrayType extends AvroType {

        private final AvroType itemType;

        /**
         * Creates a new instance of an AvroArrayType.
         * @param itemType The type of the items in the array.
         */
        AvroArrayType(AvroType itemType) {
            super(ARRAY);
            this.itemType = itemType;
        }

        /**
         * @return the type of the items.
         */
        AvroType getItemType() {
            return itemType;
        }
    }

    /**
     * An avro map type.
     * A map is defined by the type of the values in it. The key by default is of type String.
     */
    static class AvroMapType extends AvroType {

        private final AvroType valueType;

        /**
         * Creates a new instance of an AvroMapType.
         * @param valueType The type of the values in the map.
         */
        AvroMapType(AvroType valueType) {
            super(MAP);
            this.valueType = valueType;
        }

        /**
         * @return the type of the values.
         */
        AvroType getValueType() {
            return valueType;
        }
    }

    /**
     * An avro union type.
     * A union is defined by an array of AvroTypes it could possibly be.
     */
    static class AvroUnionType extends AvroType {

        private final List<AvroType> types;

        /**
         * Creates a new instance of an AvroUnionType.
         * @param types The types that define a union.
         */
        AvroUnionType(List<AvroType> types) {
            super(UNION);
            this.types = types;
        }

        /**
         * @return the types.
         */
        List<AvroType> getTypes() {
            return types;
        }
    }

    /**
     * An avro fixed type.
     * Fixed is defined by the number of bytes to read.
     */
    static class AvroFixedType extends AvroType {

        private final Long size;

        /**
         * Creates a new instance of an AvroFixedType.
         * @param size The number of bytes to read.
         */
        AvroFixedType(Long size) {
            super(FIXED);
            this.size = size;
        }

        /**
         * @return The number of bytes to read.
         */
        Long getSize() {
            return size;
        }
    }

    /**
     * Gets the AvroType specified by the json string.
     *
     * @param jsonString the json string.
     * @return {@link AvroType}
     */
    public static AvroType getType(String jsonString) {
        JsonNode schemaJson;
        try {
            schemaJson = new ObjectMapper().readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e.getMessage());
        }
        return AvroType.getType(schemaJson);
    }

    /**
     * Gets the AvroType specified by the JsonNode.
     *
     * @param jsonSchema the json node that specifies the schema.
     * @return {@link AvroType}
     */
    private static AvroType getType(JsonNode jsonSchema) {
        JsonNodeType nodeType = jsonSchema.getNodeType();
        switch (nodeType) {
            /* Primitive Avro Types. */
            case STRING:
                return getJsonStringType(jsonSchema);
            /* Union Avro Types. */
            case ARRAY:
                return getJsonArrayType(jsonSchema);
            /* Complex Avro Types. */
            case OBJECT:
                return getJsonObjectType(jsonSchema);
            default:
                throw new RuntimeException("Unsupported type");
        }
    }

    /**
     * Gets the AvroType specified by a String JsonNode.
     *
     * @param jsonSchema the json node that specifies the schema.
     * @return {@link AvroType}
     */
    private static AvroType getJsonStringType(JsonNode jsonSchema) {
        /* TODO (gapra): This could also be another named type. Not required for QQ/CF. */
        /* Example: "long" */
        String type = jsonSchema.asText();
        if (PRIMITIVE_TYPES.contains(type)) {
            return new AvroPrimitiveType(type);
        } else {
            throw new RuntimeException("Unsupported type");
        }
    }

    /**
     * Gets the AvroType specified by an Array JsonNode.
     *
     * @param jsonSchema the json node that specifies the schema.
     * @return {@link AvroType}
     */
    private static AvroType getJsonArrayType(JsonNode jsonSchema) {
        /* Example: ["null","string"] */
        List<AvroType> types = getUnionTypes(jsonSchema);
        return new AvroUnionType(types);
    }

    /**
     * Gets the AvroType specified by an Object JsonNode.
     *
     * @param jsonSchema the json node that specifies the schema.
     * @return {@link AvroType}
     */
    private static AvroType getJsonObjectType(JsonNode jsonSchema) {
        String type = jsonSchema.get("type").asText();
        switch (type) {
            /* Primitive Types. */
            case NULL:
            case BOOLEAN:
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case BYTES:
            case STRING:
                /* Example: {"type": "string"} */
                return new AvroPrimitiveType(type);
            case RECORD: {
                /* Example: { "type": "record",
                              "name": "test",
                              "fields" : [
                                           {"name": "a", "type": "long"},
                                           {"name": "b", "type": "string"}
                                         ]
                            } */
                if (jsonSchema.get("aliases") != null) {
                    throw new IllegalArgumentException("Unexpected aliases in schema.");
                }
                String fullName = jsonSchema.get("name").asText();
                String name = fullName.substring(fullName.lastIndexOf('.') + 1);
                List<AvroRecordField> fields = getRecordFields(jsonSchema.withArray("fields"));
                return new AvroRecordType(name, fields);
            }
            case ENUM: {
                /* Example: { "type": "enum",
                              "name": "Suit",
                              "symbols" : ["SPADES", "HEARTS", "DIAMONDS", "CLUBS"]
                             } */
                if (jsonSchema.get("aliases") != null) {
                    throw new IllegalArgumentException("Unexpected aliases in schema.");
                }
                String name = jsonSchema.get("name").asText();
                List<String> symbols = getEnumSymbols(jsonSchema.withArray("symbols"));
                return new AvroEnumType(name, symbols);
            }
            case ARRAY: {
                /* Example: {"type": "array", "items": "string"} */
                AvroType items = getType(jsonSchema.get("items"));
                return new AvroArrayType(items);
            }
            case MAP: {
                /* Example: {"type": "map", "values": "long"} */
                AvroType values = getType(jsonSchema.get("values"));
                return new AvroMapType(values);
            }
            case FIXED: {
                /* Example: {"type": "fixed", "size": 16, "name": "md5"} */
                Long size = jsonSchema.get("size").asLong();
                return new AvroFixedType(size);
            }
            default:
                throw new RuntimeException("Unsupported type");
        }
    }

    /**
     * Gets the types of the union.
     *
     * @param parent the JsonNode array
     * @return The types of the union.
     */
    private static List<AvroType> getUnionTypes(JsonNode parent) {
        /* Example: ["null","string"] */
        List<AvroType> types = new LinkedList<>();
        /* Get the type of each JsonNode in parent. */
        for (JsonNode child : parent) {
            AvroType type = getType(child);
            types.add(type);
        }
        return types;
    }

    /**
     * Gets the symbols of the enum.
     *
     * @param parent The JsonNode array
     * @return The symbols of the enum.
     */
    private static List<String> getEnumSymbols(JsonNode parent) {
        /* Example: ["A", "B", "C", "D"] */
        List<String> symbols = new LinkedList<>();
        for (JsonNode child : parent) {
            symbols.add(child.asText());
        }
        return symbols;
    }

    /**
     * Gets the fields of the record.
     *
     * @param parent The JsonNode array
     * @return The fields of the record.
     */
    private static List<AvroRecordField> getRecordFields(JsonNode parent) {
        /* Example: [ {"name": "a", "type": "long"}, {"name": "b", "type": "string"} ] */
        List<AvroRecordField> fields = new LinkedList<>();
        /* Get the name and type of each JsonNode in parent. */
        for (JsonNode child : parent) {
            String name = child.get("name").asText();
            AvroType type = getType(child.get("type"));

            fields.add(new AvroRecordField(name, type));
        }
        return fields;
    }
}
