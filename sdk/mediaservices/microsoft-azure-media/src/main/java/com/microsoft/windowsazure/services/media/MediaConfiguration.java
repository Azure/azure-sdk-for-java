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

import java.net.URI;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.services.media.authentication.TokenProvider;

/**
 * Provides functionality to create a media services configuration.
 *
 */
public final class MediaConfiguration {

    private MediaConfiguration() {
    }

    /**
     * The token provider object
     */
    public static final String AZURE_AD_TOKEN_PROVIDER = "media.azuread.tokenprovider";

    /**
     * The azure media services account uri
     */
    public static final String AZURE_AD_API_SERVER = "media.azuread.account_api_uri";

    /**
     * Returns the default Configuration provisioned for the specified AMS account and token provider.
     * @param apiServer the AMS account uri
     * @param azureAdTokenProvider the token provider
     * @return a Configuration
     */
    public static Configuration configureWithAzureAdTokenProvider(
            URI apiServer,
            TokenProvider azureAdTokenProvider) {

        return configureWithAzureAdTokenProvider(Configuration.getInstance(), apiServer, azureAdTokenProvider);
    }

    /**
     * Setup a Configuration with specified Configuration, AMS account and token provider
     * @param configuration The target configuration
     * @param apiServer the AMS account uri
     * @param azureAdTokenProvider the token provider
     * @return the target Configuration
     */
    public static Configuration configureWithAzureAdTokenProvider(
            Configuration configuration,
            URI apiServer,
            TokenProvider azureAdTokenProvider) {

        configuration.setProperty(AZURE_AD_API_SERVER, apiServer.toString());
        configuration.setProperty(AZURE_AD_TOKEN_PROVIDER, azureAdTokenProvider);

        return configuration;
    }
}
