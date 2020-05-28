// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.core.http.HttpHeaders;

import java.util.Map;

public interface IAuthorizationTokenProvider {
    String getUserAuthorizationToken(String resourceAddress,
                                     ResourceType resourceType,
                                     RequestVerb verb,
                                     HttpHeaders headers,
                                     AuthorizationTokenType primarymasterkey,
                                     Map<String, Object> properties);
}
