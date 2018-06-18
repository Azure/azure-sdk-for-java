/*
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.media.models;

import java.security.InvalidParameterException;

/**
 * Specifies the states of the se.
 */
public enum OperationState {

    Succeeded("Succeeded"),

    Failed("Failed"),
    
    InProgress("InProgress");

    /** The op state code. */
    private String operationStateCode;

    /**
     * Instantiates a new op state.
     * 
     * @param operationStateCode
     *            the op state code
     */
    private OperationState(String operationStateCode) {
        this.operationStateCode = operationStateCode;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public String getCode() {
        return operationStateCode;
    }

    /**
     * Create an OperationState instance from the corresponding String.
     * 
     * @param state
     *            state as integer
     * @return new StreamingEndpointState instance
     */
    public static OperationState fromCode(String state) {
        if (state.equals("Succeeded")) {
            return OperationState.Succeeded;
        } else if (state.equals("Failed")) {
            return OperationState.Failed;
        } else if (state.equals("InProgress")) {
            return OperationState.InProgress;
        } else {
            throw new InvalidParameterException("state");
        }
    }
}
