// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AzureADGraphClientTest {

    private AzureADGraphClient adGraphClient;

    private AADAuthenticationProperties aadAuthenticationProperties;

    @Mock
    private ServiceEndpointsProperties endpointsProps;

    @Before
    public void setup() {
        final List<String> activeDirectoryGroups = new ArrayList<>();
        activeDirectoryGroups.add("Test_Group");
        aadAuthenticationProperties = new AADAuthenticationProperties();
        aadAuthenticationProperties.getUserGroup().setAllowedGroups(activeDirectoryGroups);
        adGraphClient = new AzureADGraphClient(aadAuthenticationProperties, endpointsProps);
    }

    @Test
    public void testConvertGroupToGrantedAuthorities() {
        final Set<String> groups = ImmutableSet.of("Test_Group");
        final Set<SimpleGrantedAuthority> authorities = adGraphClient.toGrantedAuthoritySet(groups);
        assertThat(authorities)
            .hasSize(1)
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_Test_Group");
    }

    @Test
    public void testConvertGroupToGrantedAuthoritiesUsingAllowedGroups() {
        final Set<String> groups = ImmutableSet.of("Test_Group", "Another_Group");
        final Set<SimpleGrantedAuthority> authorities = adGraphClient.toGrantedAuthoritySet(groups);
        assertThat(authorities)
            .hasSize(1)
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_Test_Group");
    }
}
