// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.aad.implementation.constants;

/**
 * Authority prefix
 */
public class AuthorityPrefix {

    public static final String APP_ROLE = "APPROLE_"; // Used for resource-server.
    public static final String ROLE = "ROLE_"; // Used for web-application. (Except for AADAppRoleStatelessAuthenticationFilter, and AADAppRoleStatelessAuthenticationFilter is depreecated.)
    public static final String SCOPE = "SCOPE_"; // Used for resource-server

}
