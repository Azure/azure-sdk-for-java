// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.selenium;

import com.azure.test.utils.AppRunner;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.azure.test.aad.AADTestUtils.AAD_MULTI_TENANT_CLIENT_ID;
import static com.azure.test.aad.AADTestUtils.AAD_MULTI_TENANT_CLIENT_SECRET;
import static com.azure.test.aad.AADTestUtils.AAD_TENANT_ID_1;
import static com.azure.test.aad.AADTestUtils.AAD_USER_NAME_1;
import static com.azure.test.aad.AADTestUtils.AAD_USER_PASSWORD_1;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class SeleniumTestUtils {

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

    public static Map<String, String> get(AppRunner app, List<String> endPoints) {

        Map<String, String> result = new HashMap<>();
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
            result.put(endPoints.get(0), driver.findElement(By.tagName("body")).getText());
            endPoints.remove(0);
            for (String endPoint : endPoints) {
                driver.get(app.root() + endPoint);
                Thread.sleep(1000);
                result.put(endPoint, driver.findElement(By.tagName("body")).getText());
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
