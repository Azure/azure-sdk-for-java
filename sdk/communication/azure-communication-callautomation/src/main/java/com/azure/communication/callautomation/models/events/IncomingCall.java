// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import java.io.IOException;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.callautomation.models.CustomCallingContext;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

/** The IncomingCall model. */
@Immutable
public final class IncomingCall extends CallAutomationEventBase {
    private CommunicationIdentifier to;
    private CommunicationIdentifier from;
    private String callerDisplayName;
    private CustomCallingContext customContext;
    private String incomingCallContext;
    private CommunicationIdentifier onBehalfOfCallee;

    private IncomingCall() {
    }

    private IncomingCall(CommunicationIdentifier to, CommunicationIdentifier from, String callerDisplayName,
        CustomCallingContext customContext, String incomingCallContext, CommunicationIdentifier onBehalfOfCallee) {
        this.to = to;
        this.from = from;
        this.callerDisplayName = callerDisplayName;
        this.customContext = customContext;
        this.incomingCallContext = incomingCallContext;
        this.onBehalfOfCallee = onBehalfOfCallee;
    }

    /**
     * Get the to property: The communication identifier of the target user.
     *
     * @return the to value.
     */
    public CommunicationIdentifier getTo() {
        return this.to;
    }

    /**
     * Get the from property: The communication identifier of the user who initiated the call.
     *
     * @return the from value.
     */
    public CommunicationIdentifier getFrom() {
        return this.from;
    }

    /**
     * Get the callerDisplayName property: Display name of caller.
     *
     * @return the callerDisplayName value.
     */
    public String getCallerDisplayName() {
        return this.callerDisplayName;
    }

    /**
     * Get the customContext property: Custom Context of Incoming Call.
     *
     * @return the customContext value
     */
    public CustomCallingContext getCustomContext() {
        return this.customContext;
    }

    /**
     * Get the incomingCallContext property: Incoming call context.
     *
     * @return the incomingCallContext value.
     */
    public String getIncomingCallContext() {
        return this.incomingCallContext;
    }

    /**
     * Get the onBehalfOfCallee property: The communication identifier of the user who is being called on behalf of.
     *
     * @return the onBehalfOfCallee value.
     */
    public CommunicationIdentifier getOnBehalfOfCallee() {
        return this.onBehalfOfCallee;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("to", CommunicationIdentifierConverter.convert(to));
        jsonWriter.writeJsonField("from", CommunicationIdentifierConverter.convert(from));
        jsonWriter.writeStringField("callerDisplayName", callerDisplayName);
        jsonWriter.writeJsonField("customContext", customContext);
        jsonWriter.writeStringField("incomingCallContext", incomingCallContext);
        jsonWriter.writeJsonField("onBehalfOfCallee", CommunicationIdentifierConverter.convert(onBehalfOfCallee));
        super.writeFields(jsonWriter);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of IncomingCall from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of IncomingCall if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the IncomingCall.
     */
    public static IncomingCall fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final IncomingCall event = new IncomingCall();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("to".equals(fieldName)) {
                    event.to = CommunicationIdentifierConverter.convert(CommunicationIdentifierModel.fromJson(reader));
                } else if ("from".equals(fieldName)) {
                    event.from
                        = CommunicationIdentifierConverter.convert(CommunicationIdentifierModel.fromJson(reader));
                } else if ("callerDisplayName".equals(fieldName)) {
                    event.callerDisplayName = reader.getString();
                } else if ("customContext".equals(fieldName)) {
                    event.customContext = CustomCallingContext.fromJson(reader);
                } else if ("incomingCallContext".equals(fieldName)) {
                    event.incomingCallContext = reader.getString();
                } else if ("onBehalfOfCallee".equals(fieldName)) {
                    event.onBehalfOfCallee
                        = CommunicationIdentifierConverter.convert(CommunicationIdentifierModel.fromJson(reader));
                } else {
                    if (!event.readField(fieldName, reader)) {
                        reader.skipChildren();
                    }
                }
            }
            return event;
        });
    }
}
