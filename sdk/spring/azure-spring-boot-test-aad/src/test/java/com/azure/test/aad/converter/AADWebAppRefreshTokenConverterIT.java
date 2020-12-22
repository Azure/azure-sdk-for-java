// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.converter;

import com.azure.test.oauth.OAuthLoginUtils;
import com.azure.test.utils.AppRunner;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class AADWebAppRefreshTokenConverterIT {

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
        try (AppRunner app = new AppRunner(DumbApp.class)) {
            OAuthLoginUtils.addProperty(app);
            app.property("azure.activedirectory.authorization.office.scopes", "https://manage.office.com/ActivityFeed.Read");
            List<String> endPoints = new ArrayList<>();
            endPoints.add("api/accessTokenScopes");
            List<String> result = OAuthLoginUtils.get(app , endPoints);
            Assert.assertTrue(result.get(0).indexOf("profile") < 0);
            Assert.assertTrue(result.get(0).indexOf("https://manage.office.com/ActivityFeed.Read") >= 0);

        }
    }

    @EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
    @SpringBootApplication
    @RestController
    public static class DumbApp {

        @GetMapping(value = "api/accessTokenScopes")
        public Set<String> accessTokenScopes(
            @RegisteredOAuth2AuthorizedClient("office") OAuth2AuthorizedClient authorizedClient) {
            return Optional.of(authorizedClient)
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getScopes)
                .orElse(null);
        }
    }

}
