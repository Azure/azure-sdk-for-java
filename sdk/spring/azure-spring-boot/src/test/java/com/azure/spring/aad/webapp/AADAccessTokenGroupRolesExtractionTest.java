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

    private static final String GROUP_ID_1 = "d07c0bd6-4aab-45ac-b87c-23e8d00194ab";
    private static final String GROUP_ID_2 = "6eddcc22-a24a-4459-b036-b9d9fc0f0bc7";

    private final AADAuthenticationProperties properties = new AADAuthenticationProperties();
    private final AADAuthenticationProperties.UserGroupProperties userGroup =
        new AADAuthenticationProperties.UserGroupProperties();
    private AutoCloseable autoCloseable;

    @Mock
    private OAuth2AccessToken accessToken;
    @Mock
    private GraphClient graphClient;

    @BeforeAll
    public void setup() {
        this.autoCloseable = MockitoAnnotations.openMocks(this);
        GroupInformation groupInformationFromGraph = new GroupInformation();
        Set<String> groupNamesFromGraph = new HashSet<>();
        Set<String> groupIdsFromGraph = new HashSet<>();
        groupNamesFromGraph.add("group1");
        groupNamesFromGraph.add("group2");
        groupIdsFromGraph.add(GROUP_ID_1);
        groupIdsFromGraph.add(GROUP_ID_2);
        groupInformationFromGraph.setGroupsIds(groupIdsFromGraph);
        groupInformationFromGraph.setGroupsNames(groupNamesFromGraph);
        properties.setUserGroup(userGroup);
        properties.setGraphMembershipUri("https://graph.microsoft.com/v1.0/me/memberOf");
        Mockito.lenient().when(accessToken.getTokenValue())
                         .thenReturn("fake-access-token");
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
    public void testAllowedGroupsNames() {
        List<String> allowedGroupNames = new ArrayList<>();
        allowedGroupNames.add("group1");
        userGroup.setAllowedGroupNames(allowedGroupNames);
        AADOAuth2UserService userService = new AADOAuth2UserService(properties, graphClient);
        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupRoles).hasSize(1);
        assertThat(groupRoles).contains("ROLE_group1");
        assertThat(groupRoles).doesNotContain("ROLE_group2");
    }

    @Test
    public void testAllowedGroupsIds() {
        Set<String> allowedGroupIds = new HashSet<>();
        allowedGroupIds.add(GROUP_ID_1);
        userGroup.setAllowedGroupIds(allowedGroupIds);
        AADOAuth2UserService userService = new AADOAuth2UserService(properties, graphClient);
        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupRoles).hasSize(1);
        assertThat(groupRoles).contains("ROLE_" + GROUP_ID_1);
        assertThat(groupRoles).doesNotContain("ROLE_" + GROUP_ID_2);
    }

    @Test
    public void testAllowedGroupsNamesAndAllowedGroupsIds() {
        Set<String> allowedGroupIds = new HashSet<>();
        allowedGroupIds.add(GROUP_ID_1);
        List<String> allowedGroupNames = new ArrayList<>();
        allowedGroupNames.add("group1");
        userGroup.setAllowedGroupIds(allowedGroupIds);
        userGroup.setAllowedGroupNames(allowedGroupNames);
        AADOAuth2UserService userService = new AADOAuth2UserService(properties, graphClient);
        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupRoles).hasSize(2);
        assertThat(groupRoles).contains("ROLE_group1");
        assertThat(groupRoles).doesNotContain("ROLE_group2");
        assertThat(groupRoles).contains("ROLE_" + GROUP_ID_1);
        assertThat(groupRoles).doesNotContain("ROLE_" + GROUP_ID_2);
    }

    @Test
    public void testWithEnableFullList() {
        Set<String> allowedGroupIds = new HashSet<>();
        allowedGroupIds.add(GROUP_ID_1);
        List<String> allowedGroupNames = new ArrayList<>();
        allowedGroupNames.add("group1");
        userGroup.setAllowedGroupIds(allowedGroupIds);
        userGroup.setAllowedGroupNames(allowedGroupNames);
        userGroup.setEnableFullList(true);
        AADOAuth2UserService userService = new AADOAuth2UserService(properties, graphClient);
        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupRoles).hasSize(3);
        assertThat(groupRoles).contains("ROLE_group1");
        assertThat(groupRoles).contains("ROLE_" + GROUP_ID_1);
        assertThat(groupRoles).contains("ROLE_" + GROUP_ID_2);
    }

    @Test
    public void testWithoutEnableFullList() {
        List<String> allowedGroupNames = new ArrayList<>();
        Set<String> allowedGroupIds = new HashSet<>();
        allowedGroupIds.add(GROUP_ID_1);
        allowedGroupNames.add("group1");
        userGroup.setEnableFullList(false);
        userGroup.setAllowedGroupIds(allowedGroupIds);
        userGroup.setAllowedGroupNames(allowedGroupNames);
        AADOAuth2UserService userService = new AADOAuth2UserService(properties, graphClient);
        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupRoles).hasSize(2);
        assertThat(groupRoles).contains("ROLE_group1");
        assertThat(groupRoles).doesNotContain("ROLE_group2");
        assertThat(groupRoles).contains("ROLE_" + GROUP_ID_1);
        assertThat(groupRoles).doesNotContain("ROLE_" + GROUP_ID_2);
    }

    @Test
    public void testAllowedGroupIdsAllWithoutEnableFullList() {
        Set<String> allowedGroupIds = new HashSet<>();
        allowedGroupIds.add("all");
        List<String> allowedGroupNames = new ArrayList<>();
        allowedGroupNames.add("group1");
        userGroup.setAllowedGroupIds(allowedGroupIds);
        userGroup.setAllowedGroupNames(allowedGroupNames);
        userGroup.setEnableFullList(false);
        AADOAuth2UserService userService = new AADOAuth2UserService(properties, graphClient);
        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupRoles).hasSize(3);
        assertThat(groupRoles).contains("ROLE_group1");
        assertThat(groupRoles).doesNotContain("ROLE_group2");
        assertThat(groupRoles).contains("ROLE_" + GROUP_ID_1);
        assertThat(groupRoles).contains("ROLE_" + GROUP_ID_2);
    }

    @Test
    public void testIllegalGroupIdParam() {
        WebApplicationContextRunnerUtils
            .getContextRunnerWithRequiredProperties()
            .withPropertyValues(
                "azure.activedirectory.user-group.allowed-group-ids = all," + GROUP_ID_1
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AADAuthenticationProperties.class)));
    }
}
