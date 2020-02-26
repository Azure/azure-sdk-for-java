// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.RequestVerb;

import java.util.Map;

public interface IAuthorizationTokenProvider {
    String getUserAuthorizationToken(String resourceAddress,
                                     ResourceType resourceType,
                                     RequestVerb verb,
                                     Map<String, String> headers,
                                     AuthorizationTokenType primarymasterkey,
                                     Map<String, Object> properties);
}
