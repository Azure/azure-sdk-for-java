// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.implementation.webapp;

import com.azure.spring.cloud.autoconfigure.aad.properties.AadAuthenticationProperties;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.azure.spring.cloud.autoconfigure.aad.implementation.constants.AadJwtClaimNames.ROLES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AadIdTokenRolesExtractionTests {

    private AadOAuth2UserService getUserService() {
        return new AadOAuth2UserService(mock(AadAuthenticationProperties.class), new RestTemplateBuilder());
    }

    @Test
    void testNoRolesClaim() {
        OidcIdToken idToken = mock(OidcIdToken.class);
        when(idToken.getClaim(ROLES)).thenReturn(null);
        Set<String> authorityStrings = getUserService().extractRolesFromIdToken(idToken);
        assertThat(authorityStrings).hasSize(0);
    }

    @Test
    void testRolesClaimAsList() {
        OidcIdToken idToken = mock(OidcIdToken.class);
        JSONArray rolesClaim = new JSONArray().appendElement("Admin");
        when(idToken.getClaim(ROLES)).thenReturn(rolesClaim);
        Set<String> authorityStrings = getUserService().extractRolesFromIdToken(idToken);
        assertThat(authorityStrings).hasSize(1);
    }

    @Test
    void testRolesClaimIllegal() {
        OidcIdToken idToken = mock(OidcIdToken.class);
        Set<String> rolesClaim = new HashSet<>(Collections.singletonList("Admin"));
        when(idToken.getClaim(ROLES)).thenReturn(rolesClaim);
        Set<String> authorityStrings = getUserService().extractRolesFromIdToken(idToken);
        assertThat(authorityStrings).hasSize(0);
    }
}
