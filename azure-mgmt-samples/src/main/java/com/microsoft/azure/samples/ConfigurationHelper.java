/**
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.azure.samples;

import com.microsoft.azure.utility.AuthHelper;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

import java.net.URI;

public class ConfigurationHelper {
    /**
     * Create configuration builds the management configuration needed for creating the clients.
     * The config contains the baseURI which is the base of the ARM REST service, the subscription id as the context for
     * the ResourceManagementService and the AAD token required for the HTTP Authorization header.
     *
     * @return Configuration the generated configuration
     * @throws Exception all of the exceptions!!
     */
    public static Configuration createConfiguration() throws Exception {
        String baseUri = System.getenv("arm.url");

        return ManagementConfiguration.configure(
                null,
                baseUri != null ? new URI(baseUri) : null,
                System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
                AuthHelper.getAccessTokenFromServicePrincipalCredentials(
                        System.getenv(ManagementConfiguration.URI), System.getenv("arm.aad.url"),
                        System.getenv("arm.tenant"), System.getenv("arm.clientid"),
                        System.getenv("arm.clientkey"))
                        .getAccessToken());
    }
}
