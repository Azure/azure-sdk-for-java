// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.test.aad.common;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.azure.spring.test.EnvironmentVariable.AAD_TENANT_ID_1;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_NAME_1;
import static com.azure.spring.test.EnvironmentVariable.AAD_USER_PASSWORD_1;
import static com.azure.spring.test.EnvironmentVariable.AZURE_CLOUD_TYPE;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

public class AADSeleniumITHelper extends SeleniumITHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(AADSeleniumITHelper.class);

    private final String username;
    private final String password;

    public AADSeleniumITHelper(Class<?> appClass, Map<String, String> properties) {
        this(appClass, properties, AAD_USER_NAME_1, AAD_USER_PASSWORD_1);
    }

    public AADSeleniumITHelper(Class<?> appClass,
                               Map<String, String> properties,
                               String username,
                               String password) {
        super(appClass, properties);
        this.username = username;
        this.password = password;
    }

    public void logIn() {
        driver.get(app.root() + "oauth2/authorization/azure");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("loginfmt"))).sendKeys(username + Keys.ENTER);
        try {
            try {
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("passwd"))).sendKeys(password + Keys.ENTER);
            } catch (Exception exception) {
                // Sometimes AAD cannot locate the user account and will ask to select it's a work account or
                // personal account.
                // Here select work accout.
                // https://docs.microsoft.com/azure/devops/organizations/accounts/faq-azure-access?view=azure-devops#q-why-do-i-have-to-choose-between-a-work-or-school-account-and-my-personal-account
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("aadTileTitle"))).click();
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("passwd"))).sendKeys(password + Keys.ENTER);
            }
        } catch (Exception exception) {
            String passwdUrl = driver.getCurrentUrl();
            LOGGER.info(passwdUrl);
            String pageSource = driver.getPageSource();
            LOGGER.info(pageSource);
            throw exception;
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[type='submit']"))).click();
    }

    public String loginAndGetBodyText() {
        logIn();
        driver.get((app.root() + "webapiA/webApiB"));
        wait.until(ExpectedConditions.urlToBe(app.root() + "webapiA/webApiB#"));
        return driver.findElement(By.tagName("body")).getText();
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

        String oauth2AuthorizationUrlFraction = String.format(getBaseUrl(AZURE_CLOUD_TYPE)
            + "%s/oauth2/v2.0/" + "authorize?", AAD_TENANT_ID_1);
        wait.until(ExpectedConditions.urlContains(oauth2AuthorizationUrlFraction));

        String onDemandAuthorizationUrl = driver.getCurrentUrl();
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("input[type='submit']"))).click();
        return onDemandAuthorizationUrl;
    }

    public String getUsername() {
        return username;
    }

    public static String getBaseUrl(String cloudType) {
        return cloudType.equals("Global") ? "https://login.microsoftonline.com/"
            : "https://login.partner.microsoftonline.cn/";
    }

    public static String getGraphBaseUrl(String cloudType) {
        return cloudType.equals("Global") ? "https://graph.microsoft.com/"
            : "https://microsoftgraph.chinacloudapi.cn/";
    }

    public static String getServiceManagementBaseUrl(String cloudType) {
        return cloudType.equals("Global") ? "https://management.azure.com/"
            : "https://management.chinacloudapi.cn/";
    }
}
