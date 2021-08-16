// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.logout;

import com.azure.test.aad.common.AADSeleniumITHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

import static com.azure.test.aad.selenium.AADITHelper.createDefaultProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AADLogoutIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADLogoutIT.class);
    private AADSeleniumITHelper aadSeleniumITHelper;

    @Test
    public void logoutTest() {
        aadSeleniumITHelper = new AADSeleniumITHelper(DumbApp.class, createDefaultProperties());
        aadSeleniumITHelper.logIn();
        String logoutUsername = aadSeleniumITHelper.logoutAndGetLogoutUsername();
        String loginUsername = aadSeleniumITHelper.getUsername();
        assertEquals(loginUsername, logoutUsername);
    }

    @AfterAll
    public void destroy() {
        aadSeleniumITHelper.destroy();
    }

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
