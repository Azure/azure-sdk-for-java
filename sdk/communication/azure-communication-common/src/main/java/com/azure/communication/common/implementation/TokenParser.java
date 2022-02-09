// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.common.implementation;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.azure.core.credential.AccessToken;

/**
 * JWT token parser to convert raw token to Token structure
 */
public class TokenParser {
    private static final String EXP_CLAIM_REGEX = "\"exp\":*([0-9]+)";

    /**
     * Parse token raw string provided by Communication Service authentication
     * @param rawToken serialized Communication Service authentication token
     * @return deserialized AccessToken
     */
    public AccessToken parseJWTToken(String rawToken) {
        Objects.requireNonNull(rawToken, "'rawToken' cannot be null.");
        String jwt = new String(Base64.getDecoder().decode(rawToken.split("\\.")[1]), Charset.forName("UTF-8"));
        Pattern pattern = Pattern.compile(EXP_CLAIM_REGEX);
        Matcher matcher = pattern.matcher(jwt);
        matcher.find();
        long expire = Long.parseLong(matcher.group(1));

        LocalDateTime expiryLocal = LocalDateTime.ofInstant(Instant.ofEpochMilli(expire * 1000), ZoneId.of("UTC"));
        OffsetDateTime offsetExpiry = expiryLocal.atOffset(OffsetDateTime.now().getOffset());
        return new AccessToken(rawToken, offsetExpiry);
    }
}
