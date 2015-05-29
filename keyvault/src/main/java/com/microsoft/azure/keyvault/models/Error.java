/**
 * 
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.microsoft.azure.keyvault.models;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.annotate.JsonProperty;

public class Error {
    @JsonProperty("code")
    private String code;

    /**
     * @return The Code value
     */
    public String getCode() {
        return this.code;
    }

    /**
     * @param codeValue
     *            The Code value
     */
    public void setCode(String codeValue) {
        this.code = codeValue;
    }

    @JsonProperty("message")
    private String message;

    /**
     * @return The Message value
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * @param messageValue
     *            The Message value
     */
    public void setMessage(String messageValue) {
        this.message = messageValue;
    }

    /**
     * Extension data
     */
    private Map<String, Object> additionalInfo;

    /**
     * @return The AdditionalInfo value
     */
    public Map<String, Object> getAdditionalInfo() {
        return this.additionalInfo;
    }

    /**
     * @param resultValue
     *            The Result value
     */
    @JsonAnySetter()
    public void putAdditionalInfo(String key, Object value) {
        this.additionalInfo.put(key, value);
    }

    /**
     * Default constructor
     */
    public Error() {
        this.additionalInfo = new HashMap<String, Object>();
    }
}
