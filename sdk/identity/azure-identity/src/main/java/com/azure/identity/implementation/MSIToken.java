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
import java.util.Locale;

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
    @JsonCreator
    public MSIToken(
        @JsonProperty(value = "access_token") String token,
        @JsonProperty(value = "expires_on") String expiresOn) {
        super(token, EPOCH.plusSeconds(parseDateToEpochSeconds(expiresOn)));
        this.accessToken = token;
        this.expiresOn =  expiresOn;
    }

    @Override
    public String getToken() {
        return accessToken;
    }

    private static Long parseDateToEpochSeconds(String dateTime) {
        ClientLogger logger = new ClientLogger(MSIToken.class);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss XXX").withLocale(Locale.US);
        // This is the format for app service on Windows as of API version 2017-09-01.
        // The format is changed to Unix timestamp in 2019-08-01 but this API version
        // has not been deployed to Linux app services.
        DateTimeFormatter dtfWindows = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a XXX").withLocale(Locale.US);
        try {
            return Long.parseLong(dateTime);
        } catch (NumberFormatException e) {
            logger.verbose(e.getMessage());
        }

        try {
            return Instant.from(dtf.parse(dateTime)).getEpochSecond();
        } catch (DateTimeParseException e) {
            logger.verbose(e.getMessage());
        }

        try {
            return Instant.from(dtfWindows.parse(dateTime)).getEpochSecond();
        } catch (DateTimeParseException e) {
            logger.verbose(e.getMessage());
        }

        throw logger.logExceptionAsError(new IllegalArgumentException("Unable to parse date time " + dateTime));
    }

}
