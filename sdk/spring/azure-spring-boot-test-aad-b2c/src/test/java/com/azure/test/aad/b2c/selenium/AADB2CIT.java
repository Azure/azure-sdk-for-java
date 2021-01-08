// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.b2c.selenium;

import com.azure.spring.autoconfigure.b2c.AADB2COidcLoginConfigurer;
import com.azure.test.aad.b2c.utils.AADB2CTestUtils;
import java.util.Collections;
import org.junit.Assert;
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

    @Test
    public void testSignIn() throws InterruptedException {
        AADB2CSeleniumITHelper AADB2CSeleniumITHelper = new AADB2CSeleniumITHelper(DumbApp.class,
            Collections.emptyMap());
        String name = AADB2CSeleniumITHelper.getName();
        String userFlowName = AADB2CSeleniumITHelper.getUserFlowName();

        Assert.assertNotNull(name);
        Assert.assertNotNull(userFlowName);
        Assert.assertEquals(AADB2CTestUtils.AAD_B2C_SIGN_UP_OR_SIGN_IN, userFlowName);
    }

    @Test
    public void testProfileEdit() throws InterruptedException {
        AADB2CSeleniumITHelper AADB2CSeleniumITHelper = new AADB2CSeleniumITHelper(DumbApp.class,
            Collections.emptyMap());
        String currentJobTitle = AADB2CSeleniumITHelper.getJobTitle();
        String newJobTitle = JOB_TITLE_A_WORKER.equals(currentJobTitle) ? JOB_TITLE_WORKER : JOB_TITLE_A_WORKER;
        AADB2CSeleniumITHelper.profileEditJobTitle(newJobTitle);
        String name = AADB2CSeleniumITHelper.getName();
        String jobTitle = AADB2CSeleniumITHelper.getJobTitle();
        String userFlowName = AADB2CSeleniumITHelper.getUserFlowName();

        Assert.assertNotNull(name);
        Assert.assertNotNull(jobTitle);
        Assert.assertEquals(newJobTitle, jobTitle);
        Assert.assertEquals(AADB2CTestUtils.AAD_B2C_PROFILE_EDIT, userFlowName);
    }

    @Test
    public void testLogOut() throws InterruptedException {
        AADB2CSeleniumITHelper AADB2CSeleniumITHelper = new AADB2CSeleniumITHelper(DumbApp.class,
            Collections.emptyMap());
        String signInButtonText = AADB2CSeleniumITHelper.logoutAndGetSignInButtonText();
        Assert.assertEquals("Sign in", signInButtonText);
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
