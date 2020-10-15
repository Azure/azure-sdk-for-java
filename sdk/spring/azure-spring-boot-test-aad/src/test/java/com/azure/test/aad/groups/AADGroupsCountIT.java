// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.groups;

import com.azure.spring.autoconfigure.aad.AADAuthenticationFilter;
import com.azure.spring.autoconfigure.aad.UserPrincipal;
import com.azure.test.oauth.OAuthResponse;
import com.azure.test.oauth.OAuthUtils;
import com.azure.test.utils.AppRunner;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import static com.azure.test.oauth.OAuthUtils.AAD_SINGLE_TENANT_CLIENT_ID;
import static com.azure.test.oauth.OAuthUtils.AAD_SINGLE_TENANT_CLIENT_SECRET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AADGroupsCountIT {

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    public void testGroupsCount() {
        final String clientId = System.getenv(AAD_SINGLE_TENANT_CLIENT_ID);
        final String clientSecret = System.getenv(AAD_SINGLE_TENANT_CLIENT_SECRET);
        final OAuthResponse authResponse = OAuthUtils.executeOAuth2ROPCFlow(clientId, clientSecret);
        assertNotNull(authResponse);
        String idToken = authResponse.getIdToken();
        try (AppRunner app = new AppRunner(DumbApp.class)) {
            app.property("azure.activedirectory.client-id", clientId);
            app.property("azure.activedirectory.client-secret", clientSecret);
            app.property("azure.activedirectory.user-group.allowed-groups", "group1,group2");
            app.start();
            final HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", String.format("Bearer %s", idToken));
            HttpEntity<Object> entity = new HttpEntity<>(headers);
            final ResponseEntity<String> response = restTemplate.exchange(
                app.root() + "api/groupsCount",
                HttpMethod.GET,
                entity,
                String.class,
                new HashMap<>()
            );
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("111", response.getBody());
        }
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp extends WebSecurityConfigurerAdapter {

        @Autowired
        private AADAuthenticationFilter aadAuthenticationFilter;

        @Override
        protected void configure(HttpSecurity http) {
            http.addFilterBefore(aadAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        }

        @GetMapping(value = "api/groupsCount")
        public ResponseEntity<String> groupsCount(PreAuthenticatedAuthenticationToken authToken) {
            String groupsCount = Optional.of(authToken)
                                         .map(PreAuthenticatedAuthenticationToken::getPrincipal)
                                         .map(p -> (UserPrincipal) p)
                                         .map(UserPrincipal::getGroups)
                                         .map(Set::size)
                                         .map(String::valueOf)
                                         .orElse("");
            return new ResponseEntity<>(groupsCount, HttpStatus.OK);
        }
    }

}
