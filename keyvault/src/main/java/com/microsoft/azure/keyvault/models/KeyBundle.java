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

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.microsoft.azure.keyvault.webkey.JsonWebKey;

/**
 * A KeyBundle consisting of a WebKey plus its Attributes
 */
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class KeyBundle {

    /**
     * The Json web key
     */
    @JsonProperty("key")
    private JsonWebKey key;

    /**
     * @return The Key value
     */
    public JsonWebKey getKey() {
        return this.key;
    }

    /**
     * @param keyValue
     *            The Key value
     */
    public void setKey(JsonWebKey keyValue) {
        this.key = keyValue;
    }

    /**
     * The key management attributes
     */
    @JsonProperty("attributes")
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

    /**
     * The tags for the secret
     */
    @JsonProperty("tags")
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

    /**
     * Default constructor
     */
    public KeyBundle() {
        this.key = new JsonWebKey();
        this.attributes = new KeyAttributes();
    }

    public KeyIdentifier getKeyIdentifier() {
        if (key == null || key.getKid() == null || key.getKid().length() == 0) {
            return null;
        }
        return new KeyIdentifier(key.getKid());
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonGenerationException e) {
            throw new IllegalStateException(e);
        } catch (JsonMappingException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
