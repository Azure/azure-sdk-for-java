// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader.Builder;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import net.minidev.json.JSONArray;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AADAppRoleAuthenticationFilterTest {

    private static final String TOKEN = "dummy-token";

    private final UserPrincipalManager userPrincipalManager;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final SimpleGrantedAuthority roleAdmin;
    private final SimpleGrantedAuthority roleUser;
    private final AADAppRoleStatelessAuthenticationFilter filter;

    private UserPrincipal createUserPrincipal(Set<String> roles) {
        final JSONArray claims = new JSONArray();
        claims.addAll(roles);
        final JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
            .subject("john doe")
            .claim("roles", claims)
            .build();
        final JWSObject jwsObject = new JWSObject(
            new Builder(JWSAlgorithm.RS256).build(),
            new Payload(jwtClaimsSet.toString())
        );
        UserPrincipal userPrincipal = new UserPrincipal("", jwsObject, jwtClaimsSet);
        userPrincipal.setRoles(roles);
        return userPrincipal;
    }

    public AADAppRoleAuthenticationFilterTest() {
        userPrincipalManager = mock(UserPrincipalManager.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        roleAdmin = new SimpleGrantedAuthority("ROLE_admin");
        roleUser = new SimpleGrantedAuthority("ROLE_user");
        filter = new AADAppRoleStatelessAuthenticationFilter(userPrincipalManager);
    }

    @Test
    public void testDoFilterGoodCase()
        throws ParseException, JOSEException, BadJOSEException, ServletException, IOException {
        Set<String> dummyValues = new HashSet<>(2);
        dummyValues.add("user");
        dummyValues.add("admin");
        final UserPrincipal dummyPrincipal = createUserPrincipal(Collections.unmodifiableSet(dummyValues));

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TOKEN);
        when(userPrincipalManager.buildUserPrincipal(TOKEN)).thenReturn(dummyPrincipal);
        when(userPrincipalManager.isTokenIssuedByAAD(TOKEN)).thenReturn(true);

        // Check in subsequent filter that authentication is available!
        final FilterChain filterChain = (request, response) -> {
            final SecurityContext context = SecurityContextHolder.getContext();
            assertNotNull(context);
            final Authentication authentication = context.getAuthentication();
            assertNotNull(authentication);
            assertTrue(authentication.isAuthenticated(), "User should be authenticated!");
            assertEquals(dummyPrincipal, authentication.getPrincipal());

            @SuppressWarnings("unchecked") final Collection<SimpleGrantedAuthority> authorities =
                (Collection<SimpleGrantedAuthority>) authentication
                    .getAuthorities();
            Assertions.assertThat(authorities).containsExactlyInAnyOrder(roleAdmin, roleUser);
        };

        filter.doFilterInternal(request, response, filterChain);

        verify(userPrincipalManager).buildUserPrincipal(TOKEN);
        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication has not been cleaned up!");
    }

    @Test
    public void testBadJWTExceptionReturn401()
        throws ParseException, JOSEException, BadJOSEException, ServletException, IOException {

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TOKEN);
        when(userPrincipalManager.buildUserPrincipal(any())).thenThrow(new BadJWTException("bad token"));
        when(userPrincipalManager.isTokenIssuedByAAD(any())).thenReturn(true);
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        filter.doFilterInternal(request, mockHttpServletResponse, mock(FilterChain.class));
        assertEquals(HttpStatus.UNAUTHORIZED.value(), mockHttpServletResponse.getStatus());
    }

    @Test
    public void testDoFilterAddsDefaultRole()
        throws ParseException, JOSEException, BadJOSEException, ServletException, IOException {

        final UserPrincipal dummyPrincipal = createUserPrincipal(Collections.emptySet());

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TOKEN);
        when(userPrincipalManager.buildUserPrincipal(TOKEN)).thenReturn(dummyPrincipal);
        when(userPrincipalManager.isTokenIssuedByAAD(TOKEN)).thenReturn(true);

        // Check in subsequent filter that authentication is available and default roles are filled.
        final FilterChain filterChain = (request, response) -> {
            final SecurityContext context = SecurityContextHolder.getContext();
            assertNotNull(context);
            final Authentication authentication = context.getAuthentication();
            assertNotNull(authentication);
            assertTrue(authentication.isAuthenticated(), "User should be authenticated!");
            final SimpleGrantedAuthority expectedDefaultRole = new SimpleGrantedAuthority("ROLE_USER");

            @SuppressWarnings("unchecked") final Collection<SimpleGrantedAuthority> authorities =
                (Collection<SimpleGrantedAuthority>) authentication
                    .getAuthorities();
            Assertions.assertThat(authorities).containsExactlyInAnyOrder(expectedDefaultRole);
        };

        filter.doFilterInternal(request, response, filterChain);

        verify(userPrincipalManager).buildUserPrincipal(TOKEN);
        assertNull(SecurityContextHolder.getContext().getAuthentication(), "Authentication has not been cleaned up!");
    }

    @Test
    public void testToSimpleGrantedAuthoritySetWithWhitespaceRole() {
        AADAppRoleStatelessAuthenticationFilter filter = new AADAppRoleStatelessAuthenticationFilter(null);
        UserPrincipal userPrincipal = new UserPrincipal(null, null, null);
        Set<String> roles = new HashSet<>(3);
        roles.add("user");
        roles.add("");
        roles.add("ADMIN");
        userPrincipal.setRoles(Collections.unmodifiableSet(roles));
        Set<SimpleGrantedAuthority> result = filter.toSimpleGrantedAuthoritySet(userPrincipal);
        assertThat(
            "Set should contain the two granted authority 'ROLE_user' and 'ROLE_ADMIN'.",
            result,
            containsInAnyOrder(
                new SimpleGrantedAuthority("ROLE_user"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
            )
        );
    }

    @Test
    public void testToSimpleGrantedAuthoritySetWithNoRole() {
        AADAppRoleStatelessAuthenticationFilter filter = new AADAppRoleStatelessAuthenticationFilter(null);
        UserPrincipal userPrincipal = new UserPrincipal(null, null, null);
        Set<String> roles = Collections.unmodifiableSet(new HashSet<>());
        userPrincipal.setRoles(roles);
        Set<SimpleGrantedAuthority> result = filter.toSimpleGrantedAuthoritySet(userPrincipal);
        assertThat(
            "Set should contain the default authority 'ROLE_USER'.",
            result,
            containsInAnyOrder(
                new SimpleGrantedAuthority("ROLE_USER")
            )
        );
    }

    @Test
    public void testTokenNotIssuedByAAD() throws ServletException, IOException {
        when(userPrincipalManager.isTokenIssuedByAAD(TOKEN)).thenReturn(false);

        final FilterChain filterChain = (request, response) -> {
            final SecurityContext context = SecurityContextHolder.getContext();
            assertNotNull(context);
            final Authentication authentication = context.getAuthentication();
            assertNull(authentication);
        };

        filter.doFilterInternal(request, response, filterChain);
    }

    @Test
    public void testAlreadyAuthenticated() throws ServletException, IOException, ParseException, JOSEException,
        BadJOSEException {
        final Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(userPrincipalManager.isTokenIssuedByAAD(TOKEN)).thenReturn(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final FilterChain filterChain = (request, response) -> {
            final SecurityContext context = SecurityContextHolder.getContext();
            assertNotNull(context);
            assertNotNull(context.getAuthentication());
            SecurityContextHolder.clearContext();
        };

        filter.doFilterInternal(request, response, filterChain);
        verify(userPrincipalManager, times(0)).buildUserPrincipal(TOKEN);

    }

}
