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

package com.microsoft.azure.keyvault.authentication;

import java.util.Arrays;

import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import com.microsoft.windowsazure.credentials.CloudCredentials;

/**
 * A helper class that can configure a {@link HttpClientBuilder} to call the
 * {@link BearerCredentialsSupport} class.
 *
 */
public final class BearerAuthenticationSetup {

    private BearerAuthenticationSetup() {
        // non-instantiable
    }

    /**
     * Checks if the <code>credentials</code> parameter is an instance of
     * {@link BearerCredentialsSupport}. If so, forwards the call to
     * {@link #configureClientBuilder(HttpClientBuilder, BearerCredentialsSupport)}
     * .
     */
    public static void configureClientBuilder(HttpClientBuilder httpBuilder, CloudCredentials credentials) {
        if (!(credentials instanceof BearerCredentialsSupport)) {
            return;
        }
        BearerCredentialsSupport support = (BearerCredentialsSupport) credentials;
        configureClientBuilder(httpBuilder, support);
    }

    /**
     * Configures a {@link HttpClientBuilder} to call the informed
     * {@link BearerCredentialsSupport} object to answer "bearer" challenges,
     * such as <code>401 Unauthorized</code> responses. The
     * {@link BearerCredentialsSupport} object can return a token an hide
     * <code>401</code> results from users.
     */
    public static void configureClientBuilder(HttpClientBuilder httpBuilder, BearerCredentialsSupport support) {
        // Configure to use "bearer" as preferred authentication scheme (without
        // this, the client uses basic, diggest, etc).
        Builder configBuilder = RequestConfig.custom().setTargetPreferredAuthSchemes(Arrays.asList(new String[] { BearerAuthentication.NAME }));
        httpBuilder.setDefaultRequestConfig(configBuilder.build());

        // Provide a custom "bearer" authentication provider.
        RegistryBuilder<AuthSchemeProvider> schemeProviderBuilder = RegistryBuilder.create();
        schemeProviderBuilder.register(BearerAuthentication.NAME, BearerAuthenticationProvider.INSTANCE);
        httpBuilder.setDefaultAuthSchemeRegistry(schemeProviderBuilder.build());

        // Configure to use the CloudCredentialsProvider.
        httpBuilder.setDefaultCredentialsProvider(new BearerCredentialsProvider(support));
    }

}
