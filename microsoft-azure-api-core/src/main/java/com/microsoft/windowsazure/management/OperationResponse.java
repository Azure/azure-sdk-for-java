/*
 * Copyright 2013 andrerod.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.management;

public class OperationResponse {
    private int _httpStatusCode;
    
    /**
    * Gets the HTTP status code for the request.
    */
    public int getStatusCode() { return this._httpStatusCode; }
    
    /**
    * Sets the HTTP status code for the request.
    */
    public void setStatusCode(int httpStatusCode) { this._httpStatusCode = httpStatusCode; }

    private String _requestId;
    
    /**
    * Gets the request identifier.
    */
    public String getRequestId() { return this._requestId; }
    
    /**
    * Sets the request identifier.
    */
    public void setRequestId(String requestId) { this._requestId = requestId; }
}
