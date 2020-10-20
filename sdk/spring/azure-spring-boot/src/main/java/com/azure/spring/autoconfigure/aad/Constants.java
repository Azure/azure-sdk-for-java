// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.google.common.collect.ImmutableSet;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;

/**
 * Constants used for AAD related logic.
 */
public class Constants {
    public static final String SAVED_REQUEST = "SPRING_SECURITY_SAVED_REQUEST";
    public static final String CAP_CLAIMS = "CAP_Claims";
    public static final String CLAIMS = "claims";
    public static final String BEARER_PREFIX = "Bearer "; // Whitespace at the end is necessary.
    public static final String ROLE_PREFIX = "ROLE_";
    public static final Set<SimpleGrantedAuthority> DEFAULT_AUTHORITY_SET =
        ImmutableSet.of(new SimpleGrantedAuthority(ROLE_PREFIX + "USER"));
}
