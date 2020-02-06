/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AADAuthenticationFilterTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AADAuthenticationFilterAutoConfiguration.class));

    @Before
    @Ignore
    public void beforeEveryMethod() {
        Assume.assumeTrue(!Constants.CLIENT_ID.contains("real_client_id"));
        Assume.assumeTrue(!Constants.CLIENT_SECRET.contains("real_client_secret"));
        Assume.assumeTrue(!Constants.BEARER_TOKEN.contains("real_jtw_bearer_token"));
    }

    //TODO (Zhou Liu): current test case is out of date, a new test case need to cover here, do it later.
    @Test
    @Ignore
    public void doFilterInternal() {
        this.contextRunner.withPropertyValues(Constants.CLIENT_ID_PROPERTY, Constants.CLIENT_ID)
                .withPropertyValues(Constants.CLIENT_SECRET_PROPERTY, Constants.CLIENT_SECRET)
                .withPropertyValues(Constants.TARGETED_GROUPS_PROPERTY,
                        Constants.TARGETED_GROUPS.toString()
                                .replace("[", "").replace("]", ""));

        this.contextRunner.run(context -> {
            final HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader(Constants.TOKEN_HEADER)).thenReturn(Constants.BEARER_TOKEN);

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

}
