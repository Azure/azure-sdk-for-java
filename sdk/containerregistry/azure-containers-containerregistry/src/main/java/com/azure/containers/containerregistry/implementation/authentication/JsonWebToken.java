// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.implementation.authentication;

import com.azure.core.util.CoreUtils;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

class JsonWebToken {
    /**
     * Retrieves the expiration date from the specified JWT value.
     *
     * @param jwtValue The JWT value.
     * @return The date the JWT expires or null if the expiration couldn't be retrieved.
     * @throws IllegalArgumentException If the {@code jwtValue} is null or empty.
     */
    public static OffsetDateTime retrieveExpiration(String jwtValue) {
        if (CoreUtils.isNullOrEmpty(jwtValue)) {
            throw new IllegalArgumentException("Value cannot be null or empty: 'jwtValue'.");
        }

        // Need at least two '.'s
        int firstIndex = jwtValue.indexOf('.');
        if (firstIndex == -1) {
            return null;
        }

        int secondIndex = jwtValue.indexOf('.', firstIndex + 1);
        if (secondIndex == -1) {
            return null;
        }

        String jwtPayloadEncoded = jwtValue.substring(firstIndex + 1, secondIndex);

        if (CoreUtils.isNullOrEmpty(jwtPayloadEncoded)) {
            return null;
        }

        try (JsonReader jsonReader = JsonProviders.createReader(Base64.getDecoder().decode(jwtPayloadEncoded))) {
            Long expirationValue = jsonReader.readObject(reader -> {
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("exp".equals(fieldName)) {
                        return reader.getLong();
                    }
                }

                return null;
            });

            return (expirationValue == null)
                ? null
                : OffsetDateTime.ofInstant(Instant.ofEpochSecond(expirationValue), ZoneOffset.UTC);
        } catch (IOException exception) {
            return null;
        }
    }
}
