// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs;

import java.text.ParseException;
import java.util.Date;

import com.microsoft.azure.eventhubs.impl.ClientConstants;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

/**
 * Extend SecurityToken with some specifics for a JSon Web Token  
 *
 */
public class JsonSecurityToken extends SecurityToken {
    /**
     * Construct from a raw JWT.
     * @param rawToken   the JWT token data
     * @param audience   audience of the token
     * @throws ParseException if the token cannot be parsed
     */
    public JsonSecurityToken(final String rawToken, final String audience) throws ParseException {
        super(rawToken, getExpirationDateTimeUtcFromToken(rawToken), audience, ClientConstants.JWT_TOKEN_TYPE);
    }
    
    static Date getExpirationDateTimeUtcFromToken(final String token) throws ParseException {
        JWT jwt = JWTParser.parse(token);
        JWTClaimsSet claims = jwt.getJWTClaimsSet();
        return claims.getExpirationTime();
    }
}
