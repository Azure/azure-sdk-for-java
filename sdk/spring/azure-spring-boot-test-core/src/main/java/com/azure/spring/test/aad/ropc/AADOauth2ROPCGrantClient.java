// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.test.aad.ropc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;

public class AADOauth2ROPCGrantClient {

    private static final RestTemplate REST_TEMPLATE = new RestTemplate();
    private static final HttpHeaders HEADERS = new HttpHeaders();

    static {
        HEADERS.setContentType(APPLICATION_FORM_URLENCODED);
    }

    //  Refs: https://docs.microsoft.com/en-us/azure/active-directory/develop/v2-oauth-ropc
    public static OAuth2ROPCResponse getOAuth2ROPCResponseByROPCGrant(String tenantId,
                                                                      String clientId,
                                                                      String clientSecret,
                                                                      String username,
                                                                      String password,
                                                                      String scope) {
        Assert.hasText(tenantId, "tenantId can not be empty.");
        Assert.hasText(clientId, "clientId can not be empty.");
        Assert.hasText(clientSecret, "clientSecret can not be empty.");
        Assert.hasText(username, "username can not be empty.");
        Assert.hasText(password, "password can not be empty.");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("username", username);
        body.add("password", password);
        body.add("scope", scope);

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, HEADERS);
        String url = String.format("https://login.microsoftonline.com/%s/oauth2/v2.0/token", tenantId);
        return REST_TEMPLATE.postForObject(url, httpEntity, OAuth2ROPCResponse.class);
    }

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static final class OAuth2ROPCResponse {

        private String tokenType;
        private String scope;
        private long expiresIn;
        private long extExpiresIn;
        private String accessToken;
        private String refreshToken;
        private String idToken;

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
        }

        public long getExtExpiresIn() {
            return extExpiresIn;
        }

        public void setExtExpiresIn(long extExpiresIn) {
            this.extExpiresIn = extExpiresIn;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public void setRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
        }

        public String getIdToken() {
            return idToken;
        }

        public void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }

}
