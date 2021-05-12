// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapp;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class AADAccessTokenGroupRolesExtractionTest {

    private AADAuthenticationProperties properties = spy(AADAuthenticationProperties.class);
    private OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
    private AADAuthenticationProperties.UserGroupProperties userGroup =
        mock(AADAuthenticationProperties.UserGroupProperties.class);
    private GraphClient graphClient = mock(GraphClient.class);
    private AADOAuth2UserService userService = new AADOAuth2UserService(properties, graphClient);

    @BeforeEach
    private void setup() {
        Set<String> groups = new HashSet<>();
        groups.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        groups.add("6eddcc22-a24a-4459-b036-b9d9fc0f0bc7");
        groups.add("group1");
        groups.add("group2");
        when(properties.allowedGroupNamesConfigured()).thenReturn(true);
        when(properties.allowedGroupIdsConfigured()).thenReturn(true);
        when(properties.getUserGroup()).thenReturn(userGroup);
        when(properties.getGraphMembershipUri()).thenReturn("https://graph.microsoft.com/v1.0/me/memberOf");
        when(accessToken.getTokenValue()).thenReturn("fake-access-token");
        when(graphClient.getGroupsFromGraph(accessToken.getTokenValue())).thenReturn(groups);
    }

    @Test
    public void testGroupsName() {
        List<String> allowedGroupNames = new ArrayList<>();
        allowedGroupNames.add("group1");
        when(userGroup.getAllowedGroupNames()).thenReturn(allowedGroupNames);
        Set<String> groupsName = userService.extractGroupRolesFromAccessToken(accessToken);

        assertThat(groupsName).contains("ROLE_group1");
        assertThat(groupsName).doesNotContain("ROLE_group5");
        assertThat(groupsName).hasSize(1);
    }

    @Test
    public void testGroupsId() {
        Set<String> allowedGroupIds = new HashSet<>();
        allowedGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        when(userGroup.getAllowedGroupIds()).thenReturn(allowedGroupIds);

        Set<String> groupsName = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupsName).contains("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        assertThat(groupsName).doesNotContain("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194abaaa");
        assertThat(groupsName).hasSize(1);
    }

    @Test
    public void testGroupsNameAndGroupsId() {
        Set<String> allowedGroupIds = new HashSet<>();
        allowedGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        when(userGroup.getAllowedGroupIds()).thenReturn(allowedGroupIds);
        List<String> allowedGroupNames = new ArrayList<>();
        allowedGroupNames.add("group1");
        when(userGroup.getAllowedGroupNames()).thenReturn(allowedGroupNames);

        Set<String> groupsName = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupsName).contains("ROLE_group1");
        assertThat(groupsName).doesNotContain("ROLE_group5");
        assertThat(groupsName).contains("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        assertThat(groupsName).doesNotContain("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194abaaa");
        assertThat(groupsName).hasSize(2);
    }

    @Test
    public void testEnableFullList() {
        when(properties.getUserGroup().getEnableFullList()).thenReturn(true);
        Set<String> groupIds = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupIds).hasSize(4);
    }

    @Test
    public void testDisableFullList() {
        when(properties.getUserGroup().getEnableFullList()).thenReturn(false);
        Set<String> allowedGroupIds = new HashSet<>();
        allowedGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        when(userGroup.getAllowedGroupIds()).thenReturn(allowedGroupIds);
        List<String> allowedGroupNames = new ArrayList<>();
        allowedGroupNames.add("group1");
        when(userGroup.getAllowedGroupNames()).thenReturn(allowedGroupNames);

        Set<String> groupsName = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupsName).contains("ROLE_group1");
        assertThat(groupsName).doesNotContain("ROLE_group5");
        assertThat(groupsName).contains("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        assertThat(groupsName).doesNotContain("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194abaaa");
        assertThat(groupsName).hasSize(2);
    }
}
