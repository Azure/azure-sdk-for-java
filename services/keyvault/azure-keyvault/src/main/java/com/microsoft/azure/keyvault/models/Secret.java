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

/**
 * A Secret consisting of a value and id.
 */
@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class Secret {

    @JsonProperty("value")
    private String value;

    /**
     * @return The Value value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param valueValue
     *            The Value value
     */
    public void setValue(String valueValue) {
        value = valueValue;
    }

    @JsonProperty("contentType")
    private String contentType;

    /**
     * @return The ContentType value
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentTypeValue
     *            The ContentType value
     */
    public void setContentType(String contentTypeValue) {
        contentType = contentTypeValue;
    }

    @JsonProperty("id")
    private String id;

    /**
     * @return The Id value
     */
    public String getId() {
        return id;
    }

    /**
     * @param id
     *            The Id value
     */
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("attributes")
    private SecretAttributes attributes;

    /**
     * @return The Attributes value
     */
    public SecretAttributes getAttributes() {
        return attributes;
    }

    /**
     * @param attributesValue
     *            The Attributes value
     */
    public void setAttributes(SecretAttributes attributesValue) {
        attributes = attributesValue;
    }

    @JsonProperty("tags")
    private Map<String, String> tags;

    /**
     * @return The Tags value
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * @param tagsValue
     *            The Tags value
     */
    public void setTags(Map<String, String> tagsValue) {
        tags = tagsValue;
    }

    /**
     * Default constructor
     */
    public Secret() {
        attributes = new SecretAttributes();
    }

    public SecretIdentifier getSecretIdentifier() {
        if (id == null || id.length() == 0) {
            return null;
        }
        return new SecretIdentifier(id);
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
