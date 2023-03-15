// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
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
    private static final ClientLogger LOGGER = new ClientLogger(MSIToken.class);
    private static final OffsetDateTime EPOCH = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss XXX")
        .withLocale(Locale.US);

    // This is the format for app service on Windows as of API version 2017-09-01.
    // The format is changed to Unix timestamp in 2019-08-01 but this API version
    // has not been deployed to Linux app services.
    private static final DateTimeFormatter DTF_WINDOWS = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a XXX")
        .withLocale(Locale.US);

    @JsonProperty(value = "token_type")
    private String tokenType;

    @JsonProperty(value = "access_token")
    private String accessToken;

    @JsonProperty(value = "expires_on")
    private String expiresOn;

    @JsonProperty(value = "expires_in")
    private String expiresIn;

    @JsonProperty(value = "refresh_in")
    private String refreshIn;

    /**
     * Creates an access token instance.
     *
     * @param token     the token string.
     * @param expiresOn the expiration time.
     * @param expiresIn the number of seconds until expiration.
     * @param refreshIn seconds until refresh.
     */
    @JsonCreator
    public MSIToken(
        @JsonProperty(value = "access_token") String token,
        @JsonProperty(value = "expires_on") String expiresOn,
        @JsonProperty(value = "expires_in") String expiresIn,
        @JsonProperty(value = "refresh_in") String refreshIn) {
        super(token, EPOCH.plusSeconds(parseToEpochSeconds(expiresOn, expiresIn)));
        this.accessToken = token;
        this.expiresOn = expiresOn;
        this.expiresIn = expiresIn;
        this.refreshIn = refreshIn;
    }

    @Override
    public String getToken() {
        return accessToken;
    }

    private static Long parseToEpochSeconds(String expiresOn, String expiresIn) {

        // expiresIn = number of seconds until refresh
        // expiresOn = timestamp of refresh expressed as seconds since epoch.

        // if we have an expiresOn, we'll use it. Otherwise, we use expiresIn.
        String dateToParse = CoreUtils.isNullOrEmpty(expiresOn) ? expiresIn : expiresOn;

        try {
            Long seconds = Long.parseLong(dateToParse);
            // we have an expiresOn, so no parsing required.
            if (!CoreUtils.isNullOrEmpty(expiresOn)) {
                return seconds;
            } else {
                // otherwise we need the OffsetDateTime representing now plus the expiresIn duration.
                return OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(seconds).toEpochSecond();
            }
        } catch (NumberFormatException e) {
            LOGGER.verbose(e.getMessage());
        }

        try {
            return Instant.from(DTF.parse(dateToParse)).getEpochSecond();
        } catch (DateTimeParseException e) {
            LOGGER.verbose(e.getMessage());
        }

        try {
            return Instant.from(DTF_WINDOWS.parse(dateToParse)).getEpochSecond();
        } catch (DateTimeParseException e) {
            LOGGER.verbose(e.getMessage());
        }
        throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unable to parse date time " + dateToParse));
    }

    /**
     * Returns the number of seconds from creation to when refresh should occur.
     * If the service specified refresh_in, it is that value. Otherwise, it is half the value to expiry, unless that is
     * less than two hours, at which point it is the same as time to expiry.
     * @return The number of seconds until refresh.
     */
    public long getRefreshInSeconds() {
        if (refreshIn != null) {
            return Long.parseLong(refreshIn);
        }

        Duration duration = Duration.between(OffsetDateTime.now(), EPOCH.plusSeconds(parseToEpochSeconds(expiresOn, expiresIn)));
        if (duration.toHours() >= 2) {
            return duration.getSeconds() / 2;
        } else {
            return duration.getSeconds();
        }
    }
}
