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
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * A Secret consisting of a value and id.
 */
public class Secret {

    @JsonProperty("value")
    private String value;

    /**
     * @return The Value value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * @param valueValue
     *            The Value value
     */
    public void setValue(String valueValue) {
        this.value = valueValue;
    }

    @JsonProperty("contentType")
    private String contentType;

    /**
     * @return The ContentType value
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * @param contentTypeValue
     *            The ContentType value
     */
    public void setContentType(String contentTypeValue) {
        this.contentType = contentTypeValue;
    }

    @JsonProperty("id")
    private String id;

    /**
     * @return The Id value
     */
    public String getId() {
        return this.id;
    }

    /**
     * @param idValue
     *            The Id value
     */
    public void setId(String idValue) {
        this.id = idValue;

        SecretIdentifier identifierValue = null;
        if (idValue != null && idValue.length() != 0) {
            identifierValue = new SecretIdentifier(idValue);
        }
        this.secretIdentifier = identifierValue;
    }

    @JsonProperty("attributes")
    private SecretAttributes attributes;

    /**
     * @return The Attributes value
     */
    public SecretAttributes getAttributes() {
        return this.attributes;
    }

    /**
     * @param attributesValue
     *            The Attributes value
     */
    public void setAttributes(SecretAttributes attributesValue) {
        this.attributes = attributesValue;
    }

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

    private SecretIdentifier secretIdentifier;

    /**
     * @return The SecretIdentifier value
     */
    public SecretIdentifier getSecretIdentifier() {
        return this.secretIdentifier;
    }

    /**
     * Default constructor
     */
    public Secret() {
        this.attributes = new SecretAttributes();
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
