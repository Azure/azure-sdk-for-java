// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.serializer.JsonCapable;
import com.azure.core.util.serializer.JsonReader;
import com.azure.core.util.serializer.JsonToken;
import com.azure.core.util.serializer.JsonWriter;

import java.util.Objects;
import java.util.Optional;

/**
 * Represents a JSON Patch operation.
 */
@Immutable
public final class JsonPatchOperation implements JsonCapable<JsonPatchOperation> {
    private final JsonPatchOperationKind op;
    private final String from;
    private final String path;
    private final Option<String> value;

    /**
     * Creates a JSON Patch operation.
     * <p>
     * When {@code optionalValue} is null the value won't be included in the JSON request, use {@link Optional#empty()}
     * to indicate a JSON null.
     *
     * @param op The kind of operation.
     * @param from Optional from target path.
     * @param path Operation target path.
     * @param value Optional value.
     */
    public JsonPatchOperation(JsonPatchOperationKind op, String from, String path, Option<String> value) {
        this.op = op;
        this.from = from;
        this.path = path;
        this.value = value;
    }

    /**
     * Gets the operation kind.
     *
     * @return The kind of operation.
     */
    public JsonPatchOperationKind getOp() {
        return op;
    }

    /**
     * Gets the operation from target path.
     *
     * @return The operation from target path.
     */
    public String getFrom() {
        return from;
    }

    /**
     * Gets the operation target path.
     *
     * @return The operation target path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the operation value.
     * <p>
     * If the operation doesn't take a value {@link Option#uninitialized()} will be returned.
     *
     * @return The operation value.
     */
    public Option<String> getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(op.toString(), from, path,
            (value == null) ? null : value.getValue());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JsonPatchOperation)) {
            return false;
        }

        if (this == obj) {
            return true;
        }

        JsonPatchOperation other = (JsonPatchOperation) obj;
        return Objects.equals(op, other.op)
            && Objects.equals(from, other.from)
            && Objects.equals(path, other.path)
            && Objects.equals(value, other.value);
    }

    @Override
    public String toString() {
        return toJson(new StringBuilder()).toString();
    }

    @Override
    public StringBuilder toJson(StringBuilder stringBuilder) {
        stringBuilder.append("{\"op\":\"")
            .append(op.toString())
            .append("\"");

        if (from != null) {
            stringBuilder.append(",\"from\":\"")
                .append(from)
                .append("\"");
        }

        stringBuilder.append(",\"path\":\"")
            .append(path)
            .append("\"");

        if (value.isInitialized()) {
            stringBuilder.append(",\"value\":")
                .append(value.getValue());
        }

        return stringBuilder.append("}");
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        // Write the start object and "op" property.
        jsonWriter.writeStartObject().writeStringField("op", op.toString());

        // Only write "from" property if it isn't null.
        if (from != null) {
            jsonWriter.writeStringField("from", from);
        }

        // Write the "path" property.
        jsonWriter.writeStringField("path", path);

        // Only write the "value" property if it exists.
        if (value.isInitialized()) {
            String val = value.getValue();
            if (val == null) {
                jsonWriter.writeNullField("value");
            } else {
                jsonWriter.writeRawField("value", val);
            }
        }

        // Write the end object and flush the written data.
        return jsonWriter.writeEndObject().flush();
    }

    /**
     * Creates an instance of {@link JsonPatchOperation} by reading the {@link JsonReader}.
     * <p>
     * null will be returned if the {@link JsonReader} points to {@link JsonToken#NULL}.
     * <p>
     * {@link IllegalStateException} will be thrown if the {@link JsonReader} doesn't point to either
     * {@link JsonToken#NULL} or {@link JsonToken#START_OBJECT}.
     *
     * @param jsonReader The {@link JsonReader} that will be read.
     * @return An instance of {@link JsonPatchOperation} if the {@link JsonReader} is pointing to
     * {@link JsonPatchOperation} JSON content, or null if it's pointing to {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@link JsonReader} wasn't pointing to either {@link JsonToken#NULL}
     * or {@link JsonToken#START_OBJECT}.
     */
    public static JsonPatchOperation fromJson(JsonReader jsonReader) {
        JsonToken token = jsonReader.beginReadingObject();

        if (token == JsonToken.NULL) {
            return null;
        } else if (token != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Unexpected token to begin deserialization: " + token);
        }

        JsonPatchOperationKind op = null;
        String from = null;
        String path = null;
        Option<String> value = Option.uninitialized();

        while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonReader.getFieldName();
            token = jsonReader.nextToken();

            switch (fieldName) {
                case "op":
                    op = JsonPatchOperationKind.fromString(jsonReader.getStringValue());
                    break;

                case "from":
                    from = jsonReader.getStringValue();
                    break;

                case "path":
                    path = jsonReader.getStringValue();
                    break;

                case "value":
                    if (token == JsonToken.START_ARRAY || token == JsonToken.START_OBJECT) {
                        // value is an arbitrary array or object, read the entire JSON sub-stream.
                        value = Option.of(jsonReader.readChildren());
                    } else if (token == JsonToken.NULL) {
                        value = Option.empty();
                    } else {
                        value = Option.of(jsonReader.getTextValue());
                    }

                    break;

                default:
                    break;
            }
        }

        return new JsonPatchOperation(op, from, path, value);
    }
}
