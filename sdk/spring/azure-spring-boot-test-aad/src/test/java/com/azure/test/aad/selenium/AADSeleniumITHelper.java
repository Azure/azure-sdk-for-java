package com.azure.test.aad.selenium;

import com.azure.spring.test.AppRunner;
import org.junit.Assert;
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
import java.util.Map;
import java.util.regex.Pattern;

import static com.azure.spring.test.aad.EnvironmentVariables.AAD_MULTI_TENANT_CLIENT_ID;
import static com.azure.spring.test.aad.EnvironmentVariables.AAD_MULTI_TENANT_CLIENT_SECRET;
import static com.azure.spring.test.aad.EnvironmentVariables.AAD_TENANT_ID_1;
import static com.azure.spring.test.aad.EnvironmentVariables.AAD_USER_NAME_1;
import static com.azure.spring.test.aad.EnvironmentVariables.AAD_USER_PASSWORD_1;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class AADSeleniumITHelper {

    private final String username;
    private final String password;
    private final AppRunner app;
    private final WebDriver driver;
    private static final Map<String, String> DEFAULT_PROPERTIES = new HashMap<>();

    static {
        DEFAULT_PROPERTIES.put("azure.activedirectory.tenant-id", AAD_TENANT_ID_1);
        DEFAULT_PROPERTIES.put("azure.activedirectory.client-id", AAD_MULTI_TENANT_CLIENT_ID);
        DEFAULT_PROPERTIES.put("azure.activedirectory.client-secret", AAD_MULTI_TENANT_CLIENT_SECRET);
        DEFAULT_PROPERTIES.put("azure.activedirectory.user-group.allowed-groups", "group1");
        DEFAULT_PROPERTIES.put("azure.activedirectory.post-logout-redirect-uri", "http://localhost:${server.port}");

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
                throw new IllegalStateException("Unrecognized osName. osName = " + System.getProperty("os.name"));
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    public AADSeleniumITHelper(Class<?> appClass, Map<String, String> properties) throws InterruptedException {
        username = AAD_USER_NAME_1;
        password = AAD_USER_PASSWORD_1;
        app = new AppRunner(appClass);
        DEFAULT_PROPERTIES.forEach(app::property);
        properties.forEach(app::property);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--incognito", "--no-sandbox", "--disable-dev-shm-usage");
        this.driver = new ChromeDriver(options);

        this.app.start();
        login();
    }

    private void login() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(this.driver, 10);
        driver.get(app.root() + "oauth2/authorization/azure");
        wait.until(presenceOfElementLocated(By.name("loginfmt"))).sendKeys(username + Keys.ENTER);
        Thread.sleep(10000);
        driver.findElement(By.name("passwd")).sendKeys(password + Keys.ENTER);
        Thread.sleep(10000);
        driver.findElement(By.cssSelector("input[type='submit']")).click();
        Thread.sleep(10000);
    }

    public String httpGet(String endpoint) throws InterruptedException {
        driver.get((app.root() + endpoint));
        Thread.sleep(1000);
        return driver.findElement(By.tagName("body")).getText();
    }

    public void logoutTest() throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        driver.get(app.root() + "logout");
        wait.until(presenceOfElementLocated(By.cssSelector("button[type='submit']"))).click();
        Thread.sleep(10000);
        String cssSelector = "div[data-test-id='" + username + "']";
        driver.findElement(By.cssSelector(cssSelector)).click();
        Thread.sleep(10000);
        String id = driver.findElement(By.cssSelector("div[tabindex='0']")).getAttribute("data-test-id");
        Assert.assertEquals(username, id);
    }
}
