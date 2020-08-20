// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.autoconfigure.aad;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.GrantedAuthority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class AzureADGraphClientTest {

    private AzureADGraphClient adGraphClient;

    private AADAuthenticationProperties aadAuthProps;

    @Mock
    private ServiceEndpointsProperties endpointsProps;

    @Before
    public void setup() {
        final List<String> activeDirectoryGroups = new ArrayList<>();
        activeDirectoryGroups.add("Test_Group");
        aadAuthProps = new AADAuthenticationProperties();
        aadAuthProps.setActiveDirectoryGroups(activeDirectoryGroups);
        adGraphClient = new AzureADGraphClient("client", "pass", aadAuthProps, endpointsProps);
    }

    @Test
    public void testConvertGroupToGrantedAuthorities() {

        final List<UserGroup> userGroups = Collections.singletonList(
            new UserGroup("testId", "Group", "Test_Group"));

        final Set<GrantedAuthority> authorities = adGraphClient.convertGroupsToGrantedAuthorities(userGroups);
        assertThat(authorities).hasSize(1).extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_Test_Group");
    }

    @Test
    public void testConvertGroupToGrantedAuthoritiesUsingAllowedGroups() {
        final List<UserGroup> userGroups = Arrays
            .asList(new UserGroup("testId", "Group", "Test_Group"),
                new UserGroup("testId", "Group", "Another_Group"));
        aadAuthProps.getUserGroup().getAllowedGroups().add("Another_Group");
        final Set<GrantedAuthority> authorities = adGraphClient.convertGroupsToGrantedAuthorities(userGroups);
        assertThat(authorities).hasSize(2).extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_Test_Group", "ROLE_Another_Group");
    }
}
