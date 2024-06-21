// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.models.DialogInputType;
import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/** The DialogTransfer model. */
@Fluent
public final class DialogTransfer extends CallAutomationEventBase {
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
     * Transfer type
     */
    @JsonProperty(value = "transferType", access = JsonProperty.Access.WRITE_ONLY)
    private String transferType;

    /*
     * Transfer destination
     */
    @JsonProperty(value = "transferDestination", access = JsonProperty.Access.WRITE_ONLY)
    private String transferDestination;

    /*
     * IVR context
     */
    @JsonProperty(value = "ivrContext", access = JsonProperty.Access.WRITE_ONLY)
    private Object ivrContext;

    /** Creates an instance of DialogTransfer class. */
    public DialogTransfer() {}

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
     * Get the transferType property: Transfer type.
     *
     * @return the transferType value.
     */
    public String getTransferType() {
        return this.transferType;
    }

    /**
     * Get the transferDestination property: Transfer destination.
     *
     * @return the transferDestination value.
     */
    public String getTransferDestination() {
        return this.transferDestination;
    }

    /**
     * Get the ivrContext property: IVR context.
     *
     * @return the ivrContext value.
     */
    public Object getIvrContext() {
        return this.ivrContext;
    }

    /**
     * Reads an instance of DialogTransfer from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of DialogTransfer if the JsonReader was pointing to an instance of it, or null
     * if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the DialogTransfer.
     */
    public static DialogTransfer fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final DialogTransfer event = new DialogTransfer();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("resultInformation".equals(fieldName)) {
                    event.resultInformation = ResultInformation.fromJson(reader);
                } else if ("dialogInputType".equals(fieldName)) {
                    event.dialogInputType = DialogInputType.fromString(reader.getString());
                } else if ("dialogId".equals(fieldName)) {
                    event.dialogId = reader.getString();
                } else if ("transferType".equals(fieldName)) {
                    event.transferType = reader.getString();
                } else if ("transferDestination".equals(fieldName)) {
                    event.transferDestination = reader.getString();
                } else if ("ivrContext".equals(fieldName)) {
                    event.ivrContext = reader.readUntyped();
                } else {
                    if (!event.handleField(fieldName, reader)) {
                        reader.skipChildren();
                    }
                }
            }
            return event;
        });
    }
}
