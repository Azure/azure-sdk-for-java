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
    private OAuth2AccessToken accessToken;
    @Mock
    private GraphClient graphClient;

    private AADAuthenticationProperties properties = new AADAuthenticationProperties();
    private AADAuthenticationProperties.UserGroupProperties userGroup =
        new AADAuthenticationProperties.UserGroupProperties();
    private AADOAuth2UserService userService;
    private AutoCloseable autoCloseable;
    private GroupInformation groupInformationFromGraph;
    private Set<String> allowedGroupNames;
    private Set<String> allowedGroupIds;

    @BeforeEach
    public void setup() {
        groupInformationFromGraph = new GroupInformation();
        allowedGroupIds = groupInformationFromGraph.getGroupsId();
        allowedGroupNames = groupInformationFromGraph.getGroupsName();
        this.autoCloseable = MockitoAnnotations.openMocks(this);
        properties.setUserGroup(userGroup);
        properties.setGraphMembershipUri("https://graph.microsoft.com/v1.0/me/memberOf");
        Mockito.lenient().when(accessToken.getTokenValue()).thenReturn("fake-access-token");
        userService = new AADOAuth2UserService(properties, graphClient);
    }

    @AfterEach
    public void close() throws Exception {
        this.autoCloseable.close();
    }

    @Test
    public void testGroupsName() {
        allowedGroupNames.add("group1");
        allowedGroupNames.add("group2");
        List<String> customizeGroupName = new ArrayList<>();
        customizeGroupName.add("group1");

        Mockito.lenient().when(graphClient.getGroupInformation(accessToken.getTokenValue()))
               .thenReturn(groupInformationFromGraph);
        userGroup.setAllowedGroupNames(customizeGroupName);
        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);

        assertThat(groupRoles).contains("ROLE_group1");
        assertThat(groupRoles).doesNotContain("ROLE_group5");
        assertThat(groupRoles).hasSize(1);
    }

    @Test
    public void testGroupsId() {
        allowedGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        List<String> customizeGroupId = new ArrayList<>();
        customizeGroupId.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");

        Mockito.lenient().when(graphClient.getGroupInformation(accessToken.getTokenValue()))
               .thenReturn(groupInformationFromGraph);
        userGroup.setAllowedGroupIds(allowedGroupIds);
        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);

        assertThat(groupRoles).contains("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        assertThat(groupRoles).doesNotContain("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194abaaa");
        assertThat(groupRoles).hasSize(1);
    }

    @Test
    public void testGroupsNameAndGroupsId() {
        allowedGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        allowedGroupNames.add("group1");

        Set<String> customizeGroupIds = new HashSet<>();
        customizeGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        List<String> customizeGroupName = new ArrayList<>();
        customizeGroupName.add("group1");

        userGroup.setAllowedGroupIds(customizeGroupIds);
        userGroup.setAllowedGroupNames(customizeGroupName);

        Mockito.lenient().when(graphClient.getGroupInformation(accessToken.getTokenValue()))
               .thenReturn(groupInformationFromGraph);

        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupRoles).contains("ROLE_group1");
        assertThat(groupRoles).doesNotContain("ROLE_group5");
        assertThat(groupRoles).contains("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        assertThat(groupRoles).doesNotContain("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194abaaa");
        assertThat(groupRoles).hasSize(2);
    }

    @Test
    public void testWithEnableFullList() {
        allowedGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        allowedGroupIds.add("6eddcc22-a24a-4459-b036-b9d9fc0f0bc7");
        allowedGroupNames.add("group1");

        Set<String> customizeGroupIds = new HashSet<>();
        customizeGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        List<String> customizeGroupName = new ArrayList<>();
        customizeGroupName.add("group1");

        userGroup.setAllowedGroupIds(customizeGroupIds);
        userGroup.setAllowedGroupNames(customizeGroupName);
        userGroup.setEnableFullList(true);

        Mockito.lenient().when(graphClient.getGroupInformation(accessToken.getTokenValue()))
               .thenReturn(groupInformationFromGraph);
        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupRoles).hasSize(3);
        assertThat(groupRoles).contains("ROLE_group1");
    }

    @Test
    public void testWithoutEnableFullList() {
        allowedGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        allowedGroupIds.add("6eddcc22-a24a-4459-b036-b9d9fc0f0bc7");
        allowedGroupNames.add("group1");
        allowedGroupNames.add("group2");

        List<String> customizeGroupNames = new ArrayList<>();
        Set<String> customizeGroupIds = new HashSet<>();
        customizeGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        customizeGroupNames.add("group1");

        userGroup.setEnableFullList(false);
        userGroup.setAllowedGroupIds(customizeGroupIds);
        userGroup.setAllowedGroupNames(customizeGroupNames);

        Mockito.lenient().when(graphClient.getGroupInformation(accessToken.getTokenValue()))
               .thenReturn(groupInformationFromGraph);
        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);

        assertThat(groupRoles).contains("ROLE_group1");
        assertThat(groupRoles).doesNotContain("ROLE_group5");
        assertThat(groupRoles).contains("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        assertThat(groupRoles).doesNotContain("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194abaaa");
        assertThat(groupRoles).hasSize(2);
    }

    @Test
    public void testAllGroupIds() {
        allowedGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        allowedGroupIds.add("6eddcc22-a24a-4459-b036-b9d9fc0f0bc7");
        allowedGroupNames.add("group1");
        allowedGroupNames.add("group2");

        Set<String> customizeGroupIds = new HashSet<>();
        customizeGroupIds.add("all");
        List<String> customizeGroupName = new ArrayList<>();
        customizeGroupName.add("group1");

        userGroup.setAllowedGroupIds(customizeGroupIds);
        userGroup.setAllowedGroupNames(customizeGroupName);
        userGroup.setEnableFullList(true);

        Mockito.lenient().when(graphClient.getGroupInformation(accessToken.getTokenValue()))
               .thenReturn(groupInformationFromGraph);
        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupRoles).hasSize(3);
        assertThat(groupRoles).contains("ROLE_group1");
        assertThat(groupRoles).doesNotContain("ROLE_group2");
    }
}
