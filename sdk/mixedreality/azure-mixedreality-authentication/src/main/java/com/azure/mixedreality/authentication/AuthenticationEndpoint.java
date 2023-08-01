// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.authentication;

class AuthenticationEndpoint {
    /**
     * Constructs an authentication endpoint from a service domain.
     *
     * @param accountDomain The Mixed Reality service account domain.
     * @return A Mixed Reality STS service endpoint.
     */
    public static String constructFromDomain(String accountDomain) {
        return "https://sts." + accountDomain;
    }

    /**
     * Constructs the authentication scope from the {@code endpoint}.
     *
     * @param endpoint The Mixed Reality STS service endpoint.
     * @return An authentication scope.
     */
    public static String constructScope(String endpoint) {
        return String.format("%s/.default", endpoint);
    }
}
