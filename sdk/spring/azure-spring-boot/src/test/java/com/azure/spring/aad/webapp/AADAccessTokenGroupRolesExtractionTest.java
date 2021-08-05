// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapp;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AADAccessTokenGroupRolesExtractionTest {

    private static final String GROUP_ID_1 = "d07c0bd6-4aab-45ac-b87c-23e8d00194ab";
    private static final String GROUP_ID_2 = "6eddcc22-a24a-4459-b036-b9d9fc0f0bc7";

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
        Mockito.lenient().when(accessToken.getTokenValue())
                         .thenReturn("fake-access-token");
        Mockito.lenient().when(graphClient.getGroupInformation(accessToken.getTokenValue()))
                         .thenReturn(groupInformationFromGraph);
    }

    @AfterAll
    public void close() throws Exception {
        this.autoCloseable.close();
    }

    private AADAuthenticationProperties getProperties() {
        AADAuthenticationProperties properties = new AADAuthenticationProperties();
        AADAuthenticationProperties.UserGroupProperties userGroup =
            new AADAuthenticationProperties.UserGroupProperties();
        properties.setUserGroup(userGroup);
        properties.setGraphMembershipUri("https://graph.microsoft.com/v1.0/me/memberOf");
        return properties;
    }

    @Test
    public void testAllowedGroupsNames() {
        List<String> allowedGroupNames = new ArrayList<>();
        allowedGroupNames.add("group1");

        AADAuthenticationProperties properties = getProperties();
        properties.getUserGroup().setAllowedGroupNames(allowedGroupNames);

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

        AADAuthenticationProperties properties = getProperties();
        properties.getUserGroup().setAllowedGroupIds(allowedGroupIds);

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


        AADAuthenticationProperties properties = getProperties();
        properties.getUserGroup().setAllowedGroupIds(allowedGroupIds);
        properties.getUserGroup().setAllowedGroupNames(allowedGroupNames);

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

        AADAuthenticationProperties properties = getProperties();
        properties.getUserGroup().setAllowedGroupIds(allowedGroupIds);
        properties.getUserGroup().setAllowedGroupNames(allowedGroupNames);
        properties.getUserGroup().setEnableFullList(true);

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

        AADAuthenticationProperties properties = getProperties();
        properties.getUserGroup().setEnableFullList(false);
        properties.getUserGroup().setAllowedGroupIds(allowedGroupIds);
        properties.getUserGroup().setAllowedGroupNames(allowedGroupNames);

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

        AADAuthenticationProperties properties = getProperties();
        properties.getUserGroup().setAllowedGroupIds(allowedGroupIds);
        properties.getUserGroup().setAllowedGroupNames(allowedGroupNames);
        properties.getUserGroup().setEnableFullList(false);

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
