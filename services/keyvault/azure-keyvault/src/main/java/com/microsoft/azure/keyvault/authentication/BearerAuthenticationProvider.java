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

import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;

/**
 * A factory of {@link BearerAuthentication} authentication schemes.
 * <p>
 * The typical way of using this class is registering to an http client builder
 * by calling {@link HttpClientBuilder#setDefaultAuthSchemeRegistry(Lookup)}:
 * </p>
 *
 * <pre>
 * HttpClientBuilder httpBuilder = ...
 * RegistryBuilder&lt;AuthSchemeProvider&gt; schemeProviderBuilder = RegistryBuilder.create();
 * schemeProviderBuilder.register(BearerAuthentication.NAME, BearerAuthenticationProvider.INSTANCE);
 * httpBuilder.setDefaultAuthSchemeRegistry(schemeProviderBuilder.build());
 * </pre>
 *
 * @see BearerAuthentication
 * @see BearerCredentialsProvider
 */
final class BearerAuthenticationProvider implements AuthSchemeProvider {

    /**
     * The class singleton.
     */
    public static final BearerAuthenticationProvider INSTANCE = new BearerAuthenticationProvider();

    private BearerAuthenticationProvider() {
    }

    @Override
    public AuthScheme create(HttpContext context) {
        return new BearerAuthentication(context);
    }
}