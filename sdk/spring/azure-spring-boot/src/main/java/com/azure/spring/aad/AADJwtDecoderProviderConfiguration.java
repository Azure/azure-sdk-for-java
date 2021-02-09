// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
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
public class AADJwtDecoderProviderConfiguration {

    private static final String OIDC_METADATA_PATH = "/.well-known/openid-configuration";
    private static final RestTemplate REST = new RestTemplate();
    private static final ParameterizedTypeReference<Map<String, Object>> TYPE_REFERENCE =
        new ParameterizedTypeReference<Map<String, Object>>() {
        };

    public static Map<String, Object> getConfigurationForOidcIssuerLocation(String oidcIssuerLocation) {
        return getConfiguration(oidcIssuerLocation, oidc(URI.create(oidcIssuerLocation)));
    }

    private static Map<String, Object> getConfiguration(String issuer, URI... uris) {
        String errorMessage = "Unable to resolve the Configuration with the provided Issuer of " + issuer;

        for (URI uri : uris) {
            try {
                RequestEntity<Void> request = RequestEntity.get(uri).build();
                ResponseEntity<Map<String, Object>> response = REST.exchange(request,
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
                // else try another endpoint
            }
        }
        throw new IllegalArgumentException(errorMessage);
    }

    private static URI oidc(URI issuer) {
        return UriComponentsBuilder.fromUri(issuer)
                                   .replacePath(issuer.getPath() + OIDC_METADATA_PATH)
                                   .build(Collections.emptyMap());
    }

}

