// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.models.DialogInputType;
import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The DialogSensitivityUpdate model. */
@Fluent
public final class DialogSensitivityUpdate extends CallAutomationEventBase {
    /*
     * Contains the resulting SIP code/sub-code and message from NGC services.
     */
    private ResultInformation resultInformation;

    /*
     * Determines the type of the dialog.
     */
    private DialogInputType dialogInputType;

    /*
     * Dialog ID
     */
    private String dialogId;

    /*
     * SensitiveMask
     */
    private Boolean sensitiveMask;

    /** Creates an instance of DialogSensitivityUpdate class. */
    public DialogSensitivityUpdate() {}

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
     * Get the dialogId property: Dialog ID.
     *
     * @return the dialogId value.
     */
    public String getDialogId() {
        return this.dialogId;
    }

    /**
     * Get the sensitiveMask property: SensitiveMask.
     *
     * @return the sensitiveMask value.
     */
    public Boolean isSensitiveMask() {
        return this.sensitiveMask;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeJsonField("resultInformation", resultInformation);
        jsonWriter.writeStringField("dialogInputType", dialogInputType != null ? dialogInputType.toString() : null);
        jsonWriter.writeStringField("dialogId", dialogId);
        jsonWriter.writeBooleanField("sensitiveMask", sensitiveMask);
        super.writeFields(jsonWriter);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of DialogSensitivityUpdate from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of DialogSensitivityUpdate if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the DialogSensitivityUpdate.
     */
    public static DialogSensitivityUpdate fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final DialogSensitivityUpdate event = new DialogSensitivityUpdate();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("resultInformation".equals(fieldName)) {
                    event.resultInformation = ResultInformation.fromJson(reader);
                } else if ("dialogInputType".equals(fieldName)) {
                    event.dialogInputType = DialogInputType.fromString(reader.getString());
                } else if ("dialogId".equals(fieldName)) {
                    event.dialogId = reader.getString();
                } else if ("sensitiveMask".equals(fieldName)) {
                    event.sensitiveMask = reader.getBoolean();
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
