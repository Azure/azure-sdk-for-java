package com.azure.test.aad.b2c.selenium;

import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_CLIENT_SECRET;
import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_PROFILE_EDIT;
import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_REPLY_URL;
import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_SIGN_UP_OR_SIGN_IN;
import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_BASE_URI;
import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_USER_EMAIL;
import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_USER_PASSWORD;
import static com.azure.spring.test.EnvironmentVariable.AZURE_CLOUD_TYPE;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import com.azure.spring.test.Constant;
import com.azure.test.aad.common.SeleniumITHelper;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AADB2CSeleniumITHelper extends SeleniumITHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADB2CSeleniumITHelper.class);

    private final String userEmail;
    private final String userPassword;
    private final boolean isAzureCloudGlobal;

    public static Map<String, String> createDefaultProperteis() {
        Map<String, String> defaultProperteis = new HashMap<>();
        defaultProperteis.put("azure.activedirectory.b2c.base-uri", AAD_B2C_BASE_URI);
        defaultProperteis.put("azure.activedirectory.b2c.client-id", AAD_B2C_CLIENT_ID);
        defaultProperteis.put("azure.activedirectory.b2c.client-secret", AAD_B2C_CLIENT_SECRET);
        defaultProperteis.put("azure.activedirectory.b2c.reply-url", AAD_B2C_REPLY_URL);
        defaultProperteis
            .put("azure.activedirectory.b2c.user-flows.sign-up-or-sign-in", AAD_B2C_SIGN_UP_OR_SIGN_IN);
        defaultProperteis
            .put("azure.activedirectory.b2c.user-flows.profile-edit", AAD_B2C_PROFILE_EDIT);
        return defaultProperteis;
    }

    public AADB2CSeleniumITHelper(Class<?> appClass, Map<String, String> properties) {
        super(appClass, properties);
        userEmail = AAD_B2C_USER_EMAIL;
        userPassword = AAD_B2C_USER_PASSWORD;
        isAzureCloudGlobal = Constant.AZURE_CLOUD_TYPE_GLOBAL.equalsIgnoreCase(AZURE_CLOUD_TYPE);
    }

    public void logIn() {
        driver.get(app.root());
        LOGGER.info("Current url is " + driver.getCurrentUrl());
        wait.until(ExpectedConditions.urlMatches("^https://"));
        if (isAzureCloudGlobal) {
            wait.until(presenceOfElementLocated(By.id("email"))).sendKeys(userEmail);
        } else {
            wait.until(presenceOfElementLocated(By.id("logonIdentifier"))).sendKeys(userEmail);
        }

        wait.until(presenceOfElementLocated(By.id("password"))).sendKeys(userPassword);

        if (isAzureCloudGlobal) {
            wait.until(presenceOfElementLocated(By.cssSelector("button[type='submit']"))).sendKeys(Keys.ENTER);
        } else {
            wait.until(presenceOfElementLocated(By.id("next"))).sendKeys(Keys.ENTER);
        }
        manualRedirection();
    }

    public void profileEditJobTitle(String newJobTitle) {
        wait.until(presenceOfElementLocated(By.id("profileEdit"))).click();
        changeJobTile(newJobTitle);
        if (isAzureCloudGlobal) {
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))).click();
        } else {
            wait.until(presenceOfElementLocated(By.id("continue"))).sendKeys(Keys.ENTER);
        }
        manualRedirection();
    }

    public void logout() {
        wait.until(presenceOfElementLocated(By.id("logout"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))).submit();
        manualRedirection();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
            "a[href='/oauth2/authorization/" + AAD_B2C_SIGN_UP_OR_SIGN_IN + "']"))).click();
    }

    private void manualRedirection() {
        wait.until(ExpectedConditions.urlMatches("^http://localhost"));
        String currentUrl = driver.getCurrentUrl();
        String newCurrentUrl = currentUrl.replaceFirst("http://localhost:8080/", app.root());
        driver.get(newCurrentUrl);
    }

    public void changeJobTile(String newValue) {
        String elementId = "jobTitle";
        wait.until(presenceOfElementLocated(By.id(elementId))).clear();
        wait.until(presenceOfElementLocated(By.id(elementId))).sendKeys(newValue);
    }

    public String getJobTitle() {
        return driver.findElement(By.cssSelector("tbody"))
            .findElement(By.xpath("tr[10]"))
            .findElement(By.xpath("th[2]"))
            .getText();
    }

    public String getName() {
        String currentUrl = driver.getCurrentUrl();
        LOGGER.info("AADB2CSeleniumITHelper, currenturl = {}", currentUrl);
        String pageSource = driver.getPageSource();
        LOGGER.info("AADB2CSeleniumITHelper, pageSource = {}", pageSource);
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

    public String getSignInButtonText() {
        if (isAzureCloudGlobal) {
            return wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))).getText();
        } else {
            return wait.until(ExpectedConditions.elementToBeClickable(By.id("next"))).getText();
        }
    }
}
