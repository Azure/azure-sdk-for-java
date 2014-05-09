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
package com.microsoft.windowsazure.services.media.implementation;

import java.io.IOException;
import java.net.URI;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.microsoft.windowsazure.core.UserAgentFilter;
import com.microsoft.windowsazure.core.pipeline.jersey.ClientFilterRequestAdapter;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.exception.ServiceExceptionFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.representation.Form;

/**
 * The OAuth rest proxy.
 * 
 */
public class OAuthRestProxy implements OAuthContract {
    private Client channel;

    private final String grantType = "client_credentials";

    private static Log log = LogFactory.getLog(OAuthContract.class);

    @Inject
    public OAuthRestProxy(Client channel, UserAgentFilter userAgentFilter) {
        this.channel = channel;
        channel.addFilter(new ClientFilterRequestAdapter(userAgentFilter));
    }

    /**
     * Gets an OAuth access token with specified OAUTH URI, client ID, client
     * secret, and scope.
     * 
     * @param oAuthUri
     *            A <code>URI</code> object which represents an OAUTH URI.
     * 
     * @param clientId
     *            A <code>String</code> object which represents a client ID.
     * 
     * @param clientSecret
     *            A <code>String</code> object which represents a client secret.
     * 
     * @param scope
     *            A <code>String</code> object which represents the scope.
     * 
     * @return OAuthTokenResponse
     * @throws ServiceException
     */
    @Override
    public OAuthTokenResponse getAccessToken(URI oAuthUri, String clientId,
            String clientSecret, String scope) throws ServiceException {
        OAuthTokenResponse response = null;
        Form requestForm = new Form();
        ClientResponse clientResponse;
        String responseJson;

        requestForm.add("grant_type", grantType);
        requestForm.add("client_id", clientId);
        requestForm.add("client_secret", clientSecret);
        requestForm.add("scope", scope);

        try {
            clientResponse = channel.resource(oAuthUri)
                    .accept(MediaType.APPLICATION_FORM_URLENCODED)
                    .type(MediaType.APPLICATION_FORM_URLENCODED)
                    .post(ClientResponse.class, requestForm);
        } catch (UniformInterfaceException e) {
            log.warn("OAuth server returned error acquiring access_token", e);
            throw ServiceExceptionFactory
                    .process(
                            "OAuth",
                            new ServiceException(
                                    "OAuth server returned error acquiring access_token",
                                    e));
        }

        responseJson = clientResponse.getEntity(String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            TypeReference<OAuthTokenResponse> typeReference = new TypeReference<OAuthTokenResponse>() {
            };
            response = mapper.readValue(responseJson, typeReference);
        } catch (JsonParseException e) {
            log.warn(
                    "The response from OAuth server cannot be parsed correctly",
                    e);
            throw ServiceExceptionFactory
                    .process(
                            "OAuth",
                            new ServiceException(
                                    "The response from OAuth server cannot be parsed correctly",
                                    e));
        } catch (JsonMappingException e) {
            log.warn(
                    "The response from OAuth server cannot be mapped to OAuthResponse object",
                    e);
            throw ServiceExceptionFactory
                    .process(
                            "OAuth",
                            new ServiceException(
                                    "The response from OAuth server cannot be mapped to OAuthResponse object",
                                    e));
        } catch (IOException e) {
            log.warn("Cannot map the response from OAuth server correctly.", e);
            throw ServiceExceptionFactory
                    .process(
                            "OAuth",
                            new ServiceException(
                                    "Cannot map the response from OAuth server correctly.",
                                    e));
        }

        return response;
    }
}
