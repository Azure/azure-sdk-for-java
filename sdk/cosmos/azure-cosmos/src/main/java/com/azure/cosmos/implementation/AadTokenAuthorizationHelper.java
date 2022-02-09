// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.core.credential.SimpleTokenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This class is used internally and act as a helper in authorization of
 * AAD tokens and its supporting method.
 *
 */
public class AadTokenAuthorizationHelper {
    public static final String AAD_AUTH_SCHEMA_TYPE_SEGMENT = "type";
    public static final String AAD_AUTH_VERSION_SEGMENT = "ver";
    public static final String AAD_AUTH_SIGNATURE_SEGMENT = "sig";
    public static final String AAD_AUTH_SCHEMA_TYPE_VALUE = "aad";
    public static final String AAD_AUTH_VERSION_VALUE = "1.0";
    public static final String AAD_AUTH_TOKEN_COSMOS_SCOPE = "https://cosmos.azure.com/.default";
    private static final String AUTH_PREFIX =
            AAD_AUTH_SCHEMA_TYPE_SEGMENT + "=" + AAD_AUTH_SCHEMA_TYPE_VALUE
            + "&"
            + AAD_AUTH_VERSION_SEGMENT + "=" + AAD_AUTH_VERSION_VALUE
            + "&"
            + AAD_AUTH_SIGNATURE_SEGMENT + "=";
    private static final Logger logger = LoggerFactory.getLogger(AadTokenAuthorizationHelper.class);

    /**
     * This method will try to fetch the AAD token to access the resource and add it to the request headers. 
     *
     * @param request the request headers.
     * @param simpleTokenCache token cache that supports caching a token and refreshing it.
     * @return the request headers with authorization header updated.
     */
    public static Mono<RxDocumentServiceRequest> populateAuthorizationHeader(RxDocumentServiceRequest request, SimpleTokenCache simpleTokenCache) {
        if (request == null || request.getHeaders() == null) {
            throw new IllegalArgumentException("request");
        }
        if (simpleTokenCache == null) {
            throw new IllegalArgumentException("simpleTokenCache");
        }

        return getAuthorizationToken(simpleTokenCache)
            .map(authorization -> {
                request.getHeaders().put(HttpConstants.HttpHeaders.AUTHORIZATION, authorization);
                return request;
            });
    }

    public static Mono<String> getAuthorizationToken(SimpleTokenCache simpleTokenCache) {
        return simpleTokenCache.getToken()
            .map(accessToken -> {
                String authorization;
                String authorizationPayload = AUTH_PREFIX + accessToken.getToken();

                try {
                    authorization = URLEncoder.encode(authorizationPayload, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException("Failed to encode authorization token.", e);
                }

                return authorization;
            });
    }
}
