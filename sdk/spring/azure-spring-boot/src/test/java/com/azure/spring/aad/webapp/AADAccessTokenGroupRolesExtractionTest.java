// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapp;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    private Set<String> groupNamesFromGraph;
    private Set<String> groupIdsFromGraph;

    @BeforeAll
    public void setup() {
        this.autoCloseable = MockitoAnnotations.openMocks(this);
        groupInformationFromGraph = new GroupInformation();
        groupNamesFromGraph = new HashSet<>();
        groupIdsFromGraph = new HashSet<>();
        groupNamesFromGraph.add("group1");
        groupNamesFromGraph.add("group2");
        groupIdsFromGraph.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        groupIdsFromGraph.add("6eddcc22-a24a-4459-b036-b9d9fc0f0bc7");
        groupInformationFromGraph.setGroupsIds(groupIdsFromGraph);
        groupInformationFromGraph.setGroupsNames(groupNamesFromGraph);
        properties.setUserGroup(userGroup);
        properties.setGraphMembershipUri("https://graph.microsoft.com/v1.0/me/memberOf");
        Mockito.lenient().when(accessToken.getTokenValue()).thenReturn("fake-access-token");
        userService = new AADOAuth2UserService(properties, graphClient);
        Mockito.lenient().when(graphClient.getGroupInformation(accessToken.getTokenValue()))
               .thenReturn(groupInformationFromGraph);
    }

    @AfterEach
    public void reset() {
        userGroup.setAllowedGroupNames(Collections.emptyList());
        userGroup.setAllowedGroupIds(Collections.emptySet());
        userGroup.setEnableFullList(false);
    }

    @AfterAll
    public void close() throws Exception {
        this.autoCloseable.close();
    }

    @Test
    public void testGroupsName() {
        List<String> customizeGroupName = new ArrayList<>();
        customizeGroupName.add("group1");
        userGroup.setAllowedGroupNames(customizeGroupName);

        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);

        assertThat(groupRoles).contains("ROLE_group1");
        assertThat(groupRoles).doesNotContain("ROLE_group5");
        assertThat(groupRoles).hasSize(1);
    }

    @Test
    public void testGroupsId() {
        Set<String> customizeGroupId = new HashSet<>();
        customizeGroupId.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        userGroup.setAllowedGroupIds(customizeGroupId);

        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);

        assertThat(groupRoles).contains("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        assertThat(groupRoles).doesNotContain("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194abaaa");
        assertThat(groupRoles).hasSize(1);
    }

    @Test
    public void testGroupsNameAndGroupsId() {
        Set<String> customizeGroupIds = new HashSet<>();
        customizeGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        List<String> customizeGroupName = new ArrayList<>();
        customizeGroupName.add("group1");

        userGroup.setAllowedGroupIds(customizeGroupIds);
        userGroup.setAllowedGroupNames(customizeGroupName);

        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupRoles).contains("ROLE_group1");
        assertThat(groupRoles).doesNotContain("ROLE_group5");
        assertThat(groupRoles).contains("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        assertThat(groupRoles).doesNotContain("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194abaaa");
        assertThat(groupRoles).hasSize(2);
    }

    @Test
    public void testWithEnableFullList() {
        Set<String> customizeGroupIds = new HashSet<>();
        customizeGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        List<String> customizeGroupName = new ArrayList<>();
        customizeGroupName.add("group1");

        userGroup.setAllowedGroupIds(customizeGroupIds);
        userGroup.setAllowedGroupNames(customizeGroupName);
        userGroup.setEnableFullList(true);

        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupRoles).hasSize(3);
        assertThat(groupRoles).contains("ROLE_group1");
    }

    @Test
    public void testWithoutEnableFullList() {
        List<String> customizeGroupNames = new ArrayList<>();
        Set<String> customizeGroupIds = new HashSet<>();
        customizeGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        customizeGroupNames.add("group1");

        userGroup.setEnableFullList(false);
        userGroup.setAllowedGroupIds(customizeGroupIds);
        userGroup.setAllowedGroupNames(customizeGroupNames);

        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);

        assertThat(groupRoles).contains("ROLE_group1");
        assertThat(groupRoles).doesNotContain("ROLE_group5");
        assertThat(groupRoles).contains("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        assertThat(groupRoles).doesNotContain("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194abaaa");
        assertThat(groupRoles).hasSize(2);
    }

    @Test
    public void testAllGroupIds() {
        Set<String> customizeGroupIds = new HashSet<>();
        customizeGroupIds.add("all");
        List<String> customizeGroupName = new ArrayList<>();
        customizeGroupName.add("group1");

        userGroup.setAllowedGroupIds(customizeGroupIds);
        userGroup.setAllowedGroupNames(customizeGroupName);
        userGroup.setEnableFullList(true);

        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupRoles).hasSize(3);
        assertThat(groupRoles).contains("ROLE_group1");
        assertThat(groupRoles).doesNotContain("ROLE_group2");
    }

    @Test
    public void testIllegalGroupIdParam() {
        Set<String> customizeGroupIds = new HashSet<>();
        customizeGroupIds.add("all");
        customizeGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        customizeGroupIds.add("6eddcc22-a24a-4459-b036-b9d9fc0f0bc7");
        List<String> customizeGroupName = new ArrayList<>();
        customizeGroupName.add("group1");

        userGroup.setAllowedGroupNames(customizeGroupName);

        assertThrows(IllegalStateException.class, () ->
            userGroup.setAllowedGroupIds(customizeGroupIds), "When 'all' is used, there is no need to configure"
            + " additional group id.");
    }
}
