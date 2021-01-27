// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.role;

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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

public class AADRoleIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADRoleIT.class);
    private AADSeleniumITHelper aadSeleniumITHelper;

    @Test
    public void roleTest() {
        aadSeleniumITHelper = new AADSeleniumITHelper(DumbApp.class, createDefaultProperties());
        aadSeleniumITHelper.logIn();
        String httpResponse = aadSeleniumITHelper.httpGet("api/home");
        Assert.assertTrue(httpResponse.contains("home"));
        httpResponse = aadSeleniumITHelper.httpGet("api/group1");
        Assert.assertTrue(httpResponse.contains("group1"));
        httpResponse = aadSeleniumITHelper.httpGet("api/group_fdsaliieammQiovlikIOWssIEURsafjFelasdfe");
        Assert.assertNotEquals(httpResponse, "group_fdsaliieammQiovlikIOWssIEURsafjFelasdfe");
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

        @PreAuthorize("hasRole('ROLE_group1')")
        @GetMapping(value = "/api/group1")
        public ResponseEntity<String> group1() {
            return ResponseEntity.ok("group1");
        }

        @PreAuthorize("hasRole('ROLE_fdsaliieammQiovlikIOWssIEURsafjFelasdfe')")
        @GetMapping(value = "/api/group_fdsaliieammQiovlikIOWssIEURsafjFelasdfe")
        public ResponseEntity<String> nonExistGroup() {
            return ResponseEntity.ok("group_fdsaliieammQiovlikIOWssIEURsafjFelasdfe");
        }
    }
}
