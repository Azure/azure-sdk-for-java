// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.test.aad.selenium.groupid.role;


import com.azure.test.aad.common.AADSeleniumITHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.AAD_SINGLE_TENANT_CLIENT_ID_WITH_ROLE;
import static com.azure.spring.test.EnvironmentVariable.AAD_SINGLE_TENANT_CLIENT_SECRET_WITH_ROLE;
import static com.azure.spring.test.EnvironmentVariable.AZURE_CLOUD_TYPE;
import static com.azure.test.aad.selenium.AADITHelper.createDefaultProperties;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AADGroupIdRoleIT {
    private AADSeleniumITHelper aadSeleniumITHelper;

    @Test
    public void roleTest() {
        Map<String, String> properties = createDefaultProperties();
        properties.put("azure.activedirectory.client-id", AAD_SINGLE_TENANT_CLIENT_ID_WITH_ROLE);
        properties.put("azure.activedirectory.client-secret", AAD_SINGLE_TENANT_CLIENT_SECRET_WITH_ROLE);
        if("Global".equals(AZURE_CLOUD_TYPE)){
            properties.put("azure.activedirectory.user-group.allowed-group-ids", "ebc90e49-2b23-45fc-8b9c-ba8ffc605ff9");
        }else{
            properties.put("azure.activedirectory.user-group.allowed-group-ids", "baeef6a1-4bcb-432a-975a-e9e0ad4ad16c");
        }

        aadSeleniumITHelper = new AADSeleniumITHelper(DumbApp.class, properties);
        aadSeleniumITHelper.logIn();
        if("Global".equals(AZURE_CLOUD_TYPE)){
            String httpResponse = aadSeleniumITHelper.httpGet("api/group1");
            Assertions.assertTrue(httpResponse.contains("group1"));
        }else{
            String httpResponse = aadSeleniumITHelper.httpGet("api/group2");
            Assertions.assertTrue(httpResponse.contains("group2"));
        }
    }

    @AfterAll
    public void destroy() {
        aadSeleniumITHelper.destroy();
    }

    @SpringBootApplication
    @RestController
    public static class DumbApp {
        @PreAuthorize("hasRole('ROLE_ebc90e49-2b23-45fc-8b9c-ba8ffc605ff9')")
        @GetMapping(value = "/api/group1")
        public ResponseEntity<String> group1() {
            return ResponseEntity.ok("group1");
        }

        @PreAuthorize("hasRole('ROLE_baeef6a1-4bcb-432a-975a-e9e0ad4ad16c')")
        @GetMapping(value = "/api/group2")
        public ResponseEntity<String> group2() {
            return ResponseEntity.ok("group2");
        }
    }
}
