// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.serializer.json.jackson.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.DecimalNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.FloatNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Utility class that helps convert between Jackson and Azure JSON models.
 */
final class JacksonJsonElementUtils {
    /**
     * Reads a {@link JsonNode} from the {@link JsonReader}.
     * <p>
     * Throws an {@link IllegalArgumentException} if the {@link JsonReader} is pointing to an unsupported token type.
     *
     * @param jsonReader The {@link JsonReader} being read from.
     * @return The {@link JsonNode} read from the {@link JsonReader}.
     * @throws IOException If an error occurs while reading the {@link JsonReader}.
     * @throws IllegalArgumentException If the {@link JsonReader} is pointing to an unsupported token type.
     * @throws NullPointerException If the {@code jsonReader} is null.
     */
    public static JsonNode readJsonNode(JsonReader jsonReader) throws IOException {
        Objects.requireNonNull(jsonReader, "'jsonReader' cannot be null.");

        JsonToken currentToken = jsonReader.currentToken();
        if (currentToken == null) {
            currentToken = jsonReader.nextToken();
        }

        switch (currentToken) {
            case START_OBJECT:
                return readObjectNode(jsonReader);

            case START_ARRAY:
                return readArrayNode(jsonReader);

            case BOOLEAN:
                return jsonReader.getBoolean() ? BooleanNode.TRUE : BooleanNode.FALSE;

            case NULL:
                return NullNode.getInstance();

            case NUMBER:
                return parseNumeric(jsonReader.getString());

            case STRING:
                return new TextNode(jsonReader.getString());

            default:
                throw new IllegalArgumentException("Unsupported JsonToken type: " + currentToken);
        }
    }

    /**
     * Creates a NumericNode representing the string-based number.
     * <p>
     * Parsing of the string value is decided by the format of the string. If the string contains a decimal point
     * ({@code .}) or an exponent ({@code e} or {@code E}), the string will be parsed as a floating point number,
     * otherwise it will be parsed as an integer.
     * <p>
     * Parsing attempts to use the smallest container that can represent the number. For floating points it'll attempt
     * to use {@link Float#parseFloat(String)}, if that fails it'll use {@link Double#parseDouble(String)}, and finally
     * if that fails it'll use {@link BigDecimal#BigDecimal(String)}. For integers it'll attempt to use
     * {@link Integer#parseInt(String)}, if that fails it'll use {@link Long#parseLong(String)}, and finally if that
     * fails it'll use {@link BigInteger#BigInteger(String)}.
     * <p>
     * If the string is one of the special floating point representations ({@code NaN}, {@code Infinity}, etc), then
     * the value will be represented using {@link Float}.
     *
     * @param value The string-based numeric value the JsonNumber will represent.
     * @throws NumberFormatException If the string is not a valid number.
     */
    private static NumericNode parseNumeric(String value) {
        int length = value.length();
        boolean floatingPoint = false;
        boolean infinity = value.contains("Infinity");
        if (infinity) {
            // Use Double.parseDouble to handle Infinity.
            return new DoubleNode(Double.parseDouble(value));
        }

        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);
            if (c == '.' || c == 'e' || c == 'E') {
                floatingPoint = true;
                break;
            }
        }

        return floatingPoint ? handleFloatingPoint(value) : handleInteger(value);
    }

    private static NumericNode handleFloatingPoint(String value) {
        // Floating point parsing will return Infinity if the String value is larger than what can be contained by
        // the numeric type. Check if the String contains the Infinity representation to know when to scale up the
        // numeric type.
        // Additionally, due to the handling of values that can't fit into the numeric type, the only time floating
        // point parsing will throw is when the string value is invalid.
        float f = Float.parseFloat(value);

        // If the float wasn't infinite, return it.
        if (!Float.isInfinite(f)) {
            return new FloatNode(f);
        }

        double d = Double.parseDouble(value);
        if (!Double.isInfinite(d)) {
            return new DoubleNode(d);
        }

        return new DecimalNode(new BigDecimal(value));
    }

    private static NumericNode handleInteger(String value) {
        try {
            return new IntNode(Integer.parseInt(value));
        } catch (NumberFormatException failedInteger) {
            try {
                return new LongNode(Long.parseLong(value));
            } catch (NumberFormatException failedLong) {
                failedLong.addSuppressed(failedInteger);
                try {
                    return new BigIntegerNode(new BigInteger(value));
                } catch (NumberFormatException failedBigDecimal) {
                    failedBigDecimal.addSuppressed(failedLong);
                    throw failedBigDecimal;
                }
            }
        }
    }

    /**
     * Reads an array value from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} to read the array value from.
     * @return The {@link ArrayNode} read from the {@link JsonReader}.
     * @throws IOException If an error occurs while reading the array value.
     * @throws IllegalArgumentException If the array contains an unsupported {@link JsonNode} type.
     */
    static ArrayNode readArrayNode(JsonReader jsonReader) throws IOException {
        ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();

        while (jsonReader.nextToken() != JsonToken.END_ARRAY) {
            arrayNode.add(readJsonNode(jsonReader));
        }

        return arrayNode;
    }

    /**
     * Reads an object value from the {@link JsonReader}.
     *
     * @param jsonReader The {@link JsonReader} to read the object value from.
     * @return The {@link ObjectNode} read from the {@link JsonReader}.
     * @throws IOException If an error occurs while reading the object value.
     * @throws IllegalArgumentException If the object contains an unsupported {@link JsonNode} type.
     */
    static ObjectNode readObjectNode(JsonReader jsonReader) throws IOException {
        ObjectNode objectNode = JsonNodeFactory.instance.objectNode();

        while (jsonReader.nextToken() != JsonToken.END_ARRAY) {
            String fieldName = jsonReader.getFieldName();
            jsonReader.nextToken();
            objectNode.set(fieldName, readJsonNode(jsonReader));
        }

        return objectNode;
    }

    /**
     * Writes the {@link JsonNode} to the {@link JsonWriter}.
     * <p>
     * Throws an {@link IllegalArgumentException} if the {@link JsonNode} type is not supported ({@link MissingNode} or
     * {@link POJONode}).
     *
     * @param jsonWriter The {@link JsonWriter} to write the {@link JsonNode} to.
     * @param jsonNode The {@link JsonNode} to write to the {@link JsonWriter}.
     * @return The {@link JsonWriter} after writing the {@link JsonNode}.
     * @throws IOException If an error occurs while writing the {@link JsonNode}.
     * @throws IllegalArgumentException If the {@link JsonNode} type is not supported.
     */
    public static JsonWriter writeJsonNode(JsonWriter jsonWriter, JsonNode jsonNode) throws IOException {
        if (jsonNode == null) {
            return jsonWriter.writeNull();
        } else if (jsonNode instanceof ArrayNode) {
            return writeArrayNode(jsonWriter, (ArrayNode) jsonNode);
        } else if (jsonNode instanceof BinaryNode) {
            return jsonWriter.writeBinary(((BinaryNode) jsonNode).binaryValue());
        } else if (jsonNode.isBoolean()) {
            return jsonWriter.writeBoolean(jsonNode.booleanValue());
        } else if (jsonNode.isNull()) {
            return jsonWriter.writeNull();
        } else if (jsonNode.isNumber()) {
            return jsonWriter.writeNumber(jsonNode.numberValue());
        } else if (jsonNode instanceof ObjectNode) {
            return writeObjectNode(jsonWriter, (ObjectNode) jsonNode);
        } else if (jsonNode.isTextual()) {
            return jsonWriter.writeString(jsonNode.textValue());
        } else {
            throw new IllegalArgumentException("Unsupported JsonNode type: " + jsonNode.getNodeType());
        }
    }

    /**
     * Writes the array value of the {@link ArrayNode} to the {@link JsonWriter}.
     * <p>
     * If {@code arrayNode} is null the method will write a null value to the {@link JsonWriter}.
     *
     * @param jsonWriter The {@link JsonWriter} to write the array value to.
     * @param arrayNode The {@link ArrayNode} to write the array value from.
     * @return The {@link JsonWriter} after writing the array value.
     * @throws IOException If an error occurs while writing the array value.
     * @throws IllegalArgumentException If the {@link ArrayNode} contains an unsupported {@link JsonNode} type.
     */
    static JsonWriter writeArrayNode(JsonWriter jsonWriter, ArrayNode arrayNode) throws IOException {
        if (arrayNode == null) {
            return jsonWriter.writeNull();
        }

        jsonWriter.writeStartArray();
        for (JsonNode jsonNode : arrayNode) {
            writeJsonNode(jsonWriter, jsonNode);
        }
        jsonWriter.writeEndArray();

        return jsonWriter;
    }

    /**
     * Writes the object value of the {@link ObjectNode} to the {@link JsonWriter}.
     * <p>
     * If {@code objectNode} is null the method will write a null value to the {@link JsonWriter}.
     *
     * @param jsonWriter The {@link JsonWriter} to write the object value to.
     * @param objectNode The {@link TextNode} to write the object value from.
     * @return The {@link JsonWriter} after writing the object value.
     * @throws IOException If an error occurs while writing the object value.
     * @throws IllegalArgumentException If the {@link ObjectNode} contains an unsupported {@link JsonNode} type.
     */
    static JsonWriter writeObjectNode(JsonWriter jsonWriter, ObjectNode objectNode) throws IOException {
        if (objectNode == null) {
            return jsonWriter.writeNull();
        }

        jsonWriter.writeStartObject();
        Iterator<Map.Entry<String, JsonNode>> it = objectNode.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> entry = it.next();
            jsonWriter.writeFieldName(entry.getKey());
            writeJsonNode(jsonWriter, entry.getValue());
        }
        jsonWriter.writeEndObject();

        return jsonWriter;
    }

    private JacksonJsonElementUtils() {
    }
}
