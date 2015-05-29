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

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import com.microsoft.azure.keyvault.webkey.Base64UrlDeserializer;

public class BackupKeyResponseMessage {

    @JsonProperty(MessagePropertyNames.VALUE)
    @JsonDeserialize(using = Base64UrlDeserializer.class)
    private byte[] value;

    /**
     * @return The Value value
     */
    public byte[] getValue() {
        return this.value;
    }

    /**
     * @param valueValue
     *            The Value value
     */
    public void setValue(byte[] valueValue) {
        this.value = valueValue;
    }
}
