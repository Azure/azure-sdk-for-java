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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.microsoft.windowsazure.Configuration;

public class MediaConfigurationTest {

    @Test
    public void createMediaConfigurationTestSuccess() {
        // Arrange

        // Act
        Configuration configuration = MediaConfiguration
                .configureWithOAuthAuthentication(
                        "https://testMediaServiceBaseUri", "testOAuthUri",
                        "testClientId", "testClientSecret", "testScope");

        // Assert
        assertEquals("https://testMediaServiceBaseUri",
                configuration.getProperty("media.uri"));
        assertEquals("testOAuthUri",
                configuration.getProperty("media.oauth.uri"));
        assertEquals("testClientId",
                configuration.getProperty("media.oauth.client.id"));
        assertEquals("testClientSecret",
                configuration.getProperty("media.oauth.client.secret"));
        assertEquals("testScope",
                configuration.getProperty("media.oauth.scope"));
    }

    @Test
    public void createMediaConfigurationPassingExistingConfigurationSuccess() {
        // Arrange
        Configuration preConfiguration = new Configuration();
        preConfiguration.setProperty("preexistingName", "preexistingValue");

        // Act
        Configuration configuration = MediaConfiguration
                .configureWithOAuthAuthentication(preConfiguration,
                        "https://testMediaServiceBaseUri", "testOAuthUri",
                        "testClientId", "testClientSecret", "testScope");

        // Assert
        assertEquals("preexistingValue",
                configuration.getProperty("preexistingName"));
        assertEquals("https://testMediaServiceBaseUri",
                configuration.getProperty("media.uri"));
        assertEquals("testOAuthUri",
                configuration.getProperty("media.oauth.uri"));
        assertEquals("testClientId",
                configuration.getProperty("media.oauth.client.id"));
        assertEquals("testClientSecret",
                configuration.getProperty("media.oauth.client.secret"));

    }

    @Test
    public void createMediaConfigurationWithProfileConfigurationSuccess() {
        // Arrange
        Configuration preConfiguration = new Configuration();
        preConfiguration.setProperty("preexistingName", "preexistingValue");

        // Act
        Configuration configuration = MediaConfiguration
                .configureWithOAuthAuthentication("testProfile",
                        preConfiguration, "https://testMediaServiceBaseUri",
                        "testOAuthUri", "testClientId", "testClientSecret",
                        "testScope");

        // Assert
        assertEquals("preexistingValue",
                configuration.getProperty("preexistingName"));
        assertEquals("https://testMediaServiceBaseUri",
                configuration.getProperty("testProfile.media.uri"));
        assertEquals("testOAuthUri",
                configuration.getProperty("testProfile.media.oauth.uri"));
        assertEquals("testClientId",
                configuration.getProperty("testProfile.media.oauth.client.id"));
        assertEquals("testClientSecret",
                configuration
                        .getProperty("testProfile.media.oauth.client.secret"));
    }

}
