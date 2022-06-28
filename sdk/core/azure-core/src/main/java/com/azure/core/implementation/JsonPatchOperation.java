// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.serializer.JsonUtils;
import com.azure.json.DefaultJsonWriter;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a JSON Patch operation.
 */
@Immutable
public final class JsonPatchOperation implements JsonSerializable<JsonPatchOperation> {
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
        AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
        JsonWriter writer = DefaultJsonWriter.fromStream(outputStream);
        toJson(writer);

        return outputStream.toString(StandardCharsets.UTF_8);
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) {
        // Write the start object and "op" property.
        jsonWriter.writeStartObject().writeStringField("op", op.toString())
            // Only write "from" property if it isn't null.
            .writeStringField("from", from, false)
            // Write the "path" property.
            .writeStringField("path", path);

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
     * {@link IllegalStateException} will be thrown if the {@link JsonReader} doesn't point to either {@link
     * JsonToken#NULL} or {@link JsonToken#START_OBJECT}.
     *
     * @param jsonReader The {@link JsonReader} that will be read.
     * @return An instance of {@link JsonPatchOperation} if the {@link JsonReader} is pointing to {@link
     * JsonPatchOperation} JSON content, or null if it's pointing to {@link JsonToken#NULL}.
     * @throws IllegalStateException If the {@link JsonReader} wasn't pointing to either {@link JsonToken#NULL} or
     * {@link JsonToken#START_OBJECT}.
     */
    public static JsonPatchOperation fromJson(JsonReader jsonReader) {
        return JsonUtils.readObject(jsonReader, reader -> {
            JsonPatchOperationKind op = null;
            String from = null;
            String path = null;
            Option<String> value = Option.uninitialized();

            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonReader.getFieldName();
                jsonReader.nextToken();

                if ("op".equals(fieldName)) {
                    op = JsonPatchOperationKind.fromString(jsonReader.getStringValue());
                } else if ("from".equals(fieldName)) {
                    from = jsonReader.getStringValue();
                } else if ("path".equals(fieldName)) {
                    path = jsonReader.getStringValue();
                } else if ("value".equals(fieldName)) {
                    if (reader.isStartArrayOrObject()) {
                        // value is an arbitrary array or object, read the entire JSON sub-stream.
                        value = Option.of(jsonReader.readChildren());
                    } else {
                        value = Option.of(jsonReader.getTextValue());
                    }
                } else {
                    reader.skipChildren();
                }
            }

            return new JsonPatchOperation(op, from, path, value);
        });
    }
}
