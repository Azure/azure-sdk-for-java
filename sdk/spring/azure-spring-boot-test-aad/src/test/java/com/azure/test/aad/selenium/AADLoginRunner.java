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
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.azure.test.aad.AADTestUtils.AAD_MULTI_TENANT_CLIENT_ID;
import static com.azure.test.aad.AADTestUtils.AAD_MULTI_TENANT_CLIENT_SECRET;
import static com.azure.test.aad.AADTestUtils.AAD_TENANT_ID_1;
import static com.azure.test.aad.AADTestUtils.AAD_USER_NAME_1;
import static com.azure.test.aad.AADTestUtils.AAD_USER_PASSWORD_1;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class AADLoginRunner {

    public static final String DEFAULT_USERNAME = System.getenv(AAD_USER_NAME_1);
    private static final String DEFAULT_PASSWORD = System.getenv(AAD_USER_PASSWORD_1);

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
                throw new IllegalStateException("Can not recognize osName. osName = " + System.getProperty("os"
                    + ".name"));
            }
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private final AppRunner app;
    private final WebDriver driver;
    private final String password;
    private final String username;

    private AADLoginRunner(String username, String password, AppRunner app, WebDriver driver) {
        this.username = username;
        this.password = password;
        this.app = app;
        this.driver = driver;
    }

    public static AADLoginRunnerConfiguration build(Class<?> appClass) {
        return new AADLoginRunnerConfiguration(appClass);
    }

    public void run(BrowserCommandWithAppRunner command) {
        try {
            this.app.start();
            command.login((app, driver) -> {
                WebDriverWait wait = new WebDriverWait(this.driver, 10);
                driver.get(app.root() + "oauth2/authorization/azure");
                wait.until(presenceOfElementLocated(By.name("loginfmt")))
                    .sendKeys(this.username + Keys.ENTER);
                Thread.sleep(10000);

                driver.findElement(By.name("passwd"))
                      .sendKeys(this.password + Keys.ENTER);
                Thread.sleep(10000);

                driver.findElement(By.cssSelector("input[type='submit']")).click();
                Thread.sleep(10000);
            }).run(this.app, this.driver);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (this.driver != null) {
                this.driver.quit();
            }
            if (this.app != null) {
                this.app.close();
            }
        }
    }

    @FunctionalInterface
    public interface BrowserCommandWithAppRunner {

        default BrowserCommandWithAppRunner login(BrowserCommandWithAppRunner login) {
            Objects.requireNonNull(login);
            return (AppRunner app, WebDriver driver) -> {
                login.run(app, driver);
                run(app, driver);
            };
        }

        void run(AppRunner app, WebDriver driver) throws Exception;
    }

    public static class AADLoginRunnerConfiguration {
        private final AppRunner app;
        private final WebDriver driver;
        private Consumer<AppRunner> configure;

        private AADLoginRunnerConfiguration(Class<?> appClass) {
            this.configure = defautlConfigure();

            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--incognito", "--no-sandbox", "--disable-dev-shm-usage");
            this.driver = new ChromeDriver(options);

            this.app = new AppRunner(appClass);
        }

        public AADLoginRunnerConfiguration configure(Consumer<AppRunner> configure) {
            this.configure = configure;
            return this;
        }

        public AADLoginRunnerConfiguration extendsDefault(Consumer<AppRunner> configure) {
            this.configure = defautlConfigure().andThen(configure);
            return this;
        }

        public AADLoginRunner login(String username, String password) {
            this.configure.accept(this.app);
            return new AADLoginRunner(username, password, this.app, this.driver);
        }

        public AADLoginRunner login() {
            return login(DEFAULT_USERNAME, DEFAULT_PASSWORD);
        }

        private static Consumer<AppRunner> defautlConfigure() {
            return app -> {
                app.property("azure.activedirectory.tenant-id", System.getenv(AAD_TENANT_ID_1));
                app.property("azure.activedirectory.client-id", System.getenv(AAD_MULTI_TENANT_CLIENT_ID));
                app.property("azure.activedirectory.client-secret", System.getenv(AAD_MULTI_TENANT_CLIENT_SECRET));
                app.property("azure.activedirectory.user-group.allowed-groups", "group1");
                app.property("azure.activedirectory.post-logout-redirect-uri", "http://localhost:${server.port}");
            };
        }
    }
}
