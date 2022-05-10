// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.constants;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Constants used for AAD related logic.
 */
public final class Constants {

    private Constants() {
    }

    /**
     * Bearer prefix
     */
    public static final String BEARER_PREFIX = "Bearer "; // Whitespace at the end is necessary.

    /**
     * Conditional access policy claims
     */
    public static final String CONDITIONAL_ACCESS_POLICY_CLAIMS = "CONDITIONAL_ACCESS_POLICY_CLAIMS";

    /**
     * claims
     */
    public static final String CLAIMS = "claims";

    /**
     * Default authority set
     */
    public static final Set<SimpleGrantedAuthority> DEFAULT_AUTHORITY_SET;

    static {
        Set<SimpleGrantedAuthority> authoritySet = new HashSet<>();
        authoritySet.add(new SimpleGrantedAuthority(AuthorityPrefix.ROLE + "USER"));
        DEFAULT_AUTHORITY_SET = Collections.unmodifiableSet(authoritySet);
    }
}
