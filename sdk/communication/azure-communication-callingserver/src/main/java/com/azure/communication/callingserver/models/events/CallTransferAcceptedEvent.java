// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.models.events;

import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The CallTransferAcceptedEvent model. */
@Immutable
public final class CallTransferAcceptedEvent extends CallAutomationEventBase {
    /*
     * Operation context
     */
    @JsonProperty(value = "operationContext")
    private final String operationContext;

    /*
     * The resultInfo property.
     */
    @JsonProperty(value = "resultInfo")
    private final ResultInfo resultInfo;

    private CallTransferAcceptedEvent() {
        this.resultInfo = null;
        this.operationContext = null;
    }

    /**
     * Get the operationContext property: Operation context.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Get the resultInfo property: The resultInfo property.
     *
     * @return the resultInfo value.
     */
    public ResultInfo getResultInfo() {
        return this.resultInfo;
    }
}
