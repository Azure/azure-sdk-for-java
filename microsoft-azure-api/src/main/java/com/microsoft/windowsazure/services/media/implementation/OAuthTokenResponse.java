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

package com.microsoft.windowsazure.services.media.implementation;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * A class representing OAuth token response.
 * 
 * @author azurejava@microsoft.com
 * 
 */
public class OAuthTokenResponse {

    private String _accessToken;
    private String _scope;
    private String _tokenType;
    private long _expiresIn;

    /**
     * Sets the token type.
     * 
     * @param tokenType
     */
    @JsonProperty("token_type")
    public void setTokenType(String tokenType) {
        _tokenType = tokenType;
    }

    @JsonProperty("token_type")
    public String getTokenType() {
        return _tokenType;
    }

    @JsonProperty("expires_in")
    public long getExpiresIn() {
        return _expiresIn;
    }

    @JsonProperty("expires_in")
    public void setExpiresIn(long expiresIn) {
        _expiresIn = expiresIn;
    }

    @JsonProperty("access_token")
    public String getAccessToken() {
        return _accessToken;
    }

    @JsonProperty("access_token")
    public void setAccessToken(String accessToken) {
        _accessToken = accessToken;
    }

    @JsonProperty("scope")
    public String getScope() {
        return _scope;
    }

    @JsonProperty("scope")
    public void setScope(String scope) {
        _scope = scope;
    }
}
