// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.implementation.webapp;

import com.azure.spring.cloud.autoconfigure.aad.implementation.WebApplicationContextRunnerUtils;
import com.azure.spring.cloud.autoconfigure.aad.implementation.graph.GraphClient;
import com.azure.spring.cloud.autoconfigure.aad.implementation.graph.GroupInformation;
import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
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
class AadAccessTokenGroupRolesExtractionTests {

    private static final String GROUP_ID_1 = "d07c0bd6-4aab-45ac-b87c-23e8d00194ab";
    private static final String GROUP_ID_2 = "6eddcc22-a24a-4459-b036-b9d9fc0f0bc7";

    private AutoCloseable autoCloseable;

    @Mock
    private OAuth2AccessToken accessToken;

    @Mock
    private GraphClient graphClient;

    @BeforeAll
    void setup() {
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
    void close() throws Exception {
        this.autoCloseable.close();
    }

    @Test
    void testAllowedGroupsNames() {
        List<String> allowedGroupNames = new ArrayList<>();
        allowedGroupNames.add("group1");

        AadAuthenticationProperties properties = new AadAuthenticationProperties();
        properties.getUserGroup().setAllowedGroupNames(allowedGroupNames);

        AadOAuth2UserService userService = new AadOAuth2UserService(properties, graphClient);
        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupRoles).hasSize(1);
        assertThat(groupRoles).contains("ROLE_group1");
        assertThat(groupRoles).doesNotContain("ROLE_group2");
    }

    @Test
    void testAllowedGroupsIds() {
        Set<String> allowedGroupIds = new HashSet<>();
        allowedGroupIds.add(GROUP_ID_1);

        AadAuthenticationProperties properties = new AadAuthenticationProperties();
        properties.getUserGroup().setAllowedGroupIds(allowedGroupIds);

        AadOAuth2UserService userService = new AadOAuth2UserService(properties, graphClient);
        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupRoles).hasSize(1);
        assertThat(groupRoles).contains("ROLE_" + GROUP_ID_1);
        assertThat(groupRoles).doesNotContain("ROLE_" + GROUP_ID_2);
    }

    @Test
    void testAllowedGroupsNamesAndAllowedGroupsIds() {
        Set<String> allowedGroupIds = new HashSet<>();
        allowedGroupIds.add(GROUP_ID_1);
        List<String> allowedGroupNames = new ArrayList<>();
        allowedGroupNames.add("group1");


        AadAuthenticationProperties properties = new AadAuthenticationProperties();
        properties.getUserGroup().setAllowedGroupIds(allowedGroupIds);
        properties.getUserGroup().setAllowedGroupNames(allowedGroupNames);

        AadOAuth2UserService userService = new AadOAuth2UserService(properties, graphClient);
        Set<String> groupRoles = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupRoles).hasSize(2);
        assertThat(groupRoles).contains("ROLE_group1");
        assertThat(groupRoles).doesNotContain("ROLE_group2");
        assertThat(groupRoles).contains("ROLE_" + GROUP_ID_1);
        assertThat(groupRoles).doesNotContain("ROLE_" + GROUP_ID_2);
    }

    @Test
    void testIllegalGroupIdParam() {
        WebApplicationContextRunnerUtils
            .webApplicationContextRunner()
            .withPropertyValues(
                "spring.cloud.azure.active-directory.user-group.allowed-group-ids = all," + GROUP_ID_1
            )
            .run(context ->
                assertThrows(IllegalStateException.class, () -> context.getBean(AadAuthenticationProperties.class)));
    }
}
