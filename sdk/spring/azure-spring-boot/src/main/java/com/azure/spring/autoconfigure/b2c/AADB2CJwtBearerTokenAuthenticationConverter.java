// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

import com.azure.spring.aad.webapi.AADJwtBearerTokenAuthenticationConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import java.util.Map;

/**
 * A {@link Converter} that takes a {@link Jwt} and converts it into a {@link BearerTokenAuthentication}.
 *
 * @deprecated Use {@link AADJwtBearerTokenAuthenticationConverter} instead.
 */
@Deprecated
public class AADB2CJwtBearerTokenAuthenticationConverter extends AADJwtBearerTokenAuthenticationConverter {

    /**
     * Creates a new instance of {@link AADB2CJwtBearerTokenAuthenticationConverter}.
     */
    public AADB2CJwtBearerTokenAuthenticationConverter() {
        super();
    }

    /**
     * Creates a new instance of {@link AADB2CJwtBearerTokenAuthenticationConverter}.
     *
     * @param authoritiesClaimName the authorities claim name
     */
    public AADB2CJwtBearerTokenAuthenticationConverter(String authoritiesClaimName) {
        super(authoritiesClaimName);
    }

    /**
     * Creates a new instance of {@link AADB2CJwtBearerTokenAuthenticationConverter}.
     *
     * @param authoritiesClaimName the authorities claim name
     * @param authorityPrefix the authority prefix
     */
    public AADB2CJwtBearerTokenAuthenticationConverter(String authoritiesClaimName,
                                                       String authorityPrefix) {
        super(authoritiesClaimName, authorityPrefix);
    }

    /**
     * Creates a new instance of {@link AADB2CJwtBearerTokenAuthenticationConverter}.
     *
     * @param principalClaimName the principal claim name
     * @param claimToAuthorityPrefixMap the claim to authority prefix map
     */
    public AADB2CJwtBearerTokenAuthenticationConverter(String principalClaimName,
                                                       Map<String, String> claimToAuthorityPrefixMap) {
        super(principalClaimName, claimToAuthorityPrefixMap);
    }
}
