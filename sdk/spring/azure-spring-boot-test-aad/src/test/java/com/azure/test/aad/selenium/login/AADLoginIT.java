// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.login;

import com.azure.test.aad.selenium.SeleniumTestUtils;
import com.azure.test.utils.AppRunner;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AADLoginIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADLoginIT.class);

    @Test
    public void loginTest() {

        try (AppRunner app = new AppRunner(DumbApp.class)) {
            SeleniumTestUtils.addProperty(app);
            List<String> endPoints = new ArrayList<>();
            endPoints.add("api/home");
            endPoints.add("api/group1");
            endPoints.add("api/status403");
            Map<String, String> result = SeleniumTestUtils.get(app, endPoints);
            Assert.assertEquals("home", result.get("api/home"));
            Assert.assertEquals("group1", result.get("api/group1"));
            Assert.assertNotEquals("error", result.get("api/status403"));
        }


    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp {

        @PreAuthorize("hasRole('ROLE_group1')")
        @GetMapping(value = "/api/group1")
        public ResponseEntity<String> group1() {
            return ResponseEntity.ok("group1");
        }

        @GetMapping(value = "/api/home")
        public ResponseEntity<String> home(Principal principal) {
            LOGGER.info(((OAuth2AuthenticationToken) principal).getAuthorities().toString());
            return ResponseEntity.ok("home");
        }

        @PreAuthorize("hasRole('ROLE_fdsaliieammQiovlikIOWssIEURsafjFelasdfe')")
        @GetMapping(value = "/api/status403")
        public ResponseEntity<String> status403() {
            return ResponseEntity.ok("error");
        }
    }
}
