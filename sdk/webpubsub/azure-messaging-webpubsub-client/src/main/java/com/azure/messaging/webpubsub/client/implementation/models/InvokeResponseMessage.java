// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.azure.messaging.webpubsub.client.models.AckResponseError;
import com.azure.messaging.webpubsub.client.models.WebPubSubDataFormat;

import java.io.IOException;
import java.util.Base64;

/**
 * The invoke response message received from the service. This is the correlated
 * response to an upstream {@link InvokeMessage}.
 */
@Immutable
public final class InvokeResponseMessage extends WebPubSubMessage {

    private static final String TYPE = "invokeResponse";

    private final String invocationId;
    private final Boolean success;
    private final WebPubSubDataFormat dataType;
    private final BinaryData data;
    private final AckResponseError error;

    /**
     * Creates a new instance of InvokeResponseMessage.
     *
     * @param invocationId the invocation ID.
     * @param success whether the invocation was successful.
     * @param dataType the data type.
     * @param data the data.
     * @param error the error details.
     */
    public InvokeResponseMessage(String invocationId, Boolean success, WebPubSubDataFormat dataType, BinaryData data,
        AckResponseError error) {
        this.invocationId = invocationId;
        this.success = success;
        this.dataType = dataType;
        this.data = data;
        this.error = error;
    }

    /**
     * Gets the invocation ID.
     *
     * @return the invocation ID.
     */
    public String getInvocationId() {
        return invocationId;
    }

    /**
     * Gets whether the invocation was successful.
     *
     * @return whether the invocation was successful, or null if not present.
     */
    public Boolean isSuccess() {
        return success;
    }

    /**
     * Gets the data type.
     *
     * @return the data type.
     */
    public WebPubSubDataFormat getDataType() {
        return dataType;
    }

    /**
     * Gets the response data.
     *
     * @return the data.
     */
    public BinaryData getData() {
        return data;
    }

    /**
     * Gets the error details.
     *
     * @return the error details.
     */
    public AckResponseError getError() {
        return error;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject().writeStringField("type", TYPE).writeStringField("invocationId", invocationId);

        if (success != null) {
            jsonWriter.writeBooleanField("success", success);
        }

        if (dataType != null) {
            jsonWriter.writeStringField("dataType", dataType.toString());
        }

        if (data != null) {
            if (dataType == WebPubSubDataFormat.TEXT) {
                jsonWriter.writeStringField("data", data.toString());
            } else if (dataType == WebPubSubDataFormat.BINARY || dataType == WebPubSubDataFormat.PROTOBUF) {
                jsonWriter.writeBinaryField("data", data.toBytes());
            } else {
                jsonWriter.writeRawField("data", data.toString());
            }
        }

        if (error != null) {
            jsonWriter.writeJsonField("error", error);
        }

        return jsonWriter.writeEndObject();
    }

    public static InvokeResponseMessage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String invocationId = null;
            Boolean success = null;
            WebPubSubDataFormat dataType = null;
            String rawData = null;
            AckResponseError error = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("invocationId".equals(fieldName)) {
                    invocationId = reader.getString();
                } else if ("success".equals(fieldName)) {
                    success = reader.getNullable(JsonReader::getBoolean);
                } else if ("dataType".equals(fieldName)) {
                    dataType = WebPubSubDataFormat.fromString(reader.getString());
                } else if ("data".equals(fieldName)) {
                    if (reader.isStartArrayOrObject()) {
                        rawData = reader.readChildren();
                    } else if (reader.currentToken() != JsonToken.NULL) {
                        rawData = reader.getText();
                    }
                } else if ("error".equals(fieldName)) {
                    error = AckResponseError.fromJson(reader);
                } else {
                    reader.skipChildren();
                }
            }

            BinaryData binaryData;
            if (rawData == null) {
                binaryData = null;
            } else if (dataType == WebPubSubDataFormat.TEXT) {
                binaryData = BinaryData.fromString(rawData);
            } else if (dataType == WebPubSubDataFormat.BINARY || dataType == WebPubSubDataFormat.PROTOBUF) {
                binaryData = BinaryData.fromBytes(Base64.getDecoder().decode(rawData));
            } else {
                // WebPubSubDataFormat.JSON or default
                try (JsonReader jsonReaderData = JsonProviders.createReader(rawData)) {
                    binaryData = BinaryData.fromObject(jsonReaderData.readUntyped());
                }
            }

            return new InvokeResponseMessage(invocationId, success, dataType, binaryData, error);
        });
    }
}
