// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.implementation.accesshelpers.DialogStateResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.models.DialogStateResponse;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/** The DialogStateResult model. */
@Immutable
public class DialogStateResult {

    /*
     * The dialog ID.
     */
    @JsonProperty(value = "dialogId")
    private String dialogId;

    /*
     * Defines options for dialog.
     */
    @JsonProperty(value = "dialogOptions")
    private StartDialogOptions dialogOptions;

    /*
     * Determines the type of the dialog.
     */
    @JsonProperty(value = "dialogInputType")
    private DialogInputType dialogInputType;

    /*
     * The value to identify context of the operation.
     */
    @JsonProperty(value = "operationContext")
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
}
