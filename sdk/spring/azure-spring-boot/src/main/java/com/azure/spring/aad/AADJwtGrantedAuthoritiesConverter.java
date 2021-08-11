// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad;

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

import static com.azure.spring.aad.webapi.AADResourceServerProperties.DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP;

/**
 * Extracts the {@link GrantedAuthority}s from scope attributes typically found in a {@link Jwt}.
 */
public class AADJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADJwtGrantedAuthoritiesConverter.class);

    private final Map<String, String> claimToAuthorityPrefixMap;

    public AADJwtGrantedAuthoritiesConverter() {
        claimToAuthorityPrefixMap = DEFAULT_CLAIM_TO_AUTHORITY_PREFIX_MAP;
    }

    public AADJwtGrantedAuthoritiesConverter(Map<String, String> claimToAuthorityPrefixMap) {
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
