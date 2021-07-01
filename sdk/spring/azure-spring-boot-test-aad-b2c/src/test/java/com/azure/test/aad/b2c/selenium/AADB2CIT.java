// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.b2c.selenium;

import com.azure.spring.autoconfigure.b2c.AADB2COidcLoginConfigurer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_PROFILE_EDIT;
import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_SIGN_UP_OR_SIGN_IN;
import static com.azure.test.aad.b2c.selenium.AADB2CSeleniumITHelper.createDefaultProperteis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AADB2CIT {

    private final String JOB_TITLE_A_WORKER = "a worker";
    private final String JOB_TITLE_WORKER = "worker";
    private AADB2CSeleniumITHelper aadB2CSeleniumITHelper;

    @BeforeEach
    public void initAndSignIn() {
        aadB2CSeleniumITHelper = new AADB2CSeleniumITHelper(DumbApp.class, createDefaultProperteis());
        aadB2CSeleniumITHelper.logIn();
    }

    @Test
    public void testSignIn() {
        String name = aadB2CSeleniumITHelper.getName();
        String userFlowName = aadB2CSeleniumITHelper.getUserFlowName();
        assertNotNull(name);
        assertNotNull(userFlowName);
        assertEquals(AAD_B2C_SIGN_UP_OR_SIGN_IN, userFlowName);
    }

    @Test
    public void testProfileEdit() {
        aadB2CSeleniumITHelper.profileEditJobTitle(JOB_TITLE_A_WORKER);
        String currentJobTitle = aadB2CSeleniumITHelper.getJobTitle();
        String newJobTitle = JOB_TITLE_A_WORKER.equals(currentJobTitle) ? JOB_TITLE_WORKER : JOB_TITLE_A_WORKER;
        aadB2CSeleniumITHelper.profileEditJobTitle(newJobTitle);
        String name = aadB2CSeleniumITHelper.getName();
        String jobTitle = aadB2CSeleniumITHelper.getJobTitle();
        String userFlowName = aadB2CSeleniumITHelper.getUserFlowName();
        assertNotNull(name);
        assertNotNull(jobTitle);
        assertEquals(newJobTitle, jobTitle);
        assertEquals(AAD_B2C_PROFILE_EDIT, userFlowName);
    }

    @Test
    public void testLogOut() {
        aadB2CSeleniumITHelper.logout();
        String signInButtonText = aadB2CSeleniumITHelper.getSignInButtonText();
        assertEquals("Sign in", signInButtonText);
    }

    @AfterEach
    public void destroy() {
        aadB2CSeleniumITHelper.destroy();
    }

    @EnableWebSecurity
    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @Controller
    public static class DumbApp extends WebSecurityConfigurerAdapter {

        private final AADB2COidcLoginConfigurer configurer;

        private final String profileEdit;

        public DumbApp(AADB2COidcLoginConfigurer configurer) {
            this.profileEdit = AAD_B2C_PROFILE_EDIT;
            this.configurer = configurer;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            // @formatter:off
            http.authorizeRequests()
                    .anyRequest().authenticated()
                    .and()
                .apply(configurer);
            // @formatter:on
        }

        @GetMapping(value = "/")
        public String index(Model model, OAuth2AuthenticationToken token) {
            initializeModel(model, token);
            return "index";
        }

        private void initializeModel(Model model, OAuth2AuthenticationToken token) {
            if (token != null) {
                final OAuth2User user = token.getPrincipal();
                model.addAttribute("grant_type", user.getAuthorities());
                model.addAllAttributes(user.getAttributes());
            }
            model.addAttribute("aadb2c_profileedit", profileEdit);
        }
    }
}
