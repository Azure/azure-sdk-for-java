// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapp;

import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.azure.spring.aad.implementation.constants.AADTokenClaim.ROLES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AADIdTokenRolesExtractionTest {

    private AADOAuth2UserService getUserService() {
        return new AADOAuth2UserService();
    }

    @Test
    public void testNoRolesClaim() {
        OidcIdToken idToken = mock(OidcIdToken.class);
        when(idToken.getClaim(ROLES)).thenReturn(null);
        Set<String> authorityStrings = getUserService().extractRolesFromIdToken(idToken);
        assertThat(authorityStrings).hasSize(0);
    }

    @Test
    public void testRolesClaimAsList() {
        OidcIdToken idToken = mock(OidcIdToken.class);
        JSONArray rolesClaim = new JSONArray().appendElement("Admin");
        when(idToken.getClaim(ROLES)).thenReturn(rolesClaim);
        Set<String> authorityStrings = getUserService().extractRolesFromIdToken(idToken);
        assertThat(authorityStrings).hasSize(1);
    }

    @Test
    public void testRolesClaimIllegal() {
        OidcIdToken idToken = mock(OidcIdToken.class);
        Set<String> rolesClaim = new HashSet<>(Collections.singletonList("Admin"));
        when(idToken.getClaim(ROLES)).thenReturn(rolesClaim);
        Set<String> authorityStrings = getUserService().extractRolesFromIdToken(idToken);
        assertThat(authorityStrings).hasSize(0);
    }
}
