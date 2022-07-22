// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.implementation.jwt;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import reactor.core.publisher.Mono;

/**
 * Extracts the {@link GrantedAuthority}s from scope attributes typically found in a {@link Jwt}.
 */
public class AadReactiveJwtGrantedAuthoritiesConverter implements Converter<Jwt, Mono<? extends AbstractAuthenticationToken>> {

    private final JwtAuthenticationConverter converter;

    public AadReactiveJwtGrantedAuthoritiesConverter(JwtAuthenticationConverter converter) {
        this.converter = converter;
    }

    @Override
    public Mono<? extends AbstractAuthenticationToken> convert(Jwt source) {
        return Mono.justOrEmpty((this.converter == null) ? null : this.converter.convert(source));
    }
}
