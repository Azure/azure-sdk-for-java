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

package com.microsoft.windowsazure.services.management.models;

/**
 * The base result class for all the result of service management operation.
 * 
 */
public class OperationResult {

    /** The request id. */
    protected final String requestId;

    /** The status code. */
    protected final int statusCode;

    /**
     * Instantiates a new operation result.
     * 
     * @param statusCode
     *            the status code
     * @param requestId
     *            the request id
     */
    public OperationResult(int statusCode, String requestId) {
        this.statusCode = statusCode;
        this.requestId = requestId;
    }

    /**
     * Gets the status code.
     * 
     * @return the status code
     */
    public int getStatusCode() {
        return this.statusCode;
    }

    /**
     * Gets the request id.
     * 
     * @return the request id
     */
    public String getRequestId() {
        return this.requestId;
    }

}
