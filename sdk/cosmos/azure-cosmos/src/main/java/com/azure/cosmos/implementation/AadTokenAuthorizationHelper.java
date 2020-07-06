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
    public static final String AAD_AUTH_TOKEN_GENERAL_SCOPE = "https://management.azure.com/.default";
    public static final String AAD_AUTH_TOKEN_COSMOS_GENERAL_SCOPE = "https://cosmos.azure.com/.default";
    private static final String AUTH_PREFIX = "type=aad&ver=1.0&sig=";
    private static final Logger logger = LoggerFactory.getLogger(AadTokenAuthorizationHelper.class);

    /**
     * This method will try to fetch the AAD token to access the resource and add it to the rquest headers.
     *
     * @param request the request headers.
     * @param simpleTokenCache token cache that supports caching a token and refreshing it.
     * @return the request headers with authorization header updated.
     */
    public static Mono<RxDocumentServiceRequest> populateAuthorizationHeader(RxDocumentServiceRequest request, SimpleTokenCache simpleTokenCache) {
        if (request == null) {
            throw new IllegalArgumentException("request");
        }
        if (simpleTokenCache == null) {
            throw new IllegalArgumentException("simpleTokenCache");
        }

        return simpleTokenCache.getToken()
            .map(accessToken -> {
                StringBuilder authorizationBuilder = new StringBuilder()
                    .append(AAD_AUTH_SCHEMA_TYPE_SEGMENT).append("=").append(AAD_AUTH_SCHEMA_TYPE_VALUE)
                    .append(AAD_AUTH_VERSION_SEGMENT).append("=").append(AAD_AUTH_VERSION_VALUE)
                    .append(AAD_AUTH_SIGNATURE_SEGMENT).append("=").append(accessToken.getToken());
                try {
                    String authorization = URLEncoder.encode(authorizationBuilder.toString(), "UTF-8");
                    request.getHeaders().put(HttpConstants.HttpHeaders.AUTHORIZATION, authorization);
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalStateException("Failed to encode authorization token.", e);
                }

                return request;
            });
    }

}
