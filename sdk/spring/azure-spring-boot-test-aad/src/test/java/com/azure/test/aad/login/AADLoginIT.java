// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.login;

import com.azure.test.aad.selenium.AADLoginRunner;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
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

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class AADLoginIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADLoginIT.class);

    @Test
    public void loginTest() {
        AADLoginRunner.build(DumbApp.class).login().run((app, driver, wait) -> {
            AADLoginRunner.EasyTester tester = new AADLoginRunner.EasyTester(app, driver);
            tester.assertEquals("api/home", "home");
            tester.assertEquals("api/group1", "group1");
            tester.assertNotEquals("api/status403", "error");
        });
    }

    @Test
    public void logoutTest() {
        AADLoginRunner.build(DumbApp.class).login().run((app, driver, wait) -> {
            driver.get(app.root() + "logout");
            wait.until(presenceOfElementLocated(By.cssSelector("button[type='submit']"))).click();
            Thread.sleep(10000);
            String cssSelector = "div[data-test-id='" + AADLoginRunner.DEFAULT_USERNAME + "']";
            driver.findElement(By.cssSelector(cssSelector)).click();
            Thread.sleep(10000);
            String id = driver.findElement(By.cssSelector("div[tabindex='0']")).getAttribute("data-test-id");
            Assert.assertEquals(AADLoginRunner.DEFAULT_USERNAME, id);
        });
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
