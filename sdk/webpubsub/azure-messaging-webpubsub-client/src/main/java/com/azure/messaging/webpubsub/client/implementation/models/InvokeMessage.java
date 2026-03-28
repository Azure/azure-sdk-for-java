// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.webpubsub.client.implementation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Objects;

/**
 * The invoke message sent upstream. This carries a request-response invocation
 * and correlates with a matching {@link InvokeResponseMessage} from the service.
 */
@Fluent
public final class InvokeMessage extends WebPubSubMessage {

    private static final String TYPE = "invoke";

    private String invocationId;

    private String target = "event";

    private String event;

    private String dataType;

    private Object data;

    /**
     * Creates a new instance of InvokeMessage.
     */
    public InvokeMessage() {
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
    public InvokeMessage setInvocationId(String invocationId) {
        this.invocationId = invocationId;
        return this;
    }

    /**
     * Gets the target.
     *
     * @return the target.
     */
    public String getTarget() {
        return target;
    }

    /**
     * Sets the target. Currently, only "event" is supported.
     *
     * @param target the target.
     * @return itself.
     */
    public InvokeMessage setTarget(String target) {
        this.target = target;
        return this;
    }

    /**
     * Gets the event name.
     *
     * @return the event name.
     */
    public String getEvent() {
        return event;
    }

    /**
     * Sets the event name when targeting upstream events.
     *
     * @param event the event name.
     * @return itself.
     */
    public InvokeMessage setEvent(String event) {
        this.event = event;
        return this;
    }

    /**
     * Gets the data type.
     *
     * @return the data type.
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the data type.
     *
     * @param dataType the data type.
     * @return itself.
     */
    public InvokeMessage setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    /**
     * Gets the data.
     *
     * @return the data.
     */
    public Object getData() {
        return data;
    }

    /**
     * Sets the data.
     *
     * @param data the data.
     * @return itself.
     */
    public InvokeMessage setData(Object data) {
        this.data = data;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject().writeStringField("type", TYPE).writeStringField("invocationId", invocationId);

        if (target != null) {
            jsonWriter.writeStringField("target", target);
        }
        if (event != null) {
            jsonWriter.writeStringField("event", event);
        }
        if (dataType != null && data != null) {
            jsonWriter.writeStringField("dataType", dataType);
            jsonWriter.writeStringField("data", Objects.toString(data, null));
        }

        return jsonWriter.writeEndObject();
    }

    public static InvokeMessage fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            InvokeMessage invokeMessage = new InvokeMessage();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("invocationId".equals(fieldName)) {
                    invokeMessage.invocationId = reader.getString();
                } else if ("target".equals(fieldName)) {
                    invokeMessage.target = reader.getString();
                } else if ("event".equals(fieldName)) {
                    invokeMessage.event = reader.getString();
                } else if ("dataType".equals(fieldName)) {
                    invokeMessage.dataType = reader.getString();
                } else if ("data".equals(fieldName)) {
                    invokeMessage.data = reader.readUntyped();
                } else {
                    reader.skipChildren();
                }
            }

            return invokeMessage;
        });
    }
}
