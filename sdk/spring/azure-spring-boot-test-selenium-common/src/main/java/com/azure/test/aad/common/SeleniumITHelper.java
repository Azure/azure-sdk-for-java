// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.common;

import com.azure.spring.test.AppRunner;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

public class SeleniumITHelper {
    Logger logger = LoggerFactory.getLogger(SeleniumITHelper.class);
    public static String folderName = "webdriver";

    protected AppRunner app;
    protected WebDriver driver;
    protected WebDriverWait wait;

    public SeleniumITHelper(Class<?> appClass, Map<String, String> properties)  {
        createDriver(appClass);
        createAppRunner(appClass, properties);
    }

    protected void createDriver(Class<?> appClass) {
        if (driver == null) {
            String currentPath = appClass.getClassLoader().getResource(("")).getPath();
            String sdkSpring = File.separator + "sdk" + File.separator + "spring";
            String destination = currentPath + File.separator + folderName;
            while (StringUtils.isNotEmpty(currentPath)) {
                if (StringUtils.endsWith(currentPath, sdkSpring)) {
                    destination = currentPath + File.separator + folderName;
                    break;
                } else {
                    currentPath = new File(currentPath).getParent();
                }
            }

            setPathExecutableRecursively(destination);

            System.setProperty("wdm.cachePath", destination);
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--incognito", "--no-sandbox", "--disable-dev-shm-usage");
            driver = new ChromeDriver(options);
            wait = new WebDriverWait(driver, 10);
        }
    }

    protected void createAppRunner(Class<?> appClass, Map<String, String> properties) {
        app = new AppRunner(appClass);
        properties.forEach(app::property);
        app.start();
    }

    /**
     * Manually invoke destroy to complete resource release.
     */
    public void destroy() {
        driver.quit();
        app.close();
    }

    private void setPathExecutableRecursively(String path) {
        File file = new File(path);
        if (!file.exists()) {
            logger.warn("Path " + path + " does not exist!");
            return;
        }
        if (!file.setExecutable(true)) {
            logger.error("Failed to set executable for " + path);
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (null != files && files.length > 0) {
                setPathExecutableRecursively(file.getAbsolutePath());
            }
        }
    }
}
