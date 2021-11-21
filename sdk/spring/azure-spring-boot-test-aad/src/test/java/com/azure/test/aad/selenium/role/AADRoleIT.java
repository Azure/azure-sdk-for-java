// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.role;

import com.azure.test.aad.common.AADSeleniumITHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.AAD_SINGLE_TENANT_CLIENT_ID_WITH_ROLE;
import static com.azure.spring.test.EnvironmentVariable.AAD_SINGLE_TENANT_CLIENT_SECRET_WITH_ROLE;
import static com.azure.test.aad.selenium.AADITHelper.createDefaultProperties;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AADRoleIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADRoleIT.class);
    private AADSeleniumITHelper aadSeleniumITHelper;

    @Test
    public void roleTest() {
        Map<String, String> properties = createDefaultProperties();
        properties.put("azure.activedirectory.client-id", AAD_SINGLE_TENANT_CLIENT_ID_WITH_ROLE);
        properties.put("azure.activedirectory.client-secret", AAD_SINGLE_TENANT_CLIENT_SECRET_WITH_ROLE);
        aadSeleniumITHelper = new AADSeleniumITHelper(DumbApp.class, properties);
        aadSeleniumITHelper.logIn();
        String httpResponse = aadSeleniumITHelper.httpGet("api/home");
        Assertions.assertTrue(httpResponse.contains("home"));
        httpResponse = aadSeleniumITHelper.httpGet("api/group1");
        Assertions.assertTrue(httpResponse.contains("group1"));
        httpResponse = aadSeleniumITHelper.httpGet("api/user");
        Assertions.assertTrue(httpResponse.contains("user"));
        httpResponse = aadSeleniumITHelper.httpGet("api/group_fdsaliieammQiovlikIOWssIEURsafjFelasdfe");
        Assertions.assertNotEquals(httpResponse, "group_fdsaliieammQiovlikIOWssIEURsafjFelasdfe");
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

        @PreAuthorize("hasRole('ROLE_group1')")
        @GetMapping(value = "/api/group1")
        public ResponseEntity<String> group1() {
            return ResponseEntity.ok("group1");
        }

        @PreAuthorize("hasAuthority('APPROLE_User')")
        @GetMapping(value = "/api/user")
        public ResponseEntity<String> user() {
            return ResponseEntity.ok("user");
        }

        @PreAuthorize("hasRole('ROLE_fdsaliieammQiovlikIOWssIEURsafjFelasdfe')")
        @GetMapping(value = "/api/group_fdsaliieammQiovlikIOWssIEURsafjFelasdfe")
        public ResponseEntity<String> nonExistGroup() {
            return ResponseEntity.ok("group_fdsaliieammQiovlikIOWssIEURsafjFelasdfe");
        }
    }
}
