package com.azure.test.aad.b2c.selenium;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import com.azure.test.aad.b2c.utils.AADB2CTestUtils;
import com.azure.test.aad.common.SeleniumITHelper;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AADB2CSeleniumITHelper extends SeleniumITHelper {

    private String userEmail;
    private String userPassword;

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
    }

    public AADB2CSeleniumITHelper(Class<?> appClass, Map<String, String> properties) {
        userEmail = AADB2CTestUtils.AAD_B2C_USER_EMAIL;
        userPassword = AADB2CTestUtils.AAD_B2C_USER_PASSWORD;
        createDriver();
        createAppRunner(appClass, properties);
    }

    public void signIn() {
        driver.get(app.root());
        wait.until(presenceOfElementLocated(By.id("email"))).sendKeys(userEmail);
        wait.until(presenceOfElementLocated(By.id("password"))).sendKeys(userPassword);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("next"))).click();
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
            "a[href='/oauth2/authorization/" + AADB2CTestUtils.AAD_B2C_SIGN_UP_OR_SIGN_IN + "']"))).click();
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
