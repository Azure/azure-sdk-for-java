package com.azure.storage.internal.avro.implementation.schema;

import com.azure.storage.internal.avro.implementation.AvroConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.util.ArrayList;
import java.util.Iterator;
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
 */
public class AvroType {

    private final String type;

    /**
     * Creates a new instance of an AvroType.
     * @param type The type associated with the AvroType.
     */
    public AvroType(String type) {
        this.type = type;
    }

    /**
     * @return the type.
     */
    public String getType() {
        return type;
    }

    /**
     * An avro primitive type.
     * No additional data is required to parse a primitive.
     */
    public static class AvroPrimitiveType extends AvroType {

        /**
         * Creates a new instance of an AvroPrimitiveType.
         * @param type The type associated with the AvroType.
         * @see AvroConstants.Types#PRIMITIVE_TYPES
         */
        public AvroPrimitiveType(String type) {
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
        public String getName() {
            return name;
        }

        /**
         * @return the fields.
         */
        public List<AvroRecordField> getFields() {
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
        public String getName() {
            return name;
        }

        /**
         * @return the symbols.
         */
        public List<String> getSymbols() {
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
        public AvroType getItemType() {
            return itemType;
        }
    }

    public static class AvroMapType extends AvroType {

        private final AvroType values;

        AvroMapType(AvroType values) {
            super(MAP);
            this.values = values;
        }

        public AvroType getValues() {
            return values;
        }

    }

    static class AvroUnionType extends AvroType {

        private final List<AvroType> types;

        AvroUnionType(List<AvroType> types) {
            super(UNION);
            this.types = types;
        }

        List<AvroType> getTypes() {
            return types;
        }

    }

    public static class AvroFixedType extends AvroType {

        private final Long size;

        AvroFixedType(Long size) {
            super(FIXED);
            this.size = size;
        }

        public Long getSize() {
            return size;
        }

    }

    public static AvroType getType(JsonNode schema) {
        JsonNodeType nodeType = schema.getNodeType();
        switch (nodeType) {
            /* Primitive Avro Types. */
            case STRING: {
                /* TODO : This could also be another named type. */
                String type = schema.asText();
                if (PRIMITIVE_TYPES.contains(type)) {
                    return new AvroPrimitiveType(type);
                }
            }
            /* Union Avro Types. */
            case ARRAY: {
                List<AvroType> types = getUnionTypes(schema.iterator());
                return new AvroUnionType(types);
            }
            /* Complex Avro Types. */
            case OBJECT: {
                String type = schema.get("type").asText();
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
                        return new AvroPrimitiveType(type);
                    case RECORD:{
                        if (schema.get("aliases") != null) {
                            throw new IllegalArgumentException("Unexpected aliases in schema.");
                        }
                        String name = schema.get("name").asText();
                        List<AvroRecordField> fields = getRecordFields(schema.withArray("fields"));
                        return new AvroRecordType(name, fields);
                    }
                    case ENUM: {
                        if (schema.get("aliases") != null) {
                            throw new IllegalArgumentException("Unexpected aliases in schema.");
                        }
                        String name = schema.get("name").asText();
                        List<String> symbols = getEnumSymbols(schema.withArray("symbols"));
                        return new AvroEnumType(name, symbols);
                    }
                    case ARRAY: {
                        AvroType items = getType(schema.get("items"));
                        return new AvroArrayType(items);
                    }
                    case MAP: {
                        AvroType values = getType(schema.get("values"));
                        return new AvroMapType(values);
                    }
                    case FIXED: {
                        Long size = schema.get("size").asLong();
                        return new AvroFixedType(size);
                    }
                }
            }
            default:
                throw new RuntimeException("Unsupported type");
        }
    }

    private static List<AvroType> getUnionTypes(Iterator<JsonNode> t) {
        List<AvroType> types = new ArrayList<>();
        t.forEachRemaining(typeNode -> {
            AvroType type = getType(typeNode);
            types.add(type);
        });
        return types;
    }

    private static List<String> getEnumSymbols(JsonNode s) {
        List<String> symbols = new ArrayList<>();
        for (JsonNode symbol : s) {
            symbols.add(symbol.asText());
        }
        return symbols;
    }

    private static List<AvroRecordField> getRecordFields(JsonNode f) {
        List<AvroRecordField> fields = new ArrayList<>();
        for (JsonNode field : f) {
            String name = field.get("name").asText();
            AvroType type = getType(field.get("type"));
            fields.add(new AvroRecordField(name, type));
        }
        return fields;
    }

}
