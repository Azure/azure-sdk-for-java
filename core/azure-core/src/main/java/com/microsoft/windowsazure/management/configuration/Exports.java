/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.management.configuration;

import com.microsoft.windowsazure.core.Builder;
import com.microsoft.windowsazure.core.Builder.Registry;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class Exports implements Builder.Exports {

    @Override
    public void register(Registry registry) {
        
        registry.add(new Builder.Factory<URI>() {
            @Override
            public <S> URI create(String profile,
                    Class<S> service, Builder builder,
                    Map<String, Object> properties) {
                URI uri = null;
                
                if (properties.get(ManagementConfiguration.URI) != null) {
                    try {
                        if (properties.get(ManagementConfiguration.URI).getClass() == URI.class) {
                            uri = (URI) properties.get(ManagementConfiguration.URI);
                        } else if (properties.get(ManagementConfiguration.URI).getClass() == String.class) {
                            uri = new URI((String) properties.get(ManagementConfiguration.URI));
                        }
                    } catch (URISyntaxException e) {
                        // Intentionally blank
                    }
                }
                return uri;
            }
        });
    }
}
