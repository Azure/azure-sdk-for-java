// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.aad.security.constants;

/**
 * Claim names in ID token or access token.
 *
 * @since 4.0.0
 * @see <a href="https://docs.microsoft.com/azure/active-directory/develop/access-tokens">Access tokens</a>
 * @see <a href="https://docs.microsoft.com/azure/active-directory/develop/id-tokens">ID tokens</a>
 */
public final class AadJwtClaimNames {

    private AadJwtClaimNames() {
    }

    public static final String AUD = "aud";
    public static final String ISS = "iss";
    public static final String NAME = "name";
    public static final String ROLES = "roles";
    public static final String SCP = "scp";
    public static final String SUB = "sub";
    public static final String TID = "tid";
}
