// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal;

import java.util.Map;

public interface IAuthorizationTokenProvider {
    String getUserAuthorizationToken(String resourceAddress,
                                     ResourceType resourceType,
                                     String get,
                                     Map<String, String> headers,
                                     AuthorizationTokenType primarymasterkey,
                                     Map<String, Object> properties);
}
