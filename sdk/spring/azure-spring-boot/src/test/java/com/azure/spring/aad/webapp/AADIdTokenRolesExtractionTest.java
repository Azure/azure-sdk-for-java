// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.aad.webapp;

import com.azure.spring.autoconfigure.aad.AADAuthenticationProperties;
import net.minidev.json.JSONArray;
import org.junit.Test;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.azure.spring.autoconfigure.aad.AADTokenClaim.ROLES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AADIdTokenRolesExtractionTest {

    private OidcIdToken idToken = mock(OidcIdToken.class);
    private AADAuthenticationProperties properties = mock(AADAuthenticationProperties.class);
    private AADOAuth2UserService userService = new AADOAuth2UserService(properties);

    @Test
    public void testNoRolesClaim() {
        when(idToken.getClaim(ROLES)).thenReturn(null);
        Set<String> authorityStrings = userService.extractRolesFromIdToken(idToken);
        assertThat(authorityStrings).hasSize(0);
    }

    @Test
    public void testRolesClaimAsList() {
        JSONArray rolesClaim = new JSONArray().appendElement("Admin");
        when(idToken.getClaim(ROLES)).thenReturn(rolesClaim);
        Set<String> authorityStrings = userService.extractRolesFromIdToken(idToken);
        assertThat(authorityStrings).hasSize(1);
    }

    @Test
    public void testRolesClaimIllegal() {
        Set<String> rolesClaim = new HashSet<>(Arrays.asList("Admin"));
        when(idToken.getClaim(ROLES)).thenReturn(rolesClaim);
        Set<String> authorityStrings = userService.extractRolesFromIdToken(idToken);
        assertThat(authorityStrings).hasSize(0);
    }
}
