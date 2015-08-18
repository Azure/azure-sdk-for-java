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
package com.microsoft.windowsazure.services.servicebus.implementation;

import com.microsoft.windowsazure.exception.ServiceException;
import com.sun.jersey.api.client.ClientHandlerException;

import java.net.URI;
import java.net.URISyntaxException;

public class WrapFilter extends AuthorizationFilter {
    private final WrapTokenManager tokenManager;

    public WrapFilter(WrapTokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    protected String createAuthorization(String targetUri) {
        URI uri = URI.create(targetUri);
        String result;
        try {
            result = tokenManager.getAccessToken(uri);
        } catch (ServiceException e) {
            // must wrap exception because of base class signature
            throw new ClientHandlerException(e);
        } catch (URISyntaxException e) {
            // must wrap exception because of base class signature
            throw new ClientHandlerException(e);
        }

        return "WRAP access_token=\"" + result + "\"";
    }
}
