/**
 * Copyright 2011 Microsoft Corporation
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

package com.microsoft.windowsazure.services.media;

import java.util.Date;

/**
 * A class representing active token for OAuthTokenResponse.
 * 
 * @author azurejava@microsoft.com
 * 
 */
public class ActiveToken {

    private Date expiresUtc;
    private OAuthTokenResponse oAuthTokenResponse;

    /**
     * Gets the expiration time in UTC.
     * 
     * @return The token expiration time in UTC.
     */
    public Date getExpiresUtc() {
        return expiresUtc;
    }

    /**
     * Sets the token expiration time in UTC.
     * 
     * @param expiresUtc
     */
    public void setExpiresUtc(Date expiresUtc) {
        this.expiresUtc = expiresUtc;
    }

    /**
     * Gets the OAuth token response.
     * 
     * @return The OAuth token response.
     */
    public OAuthTokenResponse getOAuthTokenResponse() {
        return oAuthTokenResponse;
    }

    /**
     * Sets the OAuth token response.
     * 
     * @param oAuth2TokenResponse
     */
    public void setOAuth2TokenResponse(OAuthTokenResponse oAuth2TokenResponse) {
        this.oAuthTokenResponse = oAuth2TokenResponse;
    }

}
