// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.conditional.access.webapi.b;

import com.azure.spring.test.AppRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.CONDITIONAL_ACCESS_POLICY_TEST_WEB_API_B_CLIENT_ID;

public class ConditionalAccessPolicyTestWebApiB {
    private static void start() {
        Map<String, String> properties = new HashMap<>();
        properties.put("azure.activedirectory.client-id", CONDITIONAL_ACCESS_POLICY_TEST_WEB_API_B_CLIENT_ID);
        properties.put("azure.activedirectory.app-id-uri", "api://" + CONDITIONAL_ACCESS_POLICY_TEST_WEB_API_B_CLIENT_ID);
        properties.put("server.port", "8883");
        AppRunner app = new AppRunner(DumbApp.class);
        properties.forEach(app::property);
        app.start();
    }

    public static void main(String[] args) {
        start();
    }

    @EnableWebSecurity
    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt);
        }

        @GetMapping("/webapiB")
        @PreAuthorize("hasAuthority('SCOPE_File.Read')")
        public String file() {
            return "Response from webapiB.";
        }
    }
}
