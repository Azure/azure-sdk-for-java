// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The ResponsesComputerCallItemAction model.
 */
@Immutable
public class ResponsesComputerCallItemAction implements JsonSerializable<ResponsesComputerCallItemAction> {

    /*
     * The type property.
     */
    @Generated
    private ResponsesComputerCallActionType type;

    /**
     * Creates an instance of ResponsesComputerCallItemAction class.
     */
    @Generated
    public ResponsesComputerCallItemAction() {
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    public ResponsesComputerCallActionType getType() {
        return this.type;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesComputerCallItemAction from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesComputerCallItemAction if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ResponsesComputerCallItemAction.
     */
    @Generated
    public static ResponsesComputerCallItemAction fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String discriminatorValue = null;
            try (JsonReader readerToUse = reader.bufferObject()) {
                // Prepare for reading
                readerToUse.nextToken();
                while (readerToUse.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = readerToUse.getFieldName();
                    readerToUse.nextToken();
                    if ("type".equals(fieldName)) {
                        discriminatorValue = readerToUse.getString();
                        break;
                    } else {
                        readerToUse.skipChildren();
                    }
                }
                // Use the discriminator value to determine which subtype should be deserialized.
                if ("click".equals(discriminatorValue)) {
                    return ResponsesComputerCallClickAction.fromJson(readerToUse.reset());
                } else if ("double_click".equals(discriminatorValue)) {
                    return ResponsesComputerCallDoubleClickAction.fromJson(readerToUse.reset());
                } else if ("scroll".equals(discriminatorValue)) {
                    return ResponsesComputerCallScrollAction.fromJson(readerToUse.reset());
                } else if ("screenshot".equals(discriminatorValue)) {
                    return ResponsesComputerCallScreenshotAction.fromJson(readerToUse.reset());
                } else if ("type".equals(discriminatorValue)) {
                    return ResponsesComputerCallTypeAction.fromJson(readerToUse.reset());
                } else if ("wait".equals(discriminatorValue)) {
                    return ResponsesComputerCallWaitAction.fromJson(readerToUse.reset());
                } else if ("keypress".equals(discriminatorValue)) {
                    return ResponsesComputerCallKeyPressAction.fromJson(readerToUse.reset());
                } else if ("drag".equals(discriminatorValue)) {
                    return ResponsesComputerCallDragAction.fromJson(readerToUse.reset());
                } else if ("move".equals(discriminatorValue)) {
                    return ResponsesComputerCallMoveAction.fromJson(readerToUse.reset());
                } else {
                    return fromJsonKnownDiscriminator(readerToUse.reset());
                }
            }
        });
    }

    @Generated
    static ResponsesComputerCallItemAction fromJsonKnownDiscriminator(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResponsesComputerCallItemAction deserializedResponsesComputerCallItemAction
                = new ResponsesComputerCallItemAction();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName)) {
                    deserializedResponsesComputerCallItemAction.type
                        = ResponsesComputerCallActionType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedResponsesComputerCallItemAction;
        });
    }
}
