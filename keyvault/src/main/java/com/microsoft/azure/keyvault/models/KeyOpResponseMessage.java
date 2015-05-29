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

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.microsoft.azure.keyvault.webkey.Base64UrlSerializer;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class KeyOpResponseMessage {
    private KeyIdentifier identifier;

    /**
     * @return The Identifier value
     */
    public KeyIdentifier getIdentifier() {
        return this.identifier;
    }

    @JsonProperty(MessagePropertyNames.KID)
    private String kid;

    /**
     * @return The Kid value
     */
    public String getKid() {
        return this.kid;
    }

    /**
     * @param kidValue
     *            The Kid value
     */
    public void setKid(String kidValue) {
        this.kid = kidValue;

        KeyIdentifier identifierValue = null;
        if (kidValue != null && kidValue.length() != 0) {
            identifierValue = new KeyIdentifier(kidValue);
        }
        this.identifier = identifierValue;
    }

    @JsonProperty(MessagePropertyNames.VALUE)
    @JsonSerialize(using = Base64UrlSerializer.class)
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