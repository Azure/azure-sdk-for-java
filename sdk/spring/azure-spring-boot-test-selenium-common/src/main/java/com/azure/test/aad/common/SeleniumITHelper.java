// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.common;

import com.azure.spring.test.AppRunner;
import java.util.Map;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SeleniumITHelper {

    protected AppRunner app;
    protected WebDriver driver;
    protected WebDriverWait wait;

    public SeleniumITHelper(Class<?> appClass, Map<String, String> properties) {
        createDriver();
        createAppRunner(appClass, properties);
    }


    protected void createDriver() {
        if (driver == null) {
            System.setProperty("wdm.cachePath", getClass().getClassLoader().getResource("selenium").getPath());
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
