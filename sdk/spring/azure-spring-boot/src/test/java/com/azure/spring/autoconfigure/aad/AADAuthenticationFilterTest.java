// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.aad;

import com.azure.spring.aad.AADAuthorizationServerEndpoints;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.proc.BadJOSEException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AADAuthenticationFilterTest {
    private static final String TOKEN = "dummy-token";
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AADAuthenticationFilterAutoConfiguration.class));
    private final UserPrincipalManager userPrincipalManager;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final AADAuthenticationFilter filter;

    public AADAuthenticationFilterTest() {
        userPrincipalManager = mock(UserPrincipalManager.class);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filter = new AADAuthenticationFilter(
            mock(AADAuthenticationProperties.class),
            mock(AADAuthorizationServerEndpoints.class),
            userPrincipalManager
        );
    }

    //TODO (Zhou Liu): current test case is out of date, a new test case need to cover here, do it later.
    @Test
    @Disabled
    public void doFilterInternal() {
        this.contextRunner.withPropertyValues("azure.activedirectory.client-id", TestConstants.CLIENT_ID)
                .withPropertyValues("azure.activedirectory.client-secret", TestConstants.CLIENT_SECRET)
                .withPropertyValues("azure.activedirectory.client-secret",
                        TestConstants.TARGETED_GROUPS.toString()
                                                     .replace("[", "").replace("]", ""));

        this.contextRunner.run(context -> {
            final HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader(TestConstants.TOKEN_HEADER)).thenReturn(TestConstants.BEARER_TOKEN);

            final HttpServletResponse response = mock(HttpServletResponse.class);
            final FilterChain filterChain = mock(FilterChain.class);


            final AADAuthenticationFilter azureADJwtTokenFilter = context.getBean(AADAuthenticationFilter.class);
            assertThat(azureADJwtTokenFilter).isNotNull();
            assertThat(azureADJwtTokenFilter).isExactlyInstanceOf(AADAuthenticationFilter.class);

            azureADJwtTokenFilter.doFilterInternal(request, response, filterChain);

            final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            assertThat(authentication.getPrincipal()).isNotNull();
            assertThat(authentication.getPrincipal()).isExactlyInstanceOf(UserPrincipal.class);
            assertThat(authentication.getAuthorities()).isNotNull();
            assertThat(authentication.getAuthorities().size()).isEqualTo(2);
            assertThat(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_group1"))
                    && authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_group2"))
            ).isTrue();

            final UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            assertThat(principal.getIssuer()).isNotNull().isNotEmpty();
            assertThat(principal.getKid()).isNotNull().isNotEmpty();
            assertThat(principal.getSubject()).isNotNull().isNotEmpty();

            assertThat(principal.getClaims()).isNotNull().isNotEmpty();
            final Map<String, Object> claims = principal.getClaims();
            assertThat(claims.get("iss")).isEqualTo(principal.getIssuer());
            assertThat(claims.get("sub")).isEqualTo(principal.getSubject());
        });
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
