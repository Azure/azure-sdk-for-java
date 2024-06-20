// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.models.DialogInputType;
import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/** The DialogHangup model. */
@Fluent
public final class DialogHangup extends CallAutomationEventBase {
    /*
     * Contains the resulting SIP code/sub-code and message from NGC services.
     */
    @JsonProperty(value = "resultInformation", access = JsonProperty.Access.WRITE_ONLY)
    private ResultInformation resultInformation;

    /*
     * Determines the type of the dialog.
     */
    @JsonProperty(value = "dialogInputType")
    private DialogInputType dialogInputType;

    /*
     * Dialog ID
     */
    @JsonProperty(value = "dialogId", access = JsonProperty.Access.WRITE_ONLY)
    private String dialogId;

    /*
     * Ivr Context
     */
    @JsonProperty(value = "ivrContext", access = JsonProperty.Access.WRITE_ONLY)
    private Object ivrContext;

    /** Creates an instance of DialogHangup class. */
    public DialogHangup() {}

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
     * Get the ivrContext property: Ivr Context.
     *
     * @return the ivrContext value.
     */
    public Object getIvrContext() {
        return this.ivrContext;
    }

    static DialogHangup fromJsonImpl(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final DialogHangup event = new DialogHangup();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("resultInformation".equals(fieldName)) {
                    event.resultInformation = ResultInformation.fromJson(reader);
                } else if ("dialogInputType".equals(fieldName)) {
                    event.dialogInputType = DialogInputType.fromString(reader.getString());
                } else if ("dialogId".equals(fieldName)) {
                    event.dialogId = reader.getString();
                } else if ("ivrContext".equals(fieldName)) {
                    event.ivrContext = reader.readUntyped();
                } else {
                    reader.skipChildren();
                }
            }
            return event;
        });
    }
}
