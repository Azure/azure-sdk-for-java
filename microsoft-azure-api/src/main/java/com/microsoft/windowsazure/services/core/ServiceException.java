/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.core;

import java.util.HashMap;
import java.util.Map;

public class ServiceException extends Exception {

    private static final long serialVersionUID = -4942076377009150131L;

    int httpStatusCode;
    String httpReasonPhrase;
    String serviceName;

    String errorCode;
    String errorMessage;
    Map<String, String> errorValues;
    String rawResponseBody;

    public ServiceException() {
        init();
    }

    public ServiceException(String message) {
        super(message);
        init();
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        init();
    }

    public ServiceException(Throwable cause) {
        super(cause);
        init();
    }

    private void init() {
        errorValues = new HashMap<String, String>();
    }

    @Override
    public String getMessage() {
        if (this.rawResponseBody == null)
            return super.getMessage();
        else
            return super.getMessage() + "\nResponse Body: " + this.rawResponseBody;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getHttpReasonPhrase() {
        return httpReasonPhrase;
    }

    public void setHttpReasonPhrase(String httpReasonPhrase) {
        this.httpReasonPhrase = httpReasonPhrase;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Map<String, String> getErrorValues() {
        return errorValues;
    }

    public void setErrorValues(Map<String, String> errorValues) {
        this.errorValues = errorValues;
    }

    public String getErrorValue(String name) {
        return errorValues.get(name);
    }

    public void setErrorValue(String name, String value) {
        this.errorValues.put(name, value);
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public void setRawResponseBody(String body) {
        this.rawResponseBody = body;
    }

    public String getRawResponseBody() {
        return rawResponseBody;
    }
}
