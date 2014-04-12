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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.inject.Named;
import javax.management.timer.Timer;

import com.microsoft.windowsazure.core.utils.DateFactory;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.MediaConfiguration;

/**
 * An OAuth token manager class.
 * 
 */
public class OAuthTokenManager {
    private final DateFactory dateFactory;
    private final URI acsBaseUri;
    private final String clientId;
    private final String clientSecret;
    private final OAuthContract contract;
    private ActiveToken activeToken;
    private final String scope;

    /**
     * Creates an OAuth token manager instance with specified contract, date
     * factory, ACS base URI, client ID, and client secret.
     * 
     * @param contract
     *            A <code>OAuthContract</code> object instance that represents
     *            the OAUTH contract.
     * 
     * @param dateFactory
     *            A <code>DateFactory</code> object instance that represents the
     *            date factory.
     * 
     * @param oAuthUri
     *            A <code>String</code> object instance that represents the ACS
     *            base URI.
     * 
     * @param clientId
     *            A <code>String</code> object instance that represents the
     *            client ID.
     * 
     * @param clientSecret
     *            A <code>String</code> object instance that represents the
     *            client secret.
     * @throws URISyntaxException
     * 
     */
    public OAuthTokenManager(OAuthContract contract, DateFactory dateFactory,
            @Named(MediaConfiguration.OAUTH_URI) String oAuthUri,
            @Named(MediaConfiguration.OAUTH_CLIENT_ID) String clientId,
            @Named(MediaConfiguration.OAUTH_CLIENT_SECRET) String clientSecret,
            @Named(MediaConfiguration.OAUTH_SCOPE) String scope)
            throws URISyntaxException {
        this.contract = contract;
        this.dateFactory = dateFactory;
        this.acsBaseUri = new URI(oAuthUri);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.activeToken = null;
    }

    /**
     * Gets an OAuth access token with specified media service scope.
     * 
     * @param mediaServiceScope
     *            A <code>String</code> instance that represents the media
     *            service scope.
     * 
     * @return String
     * 
     * @throws ServiceException
     * @throws URISyntaxException
     */
    public String getAccessToken() throws ServiceException, URISyntaxException {
        Date now = dateFactory.getDate();
        if (this.activeToken == null
                || now.after(this.activeToken.getExpiresUtc())) {
            OAuthTokenResponse oAuth2TokenResponse = contract.getAccessToken(
                    acsBaseUri, clientId, clientSecret, scope);
            Date expiresUtc = new Date(now.getTime()
                    + oAuth2TokenResponse.getExpiresIn() * Timer.ONE_SECOND / 2);

            ActiveToken newToken = new ActiveToken();
            newToken.setAccessToken(oAuth2TokenResponse.getAccessToken());
            newToken.setExpiresUtc(expiresUtc);

            this.activeToken = newToken;
        }
        return this.activeToken.getAccessToken();
    }
}
