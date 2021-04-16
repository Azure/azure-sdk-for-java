// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.user.name.attribute;

import com.azure.test.aad.selenium.AADSeleniumITHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.AAD_SINGLE_TENANT_CLIENT_ID_WITH_ROLE;
import static com.azure.spring.test.EnvironmentVariable.AAD_SINGLE_TENANT_CLIENT_SECRET_WITH_ROLE;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_NAME_1;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_PASSWORD_1;
import static com.azure.test.aad.selenium.AADSeleniumITHelper.createDefaultProperties;

public class UserNameAttributeIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserNameAttributeIT.class);
    private AADSeleniumITHelper aadSeleniumITHelper;

    @Test
    public void roleTest() {
        Map<String, String> properties = createDefaultProperties();
        properties.put("azure.activedirectory.client-id", AAD_SINGLE_TENANT_CLIENT_ID_WITH_ROLE);
        properties.put("azure.activedirectory.client-secret", AAD_SINGLE_TENANT_CLIENT_SECRET_WITH_ROLE);
        aadSeleniumITHelper = new AADSeleniumITHelper(DumbApp.class, properties, AAD_USER_NAME_1, AAD_USER_PASSWORD_1);
        aadSeleniumITHelper.logIn();
        String httpResponse = aadSeleniumITHelper.httpGet("api/principalName");
        Assert.assertTrue(httpResponse.contains(AAD_USER_NAME_1));
    }

    @After
    public void destroy() {
        aadSeleniumITHelper.destroy();
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp {

        @GetMapping(value = "/api/principalName")
        public ResponseEntity<String> home(Principal principal) {
            String principalName = principal.getName();
            LOGGER.info(principalName);
            return ResponseEntity.ok(principalName);
        }
    }
}
