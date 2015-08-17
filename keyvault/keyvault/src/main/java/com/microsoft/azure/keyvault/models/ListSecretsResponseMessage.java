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

public class ListSecretsResponseMessage {
    @JsonProperty(MessagePropertyNames.VALUE)
    private SecretItem[] value;

    /**
     * @return The Value value
     */
    public SecretItem[] getValue() {
        return value;
    }

    /**
     * @param valueValue
     *            The Value value
     */
    public void setValue(SecretItem[] valueValue) {
        value = valueValue;
    }

    @JsonProperty(MessagePropertyNames.NEXTLINK)
    private String nextLink;

    /**
     * @return The NextLink value
     */
    public String getNextLink() {
        return nextLink;
    }

    /**
     * @param nextLinkValue
     *            The NextLink value
     */
    public void setNextLink(String nextLinkValue) {
        nextLink = nextLinkValue;
    }
}
