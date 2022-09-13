// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The RecognizeFailed model. */
@Fluent
public final class RecognizeFailed extends CallAutomationEventBase {
    /*
     * Operation context
     */
    @JsonProperty(value = "operationContext")
    private String operationContext;

    /*
     * Defines the code, sub-code and message for the operation
     */
    @JsonProperty(value = "resultInfo")
    private ResultInformation resultInfo;

    /**
     * Get the operationContext property: Operation context.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Set the operationContext property: Operation context.
     *
     * @param operationContext the operationContext value to set.
     * @return the RecognizeFailed object itself.
     */
    public RecognizeFailed setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Get the resultInfo property: Defines the code, sub-code and message for the operation.
     *
     * @return the resultInfo value.
     */
    public ResultInformation getResultInfo() {
        return this.resultInfo;
    }

    /**
     * Set the resultInfo property: Defines the code, sub-code and message for the operation.
     *
     * @param resultInfo the resultInfo value to set.
     * @return the RecognizeFailed object itself.
     */
    public RecognizeFailed setResultInfo(ResultInformation resultInfo) {
        this.resultInfo = resultInfo;
        return this;
    }
}
