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
package com.microsoft.windowsazure.services.media.implementation;

import static org.junit.Assert.*;

import java.net.URI;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.media.MediaConfiguration;
import com.sun.jersey.api.client.Client;

public class OAuthRestProxyIntegrationTest {
    @Test
    public void serviceCanBeCalledToCreateAccessToken() throws Exception {
        // Arrange
        Configuration config = Configuration.getInstance();
        overrideWithEnv(config, MediaConfiguration.OAUTH_URI);
        overrideWithEnv(config, MediaConfiguration.OAUTH_CLIENT_ID);
        overrideWithEnv(config, MediaConfiguration.OAUTH_CLIENT_SECRET);
        OAuthContract oAuthContract = new OAuthRestProxy(config.create(Client.class));

        // Act
        URI oAuthUri = new URI((String) config.getProperty(MediaConfiguration.OAUTH_URI));
        String clientId = (String) config.getProperty(MediaConfiguration.OAUTH_CLIENT_ID);
        String clientSecret = (String) config.getProperty(MediaConfiguration.OAUTH_CLIENT_SECRET);
        String scope = "urn:WindowsAzureMediaServices";
        OAuthTokenResponse result = oAuthContract.getAccessToken(oAuthUri, clientId, clientSecret, scope);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getAccessToken());
    }

    private static void overrideWithEnv(Configuration config, String key) {
        String value = System.getenv(key);
        if (value == null)
            return;

        config.setProperty(key, value);
    }
}
