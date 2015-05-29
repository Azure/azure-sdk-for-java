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

import org.apache.http.Header;

import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;

/**
 * Indicates that the underlying object supports the <code>bearer</code>
 * authentication scheme.
 * <p>
 * In order to make the Apache HTTP client call this object, see
 * {@link BearerAuthenticationSetup}.
 * 
 * @author Fernando Colombo (Microsoft)
 */
public interface BearerCredentialsSupport {

    /**
     * Authenticates a request by answering a challenge.
     * <p>
     * Implementations typically call
     * {@link BearerAuthentication#getParameters()} to identify the challenge,
     * then obtain a token that satisfies the challenge, and finally return a
     * <code>authorization</code> header object.
     * </p>
     * <p>
     * This method can return <code>null</code>, in which case the user will
     * typically see a <code>401 Unauthorized</code> error.
     * </p>
     */
    Header authenticate(ServiceRequestContext context, BearerAuthentication authentication);

}
