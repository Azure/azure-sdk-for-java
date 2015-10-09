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
package com.microsoft.azure.storage.util;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import com.microsoft.azure.keyvault.authentication.KeyVaultCredentials;
import com.microsoft.windowsazure.core.pipeline.filter.ServiceRequestContext;

/**
 * A class that stores KeyVault credentials and knows how to respond to
 * authentication challenges. See KeyVaultCredentials for more information.
 */
public class KVCredentials extends KeyVaultCredentials {

    private String authClientID;

    // Note: It's very important, in a real application, to keep this secure.
    private String authClientSecret;

    /**
     * Constructs a new KVCredentials object.
     * 
     * @param clientID
     *            The ClientID for this application
     * @param clientSecret
     *            The client secret for this application
     */
    public KVCredentials(String clientID, String clientSecret) {
        this.authClientID = clientID;
        this.authClientSecret = clientSecret;
    }

    /**
     * Actually do the authentication. This method will be called by the super
     * class.
     * 
     * @param request
     *            The request being sent
     * @param challenge
     *            Information about the challenge from the service.
     */
    @Override
    public Header doAuthenticate(ServiceRequestContext request,
            Map<String, String> challenge) {
        String authorization = challenge.get("authorization");
        String resource = challenge.get("resource");
        String clientId = this.authClientID;
        String clientKey = this.authClientSecret;
        AuthenticationResult token = getAccessTokenFromClientCredentials(
                authorization, resource, clientId, clientKey);
        return new BasicHeader("Authorization", token.getAccessTokenType()
                + " " + token.getAccessToken());
    }

    /**
     * Creates the access token
     * 
     * @param authorization
     *            The authorization from the service
     * @param resource
     *            The resource being accessed
     * @param clientId
     *            The ClientID for this application
     * @param clientKey
     *            The Client Secret for this application
     * @return The access token to use to authenticate to the service
     */
    private static AuthenticationResult getAccessTokenFromClientCredentials(
            String authorization, String resource, String clientId,
            String clientKey) {
        AuthenticationContext context = null;
        AuthenticationResult result = null;
        ExecutorService service = null;
        try {
            service = Executors.newFixedThreadPool(1);
            context = new AuthenticationContext(authorization, false, service);
            ClientCredential credentials = new ClientCredential(clientId,
                    clientKey);
            Future<AuthenticationResult> future = context.acquireToken(
                    resource, credentials, null);
            result = future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            service.shutdown();
        }

        if (result == null) {
            throw new RuntimeException("authentication result was null");
        }
        return result;
    }
}
