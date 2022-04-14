// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.implementation.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.azure.spring.cloud.autoconfigure.aad.properties.AadResourceServerProperties.DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP;

/**
 * Extracts the {@link GrantedAuthority}s from scope attributes typically found in a {@link Jwt}.
 */
public class AadJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AadJwtGrantedAuthoritiesConverter.class);

    private final Map<String, String> claimToAuthorityPrefixMap;

    /**
     * Creates a new instance of {@link AadJwtGrantedAuthoritiesConverter}.
     */
    public AadJwtGrantedAuthoritiesConverter() {
        claimToAuthorityPrefixMap = DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP;
    }

    /**
     * Creates a new instance of {@link AadJwtGrantedAuthoritiesConverter}.
     *
     * @param claimToAuthorityPrefixMap the claim to authority prefix map
     */
    public AadJwtGrantedAuthoritiesConverter(Map<String, String> claimToAuthorityPrefixMap) {
        this.claimToAuthorityPrefixMap = claimToAuthorityPrefixMap;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        claimToAuthorityPrefixMap.forEach((authoritiesClaimName, authorityPrefix) ->
            Optional.of(authoritiesClaimName)
                    .map(jwt::getClaim)
                    .map(this::getClaimValueAsCollection)
                    .map(Collection::stream)
                    .orElseGet(Stream::empty)
                    .map(authority -> authorityPrefix + authority)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(grantedAuthorities::add));
        LOGGER.debug("User {}'s authorities created from jwt token: {}.", jwt.getSubject(), grantedAuthorities);
        return grantedAuthorities;
    }

    private Collection<?> getClaimValueAsCollection(Object claimValue) {
        if (claimValue instanceof String) {
            return Arrays.asList(((String) claimValue).split(" "));
        } else if (claimValue instanceof Collection) {
            return (Collection<?>) claimValue;
        } else {
            return Collections.emptyList();
        }
    }
}
