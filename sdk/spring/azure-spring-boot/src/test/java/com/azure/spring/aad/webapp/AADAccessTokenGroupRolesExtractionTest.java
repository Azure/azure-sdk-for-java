// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapp;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import com.azure.spring.autoconfigure.aad.JacksonObjectMapperFactory;
import com.azure.spring.autoconfigure.aad.Membership;
import com.azure.spring.autoconfigure.aad.Memberships;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class AADAccessTokenGroupRolesExtractionTest {

    private final String membershipJson = "{\"@odata.context\":\"https://graph.microsoft.com/v1"
        + ".0/$metadata#directoryObjects\",\"value\":[{\"@odata.type\":\"#microsoft.graph.group\","
        + "\"id\":\"d07c0bd6-4aab-45ac-b87c-23e8d00194ab\",\"deletedDateTime\":null,\"classification\":null,"
        + "\"createdDateTime\":\"2021-04-13T02:00:25Z\",\"creationOptions\":[],\"description\":\"group1\","
        + "\"displayName\":\"group1\",\"expirationDateTime\":null,\"groupTypes\":[],\"isAssignableToRole\":null,"
        + "\"mail\":null,\"mailEnabled\":false,\"mailNickname\":\"151ea924-9\",\"membershipRule\":null,"
        + "\"membershipRuleProcessingState\":null,\"onPremisesDomainName\":null,\"onPremisesLastSyncDateTime\":null,"
        + "\"onPremisesNetBiosName\":null,\"onPremisesSamAccountName\":null,\"onPremisesSecurityIdentifier\":null,"
        + "\"onPremisesSyncEnabled\":null,\"preferredDataLocation\":null,\"preferredLanguage\":null,"
        + "\"proxyAddresses\":[],\"renewedDateTime\":\"2021-04-13T02:00:25Z\",\"resourceBehaviorOptions\":[],"
        + "\"resourceProvisioningOptions\":[],\"securityEnabled\":true,"
        + "\"securityIdentifier\":\"S-1-12-1-3497790422-1168919211-3894639800-2878603728\",\"theme\":null,"
        + "\"visibility\":null,\"onPremisesProvisioningErrors\":[]},{\"@odata.type\":\"#microsoft.graph.group\","
        + "\"id\":\"6eddcc22-a24a-4459-b036-b9d9fc0f0bc7\",\"deletedDateTime\":null,\"classification\":null,"
        + "\"createdDateTime\":\"2021-04-13T02:00:53Z\",\"creationOptions\":[],\"description\":\"group2\","
        + "\"displayName\":\"group2\",\"expirationDateTime\":null,\"groupTypes\":[],\"isAssignableToRole\":null,"
        + "\"mail\":null,\"mailEnabled\":false,\"mailNickname\":\"3cbfa6d0-e\",\"membershipRule\":null,"
        + "\"membershipRuleProcessingState\":null,\"onPremisesDomainName\":null,\"onPremisesLastSyncDateTime\":null,"
        + "\"onPremisesNetBiosName\":null,\"onPremisesSamAccountName\":null,\"onPremisesSecurityIdentifier\":null,"
        + "\"onPremisesSyncEnabled\":null,\"preferredDataLocation\":null,\"preferredLanguage\":null,"
        + "\"proxyAddresses\":[],\"renewedDateTime\":\"2021-04-13T02:00:53Z\",\"resourceBehaviorOptions\":[],"
        + "\"resourceProvisioningOptions\":[],\"securityEnabled\":true,"
        + "\"securityIdentifier\":\"S-1-12-1-1860029474-1146724938-3652794032-3339390972\",\"theme\":null,"
        + "\"visibility\":null,\"onPremisesProvisioningErrors\":[]}]}";

    private AADAuthenticationProperties properties = spy(AADAuthenticationProperties.class);
    private OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
    private AADAuthenticationProperties.UserGroupProperties userGroup =
        mock(AADAuthenticationProperties.UserGroupProperties.class);
    private GraphClientTest graphClientTest = new GraphClientTest(properties);
    private AADOAuth2UserService userService = new AADOAuth2UserService(properties, graphClientTest);

    private void setup() {
        when(properties.allowedGroupsConfigured()).thenReturn(true);
        when(properties.getUserGroup()).thenReturn(userGroup);
        when(properties.getGraphMembershipUri()).thenReturn("https://graph.microsoft.com/v1.0/me/memberOf");
        when(accessToken.getTokenValue()).thenReturn("fake-access-token");
    }

    @Test
    public void testGroupsName() {
        setup();
        List<String> allowedGroupNames = new ArrayList<>();
        allowedGroupNames.add("group1");
        when(userGroup.getAllowedGroupNames()).thenReturn(allowedGroupNames);
        Set<String> groupsName = userService.extractGroupRolesFromAccessToken(accessToken);

        assertThat(groupsName).contains("ROLE_group1");
        assertThat(groupsName).hasSize(1);
    }

    @Test
    public void testGroupsId() {
        setup();
        Set<String> allowedGroupIds = new HashSet<>();
        allowedGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        when(userGroup.getAllowedGroupIds()).thenReturn(allowedGroupIds);

        Set<String> groupsName = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupsName).contains("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        assertThat(groupsName).hasSize(1);
    }

    @Test
    public void testGroupsNameAndGroupsId() {
        setup();
        Set<String> allowedGroupIds = new HashSet<>();
        allowedGroupIds.add("d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        when(userGroup.getAllowedGroupIds()).thenReturn(allowedGroupIds);
        List<String> allowedGroupNames = new ArrayList<>();
        allowedGroupNames.add("group1");
        when(userGroup.getAllowedGroupNames()).thenReturn(allowedGroupNames);

        Set<String> groupsName = userService.extractGroupRolesFromAccessToken(accessToken);
        assertThat(groupsName).contains("ROLE_group1");
        assertThat(groupsName).contains("ROLE_d07c0bd6-4aab-45ac-b87c-23e8d00194ab");
        assertThat(groupsName).hasSize(2);
    }

    class GraphClientTest extends GraphClient {

        GraphClientTest(AADAuthenticationProperties properties) {
            super(properties);
        }

        public Set<String> getGroupsFromGraph(String accessToken) {
            final Set<String> groups = new LinkedHashSet<>();
            final ObjectMapper objectMapper = JacksonObjectMapperFactory.getInstance();
            String aadMembershipRestUri = properties.getGraphMembershipUri();
            while (aadMembershipRestUri != null) {
                Memberships memberships;
                try {
                    String membershipsJson = getUserMemberships();
                    memberships = objectMapper.readValue(membershipsJson, Memberships.class);
                } catch (IOException ioException) {
                    break;
                }
                memberships.getValue()
                           .stream()
                           .filter(this::isGroupObject)
                           .map(Membership::getDisplayName)
                           .forEach(groups::add);
                memberships.getValue()
                           .stream()
                           .filter(this::isGroupObject)
                           .map(Membership::getObjectID)
                           .forEach(groups::add);
                aadMembershipRestUri = Optional.of(memberships)
                                               .map(Memberships::getOdataNextLink)
                                               .orElse(null);
            }
            return groups;
        }

        public String getUserMemberships() {
            return membershipJson;
        }

        public boolean isGroupObject(final Membership membership) {
            return membership.getObjectType().equals(Membership.OBJECT_TYPE_GROUP);
        }
    }
}
