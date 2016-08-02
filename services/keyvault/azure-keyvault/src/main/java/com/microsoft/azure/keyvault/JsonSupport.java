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

package com.microsoft.azure.keyvault;

import java.io.IOException;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;

final class JsonSupport {

    private JsonSupport() {
        // not instantiable
    }

    static <T> ObjectReader getJsonReader(Class<T> clazz) {
        return new ObjectMapper().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false).reader(clazz);
    }

    static ObjectWriter getJsonWriter() {
        return new ObjectMapper().writer();
    }

    static <T> KeyOpRequestMessageWithRawJsonContent serializeKeyOpRequest(T object) {
        KeyOpRequestMessageWithRawJsonContent result = new KeyOpRequestMessageWithRawJsonContent();
        try {
            String json = JsonSupport.getJsonWriter().writeValueAsString(object);
            result.setRawJsonRequest(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    static <T> SecretRequestMessageWithRawJsonContent serializeSecretOpRequest(T object) {
        SecretRequestMessageWithRawJsonContent result = new SecretRequestMessageWithRawJsonContent();
        try {
            String json = JsonSupport.getJsonWriter().writeValueAsString(object);
            result.setRawJsonRequest(json);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

}