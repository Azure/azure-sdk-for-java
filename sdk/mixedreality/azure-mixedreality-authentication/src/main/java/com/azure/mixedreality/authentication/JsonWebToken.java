// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

import com.azure.core.util.CoreUtils;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Map;

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

        String[] jwtParts = jwtValue.split("[.]");

        // Would normally be 3, but 2 is the minimum here since Java's split ignores trailing empty strings.
        if (jwtParts.length < 2) {
            return null;
        }

        String jwtPayloadEncoded = jwtParts[1];

        if (CoreUtils.isNullOrEmpty(jwtPayloadEncoded)) {
            return null;
        }

        byte[] jwtPayloadDecodedData = Base64.getDecoder().decode(jwtPayloadEncoded);

        try (JsonReader jsonReader = JsonProviders.createReader(jwtPayloadDecodedData)) {
            Map<String, Object> jsonTree = jsonReader.readMap(JsonReader::readUntyped);
            Object exp = jsonTree.get("exp");

            if (exp == null) {
                return null;
            } else {
                return OffsetDateTime.ofInstant(Instant.ofEpochSecond((long) exp), ZoneOffset.UTC);
            }
        } catch (IOException exception) {
            return null;
        }
    }
}
