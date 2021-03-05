package com.azure.test.aad.selenium;

import com.azure.spring.utils.AzureCloudUrls;
import com.azure.test.aad.common.SeleniumITHelper;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.*;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class AADSeleniumITHelper extends SeleniumITHelper {
    private String username;
    private String password;

    private static final Logger LOGGER = LoggerFactory.getLogger(AADSeleniumITHelper.class);

    public static Map<String, String> createDefaultProperties() {
        Map<String, String> defaultProperties = new HashMap<>();
        defaultProperties.put("azure.activedirectory.tenant-id", AAD_TENANT_ID_1);
        defaultProperties.put("azure.activedirectory.client-id", AAD_SINGLE_TENANT_CLIENT_ID);
        defaultProperties.put("azure.activedirectory.client-secret", AAD_SINGLE_TENANT_CLIENT_SECRET);
        defaultProperties.put("azure.activedirectory.user-group.allowed-groups", "group1");
        defaultProperties.put("azure.activedirectory.post-logout-redirect-uri", "http://localhost:${server.port}");
        defaultProperties.put("azure.activedirectory.base-uri", AzureCloudUrls.getBaseUrl(AZURE_CLOUD_TYPE));
        defaultProperties.put("azure.activedirectory.graph-base-uri", AzureCloudUrls.getGraphBaseUrl(AZURE_CLOUD_TYPE));
        return defaultProperties;
    }

    public AADSeleniumITHelper(Class<?> appClass, Map<String, String> properties) {
        this(appClass, properties, AAD_USER_NAME_1, AAD_USER_PASSWORD_1);
    }

    public AADSeleniumITHelper(Class<?> appClass, Map<String, String> properties, String username, String password) {
        super(appClass, properties);
        this.username = username;
        this.password = password;
    }

    public void logIn() {
        driver.get(app.root() + "oauth2/authorization/azure");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("loginfmt"))).sendKeys(username + Keys.ENTER);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("passwd"))).sendKeys(password + Keys.ENTER);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='submit']"))).click();
    }

    public boolean logInAADConditionalAccess() {
        driver.get(app.root() + "oauth2/authorization/azure");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("loginfmt"))).sendKeys(username + Keys.ENTER);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("passwd"))).sendKeys(password + Keys.ENTER);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='submit']"))).click();
        driver.get((app.root() + "obo"));
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            LOGGER.error("Current Thread Failed to sleep", e);
        }
        this.httpGet("obo");
        return driver.findElement(By.tagName("body")).getText().contains("response success");
    }

    public String httpGet(String endpoint) {
        driver.get((app.root() + endpoint));
        return wait.until(presenceOfElementLocated(By.tagName("body"))).getText();
    }

    public String logoutAndGetLogoutUsername() {
        driver.get(app.root() + "logout");
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))).click();
        String cssSelector = "div[data-test-id='" + username + "']";
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(cssSelector))).click();
        String id = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div[tabindex='0']")))
                        .getAttribute("data-test-id");
        return id;
    }

    public String httpGetWithIncrementalConsent(String endpoint) {
        driver.get((app.root() + endpoint));

        String oauth2AuthorizationUrlFraction = String.format(AzureCloudUrls.getBaseUrl(AZURE_CLOUD_TYPE)
            + "%s/oauth2/v2.0/" + "authorize?", AAD_TENANT_ID_1);
        wait.until(ExpectedConditions.urlContains(oauth2AuthorizationUrlFraction));

        String onDemandAuthorizationUrl = driver.getCurrentUrl();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit']"))).click();
        return onDemandAuthorizationUrl;
    }

    public String getUsername() {
        return username;
    }
}
