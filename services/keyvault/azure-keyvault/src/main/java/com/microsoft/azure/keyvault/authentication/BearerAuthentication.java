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

import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.impl.auth.RFC2617Scheme;
import org.apache.http.protocol.HttpContext;

import com.microsoft.windowsazure.core.pipeline.apache.HttpServiceRequestContext;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;

/**
 * An {@link AuthScheme} that conforms to OAuth 2.0 specification (which handles
 * {@value #NAME} tokens). This object contains the challenge of
 * <code>www-authenticate</code> HTTP headers.
 *
 * @see BearerCredentialsSupport
 */
public class BearerAuthentication extends RFC2617Scheme {

    /**
     * The scheme name, which is {@value #NAME}.
     */
    public static final String NAME = "bearer";

    private final HttpContext context;

    /**
     * Creates an instance specifying the {@link HttpContext}.
     */
    public BearerAuthentication(HttpContext context) {
        this.context = context;
    }

    /**
     * Always returns <code>false</code> because the same token can be used in
     * multiple connections.
     */
    @Override
    public boolean isConnectionBased() {
        return false;
    }

    /**
     * Always returns <code>false</code> to avoid caching the token.
     */
    @Override
    public boolean isComplete() {
        return false;
    }

    /**
     * Always returns {@value #NAME}.
     */
    @Override
    public String getSchemeName() {
        return BearerAuthentication.NAME;
    }

    /**
     * Returns authentication parameters, which typically are the contents of
     * the <code>www-authenticate</code> HTTP header. Use this to determine the
     * challenge to be answered.
     * <p>
     * The returned map is shared and not thread-safe.
     * </p>
     */
    @Override
    public Map<String, String> getParameters() {
        return super.getParameters();
    }

    /**
     * Implements authentication by delegating to the credentials object.
     * <p>
     * This method checks if the <code>credentials</code> parameter implements
     * {@link BearerCredentialsSupport}. If not, it silently returns
     * <code>null</code>, which tells the HTTP stack that no authentication has
     * been performed (typically the user will see a 401 error).
     * </p>
     * Otherwise it simply forwards the call to
     * {@link BearerCredentialsSupport#authenticate(ServiceRequestContext, BearerAuthentication)}
     * and returns whatever that call returns.
     */
    @Override
    @SuppressWarnings("deprecation")
    public Header authenticate(Credentials credentials, HttpRequest request) throws AuthenticationException {
        if (!(credentials instanceof BearerCredentialsSupport)) {
            return null;
        }
        BearerCredentialsSupport adapter = (BearerCredentialsSupport) credentials;
        Header result = adapter.authenticate(new HttpServiceRequestContext(request, context), this);
        return result;
    }
}