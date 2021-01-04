// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.logout;

import com.azure.test.aad.selenium.AADSeleniumITHelper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Collections;

public class AADLogoutIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADLogoutIT.class);

    @Test
    public void logoutTest() throws InterruptedException {
        AADSeleniumITHelper aadSeleniumITHelper = new AADSeleniumITHelper(DumbApp.class, Collections.emptyMap());
        aadSeleniumITHelper.logoutTest();
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
