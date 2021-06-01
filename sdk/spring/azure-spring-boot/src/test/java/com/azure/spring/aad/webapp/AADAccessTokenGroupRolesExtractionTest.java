// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapp;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class AADAccessTokenGroupRolesExtractionTest {

    @Mock
    private AADAuthenticationProperties properties;
    @Mock
    private OAuth2AccessToken accessToken;
    @Mock
    private GraphClient graphClient;

    private AADAuthenticationProperties.UserGroupProperties userGroup =
        new AADAuthenticationProperties.UserGroupProperties();
    private AADOAuth2UserService userService;
    private AutoCloseable autoCloseable;

    @BeforeEach
    public void setup() {
        this.autoCloseable = MockitoAnnotations.openMocks(this);
        Set<String> groups = new HashSet<>();
        groups.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        groups.add("6eddcc22-a24a-4459-b036-b9d9fc0f0bc7");
        groups.add("group1");
        groups.add("group2");
        Mockito.lenient().when(properties.allowedGroupNamesConfigured()).thenReturn(true);
        Mockito.lenient().when(properties.allowedGroupIdsConfigured()).thenReturn(true);
        Mockito.lenient().when(properties.getUserGroup()).thenReturn(userGroup);
        Mockito.lenient().when(properties.getGraphMembershipUri()).thenReturn("https://graph.microsoft.com/v1"
            + ".0/me/memberOf");
        Mockito.lenient().when(accessToken.getTokenValue()).thenReturn("fake-access-token");
        Mockito.lenient().when(graphClient.getGroupsFromGraph(accessToken.getTokenValue())).thenReturn(groups);
        userService = new AADOAuth2UserService(properties, graphClient);
    }

    @AfterEach
    public void close() throws Exception {
        this.autoCloseable.close();
    }

    @Test
    public void testGroupsName() {
        List<String> allowedGroupNames = new ArrayList<>();
        allowedGroupNames.add("group1");
        userGroup.setAllowedGroupNames(allowedGroupNames);
        Set<String> groupsName = userService.extractGroupRolesFromAccessToken(accessToken);

        assertThat(groupsName).contains("ROLE_group1");
        assertThat(groupsName).doesNotContain("ROLE_group5");
        assertThat(groupsName).hasSize(1);
    }

    @Test
    public void testGroupsId() {
        Set<String> allowedGroupIds = new HashSet<>();
        allowedGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        userGroup.setAllowedGroupIds(allowedGroupIds);

        Set<String> groupsName = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupsName).contains("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        assertThat(groupsName).doesNotContain("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194abaaa");
        assertThat(groupsName).hasSize(1);
    }

    @Test
    public void testGroupsNameAndGroupsId() {
        Set<String> allowedGroupIds = new HashSet<>();
        allowedGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        userGroup.setAllowedGroupIds(allowedGroupIds);

        List<String> allowedGroupNames = new ArrayList<>();
        allowedGroupNames.add("group1");
        userGroup.setAllowedGroupNames(allowedGroupNames);

        Set<String> groupsName = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupsName).contains("ROLE_group1");
        assertThat(groupsName).doesNotContain("ROLE_group5");
        assertThat(groupsName).contains("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        assertThat(groupsName).doesNotContain("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194abaaa");
        assertThat(groupsName).hasSize(2);
    }

    @Test
    public void testEnableFullList() {
        userGroup.setEnableFullList(true);
        Set<String> groupIds = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupIds).hasSize(4);
    }

    @Test
    public void testDisableFullList() {
        userGroup.setEnableFullList(false);
        Set<String> allowedGroupIds = new HashSet<>();
        allowedGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        userGroup.setAllowedGroupIds(allowedGroupIds);
        List<String> allowedGroupNames = new ArrayList<>();
        allowedGroupNames.add("group1");
        userGroup.setAllowedGroupNames(allowedGroupNames);

        Set<String> groupsName = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupsName).contains("ROLE_group1");
        assertThat(groupsName).doesNotContain("ROLE_group5");
        assertThat(groupsName).contains("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        assertThat(groupsName).doesNotContain("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194abaaa");
        assertThat(groupsName).hasSize(2);
    }
}
