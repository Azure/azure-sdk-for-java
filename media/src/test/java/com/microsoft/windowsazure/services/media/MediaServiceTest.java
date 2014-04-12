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

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.microsoft.windowsazure.Configuration;

public class MediaServiceTest {

    @Test
    public void createMediaContractSuccessTest() {
        // Arrange
        Configuration configurationInstance = Configuration.getInstance();
        configurationInstance = MediaConfiguration
                .configureWithOAuthAuthentication(configurationInstance,
                        "mediaServiceBaseUri", "oAuthUri", "clientId",
                        "clientSecret", "testScope");

        // Act
        MediaContract mediaContract = MediaService.create();

        // Assert
        assertNotNull(mediaContract);
        configurationInstance = null;

    }

    @Test
    public void createMediaContractWithSpecifiedConfigurationTest() {
        // Arrange
        Configuration configuration = MediaConfiguration
                .configureWithOAuthAuthentication("mediaServiceBaseUri",
                        "oAuthUri", "clientId", "clientSecret", "testScope");

        // Act
        MediaContract mediaContract = MediaService.create(configuration);

        // Assert
        assertNotNull(mediaContract);
    }

    @Test
    public void createMediaContractWithSpecifiedProfileTest() {
        // Arrange
        String profile = "testProfile";

        // Act
        MediaContract mediaContract = MediaService.create(profile);

        // Assert
        assertNotNull(mediaContract);

    }

    @Test
    public void createMediaContractWithSpecifiedProfileAndConfiguration() {
        // Arrange
        String profile = "testProfile";
        Configuration configuration = MediaConfiguration
                .configureWithOAuthAuthentication(profile, new Configuration(),
                        "mediaServiceBaseUri", "oAuthUri", "clientId",
                        "clientSecret", "testScope");

        // Act
        MediaContract mediaContract = MediaService.create(profile,
                configuration);

        // Assert
        assertNotNull(mediaContract);
    }
}
