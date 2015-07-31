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
public class KeyItem {
    private KeyIdentifier identifier;

    /**
     * @return The Identifier value
     */
    public KeyIdentifier getIdentifier() {
        return identifier;
    }

    @JsonProperty(MessagePropertyNames.KID)
    private String kid;

    /**
     * @return The Kid value
     */
    public String getKid() {
        return kid;
    }

    /**
     * @param kidValue
     *            The Kid value
     */
    public void setKid(String kidValue) {
        kid = kidValue;

        KeyIdentifier identifierValue = null;
        if (kidValue != null && kidValue.length() != 0) {
            identifierValue = new KeyIdentifier(kidValue); // TODO : I suspect
                                                           // this doesn't work
                                                           // with the
                                                           // serialization
                                                           // properties used
                                                           // here
        }
        identifier = identifierValue;
    }

    @JsonProperty(MessagePropertyNames.ATTRIBUTES)
    private KeyAttributes attributes;

    /**
     * @return The Attributes value
     */
    public KeyAttributes getAttributes() {
        return attributes;
    }

    /**
     * @param attributesValue
     *            The Attributes value
     */
    public void setAttributes(KeyAttributes attributesValue) {
        attributes = attributesValue;
    }

    @JsonProperty(MessagePropertyNames.TAGS)
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
}
