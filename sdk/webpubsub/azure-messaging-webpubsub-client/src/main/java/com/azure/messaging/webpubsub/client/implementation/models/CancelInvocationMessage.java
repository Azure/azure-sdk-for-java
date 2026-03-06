// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * The cancel invocation message sent upstream to cancel a pending invocation.
 */
@Fluent
public final class CancelInvocationMessage extends WebPubSubMessage {

    private static final String TYPE = "cancelInvocation";

    private String invocationId;

    /**
     * Creates a new instance of CancelInvocationMessage.
     */
    public CancelInvocationMessage() {
    }

    /**
     * Gets the type.
     *
     * @return the type.
     */
    public String getType() {
        return TYPE;
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
     * Sets the invocation ID.
     *
     * @param invocationId the invocation ID.
     * @return itself.
     */
    public CancelInvocationMessage setInvocationId(String invocationId) {
        this.invocationId = invocationId;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("type", TYPE)
            .writeStringField("invocationId", invocationId)
            .writeEndObject();
    }

    public static CancelInvocationMessage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            CancelInvocationMessage message = new CancelInvocationMessage();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("invocationId".equals(fieldName)) {
                    message.invocationId = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            return message;
        });
    }
}
