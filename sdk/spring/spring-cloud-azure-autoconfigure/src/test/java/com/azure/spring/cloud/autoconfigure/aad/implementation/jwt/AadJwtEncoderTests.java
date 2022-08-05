// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.jwt;

import com.azure.spring.cloud.autoconfigure.aad.implementation.TestJwtClaimsSets;
import com.nimbusds.jose.KeySourceException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class AadJwtEncoderTests {

    private List<JWK> jwkList;

    private JWKSource<SecurityContext> jwkSource;

    private AadJwtEncoder jwtEncoder;

    @BeforeEach
    public void setUp() {
        this.jwkList = new ArrayList<>();
        this.jwkSource = (jwkSelector, securityContext) -> jwkSelector.select(new JWKSet(this.jwkList));
        this.jwtEncoder = new AadJwtEncoder(this.jwkSource);
    }

    @Test
    public void constructorWhenJwkSourceNullThenThrowIllegalArgumentException() {
        assertThatIllegalArgumentException().isThrownBy(() -> new AadJwtEncoder(null))
                                            .withMessage("jwkSource cannot be null");
    }

    @Test
    public void encodeWhenHeadersNullThenThrowIllegalArgumentException() {
        Map<String, Object> jwtClaimsSet = TestJwtClaimsSets.jwtClaimsSet();
        assertThatIllegalArgumentException().isThrownBy(() -> this.jwtEncoder.encode(null, jwtClaimsSet))
                                            .withMessage("jwsHeader cannot be null");
    }

    @Test
    public void encodeWhenClaimsNullThenThrowIllegalArgumentException() {
        Map<String, Object> jwsHeader = new HashMap<>();
        jwsHeader.put("alg", SignatureAlgorithm.RS256.getName());
        assertThatIllegalArgumentException().isThrownBy(() -> this.jwtEncoder.encode(jwsHeader, null))
                                            .withMessage("jwtClaimsSet cannot be null");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void encodeWhenJwkSelectFailedThenThrowJwtEncodingException() throws Exception {
        this.jwkSource = mock(JWKSource.class);
        this.jwtEncoder = new AadJwtEncoder(this.jwkSource);
        given(this.jwkSource.get(any(), any())).willThrow(new KeySourceException("key source error"));

        Map<String, Object> jwsHeader = new HashMap<>();
        jwsHeader.put("alg", SignatureAlgorithm.RS256.getName());
        Map<String, Object> jwtClaimsSet = TestJwtClaimsSets.jwtClaimsSet();

        assertThatExceptionOfType(JwtException.class)
            .isThrownBy(() -> this.jwtEncoder.encode(jwsHeader, jwtClaimsSet))
            .withMessageContaining("Failed to select a JWK signing key -> key source error");
    }
}
