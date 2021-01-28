// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;

/**
 * Provides factory methods for creating {@code OAuth2TokenValidator<Jwt>}.
 */
public class AADJwtValidators {

    public static OAuth2TokenValidator<Jwt> createDefaultWithIssuer(Set<String> trustedIssuers) {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator());
        validators.add(new AADJwtIssuerValidator(trustedIssuers));
        return new DelegatingOAuth2TokenValidator<>(validators);
    }

    public static OAuth2TokenValidator<Jwt> createDefault() {
        return new DelegatingOAuth2TokenValidator<>(Arrays.asList(new JwtTimestampValidator()));
    }

}
