/**
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

public enum OperationState {
    /** Succeeded. */
    Succeeded(0),

    /** Failed. */
    Failed(1),

    /** InProgress. */
    InProgress(2);

    /** operation state code. */
    private int operationStateCode;

    /**
     * Instantiates a new operation state.
     * 
     * @param operationStateCode
     *            the operation state code
     */
    private OperationState(int operationStateCode) {
        this.operationStateCode = operationStateCode;
    }

    /**
     * Gets the code.
     * 
     * @return the code
     */
    public int getCode() {
        return this.operationStateCode;
    }

    /**
     * From code.
     * 
     * @param operationStateCode
     *            the operation state code
     * @return the operation
     */
    public static OperationState fromCode(int operationStateCode) {
        switch (operationStateCode) {
            case 0:
                return OperationState.Succeeded;
            case 1:
                return OperationState.Failed;
            case 2:
                return OperationState.InProgress;
            default:
                throw new InvalidParameterException("operationStateCode");
        }
    }
}
