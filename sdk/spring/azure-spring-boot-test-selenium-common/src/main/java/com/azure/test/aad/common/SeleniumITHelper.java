// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.common;

import com.azure.spring.test.AppRunner;
import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeleniumITHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumITHelper.class);

    protected AppRunner app;
    protected WebDriver driver;
    protected WebDriverWait wait;

    static {
        init();
    }

    private static void init() {
        final String chromedriverLinux = "chromedriver_linux64";
        final String chromedriverWin32 = "chromedriver_win32.exe";
        final String chromedriverMac = "chromedriver_mac64";
        String classpath = SeleniumITHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        final String directory = classpath + "driver/";
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
                throw new IllegalStateException("Unrecognized osName. osName = " + System.getProperty("os.name"));
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }

    public void setDriver() {
        if (driver == null) {
            try {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--headless");
                options.addArguments("--incognito", "--no-sandbox", "--disable-dev-shm-usage");
                this.driver = new ChromeDriver(options);
                wait = new WebDriverWait(driver, 10);
            } catch (Exception e) {
                LOGGER.error("Driver initialization produces an exception. ", e);
                app.close();
            }
        }
    }

    public void destroy() {
        quitDriver();
        app.close();
    }

    public void quitDriver() {
        if (driver != null) {
            this.driver.quit();
        }
    }


}
