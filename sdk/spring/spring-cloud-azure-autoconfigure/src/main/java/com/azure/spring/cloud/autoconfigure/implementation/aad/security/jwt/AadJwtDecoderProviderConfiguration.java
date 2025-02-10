// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.aad.security.jwt;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

/**
 * Allows resolving configuration from an
 * <a href="https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfig">OpenID Provider
 * Configuration</a> or
 * <a href="https://tools.ietf.org/html/rfc8414#section-3.1">Authorization Server Metadata Request</a> based on
 * provided issuer and method invoked.
 */
final class AadJwtDecoderProviderConfiguration {

    private static final String OIDC_METADATA_PATH = "/.well-known/openid-configuration";
    private static final ParameterizedTypeReference<Map<String, Object>> TYPE_REFERENCE =
        new ParameterizedTypeReference<>() {};

    /**
     * Gets the configuration for OIDC issue location.
     *
     * @param oidcIssuerLocation the OIDC issuer location
     * @return the configuraton for OIDC issue location
     */
    static Map<String, Object> getConfigurationForOidcIssuerLocation(RestOperations restOperations, String oidcIssuerLocation) {
        URI issuer = URI.create(oidcIssuerLocation);
        String errorMessage = "Unable to resolve the Configuration with the provided Issuer of " + oidcIssuerLocation;
        URI uri = UriComponentsBuilder.fromUriString(oidcIssuerLocation)
                                      .replacePath(issuer.getPath() + OIDC_METADATA_PATH)
                                      .build(Collections.emptyMap());
        try {
            RequestEntity<Void> request = RequestEntity.get(uri).build();
            ResponseEntity<Map<String, Object>> response = restOperations.exchange(request,
                TYPE_REFERENCE);
            Map<String, Object> configuration = response.getBody();
            if (configuration == null) {
                throw new IllegalArgumentException("The configuration must not be null");
            }
            if (configuration.get("jwks_uri") == null) {
                throw new IllegalArgumentException("The public JWK set URI must not be null");
            }

            return configuration;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (RuntimeException e) {
            if (!(e instanceof HttpClientErrorException
                && ((HttpClientErrorException) e).getStatusCode().is4xxClientError())) {
                throw new IllegalArgumentException(errorMessage, e);
            }
        }
        throw new IllegalArgumentException(errorMessage);
    }


}

