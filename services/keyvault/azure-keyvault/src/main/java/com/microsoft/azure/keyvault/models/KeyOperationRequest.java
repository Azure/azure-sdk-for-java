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

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.microsoft.azure.keyvault.webkey.Base64UrlSerializer;

@JsonAutoDetect(getterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class KeyOperationRequest {
    @JsonProperty(MessagePropertyNames.ALG)
    private String alg;

    /**
     * @return The Alg value
     */
    public String getAlg() {
        return alg;
    }

    /**
     * @param algValue
     *            The Alg value
     */
    public void setAlg(String algValue) {
        alg = algValue;
    }

    /**
     * The data to be encrypted
     */
    @JsonProperty(MessagePropertyNames.VALUE)
    @JsonSerialize(using = Base64UrlSerializer.class)
    private byte[] value;

    /**
     * @return The Value value
     */
    public byte[] getValue() {
        return value;
    }

    /**
     * @param valueValue
     *            The Value value
     */
    public void setValue(byte[] valueValue) {
        value = valueValue;
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
