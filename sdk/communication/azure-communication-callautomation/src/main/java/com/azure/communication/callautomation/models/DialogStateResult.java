// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.DialogStateResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.models.DialogStateResponse;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.Objects;

/** The DialogStateResult model. */
@Immutable
public class DialogStateResult implements JsonSerializable<DialogStateResult> {

    /*
     * The dialog ID.
     */
    private String dialogId;

    /*
     * Defines options for dialog.
     */
    private StartDialogOptions dialogOptions;

    /*
     * Determines the type of the dialog.
     */
    private DialogInputType dialogInputType;

    /*
     * The value to identify context of the operation.
     */
    private String operationContext;

    static {
        DialogStateResponseConstructorProxy.setAccessor(
            new DialogStateResponseConstructorProxy.DialogStateResponseConstructorAccessor() {
                @Override
                public DialogStateResult create(DialogStateResponse internalHeaders) {
                    return new DialogStateResult(internalHeaders);
                }
            });
    }

    /**
     * Public constructor.
     *
     */
    public DialogStateResult() {
        this.dialogId = null;
        this.operationContext = null;
    }

    /**
     * Constructor of the class
     *
     * @param dialogStateResponse The response from the dialog service
     */
    DialogStateResult(DialogStateResponse dialogStateResponse) {
        Objects.requireNonNull(dialogStateResponse, "dialogStateResponse must not be null");

        this.dialogId = dialogStateResponse.getDialogId();
        this.operationContext = dialogStateResponse.getOperationContext();
    }


    /**
     * Get the dialogId property: The dialog ID.
     *
     * @return the dialogId value.
     */
    public String getDialogId() {
        return dialogId;
    }

    /**
     * Get the operationContext property: The value to identify context of the operation.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return operationContext;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("dialogId", this.dialogId);
        jsonWriter.writeStringField("operationContext", this.operationContext);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of DialogStateResult from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of DialogStateResult if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the DialogStateResult.
     */
    public static DialogStateResult fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final DialogStateResult result = new DialogStateResult();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("dialogId".equals(fieldName)) {
                    result.dialogId = reader.getString();
                } else if ("operationContext".equals(fieldName)) {
                    result.operationContext = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return result;
        });
    }
}
