package com.azure.test.aad.b2c.selenium;

import com.azure.spring.test.AppRunner;
import com.azure.test.aad.b2c.utils.AADB2CTestUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class AADB2CSeleniumITHelper {

    private final String userEmail;
    private final String userPassword;
    private final AppRunner app;
    private WebDriver driver;
    private WebDriverWait wait;
    private static final Map<String, String> DEFAULT_PROPERTIES = new HashMap<>();

    static {
        DEFAULT_PROPERTIES.put("azure.activedirectory.b2c.tenant", AADB2CTestUtils.AAD_B2C_TENANT);
        DEFAULT_PROPERTIES.put("azure.activedirectory.b2c.client-id", AADB2CTestUtils.AAD_B2C_CLIENT_ID);
        DEFAULT_PROPERTIES.put("azure.activedirectory.b2c.client-secret",
            AADB2CTestUtils.AAD_B2C_CLIENT_SECRET);
        DEFAULT_PROPERTIES.put("azure.activedirectory.b2c.reply-url", AADB2CTestUtils.AAD_B2C_REPLY_URL);
        DEFAULT_PROPERTIES
            .put("azure.activedirectory.b2c.user-flows.sign-up-or-sign-in",
                AADB2CTestUtils.AAD_B2C_SIGN_UP_OR_SIGN_IN);
        DEFAULT_PROPERTIES
            .put("azure.activedirectory.b2c.user-flows.profile-edit",
                AADB2CTestUtils.AAD_B2C_PROFILE_EDIT);

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

    public AADB2CSeleniumITHelper(Class<?> appClass, Map<String, String> properties) throws InterruptedException {
        userEmail = AADB2CTestUtils.AAD_B2C_USER_EMAIL;
        userPassword = AADB2CTestUtils.AAD_B2C_USER_PASSWORD;
        app = new AppRunner(appClass);
        DEFAULT_PROPERTIES.forEach(app::property);
        properties.forEach(app::property);
        this.app.start();
        setDriver();
    }

    public void quitDriver() {
        try {
            this.driver.quit();
        } catch (Exception e) {
            this.driver = null;
        }
    }

    private void setDriver() {
        if (driver == null) {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--incognito", "--no-sandbox", "--disable-dev-shm-usage");
            this.driver = new ChromeDriver(options);
            wait = new WebDriverWait(driver, 5);
        }
    }

    public void signIn(String userFlowName) throws InterruptedException {
        driver.get(app.root());
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        driver.findElement(By.id("email")).sendKeys(userEmail);
        driver.findElement(By.id("password")).sendKeys(userPassword);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        manualRedirection();
    }

    public void profileEditJobTitle(String newJobTitle) throws InterruptedException {
        driver.findElement(By.id("profileEdit")).click();
        changeJobTile(newJobTitle);
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        manualRedirection();
    }

    public String logoutAndGetSignInButtonText() throws InterruptedException {
        wait.until(ExpectedConditions.elementToBeClickable(By.id("logout")));
        driver.findElement(By.id("logout")).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        driver.findElement(By.cssSelector("button[type='submit']")).submit();
        manualRedirection();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
            "a[href='/oauth2/authorization/" + AADB2CTestUtils.AAD_B2C_SIGN_UP_OR_SIGN_IN + "']")));
        driver.findElement(
            By.cssSelector(
                "a[href='/oauth2/authorization/" + AADB2CTestUtils.AAD_B2C_SIGN_UP_OR_SIGN_IN + "']")).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("next")));
        return driver.findElement(By.cssSelector("button[type='submit']")).getText();
    }

    private void manualRedirection() throws InterruptedException {
        wait.until(ExpectedConditions.urlMatches("^http://localhost"));
        String currentUrl = driver.getCurrentUrl();
        String newCurrentUrl = currentUrl.replaceFirst("http://localhost:8080/", app.root());
        driver.get(newCurrentUrl);
    }

    public void changeJobTile(String newValue) {
        String elementId = "jobTitle";
        wait.until(ExpectedConditions.elementToBeClickable(By.id(elementId)));
        driver.findElement(By.id(elementId)).clear();
        driver.findElement(By.id(elementId)).sendKeys(newValue);
    }

    public String getJobTitle() {
        return driver.findElement(By.cssSelector("tbody"))
            .findElement(By.xpath("tr[10]"))
            .findElement(By.xpath("th[2]"))
            .getText();
    }

    public String getName() {
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("tbody")));
        return driver.findElement(By.cssSelector("tbody"))
            .findElement(By.xpath("tr[2]"))
            .findElement(By.xpath("th[2]"))
            .getText();
    }

    public String getUserFlowName() {
        return driver.findElement(By.cssSelector("tbody"))
            .findElement(By.xpath("tr[last()]"))
            .findElement(By.xpath("th[2]"))
            .getText();
    }

}
