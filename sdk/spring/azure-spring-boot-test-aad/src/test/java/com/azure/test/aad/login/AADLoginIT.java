// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.login;

import com.azure.test.utils.AppRunner;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.azure.test.oauth.OAuthUtils.AAD_MULTI_TENANT_CLIENT_ID;
import static com.azure.test.oauth.OAuthUtils.AAD_MULTI_TENANT_CLIENT_SECRET;
import static com.azure.test.oauth.OAuthUtils.AAD_TENANT_ID_1;
import static com.azure.test.oauth.OAuthUtils.AAD_USER_NAME_1;
import static com.azure.test.oauth.OAuthUtils.AAD_USER_PASSWORD_1;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class AADLoginIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADLoginIT.class);

    static {
        final String directory = "src/test/resources/driver/";
        final String chromedriverLinux = "chromedriver_linux64";
        final String chromedriverWin32 = "chromedriver_win32.exe";
        final String chromedriverMac = "chromedriver_mac64";
        String osName = System.getProperty("os.name").toLowerCase();
        Process process = null;
        try {
            File dir = new File(directory);
            if (Pattern.matches("linux.*", osName)) {
                process = Runtime.getRuntime().exec("chmod +x " + chromedriverLinux, null, dir);
                process.waitFor();
                System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, directory + chromedriverLinux);
            } else if (Pattern.matches("windows.*", osName)) {
                System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, directory + chromedriverWin32);
            } else if (Pattern.matches("mac.*", osName)) {
                process = Runtime.getRuntime().exec("chmod +x " + chromedriverMac, null, dir);
                process.waitFor();
                System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, directory + chromedriverMac);
            } else {
                throw new IllegalStateException("Can not recognize osName. osName = " + System.getProperty("os.name"));
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    @Test
    public void loginTest() {
        this.runApp(app -> {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--incognito");
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            WebDriver driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, 10);
            try {
                driver.get(app.root() + "api/home");
                wait.until(presenceOfElementLocated(By.name("loginfmt")))
                    .sendKeys(System.getenv(AAD_USER_NAME_1) + Keys.ENTER);
                Thread.sleep(10000);
                driver.findElement(By.name("passwd"))
                      .sendKeys(System.getenv(AAD_USER_PASSWORD_1) + Keys.ENTER);
                Thread.sleep(10000);
                driver.findElement(By.cssSelector("input[type='submit']")).click();
                Thread.sleep(10000);
                Assert.assertEquals("home", driver.findElement(By.tagName("body")).getText());

                driver.get(app.root() + "api/group1");
                Thread.sleep(1000);
                Assert.assertEquals("group1", driver.findElement(By.tagName("body")).getText());

                driver.get(app.root() + "api/status403");
                Thread.sleep(1000);
                Assert.assertNotEquals("error", driver.findElement(By.tagName("body")).getText());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                driver.quit();
            }
        });
    }

    private void runApp(Consumer<AppRunner> command) {
        try (AppRunner app = new AppRunner(AADLoginIT.DumbApp.class)) {
            app.property("azure.activedirectory.tenant-id", System.getenv(AAD_TENANT_ID_1));
            app.property("azure.activedirectory.client-id", System.getenv(AAD_MULTI_TENANT_CLIENT_ID));
            app.property("azure.activedirectory.client-secret", System.getenv(AAD_MULTI_TENANT_CLIENT_SECRET));
            app.property("azure.activedirectory.user-group.allowed-groups", "group1");

            app.start();
            command.accept(app);
        }
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp extends WebSecurityConfigurerAdapter {

        @Autowired
        private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .oidcUserService(oidcUserService);
        }

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
