// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.models.DialogInputType;
import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The DialogLanguageChange model. */
@Fluent
public final class DialogLanguageChange extends CallAutomationEventBase {

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
     * Selected Language
     */
    private String selectedLanguage;

    /*
     * Ivr Context
     */
    private Object ivrContext;

    /** Creates an instance of DialogLanguageChange class. */
    public DialogLanguageChange() {}

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
     * Get the selectedLanguage property: Selected Language.
     *
     * @return the selectedLanguage value.
     */
    public String getSelectedLanguage() {
        return this.selectedLanguage;
    }

    /**
     * Get the ivrContext property: Ivr Context.
     *
     * @return the ivrContext value.
     */
    public Object getIvrContext() {
        return this.ivrContext;
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
        jsonWriter.writeStringField("selectedLanguage", selectedLanguage);
        jsonWriter.writeUntypedField("ivrContext", ivrContext);
        super.writeFields(jsonWriter);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of DialogLanguageChange from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of DialogLanguageChange if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the DialogLanguageChange.
     */
    public static DialogLanguageChange fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final DialogLanguageChange event = new DialogLanguageChange();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("resultInformation".equals(fieldName)) {
                    event.resultInformation = ResultInformation.fromJson(reader);
                } else if ("dialogInputType".equals(fieldName)) {
                    event.dialogInputType = DialogInputType.fromString(reader.getString());
                } else if ("dialogId".equals(fieldName)) {
                    event.dialogId = reader.getString();
                } else if ("selectedLanguage".equals(fieldName)) {
                    event.selectedLanguage = reader.getString();
                } else if ("ivrContext".equals(fieldName)) {
                    event.ivrContext = reader.readUntyped();
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
