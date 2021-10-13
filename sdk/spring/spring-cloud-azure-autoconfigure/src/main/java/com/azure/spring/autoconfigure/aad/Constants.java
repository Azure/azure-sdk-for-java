// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.azure.spring.aad.implementation.constants.AuthorityPrefix;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Constants used for AAD related logic.
 */
public class Constants {
    public static final String BEARER_PREFIX = "Bearer "; // Whitespace at the end is necessary.
    public static final String CONDITIONAL_ACCESS_POLICY_CLAIMS = "CONDITIONAL_ACCESS_POLICY_CLAIMS";
    public static final String CLAIMS = "claims";
    public static final Set<SimpleGrantedAuthority> DEFAULT_AUTHORITY_SET;

    static {
        Set<SimpleGrantedAuthority> authoritySet = new HashSet<>();
        authoritySet.add(new SimpleGrantedAuthority(AuthorityPrefix.ROLE + "USER"));
        DEFAULT_AUTHORITY_SET = Collections.unmodifiableSet(authoritySet);
    }
}
