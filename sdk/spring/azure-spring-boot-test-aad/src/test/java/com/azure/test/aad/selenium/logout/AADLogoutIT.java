// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.logout;

import static com.azure.test.aad.selenium.AADSeleniumITHelper.createDefaultProperties;

import com.azure.test.aad.selenium.AADSeleniumITHelper;
import java.security.Principal;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

public class AADLogoutIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADLogoutIT.class);
    private AADSeleniumITHelper aadSeleniumITHelper;

    @Test
    public void logoutTest() {
        aadSeleniumITHelper = new AADSeleniumITHelper(DumbApp.class, createDefaultProperties());
        aadSeleniumITHelper.logIn();
        String logoutUsername = aadSeleniumITHelper.logoutAndGetLogoutUsername();
        String loginUsername = aadSeleniumITHelper.getUsername();
        Assert.assertEquals(loginUsername, logoutUsername);
    }

    @After
    public void destroy() {
        aadSeleniumITHelper.destroy();
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp {

        @GetMapping(value = "/api/home")
        public ResponseEntity<String> home(Principal principal) {
            LOGGER.info(((OAuth2AuthenticationToken) principal).getAuthorities().toString());
            return ResponseEntity.ok("home");
        }
    }
}
