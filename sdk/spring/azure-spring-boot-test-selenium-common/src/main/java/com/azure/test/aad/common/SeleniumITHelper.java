// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.common;

import com.azure.spring.test.AppRunner;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SeleniumITHelper {
    Logger logger = LoggerFactory.getLogger(SeleniumITHelper.class);
    public static String tempFolderName = "temp_selenium";

    protected AppRunner app;
    protected WebDriver driver;
    protected WebDriverWait wait;

    public SeleniumITHelper(Class<?> appClass, Map<String, String> properties)  {
        createDriver();
        createAppRunner(appClass, properties);
    }

    protected void createDriver() {
        if (driver == null) {
            String destination = System.getProperty("user.dir") + tempFolderName;
            String path = this.getClass().getResource("/selenium").getPath();
            if (path.contains(".jar")) {
                try {
                    JarUtil.copyFolderFromJar("selenium", new File(destination), JarUtil.CopyOption.REPLACE_IF_EXIST);
                } catch (IOException e) {
                    logger.error("error copy from jar to folder", e);
                }
            } else {
                try {
                    FileUtils.copyDirectory(new File(path), new File(destination));
                } catch (IOException e) {
                    logger.error("error copy from folder to folder", e);
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
        try {
            FileUtils.deleteDirectory(new File(System.getProperty("user.dir") + File.separator + tempFolderName));
        } catch (IOException e) {
            logger.error("error deleteDirectory", e);
        }
    }
}
