/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.test.keyvault.approle;

import com.microsoft.azure.spring.autoconfigure.aad.AADAppRoleStatelessAuthenticationFilter;
import com.microsoft.azure.test.AppRunner;
import com.microsoft.azure.test.oauth.OAuthResponse;
import com.microsoft.azure.test.oauth.OAuthUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

import static com.microsoft.azure.test.oauth.OAuthUtils.AAD_CLIENT_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Slf4j
public class AADAppRoleStatelessAuthenticationFilterIT {

    private RestTemplate restTemplate = new RestTemplate();

    @Test
    @Ignore
    public void testAADAppRoleStatelessAuthenticationFilter() {
        final OAuthResponse authResponse = OAuthUtils.executeOAuth2ROPCFlow();
        assertNotNull(authResponse);

        try (AppRunner app = new AppRunner(DumbApp.class)) {

            app.property("azure.activedirectory.client-id", System.getenv(AAD_CLIENT_ID));
            app.property("azure.activedirectory.session-stateless", "true");

            app.start();

            final ResponseEntity<String> response = restTemplate.exchange(app.root() + "public",
                    HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class, new HashMap<>());
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("public endpoint response", response.getBody());

            try {
                restTemplate.exchange(app.root() + "authorized",
                        HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), String.class, new HashMap<>());
            } catch (Exception e) {
                assertEquals(HttpClientErrorException.Forbidden.class, e.getClass());
            }

            final HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", String.format("Bearer %s", authResponse.getIdToken()));
            final HttpEntity entity = new HttpEntity(headers);

            final ResponseEntity<String> response2 = restTemplate.exchange(app.root() + "authorized",
                    HttpMethod.GET, entity, String.class, new HashMap<>());
            assertEquals(HttpStatus.OK, response2.getStatusCode());
            assertEquals("authorized endpoint response", response2.getBody());

            final ResponseEntity<String> response3 = restTemplate.exchange(app.root() + "admin/demo",
                    HttpMethod.GET, entity, String.class, new HashMap<>());
            assertEquals(HttpStatus.OK, response3.getStatusCode());
            assertEquals("admin endpoint response", response3.getBody());

            app.close();
            log.info("--------------------->test over");
        }
    }

    @EnableGlobalMethodSecurity(prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp extends WebSecurityConfigurerAdapter {

        @Autowired
        private AADAppRoleStatelessAuthenticationFilter aadAuthFilter;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable();

            http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER);

            http.authorizeRequests()
                    .antMatchers("/admin/**").hasRole("Admin")
                    .antMatchers("/", "/index.html", "/public").permitAll()
                    .anyRequest().authenticated();

            http.addFilterBefore(aadAuthFilter, UsernamePasswordAuthenticationFilter.class);
        }

        @GetMapping("/public")
        public String publicMethod() {
            return "public endpoint response";
        }

        @GetMapping("/authorized")
        @PreAuthorize("hasRole('ROLE_User')")
        public String onlyAuthorizedUsers() {
            return "authorized endpoint response";
        }

        @GetMapping("/admin/demo")
        public String onlyForAdmins() {
            return "admin endpoint response";
        }
    }

}
