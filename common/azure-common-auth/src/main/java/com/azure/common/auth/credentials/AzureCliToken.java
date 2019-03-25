/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.auth.credentials;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.azure.common.annotations.Beta;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * An instance of this class represents an entry in accessTokens.json.
 */
@Beta
final class AzureCliToken implements Cloneable {
    @JsonProperty("_authority")
    private String authority;
    @JsonProperty("_clientId")
    private String clientId;
    private String tokenType;
    private long expiresIn;
    private String expiresOn;
    private LocalDateTime expiresOnDate;
    private String oid;
    private String userId;
    private String servicePrincipalId;
    private String servicePrincipalTenant;
    private boolean isMRRT;
    private String resource;
    private String accessToken;
    private String refreshToken;
    private String identityProvider;

    boolean isServicePrincipal() {
        return servicePrincipalId != null;
    }

    String tenant() {
        if (isServicePrincipal()) {
            return servicePrincipalTenant;
        } else {
            String[] parts = authority.split("/");
            return parts[parts.length - 1];
        }
    }

    String clientId() {
        if (isServicePrincipal()) {
            return servicePrincipalId;
        } else {
            return clientId;
        }
    }

    String authority() {
        return authority;
    }

    boolean expired() {
        return expiresOn != null && expiresOn().isBefore(LocalDateTime.now());
    }

    String accessToken() {
        return accessToken;
    }

    LocalDateTime expiresOn() {
        if (expiresOnDate == null) {
            try {
                expiresOnDate = LocalDateTime.parse(expiresOn, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
            } catch (IllegalArgumentException e) {
                expiresOnDate = LocalDateTime.parse(expiresOn);
            }
        }
        return expiresOnDate;
    }

    AzureCliToken withAuthenticationResult(AuthenticationResult result) {
        this.accessToken = result.getAccessToken();
        this.refreshToken = result.getRefreshToken();
        this.expiresIn = result.getExpiresAfter();
        this.expiresOnDate = LocalDateTime.ofInstant(result.getExpiresOnDate().toInstant(), ZoneId.systemDefault());
        return this;
    }

    AzureCliToken withAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    String refreshToken() {
        return refreshToken;
    }

    AzureCliToken withRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    String user() {
        if (isServicePrincipal()) {
            return servicePrincipalId;
        } else {
            return userId;
        }
    }

    boolean isMRRT() {
        return isMRRT;
    }

    String resource() {
        return resource;
    }

    AzureCliToken withResource(String resource) {
        this.resource = resource;
        return this;
    }

    public AzureCliToken clone() throws CloneNotSupportedException {
        AzureCliToken token = (AzureCliToken) super.clone();
        token.expiresOnDate = LocalDateTime.from(expiresOn());
        return token;
    }
}
