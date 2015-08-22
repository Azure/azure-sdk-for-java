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

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.Args;

/**
 * A {@link CredentialsProvider} that provide "bearer" credentials.
 * <p>
 * The typical way of using this class is registering to an http client builder
 * by calling
 * {@link HttpClientBuilder#setDefaultCredentialsProvider(CredentialsProvider)}:
 * </p>
 *
 * <pre>
 * HttpClientBuilder httpBuilder = ...
 * BearerCredentialsSupport bearerSupport = ...
 * httpBuilder.setDefaultCredentialsProvider(new BearerCredentialsProvider(bearerSupport));
 * </pre>
 *
 * @see BearerAuthenticationProvider
 *
 */
class BearerCredentialsProvider implements CredentialsProvider {

    private final BearerCredentialsSupport bearerSupport;

    public BearerCredentialsProvider(BearerCredentialsSupport bearerSupport) {
        Args.notNull(bearerSupport, "bearerSupport");
        this.bearerSupport = bearerSupport;
    }

    @Override
    public void setCredentials(AuthScope authscope, Credentials credentials) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Credentials getCredentials(AuthScope authscope) {
        if (bearerSupport instanceof Credentials) {
            return (Credentials) bearerSupport;
        }
        return new BearerCredentialsAdapter(bearerSupport);
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

}