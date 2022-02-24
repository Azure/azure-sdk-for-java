// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c;

import com.azure.spring.cloud.autoconfigure.aad.implementation.webapi.AadJwtBearerTokenAuthenticationConverter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import java.util.Map;

/**
 * A {@link Converter} that takes a {@link Jwt} and converts it into a {@link BearerTokenAuthentication}.
 *
 * @deprecated Use {@link AadJwtBearerTokenAuthenticationConverter} instead.
 */
@Deprecated
public class AadB2cJwtBearerTokenAuthenticationConverter extends AadJwtBearerTokenAuthenticationConverter {

    /**
     * Creates a new instance of {@link AadB2cJwtBearerTokenAuthenticationConverter}.
     */
    public AadB2cJwtBearerTokenAuthenticationConverter() {
        super();
    }

    /**
     * Creates a new instance of {@link AadB2cJwtBearerTokenAuthenticationConverter}.
     *
     * @param authoritiesClaimName the authorities claim name
     */
    public AadB2cJwtBearerTokenAuthenticationConverter(String authoritiesClaimName) {
        super(authoritiesClaimName);
    }

    /**
     * Creates a new instance of {@link AadB2cJwtBearerTokenAuthenticationConverter}.
     *
     * @param authoritiesClaimName the authorities claim name
     * @param authorityPrefix the authority prefix
     */
    public AadB2cJwtBearerTokenAuthenticationConverter(String authoritiesClaimName,
                                                       String authorityPrefix) {
        super(authoritiesClaimName, authorityPrefix);
    }

    /**
     * Creates a new instance of {@link AadB2cJwtBearerTokenAuthenticationConverter}.
     *
     * @param principalClaimName the principal claim name
     * @param claimToAuthorityPrefixMap the claim to authority prefix map
     */
    public AadB2cJwtBearerTokenAuthenticationConverter(String principalClaimName,
                                                       Map<String, String> claimToAuthorityPrefixMap) {
        super(principalClaimName, claimToAuthorityPrefixMap);
    }
}
