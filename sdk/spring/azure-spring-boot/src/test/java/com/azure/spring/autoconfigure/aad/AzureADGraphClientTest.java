// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.azure.spring.aad.AADAuthorizationServerEndpoints;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureADGraphClientTest {

    private AzureADGraphClient client;

    @Mock
    private AADAuthorizationServerEndpoints endpoints;

    @BeforeEach
    public void setup() {
        final List<String> activeDirectoryGroups = new ArrayList<>();
        activeDirectoryGroups.add("Test_Group");
        AADAuthenticationProperties aadAuthenticationProperties = new AADAuthenticationProperties();
        aadAuthenticationProperties.getUserGroup().setAllowedGroups(activeDirectoryGroups);
        client = new AzureADGraphClient("client", "pass", aadAuthenticationProperties, endpoints);
    }

    @Test
    public void testConvertGroupToGrantedAuthorities() {
        final Set<String> groups = ImmutableSet.of("Test_Group");
        final Set<SimpleGrantedAuthority> authorities = client.toGrantedAuthoritySet(groups);
        assertThat(authorities)
            .hasSize(1)
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_Test_Group");
    }

    @Test
    public void testConvertGroupToGrantedAuthoritiesUsingAllowedGroups() {
        final Set<String> groups = ImmutableSet.of("Test_Group", "Another_Group");
        final Set<SimpleGrantedAuthority> authorities = client.toGrantedAuthoritySet(groups);
        assertThat(authorities)
            .hasSize(1)
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_Test_Group");
    }
}
