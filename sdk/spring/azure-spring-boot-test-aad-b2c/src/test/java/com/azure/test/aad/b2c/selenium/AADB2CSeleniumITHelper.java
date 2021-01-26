package com.azure.test.aad.b2c.selenium;

import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_CLIENT_ID;
import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_CLIENT_SECRET;
import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_PROFILE_EDIT;
import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_REPLY_URL;
import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_SIGN_UP_OR_SIGN_IN;
import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_TENANT;
import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_USER_EMAIL;
import static com.azure.spring.test.EnvironmentVariable.AAD_B2C_USER_PASSWORD;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import com.azure.test.aad.common.SeleniumITHelper;
import java.util.HashMap;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AADB2CSeleniumITHelper extends SeleniumITHelper {

    private String userEmail;
    private String userPassword;

    public static Map<String, String> createDefaultProperteis() {
        Map<String, String> defaultProperteis = new HashMap<>();
        defaultProperteis.put("azure.activedirectory.b2c.tenant", AAD_B2C_TENANT);
        defaultProperteis.put("azure.activedirectory.b2c.client-id", AAD_B2C_CLIENT_ID);
        defaultProperteis.put("azure.activedirectory.b2c.client-secret", AAD_B2C_CLIENT_SECRET);
        defaultProperteis.put("azure.activedirectory.b2c.reply-url", AAD_B2C_REPLY_URL);
        defaultProperteis
            .put("azure.activedirectory.b2c.user-flows.profile-edit", AAD_B2C_PROFILE_EDIT);
        defaultProperteis
            .put("azure.activedirectory.b2c.user-flows.sign-up-or-sign-in", AAD_B2C_SIGN_UP_OR_SIGN_IN);
        return defaultProperteis;
    }

    public AADB2CSeleniumITHelper(Class<?> appClass, Map<String, String> properties) {
        super(appClass, properties);
        userEmail = AAD_B2C_USER_EMAIL;
        userPassword = AAD_B2C_USER_PASSWORD;
    }

    public void logIn() {
        driver.get(app.root());
        wait.until(presenceOfElementLocated(By.id("email"))).sendKeys(userEmail);
        wait.until(presenceOfElementLocated(By.id("password"))).sendKeys(userPassword);
        wait.until(presenceOfElementLocated(By.cssSelector("button[type='submit']"))).sendKeys(Keys.ENTER);
        manualRedirection();
    }

    public void profileEditJobTitle(String newJobTitle) {
        wait.until(presenceOfElementLocated(By.id("profileEdit"))).click();
        changeJobTile(newJobTitle);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))).click();
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
        return wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))).getText();
    }
}
