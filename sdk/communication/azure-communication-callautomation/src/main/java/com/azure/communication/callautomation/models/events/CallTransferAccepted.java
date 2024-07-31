// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import java.io.IOException;

import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;


/** The CallTransferAccepted model. */
@Immutable
public final class CallTransferAccepted extends CallAutomationEventBase {
    /*
     * Contains the resulting SIP code, sub-code and message.
     */
    private ResultInformation resultInformation;

    /**
     * The participant who is being transferred away.
     */
    private CommunicationIdentifier transferee;

    /**
     * Target to whom the call is transferred.
     */
    private CommunicationIdentifier transferTarget;

    private CallTransferAccepted() {

    }

    /**
     * The participant who is being transferred away
     * @return the transferee value
     */
    public CommunicationIdentifier getTransferee() {
        return this.transferee;
    }

    /**
     * Target to whom the call is transferred.
     * @return the transferTarget value
     */
    public CommunicationIdentifier getTransferTarget() {
        return this.transferTarget;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("transferee", CommunicationIdentifierConverter.convert(transferee));
        jsonWriter.writeJsonField("transferTarget", CommunicationIdentifierConverter.convert(transferTarget));
        super.writeFields(jsonWriter);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of CallTransferAccepted from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of CallTransferAccepted if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the CallTransferAccepted.
     */
    public static CallTransferAccepted fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final CallTransferAccepted event = new CallTransferAccepted();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("transferee".equals(fieldName)) {
                    final CommunicationIdentifierModel inner = CommunicationIdentifierModel.fromJson(reader);
                    event.transferee = CommunicationIdentifierConverter.convert(inner);
                } else if ("transferTarget".equals(fieldName)) {
                    final CommunicationIdentifierModel inner = CommunicationIdentifierModel.fromJson(reader);
                    event.transferTarget = CommunicationIdentifierConverter.convert(inner);
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
