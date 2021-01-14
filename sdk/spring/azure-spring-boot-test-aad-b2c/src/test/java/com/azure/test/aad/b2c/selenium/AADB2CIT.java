// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.b2c.selenium;

import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_PROFILE_EDIT;
import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_SIGN_UP_OR_SIGN_IN;
import static com.azure.test.aad.b2c.selenium.AADB2CSeleniumITHelper.createDefaultProperteis;

import com.azure.spring.autoconfigure.b2c.AADB2COidcLoginConfigurer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

public class AADB2CIT {

    private final String JOB_TITLE_A_WORKER = "a worker";
    private final String JOB_TITLE_WORKER = "worker";
    private AADB2CSeleniumITHelper aadb2cSeleniumITHelper;

    @Before
    public void initAndSignIn() {
        aadb2cSeleniumITHelper = new AADB2CSeleniumITHelper(DumbApp.class, createDefaultProperteis());
        aadb2cSeleniumITHelper.logIn();
    }

    @Test
    public void testSignIn() {
        String name = aadb2cSeleniumITHelper.getName();
        String userFlowName = aadb2cSeleniumITHelper.getUserFlowName();
        Assert.assertNotNull(name);
        Assert.assertNotNull(userFlowName);
        Assert.assertEquals(AAD_B2C_SIGN_UP_OR_SIGN_IN, userFlowName);
    }

    @Test
    public void testProfileEdit() {
        aadb2cSeleniumITHelper.profileEditJobTitle(JOB_TITLE_A_WORKER);
        String currentJobTitle = aadb2cSeleniumITHelper.getJobTitle();
        String newJobTitle = JOB_TITLE_A_WORKER.equals(currentJobTitle) ? JOB_TITLE_WORKER : JOB_TITLE_A_WORKER;
        aadb2cSeleniumITHelper.profileEditJobTitle(newJobTitle);
        String name = aadb2cSeleniumITHelper.getName();
        String jobTitle = aadb2cSeleniumITHelper.getJobTitle();
        String userFlowName = aadb2cSeleniumITHelper.getUserFlowName();
        Assert.assertNotNull(name);
        Assert.assertNotNull(jobTitle);
        Assert.assertEquals(newJobTitle, jobTitle);
        Assert.assertEquals(AAD_B2C_PROFILE_EDIT, userFlowName);
    }

    @Test
    public void testLogOut() {
        aadb2cSeleniumITHelper.logout();
        String signInButtonText = aadb2cSeleniumITHelper.getSignInButtonText();
        Assert.assertEquals("Sign in", signInButtonText);
    }

    @After
    public void destroy() {
        aadb2cSeleniumITHelper.destroy();
    }

    @EnableWebSecurity
    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @Controller
    public static class DumbApp extends WebSecurityConfigurerAdapter {

        private final AADB2COidcLoginConfigurer configurer;

        public DumbApp(AADB2COidcLoginConfigurer configurer) {
            this.configurer = configurer;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .apply(configurer);
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
        }
    }
}
