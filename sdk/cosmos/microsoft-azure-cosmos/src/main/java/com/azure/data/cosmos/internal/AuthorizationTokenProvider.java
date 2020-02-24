// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import java.util.Map;

/**
 * Represents types that can provide functionality to generate authorization token for the Azure Cosmos DB database
 * service.
 */
public interface AuthorizationTokenProvider {
    String generateKeyAuthorizationSignature(String verb,
                                             String resourceIdOrFullName,
                                             ResourceType resourceType,
                                             Map<String, String> headers);

    String getAuthorizationTokenUsingResourceTokens(Map<String, String> resourceTokens,
                                                    String path,
                                                    String resourceId);
}
