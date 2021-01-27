// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium.ondemand;

import org.junit.After;
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
import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.AAD_USER_NAME_ON_DEMAND;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_NAME_ON_DEMAND_FAKE;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_PASSWORD_1;

public class AADOnDemandIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(com.azure.test.aad.selenium.ondemand.AADOnDemandIT.class);
//    private AADSeleniumITHelper aadSeleniumITHelper;

    @Test
    public void onDemandTest() {
        Map<String, String> properties = new HashMap<>();
        properties.put("azure.activedirectory.client-id", AAD_USER_NAME_ON_DEMAND);
        properties.put("azure.activedirectory.client-secret", AAD_USER_PASSWORD_1);
        String onDemandUser1 = AAD_USER_NAME_ON_DEMAND;
        LOGGER.info(onDemandUser1);
        String onDemandUser2 = AAD_USER_NAME_ON_DEMAND_FAKE;
        LOGGER.info(onDemandUser2);

        String username1 = onDemandUser1.split("@")[0];
        LOGGER.info(username1);
        String username2 = onDemandUser2.split("@")[0];
        LOGGER.info(username2);

        //        aadSeleniumITHelper = new AADSeleniumITHelper(com.azure.test.aad.selenium.ondemand.AADOnDemandIT.class, properties,
//            AAD_USER_NAME_2, AAD_USER_PASSWORD_2);
//        aadSeleniumITHelper.logIn();
//
//        String httpResponse = aadSeleniumITHelper.httpGet("api/home");
//        Assert.assertTrue(httpResponse.contains("home"));
    }

    @After
    public void destroy() {
//        aadSeleniumITHelper.destroy();
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