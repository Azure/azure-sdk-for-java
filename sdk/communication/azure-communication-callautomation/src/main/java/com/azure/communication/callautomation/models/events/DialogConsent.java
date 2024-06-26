// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.models.DialogInputType;
import com.azure.communication.callautomation.models.UserConsent;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The DialogConsent model. */
@Immutable
public final class DialogConsent extends CallAutomationEventBase {

    /*
     * Contains the resulting SIP code/sub-code and message from NGC services.
     */
    private ResultInformation resultInformation;

    /*
     * Determines the type of the dialog.
     */
    private DialogInputType dialogInputType;

    /*
     * UserConsent data from the Conversation Conductor
     */
    private UserConsent userConsent;

    /*
     * Dialog ID
     */
    private String dialogId;

    /** Creates an instance of DialogConsent class. */
    public DialogConsent() {}

    /**
     * Get the resultInformation property: Contains the resulting SIP code/sub-code and message from NGC services.
     *
     * @return the resultInformation value.
     */
    public ResultInformation getResultInformation() {
        return this.resultInformation;
    }

    /**
     * Get the dialogInputType property: Determines the type of the dialog.
     *
     * @return the dialogInputType value.
     */
    public DialogInputType getDialogInputType() {
        return this.dialogInputType;
    }

    /**
     * Get the userConsent property: UserConsent data from the Conversation Conductor.
     *
     * @return the userConsent value.
     */
    public UserConsent getUserConsent() {
        return this.userConsent;
    }

    /**
     * Get the dialogId property: Dialog ID.
     *
     * @return the dialogId value.
     */
    public String getDialogId() {
        return this.dialogId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("resultInformation", resultInformation);
        jsonWriter.writeStringField("dialogInputType", dialogInputType != null ? dialogInputType.toString() : null);
        jsonWriter.writeJsonField("userConsent", userConsent);
        jsonWriter.writeStringField("dialogId", dialogId);
        super.writeFields(jsonWriter);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of DialogConsent from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of DialogConsent if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the DialogConsent.
     */
    public static DialogConsent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final DialogConsent event = new DialogConsent();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("resultInformation".equals(fieldName)) {
                    event.resultInformation = ResultInformation.fromJson(reader);
                } else if ("dialogInputType".equals(fieldName)) {
                    event.dialogInputType = DialogInputType.fromString(reader.getString());
                } else if ("userConsent".equals(fieldName)) {
                    event.userConsent = UserConsent.fromJson(reader);
                } else if ("dialogId".equals(fieldName)) {
                    event.dialogId = reader.getString();
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
