package com.azure.test.aad.selenium.groupid.role;


import com.azure.test.aad.selenium.AADSeleniumITHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.AAD_SINGLE_TENANT_CLIENT_ID_WITH_ROLE;
import static com.azure.spring.test.EnvironmentVariable.AAD_SINGLE_TENANT_CLIENT_SECRET_WITH_ROLE;
import static com.azure.test.aad.selenium.AADSeleniumITHelper.createDefaultProperties;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AADGroupIdRoleIT {
    private AADSeleniumITHelper aadSeleniumITHelper;

    @Test
    public void roleTest() {
        Map<String, String> properties = createDefaultProperties();
        properties.put("azure.activedirectory.user-group.enable-group-id", "true");
        properties.put("azure.activedirectory.client-id", AAD_SINGLE_TENANT_CLIENT_ID_WITH_ROLE);
        properties.put("azure.activedirectory.client-secret", AAD_SINGLE_TENANT_CLIENT_SECRET_WITH_ROLE);
        aadSeleniumITHelper = new AADSeleniumITHelper(DumbApp.class, properties);
        aadSeleniumITHelper.logIn();
        String httpResponse = aadSeleniumITHelper.httpGet("api/group1");
        Assertions.assertTrue(httpResponse.contains("group1"));
    }

    @AfterAll
    public void destroy() {
        aadSeleniumITHelper.destroy();
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp {
        @PreAuthorize("hasRole('ROLE_ebc90e49-2b23-45fc-8b9c-ba8ffc605ff9')")
        @GetMapping(value = "/api/group1")
        public ResponseEntity<String> group1() {
            return ResponseEntity.ok("group1");
        }
    }
}
