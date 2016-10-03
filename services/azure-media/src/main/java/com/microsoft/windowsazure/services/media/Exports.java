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
package com.microsoft.windowsazure.services.media;

import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import com.microsoft.windowsazure.core.Builder;
import com.microsoft.windowsazure.core.UserAgentFilter;
import com.microsoft.windowsazure.services.media.implementation.BatchMimeMultipartBodyWritter;
import com.microsoft.windowsazure.services.media.implementation.MediaContentProvider;
import com.microsoft.windowsazure.services.media.implementation.MediaExceptionProcessor;
import com.microsoft.windowsazure.services.media.implementation.MediaRestProxy;
import com.microsoft.windowsazure.services.media.implementation.OAuthContract;
import com.microsoft.windowsazure.services.media.implementation.OAuthFilter;
import com.microsoft.windowsazure.services.media.implementation.OAuthRestProxy;
import com.microsoft.windowsazure.services.media.implementation.OAuthTokenManager;
import com.microsoft.windowsazure.services.media.implementation.ODataEntityCollectionProvider;
import com.microsoft.windowsazure.services.media.implementation.ODataEntityProvider;
import com.microsoft.windowsazure.services.media.implementation.RedirectFilter;
import com.microsoft.windowsazure.services.media.implementation.ResourceLocationManager;
import com.microsoft.windowsazure.services.media.implementation.VersionHeadersFilter;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class Exports implements Builder.Exports {

    /**
     * register the Media services.
     */
    @Override
    public void register(Builder.Registry registry) {
        registry.add(MediaContract.class, MediaExceptionProcessor.class);
        registry.add(MediaRestProxy.class);
        registry.add(OAuthContract.class, OAuthRestProxy.class);
        registry.add(OAuthTokenManager.class);
        registry.add(OAuthFilter.class);
        registry.add(ResourceLocationManager.class);
        registry.add(RedirectFilter.class);
        registry.add(VersionHeadersFilter.class);
        registry.add(UserAgentFilter.class);

        registry.alter(MediaContract.class, ClientConfig.class,
                new Builder.Alteration<ClientConfig>() {
                    @SuppressWarnings("rawtypes")
                    @Override
                    public ClientConfig alter(String profile,
                            ClientConfig instance, Builder builder,
                            Map<String, Object> properties) {

                        instance.getProperties().put(
                                JSONConfiguration.FEATURE_POJO_MAPPING, true);

                        // Turn off auto-follow redirects, because Media
                        // Services rest calls break if it's on
                        instance.getProperties().put(
                                ClientConfig.PROPERTY_FOLLOW_REDIRECTS, false);

                        try {
                            instance.getSingletons().add(
                                    new ODataEntityProvider());
                            instance.getSingletons().add(
                                    new ODataEntityCollectionProvider());
                            instance.getSingletons().add(
                                    new MediaContentProvider());
                            instance.getSingletons().add(
                                    new BatchMimeMultipartBodyWritter());
                        } catch (JAXBException e) {
                            throw new RuntimeException(e);
                        } catch (ParserConfigurationException e) {
                            throw new RuntimeException(e);
                        }

                        return instance;
                    }
                });
    }
}
