/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.media;

import com.microsoft.windowsazure.services.core.Builder;
import com.microsoft.windowsazure.services.media.implementation.MediaServicesExceptionProcessor;
import com.microsoft.windowsazure.services.media.implementation.MediaServicesRestProxy;
import com.microsoft.windowsazure.services.media.implementation.OAuthContract;
import com.microsoft.windowsazure.services.media.implementation.OAuthFilter;
import com.microsoft.windowsazure.services.media.implementation.OAuthRestProxy;
import com.microsoft.windowsazure.services.media.implementation.OAuthTokenManager;
import com.microsoft.windowsazure.services.media.implementation.RedirectFilter;
import com.microsoft.windowsazure.services.media.implementation.ResourceLocationManager;

public class Exports implements Builder.Exports {

    /**
     * register the OAUTH service.
     */
    @Override
    public void register(Builder.Registry registry) {
        registry.add(MediaServicesContract.class, MediaServicesExceptionProcessor.class);
        registry.add(MediaServicesExceptionProcessor.class);
        registry.add(MediaServicesRestProxy.class);
        registry.add(OAuthContract.class, OAuthRestProxy.class);
        registry.add(OAuthTokenManager.class);
        registry.add(OAuthFilter.class);
        registry.add(ResourceLocationManager.class);
        registry.add(RedirectFilter.class);
    }

}
