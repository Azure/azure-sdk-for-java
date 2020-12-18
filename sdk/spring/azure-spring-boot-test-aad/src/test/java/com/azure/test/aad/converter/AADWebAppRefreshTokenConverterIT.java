// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.converter;

import com.azure.spring.aad.webapp.AzureOAuth2Configuration;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static com.azure.test.oauth.OAuthUtils.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class AADWebAppRefreshTokenConverterIT {

    private final RestTemplate restTemplate = new RestTemplate();

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
        } catch (IllegalStateException e) {
            throw e;
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    @Test
    public void testRefreshTokenConverter() {
        final String clientId = System.getenv(AAD_MULTI_TENANT_CLIENT_ID);
        final String clientSecret = System.getenv(AAD_MULTI_TENANT_CLIENT_SECRET);
        try (AppRunner app = new AppRunner(DumbApp.class)) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--incognito");
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            WebDriver driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, 10);

            app.property("azure.activedirectory.client-id", clientId);
            app.property("azure.activedirectory.client-secret", clientSecret);
            app.property("azure.activedirectory.user-group.allowed-groups", "group1,group2");
            app.property("azure.activedirectory.authorization.office.scopes", "https://manage.office.com/ActivityFeed.Read");
            app.start();

            try {
                driver.get(app.root() + "api/getAccessToken");
                wait.until(presenceOfElementLocated(By.name("loginfmt")))
                    .sendKeys(System.getenv(AAD_USER_NAME_1) + Keys.ENTER);
                Thread.sleep(10000);
                driver.findElement(By.name("passwd"))
                    .sendKeys(System.getenv(AAD_USER_PASSWORD_1) + Keys.ENTER);
                Thread.sleep(10000);
                driver.findElement(By.cssSelector("input[type='submit']")).click();
                Thread.sleep(10000);
                Assert.assertTrue(driver.findElement(By.tagName("body")).getText().indexOf("profile") < 0);
                Assert.assertTrue(driver.findElement(By.tagName("body")).getText().indexOf("https://manage.office.com/ActivityFeed.Read") >= 0);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                driver.quit();
            }
        }
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp extends AzureOAuth2Configuration {

        @Autowired
        private OAuth2UserService<OidcUserRequest, OidcUser> oidcUserService;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);
            http.authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .oauth2Login()
                .userInfoEndpoint()
                .oidcUserService(oidcUserService);
        }

        @GetMapping(value = "api/getAccessToken")
        public Set<String> groupsCount(
            @RegisteredOAuth2AuthorizedClient("office") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getScopes)
                .orElse(null);
        }
    }

}
