// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import java.util.List;

/**
 * Constants for Entra Communication Token operations.
 */
final class EntraCommunicationTokenUtils {

    public static final String TEAMS_EXTENSION_SCOPE_PREFIX = "https://auth.msft.communication.azure.com/";
    public static final String COMMUNICATION_CLIENTS_SCOPE_PREFIX = "https://communication.azure.com/clients/";
    public static final String COMMUNICATION_CLIENTS_DEFAULT_SCOPE = COMMUNICATION_CLIENTS_SCOPE_PREFIX + ".default";

    public static final String TEAMS_EXTENSION_ENDPOINT = "/access/teamsExtension/:exchangeAccessToken";
    public static final String TEAMS_EXTENSION_API_VERSION = "2025-06-30";
    public static final String COMMUNICATION_CLIENTS_ENDPOINT = "/access/entra/:exchangeAccessToken";
    public static final String COMMUNICATION_CLIENTS_API_VERSION = "2025-03-02-preview";

    /**
     * Checks if all scopes start with the given prefix.
     */
    public static boolean allScopesStartWith(List<String> scopes, String prefix) {
        return scopes.stream().allMatch(scope -> scope.startsWith(prefix));
    }
}
