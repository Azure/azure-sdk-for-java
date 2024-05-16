// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface IAuthorizationTokenProvider {
    String getUserAuthorizationToken(String resourceAddress,
                                     ResourceType resourceType,
                                     RequestVerb verb,
                                     Map<String, String> headers,
                                     AuthorizationTokenType primarymasterkey,
                                     Map<String, Object> properties);

    Mono<RxDocumentServiceRequest> populateAuthorizationHeader(RxDocumentServiceRequest request);
    Mono<HttpHeaders> populateAuthorizationHeader(HttpHeaders httpHeaders);

    AuthorizationTokenType getAuthorizationTokenType();
}
