/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader.Builder;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.BadJWTException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.minidev.json.JSONArray;
import org.assertj.core.api.Assertions;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class AADAppRoleAuthenticationFilterTest {

    public static final String TOKEN = "dummy-token";

    private final UserPrincipalManager userPrincipalManager;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final SimpleGrantedAuthority roleAdmin;
    private final SimpleGrantedAuthority roleUser;
    private final AADAppRoleStatelessAuthenticationFilter filter;

    private UserPrincipal createUserPrincipal(Collection<String> roles) {
        final JSONArray claims = new JSONArray();
        claims.addAll(roles);
        final JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
            .subject("john doe")
            .claim("roles", claims)
            .build();
        final JWSObject jwsObject = new JWSObject(new Builder(JWSAlgorithm.RS256).build(),
            new Payload(jwtClaimsSet.toString()));
        return new UserPrincipal(jwsObject, jwtClaimsSet);
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
        final UserPrincipal dummyPrincipal = createUserPrincipal(Arrays.asList("user", "admin"));

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TOKEN);
        when(userPrincipalManager.buildUserPrincipal(TOKEN)).thenReturn(dummyPrincipal);

        // Check in subsequent filter that authentication is available!
        final FilterChain filterChain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response)
                throws IOException, ServletException {
                final SecurityContext context = SecurityContextHolder.getContext();
                assertNotNull(context);
                final Authentication authentication = context.getAuthentication();
                assertNotNull(authentication);
                assertTrue("User should be authenticated!", authentication.isAuthenticated());
                assertEquals(dummyPrincipal, authentication.getPrincipal());
                Assertions.assertThat((Collection<SimpleGrantedAuthority>) authentication.getAuthorities())
                    .containsExactlyInAnyOrder(roleAdmin, roleUser);
            }
        };

        filter.doFilterInternal(request, response, filterChain);

        verify(userPrincipalManager).buildUserPrincipal(TOKEN);
        assertNull("Authentication has not been cleaned up!", SecurityContextHolder.getContext().getAuthentication());
    }

    @Test(expected = ServletException.class)
    public void testDoFilterShouldRethrowJWTException()
        throws ParseException, JOSEException, BadJOSEException, ServletException, IOException {

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TOKEN);
        when(userPrincipalManager.buildUserPrincipal(any())).thenThrow(new BadJWTException("bad token"));

        filter.doFilterInternal(request, response, mock(FilterChain.class));
    }

    @Test
    public void testDoFilterAddsDefaultRole()
        throws ParseException, JOSEException, BadJOSEException, ServletException, IOException {

        final UserPrincipal dummyPrincipal = createUserPrincipal(Collections.emptyList());

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + TOKEN);
        when(userPrincipalManager.buildUserPrincipal(TOKEN)).thenReturn(dummyPrincipal);

        // Check in subsequent filter that authentication is available and default roles are filled.
        final FilterChain filterChain = new FilterChain() {
            @Override
            public void doFilter(ServletRequest request, ServletResponse response)
                throws IOException, ServletException {
                final SecurityContext context = SecurityContextHolder.getContext();
                assertNotNull(context);
                final Authentication authentication = context.getAuthentication();
                assertNotNull(authentication);
                assertTrue("User should be authenticated!", authentication.isAuthenticated());
                final SimpleGrantedAuthority expectedDefaultRole = new SimpleGrantedAuthority("ROLE_USER");
                Assertions.assertThat((Collection<SimpleGrantedAuthority>) authentication.getAuthorities())
                    .containsExactlyInAnyOrder(expectedDefaultRole);
            }
        };

        filter.doFilterInternal(request, response, filterChain);

        verify(userPrincipalManager).buildUserPrincipal(TOKEN);
        assertNull("Authentication has not been cleaned up!", SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testRolesToGrantedAuthoritiesShouldConvertRolesAndFilterNulls() {
        final JSONArray roles = new JSONArray().appendElement("user").appendElement(null).appendElement("ADMIN");
        final AADAppRoleStatelessAuthenticationFilter filter = new AADAppRoleStatelessAuthenticationFilter(null);
        final Set<SimpleGrantedAuthority> result = filter.rolesToGrantedAuthorities(roles);
        assertThat("Set should contain the two granted authority 'ROLE_user' and 'ROLE_ADMIN'", result,
            CoreMatchers.hasItems(new SimpleGrantedAuthority("ROLE_user"),
                new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

}
