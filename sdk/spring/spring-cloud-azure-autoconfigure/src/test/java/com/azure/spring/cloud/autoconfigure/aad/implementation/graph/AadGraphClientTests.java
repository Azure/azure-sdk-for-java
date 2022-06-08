// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.aad.implementation.graph;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthorizationServerEndpoints;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AadGraphClientTests {

    private AadGraphClient client;

    @Mock
    private AadAuthorizationServerEndpoints endpoints;

    @BeforeEach
    void setup() {
        final List<String> activeDirectoryGroups = new ArrayList<>();
        activeDirectoryGroups.add("Test_Group");
        AadAuthenticationProperties aadAuthenticationProperties = new AadAuthenticationProperties();
        aadAuthenticationProperties.getUserGroup().setAllowedGroupNames(activeDirectoryGroups);
        client = new AadGraphClient("client", "pass", aadAuthenticationProperties, endpoints);
    }

    @Test
    void testConvertGroupToGrantedAuthorities() {
        final Set<String> groups = new HashSet<>(1);
        groups.add("Test_Group");
        final Set<SimpleGrantedAuthority> authorities = client.toGrantedAuthoritySet(
            Collections.unmodifiableSet(groups));
        assertThat(authorities)
            .hasSize(1)
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_Test_Group");
    }

    @Test
    void testConvertGroupToGrantedAuthoritiesUsingAllowedGroups() {
        final Set<String> groups = new HashSet<>(2);
        groups.add("Test_Group");
        groups.add("Another_Group");
        final Set<SimpleGrantedAuthority> authorities = client.toGrantedAuthoritySet(
            Collections.unmodifiableSet(groups));
        assertThat(authorities)
            .hasSize(1)
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_Test_Group");
    }
}
