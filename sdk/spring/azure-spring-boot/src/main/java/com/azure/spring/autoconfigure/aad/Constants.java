// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

/**
 * Constants used for AAD related logic.
 */
public class Constants {
    public static final String BEARER_PREFIX = "Bearer "; // Whitespace at the end is necessary.
    public static final String CONDITIONAL_ACCESS_POLICY_CLAIMS = "CONDITIONAL_ACCESS_POLICY_CLAIMS";
    public static final String CLAIMS = "claims";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final Set<SimpleGrantedAuthority> DEFAULT_AUTHORITY_SET =
        Set.of(new SimpleGrantedAuthority(ROLE_PREFIX + "USER"));
    public static final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";
}
