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

package com.microsoft.windowsazure.core;

public class OperationResponse {
    private int httpStatusCode;

    /**
     * Gets the HTTP status code for the request.
     * 
     * @return The HTTP status code.
     */
    public int getStatusCode() {
        return this.httpStatusCode;
    }

    /**
     * Sets the HTTP status code for the request.
     * 
     * @param httpStatusCode
     *            The HTTP status code.
     */
    public void setStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    private String requestId;

    /**
     * Gets the request identifier.
     * 
     * @return The request identifier.
     */
    public String getRequestId() {
        return this.requestId;
    }

    /**
     * Sets the request identifier.
     * 
     * @param requestId
     *            The request identifier.
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
