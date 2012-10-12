/**
 * Copyright 2012 Microsoft Corporation
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

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.Configuration;

public class MediaConfigurationTest {

    @Test
    public void createMediaConfigurationTestSuccess() {
        // Arrange 

        // Act
        Configuration configuration = MediaConfiguration.configureWithOAuthAuthentication("testMediaServiceBaseUri",
                "testOAuthUri", "testClientId", "testClientSecret");

        // Assert
        assertEquals("https://testMediaServiceBaseUri", configuration.getProperty("media.uri"));
        assertEquals("testOAuthUri", configuration.getProperty("oauth.uri"));
        assertEquals("testClientId", configuration.getProperty("oauth.client.id"));
        assertEquals("testClientSecret", configuration.getProperty("oauth.client.secret"));
    }

    @Test
    public void createMediaConfigurationPassingExistingConfigurationSuccess() {
        // Arrange
        Configuration preConfiguration = new Configuration();
        preConfiguration.setProperty("preexistingName", "preexistingValue");

        // Act
        Configuration configuration = MediaConfiguration.configureWithOAuthAuthentication(preConfiguration,
                "testMediaServiceBaseUri", "testOAuthUri", "testClientId", "testClientSecret");

        // Assert
        assertEquals("preexistingValue", configuration.getProperty("preexistingName"));
        assertEquals("https://testMediaServiceBaseUri", configuration.getProperty("media.uri"));
        assertEquals("testOAuthUri", configuration.getProperty("oauth.uri"));
        assertEquals("testClientId", configuration.getProperty("oauth.client.id"));
        assertEquals("testClientSecret", configuration.getProperty("oauth.client.secret"));

    }

    @Test
    public void createMediaConfigurationWithProfileConfigurationSuccess() {
        // Arrange
        Configuration preConfiguration = new Configuration();
        preConfiguration.setProperty("preexistingName", "preexistingValue");

        // Act 
        Configuration configuration = MediaConfiguration.configureWithOAuthAuthentication("testProfile",
                preConfiguration, "testMediaServiceBaseUri", "testOAuthUri", "testClientId", "testClientSecret");

        // Assert
        assertEquals("preexistingValue", configuration.getProperty("preexistingName"));
        assertEquals("https://testMediaServiceBaseUri", configuration.getProperty("testProfile.media.uri"));
        assertEquals("testOAuthUri", configuration.getProperty("testProfile.oauth.uri"));
        assertEquals("testClientId", configuration.getProperty("testProfile.oauth.client.id"));
        assertEquals("testClientSecret", configuration.getProperty("testProfile.oauth.client.secret"));
    }

}
