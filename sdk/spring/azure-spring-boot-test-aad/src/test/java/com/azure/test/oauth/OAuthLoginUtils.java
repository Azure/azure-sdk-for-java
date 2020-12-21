// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.oauth;

import com.azure.test.utils.AppRunner;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.azure.test.oauth.OAuthUtils.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class OAuthLoginUtils {

    public static List<String> get(AppRunner app, List<String> endPoints) {

        List<String> result = new ArrayList<>();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--incognito");
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, 10);
        app.start();
        try {
            driver.get(app.root() + endPoints.get(0));
            wait.until(presenceOfElementLocated(By.name("loginfmt")))
                .sendKeys(System.getenv(AAD_USER_NAME_1) + Keys.ENTER);
            Thread.sleep(10000);
            driver.findElement(By.name("passwd"))
                .sendKeys(System.getenv(AAD_USER_PASSWORD_1) + Keys.ENTER);
            Thread.sleep(10000);
            driver.findElement(By.cssSelector("input[type='submit']")).click();
            Thread.sleep(10000);
            result.add(driver.findElement(By.tagName("body")).getText());
            endPoints.remove(0);
            for(String endPoint : endPoints) {
                driver.get(app.root() + endPoint);
                Thread.sleep(1000);
                result.add(driver.findElement(By.tagName("body")).getText());
            }
            return result;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
    }

    public static void addProperty(AppRunner app) {
        app.property("azure.activedirectory.tenant-id", System.getenv(AAD_TENANT_ID_1));
        app.property("azure.activedirectory.client-id", System.getenv(AAD_MULTI_TENANT_CLIENT_ID));
        app.property("azure.activedirectory.client-secret", System.getenv(AAD_MULTI_TENANT_CLIENT_SECRET));
        app.property("azure.activedirectory.user-group.allowed-groups", "group1");
    }

}
