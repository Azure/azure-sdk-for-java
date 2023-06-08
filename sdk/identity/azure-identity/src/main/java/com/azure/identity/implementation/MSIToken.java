// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.AccessToken;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Type representing response from the local MSI token provider.
 */
public final class MSIToken extends AccessToken implements JsonSerializable<MSIToken> {
    private static final ClientLogger LOGGER = new ClientLogger(MSIToken.class);
    private static final OffsetDateTime EPOCH = OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("M/d/yyyy H:mm:ss XXX")
        .withLocale(Locale.US);

    // This is the format for app service on Windows as of API version 2017-09-01.
    // The format is changed to Unix timestamp in 2019-08-01 but this API version
    // has not been deployed to Linux app services.
    private static final DateTimeFormatter DTF_WINDOWS = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a XXX")
        .withLocale(Locale.US);

    private String tokenType;
    private final String accessToken;
    private final String expiresOn;
    private final String expiresIn;


    /**
     * Creates an access token instance.
     *
     * @param token the token string.
     * @param expiresOn the expiration time.
     * @param expiresIn the number of seconds until expiration.
     */
    public MSIToken(String token, String expiresOn, String expiresIn) {
        super(token, EPOCH.plusSeconds(parseToEpochSeconds(expiresOn, expiresIn)));
        this.accessToken = token;
        this.expiresOn = expiresOn;
        this.expiresIn = expiresIn;
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
            long seconds = Long.parseLong(dateToParse);
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

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("token_type", tokenType)
            .writeStringField("access_token", accessToken)
            .writeStringField("expires_on", expiresOn)
            .writeStringField("expires_in", expiresIn)
            .writeEndObject();
    }

    /**
     * Reads an instance of MSIToken from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of MSIToken if the JsonReader was pointing to an instance of it, or null if it
     *     was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the MSIToken.
     */
    public static MSIToken fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String tokenType = null;
            String accessToken = null;
            String expiresOn = null;
            String expiresIn = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("token_type".equals(fieldName)) {
                    tokenType = reader.getString();
                } else if ("access_token".equals(fieldName)) {
                    accessToken = reader.getString();
                } else if ("expires_on".equals(fieldName)) {
                    expiresOn = reader.getString();
                } else if ("expires_in".equals(fieldName)) {
                    expiresIn = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }

            MSIToken msiToken = new MSIToken(accessToken, expiresOn, expiresIn);
            msiToken.tokenType = tokenType;

            return msiToken;
        });
    }
}
