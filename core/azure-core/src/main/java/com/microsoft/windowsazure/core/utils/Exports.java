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
package com.microsoft.windowsazure.core.utils;

import com.microsoft.windowsazure.core.Builder;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

import java.util.Map;

public class Exports implements Builder.Exports {
    @Override
    public void register(Builder.Registry registry) {
        registry.add(DateFactory.class, DefaultDateFactory.class);
        registry.add(KeyStoreCredential.class);
        
        registry.add(new Builder.Factory<KeyStoreType>() {
            @Override
            public <S> KeyStoreType create(String profile,
                    Class<S> service, Builder builder,
                    Map<String, Object> properties) {
                KeyStoreType keyStoreType = null;
                
                Object keyStoreTypeObject = properties.get(ManagementConfiguration.KEYSTORE_TYPE);
                if (keyStoreTypeObject != null) {
                    if (keyStoreTypeObject.getClass() == KeyStoreType.class) {
                        keyStoreType = (KeyStoreType) keyStoreTypeObject;
                    }
                    else if (keyStoreTypeObject.getClass() == String.class) {
                        keyStoreType = KeyStoreType.fromString((String) properties.get(ManagementConfiguration.KEYSTORE_TYPE));
                    }
                }

                return keyStoreType;
            }
        });
    }
}
