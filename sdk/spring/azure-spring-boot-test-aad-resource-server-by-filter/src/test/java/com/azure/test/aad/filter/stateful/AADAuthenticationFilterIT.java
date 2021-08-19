// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.filter.stateful;

import com.azure.spring.autoconfigure.aad.AADAuthenticationFilter;
import com.azure.spring.test.aad.AADWebApiITHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.test.Constant.toFullNameScope;
import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.AAD_MULTI_TENANT_CLIENT_SECRET;
import static com.azure.spring.test.EnvironmentVariable.AAD_SINGLE_TENANT_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.AAD_SINGLE_TENANT_CLIENT_SECRET;
import static com.azure.spring.test.EnvironmentVariable.AAD_TENANT_ID_1;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AADAuthenticationFilterIT {
    public static final String SCOPE_GRAPH_READ = "ResourceAccessGraph.Read";

    private AADWebApiITHelper singleTenantITHelper;
    private AADWebApiITHelper multiTenantITHelper;

    @BeforeAll
    public void beforeAll() {
        singleTenantITHelper = getAADWebApiITHelper(AAD_SINGLE_TENANT_CLIENT_ID, AAD_SINGLE_TENANT_CLIENT_SECRET);
        multiTenantITHelper = getAADWebApiITHelper(AAD_MULTI_TENANT_CLIENT_ID, AAD_MULTI_TENANT_CLIENT_SECRET);
    }

    private AADWebApiITHelper getAADWebApiITHelper(String clientId, String clientSecret) {
        Map<String, String> properties = new HashMap<>();
        properties.put("azure.activedirectory.tenant-id", AAD_TENANT_ID_1);
        properties.put("azure.activedirectory.client-id", clientId);
        properties.put("azure.activedirectory.client-secret", clientSecret);
        properties.put("azure.activedirectory.user-group.allowed-groups", "group1");
        return new AADWebApiITHelper(
            DumbApp.class,
            properties,
            clientId,
            clientSecret,
            Collections.singletonList(toFullNameScope(clientId, SCOPE_GRAPH_READ)));
    }

    @Test
    public void testAllowedEndpointsForSingleTenantByAccessToken() {
        assertEquals("home", singleTenantITHelper.httpGetStringByAccessToken("home"));
        assertEquals("api/all", singleTenantITHelper.httpGetStringByAccessToken("api/all"));
        assertEquals("api/group1", singleTenantITHelper.httpGetStringByAccessToken("api/group1"));
    }

    @Test
    public void testAllowedEndpointsForSingleTenantByCookie() {
        assertEquals("home",
            singleTenantITHelper.getCookieAndAccessByCookie("home", "home"));
        assertEquals("api/all",
            singleTenantITHelper.getCookieAndAccessByCookie("home", "api/all"));
        assertEquals("api/group1",
            singleTenantITHelper.getCookieAndAccessByCookie("home", "api/group1"));
    }

    @Test
    public void testNotAllowedEndpointsForSingleTenant() {
        Assertions.assertThrows(HttpClientErrorException.class,
            () -> singleTenantITHelper.httpGetStringByAccessToken("api/group2"));
    }

    @Test
    public void testAllowedEndpointsForMultiTenantByAccessToken() {
        assertEquals("home", multiTenantITHelper.httpGetStringByAccessToken("home"));
        assertEquals("api/all", multiTenantITHelper.httpGetStringByAccessToken("api/all"));
        assertEquals("api/group1", multiTenantITHelper.httpGetStringByAccessToken("api/group1"));
    }

    @Test
    public void testAllowedEndpointsForMultipleTenantByCookie() {
        assertEquals("home",
            multiTenantITHelper.getCookieAndAccessByCookie("home", "home"));
        assertEquals("api/all",
            multiTenantITHelper.getCookieAndAccessByCookie("home", "api/all"));
        assertEquals("api/group1",
            multiTenantITHelper.getCookieAndAccessByCookie("home", "api/group1"));
    }

    @Test
    public void testNotAllowedEndpointsForMultiTenant() {
        Assertions.assertThrows(HttpClientErrorException.class,
            () -> multiTenantITHelper.httpGetStringByAccessToken("api/group2"));
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp extends WebSecurityConfigurerAdapter {

        @Autowired
        private AADAuthenticationFilter aadAuthFilter;

        @Override
        protected void configure(HttpSecurity http) throws Exception {

            http.authorizeRequests().antMatchers("home").permitAll();
            http.authorizeRequests().antMatchers("api/**").authenticated();

            http.logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/").deleteCookies("JSESSIONID").invalidateHttpSession(true);

            http.authorizeRequests().anyRequest().permitAll();

            http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());

            http.addFilterBefore(aadAuthFilter, UsernamePasswordAuthenticationFilter.class);
        }

        @GetMapping(value = "home")
        public ResponseEntity<String> getHome() {
            return new ResponseEntity<>("home", HttpStatus.OK);
        }

        @GetMapping(value = "api/all")
        public ResponseEntity<String> getAll() {
            return new ResponseEntity<>("api/all", HttpStatus.OK);
        }

        @PreAuthorize("hasRole('ROLE_group1')")
        @GetMapping(value = "api/group1")
        public ResponseEntity<String> getRoleGroup1() {
            return new ResponseEntity<>("api/group1", HttpStatus.OK);
        }

        @PreAuthorize("hasRole('ROLE_group2')")
        @GetMapping(value = "api/group2")
        public ResponseEntity<String> getRoleGroup2() {
            return new ResponseEntity<>("api/group2", HttpStatus.OK);
        }
    }

}
