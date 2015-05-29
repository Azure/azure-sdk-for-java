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

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class SecretItem {
    private SecretIdentifier identifier;

    /**
     * @return The Identifier value
     */
    public SecretIdentifier getIdentifier() {
        return this.identifier;
    }

    @JsonProperty(MessagePropertyNames.ID)
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
            identifierValue = new SecretIdentifier(idValue); // TODO : I suspect this doesn't work with the serialization properties used here
        }
        this.identifier = identifierValue;
    }

    @JsonProperty(MessagePropertyNames.ATTRIBUTES)
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

    @JsonProperty(MessagePropertyNames.CONTENTTYPE)
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
}
