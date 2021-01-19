// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.test.aad.common;

import com.azure.spring.test.AppRunner;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SeleniumITHelper {

    protected AppRunner app;
    protected WebDriver driver;
    protected WebDriverWait wait;
    private final static String tempDirPath =
        System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID();

    static {
        initChromeDriver();
        deleteChromeDriverFile();
    }

    public SeleniumITHelper(Class<?> appClass, Map<String, String> properties) {
        createDriver();
        createAppRunner(appClass, properties);
    }

    private static void initChromeDriver() {
        final String chromedriverLinux = "chromedriver_linux64";
        final String chromedriverWin32 = "chromedriver_win32.exe";
        final String chromedriverMac = "chromedriver_mac64";
        String osName = System.getProperty("os.name").toLowerCase();
        Process process = null;
        File dir;
        try {
            if (Pattern.matches("linux.*", osName)) {
                dir = copyChromeDriverFile(chromedriverLinux);
                process = Runtime.getRuntime().exec("chmod +x " + chromedriverLinux, null, dir.getParentFile());
                process.waitFor();
                System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, dir.getPath());
            } else if (Pattern.matches("windows.*", osName)) {
                dir = copyChromeDriverFile(chromedriverWin32);
                System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, dir.getPath());
            } else if (Pattern.matches("mac.*", osName)) {
                dir = copyChromeDriverFile(chromedriverMac);
                process = Runtime.getRuntime().exec("chmod +x " + chromedriverMac, null, dir.getParentFile());
                process.waitFor();
                System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, dir.getPath());
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

    protected void createDriver() {
        if (driver == null) {
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

    private static File copyChromeDriverFile(String chromeDriverName)
        throws IOException {
        InputStream resourceAsStream = SeleniumITHelper.class.getClassLoader()
            .getResourceAsStream("driver/" + chromeDriverName);
        File dest = new File(tempDirPath + File.separator + chromeDriverName);
        FileUtils.copyInputStreamToFile(resourceAsStream, dest);
        return dest;
    }

    private static void deleteChromeDriverFile() {
        File targetFile = new File(tempDirPath);
        try {
            FileUtils.forceDeleteOnExit(targetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Manually invoke destroy to complete resource release.
     */
    public void destroy() {
        driver.quit();
        app.close();
    }
}
