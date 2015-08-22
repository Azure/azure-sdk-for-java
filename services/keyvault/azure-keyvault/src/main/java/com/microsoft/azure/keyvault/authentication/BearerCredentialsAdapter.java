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

import java.security.Principal;

import org.apache.http.Header;
import org.apache.http.auth.Credentials;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;

/**
 * Provides an implementation of {@link Credentials} that implements the
 * {@link BearerCredentialsSupport} interface.
 */
class BearerCredentialsAdapter implements Credentials, BearerCredentialsSupport {

    private final BearerCredentialsSupport credentialsSupport;

    public BearerCredentialsAdapter(BearerCredentialsSupport credentialsSupport) {
        this.credentialsSupport = credentialsSupport;
    }

    @Override
    public Principal getUserPrincipal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getPassword() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Header authenticate(ServiceRequestContext request, BearerAuthentication authentication) {
        return credentialsSupport.authenticate(request, authentication);
    }

}
