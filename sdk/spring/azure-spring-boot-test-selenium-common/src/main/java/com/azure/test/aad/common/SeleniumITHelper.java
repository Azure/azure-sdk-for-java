// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.common;

import com.azure.spring.test.AppRunner;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.util.Map;

public class SeleniumITHelper {
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
}
