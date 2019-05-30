// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Type representing response from the local MSI token provider.
 */
public class MSIToken {

    private static OffsetDateTime epoch = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @JsonProperty(value = "token_type")
    private String tokenType;

    @JsonProperty(value = "access_token")
    private String accessToken;

    @JsonProperty(value = "expires_on")
    private String expiresOn;

    public String accessToken() {
        return accessToken;
    }

    public String tokenType() {
        return tokenType;
    }

    public boolean isExpired() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expireOn = epoch.plusSeconds(Integer.parseInt(this.expiresOn));
        return now.plusMinutes(5).isAfter(expireOn);
    }
}
