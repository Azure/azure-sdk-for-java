// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Type representing response from the local MSI token provider.
 */
public final class MSIToken extends AccessToken {
    private static final OffsetDateTime EPOCH = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    @JsonProperty(value = "token_type")
    private String tokenType;

    @JsonProperty(value = "access_token")
    private String accessToken;

    @JsonProperty(value = "expires_on")
    private String expiresOn;

    /**
     * Creates an access token instance.
     *
     * @param token the token string.
     * @param expiresOn the expiration time.
     */
    public MSIToken(String token, OffsetDateTime expiresOn) {
        super(token, expiresOn);
    }

    @JsonCreator
    private MSIToken(
        @JsonProperty(value = "access_token") String token,
        @JsonProperty(value = "expires_on") String expiresOn) {
        this(token, EPOCH.plusSeconds(parseDateToEpochSeconds(expiresOn)));
        this.accessToken = token;
        this.expiresOn =  expiresOn;
    }

    @Override
    public String getToken() {
        return accessToken;
    }

    @Override
    public OffsetDateTime getExpiresAt() {
        return EPOCH.plusSeconds(parseDateToEpochSeconds(this.expiresOn));
    }

    @Override
    public boolean isExpired() {
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime expireOn = EPOCH.plusSeconds(parseDateToEpochSeconds(this.expiresOn));
        return now.plusMinutes(5).isAfter(expireOn);
    }

    private static Long parseDateToEpochSeconds(String dateTime) {
        ClientLogger logger = new ClientLogger(MSIToken.class);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss XXX");
        try {
            return Long.parseLong(dateTime);
        } catch (NumberFormatException e) {
            logger.error(e.getMessage());
        }

        try {
            return Instant.from(dtf.parse(dateTime)).toEpochMilli() / 1000L;
        } catch (DateTimeParseException e) {
            logger.error(e.getMessage());
        }

        throw logger.logExceptionAsError(new IllegalArgumentException("Unable to parse date time " + dateTime));
    }

}
