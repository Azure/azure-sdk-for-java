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

import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

public class CreateKeyRequestMessage {
    @JsonProperty(MessagePropertyNames.KTY)
    private String kty;

    /**
     * @return The Kty value
     */
    public String getKty() {
        return this.kty;
    }

    /**
     * @param ktyValue
     *            The Kty value
     */
    public void setKty(String ktyValue) {
        this.kty = ktyValue;
    }

    @JsonProperty(MessagePropertyNames.KEY_SIZE)
    private Integer keySize;

    /**
     * @return The KeySize value
     */
    public Integer getKeySize() {
        return this.keySize;
    }

    /**
     * @param keySizeValue
     *            The KeySize value
     */
    public void setKeySize(Integer keySizeValue) {
        this.keySize = keySizeValue;
    }

    @JsonProperty(MessagePropertyNames.KEY_OPS)
    private String[] keyOps;

    /**
     * @return The KeyOps value
     */
    public String[] getKeyOps() {
        return this.keyOps;
    }

    /**
     * @param keyOpsValue
     *            The KeyOps value
     */
    public void setKeyOps(String[] keyOpsValue) {
        this.keyOps = keyOpsValue;
    }

    @JsonProperty(MessagePropertyNames.ATTRIBUTES)
    private KeyAttributes attributes;

    /**
     * @return The Attributes value
     */
    public KeyAttributes getAttributes() {
        return this.attributes;
    }

    /**
     * @param attributesValue
     *            The Attributes value
     */
    public void setAttributes(KeyAttributes attributesValue) {
        this.attributes = attributesValue;
    }

    @JsonProperty(MessagePropertyNames.TAGS)
    private Map<String, String> tags;

    /**
     * @return The Tags value
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * @param tagsValue
     *            The Tags value
     */
    public void setTags(Map<String, String> tagsValue) {
        this.tags = tagsValue;
    }
}
