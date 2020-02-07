/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.test.keyvault;

import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.keyvault.Vault;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.mgmt.AppServiceTool;
import com.microsoft.azure.mgmt.ClientSecretAccess;
import com.microsoft.azure.mgmt.ConstantsHelper;
import com.microsoft.azure.mgmt.KeyVaultTool;
import com.microsoft.azure.mgmt.ResourceGroupTool;
import com.microsoft.azure.mgmt.VirtualMachineTool;
import com.microsoft.azure.test.AppRunner;
import com.microsoft.azure.utils.SSHShell;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

@Slf4j
public class KeyVaultIT {
  
    private static ClientSecretAccess access;
    private static Vault vault;
    private static String resourceGroupName;
    private static RestTemplate restTemplate;
    private static final String prefix = "test-keyvault";
    private static final String VM_USER_NAME = "deploy";
    private static final String VM_USER_PASSWORD = "12NewPAwX0rd!";
    private static final String KEY_VAULT_VALUE = "value";
    private static final String TEST_KEY_VAULT_JAR_FILE_NAME = "app.jar";
    private static final int DEFAULT_MAX_RETRY_TIMES = 3;
    private static String TEST_KEYVAULT_APP_JAR_PATH;
    private static String TEST_KEYVAULT_APP_ZIP_PATH;

    @BeforeClass
    public static void createKeyVault() throws IOException {
        access = ClientSecretAccess.load();
        resourceGroupName = SdkContext.randomResourceName(ConstantsHelper.TEST_RESOURCE_GROUP_NAME_PREFIX, 30);
        final KeyVaultTool tool = new KeyVaultTool(access);
        vault = tool.createVaultInNewGroup(resourceGroupName, prefix);
        vault.secrets().define("key").withValue(KEY_VAULT_VALUE).create();
        vault.secrets().define("azure-cosmosdb-key").withValue(KEY_VAULT_VALUE).create();
        restTemplate = new RestTemplate();

        TEST_KEYVAULT_APP_JAR_PATH = new File(System.getProperty("keyvault.app.jar.path")).getCanonicalPath();
        TEST_KEYVAULT_APP_ZIP_PATH = new File(System.getProperty("keyvault.app.zip.path")).getCanonicalPath();
        log.info("keyvault.app.jar.path={}", TEST_KEYVAULT_APP_JAR_PATH);
        log.info("keyvault.app.zip.path={}", TEST_KEYVAULT_APP_ZIP_PATH);
        log.info("--------------------->resources provision over");
    }
    
    @AfterClass
    public static void deleteResourceGroup() {
        final ResourceGroupTool tool = new ResourceGroupTool(access);
        tool.deleteGroup(resourceGroupName);
        log.info("--------------------->resources clean over");
    }

    @Test
    public void keyVaultAsPropertySource() {
        try (AppRunner app = new AppRunner(DumbApp.class)) {
            app.property("azure.keyvault.enabled", "true");
            app.property("azure.keyvault.uri", vault.vaultUri());
            app.property("azure.keyvault.client-id", access.clientId());
            app.property("azure.keyvault.client-key", access.clientSecret());
            app.property("azure.keyvault.tenant-id", access.tenant());

            final ConfigurableApplicationContext dummy = app.start("dummy");
            final ConfigurableEnvironment environment = dummy.getEnvironment();
            final MutablePropertySources propertySources = environment.getPropertySources();
            for (final PropertySource<?> propertySource : propertySources) {
                System.out.println("name =  " + propertySource.getName() + "\nsource = " + propertySource
                        .getSource().getClass() + "\n");
            }

            assertEquals(KEY_VAULT_VALUE, app.getProperty("key"));
            app.close();
            log.info("--------------------->test over");
        }
    }

    @Test
    public void keyVaultAsPropertySourceWithSpecificKeys() {
        try (AppRunner app = new AppRunner(DumbApp.class)) {
            app.property("azure.keyvault.enabled", "true");
            app.property("azure.keyvault.uri", vault.vaultUri());
            app.property("azure.keyvault.client-id", access.clientId());
            app.property("azure.keyvault.client-key", access.clientSecret());
            app.property("azure.keyvault.tenant-id", access.tenant());
            app.property("azure.keyvault.secret.keys", "key , azure-cosmosdb-key");

            app.start();
            assertEquals(KEY_VAULT_VALUE, app.getProperty("key"));
            app.close();
            log.info("--------------------->test over");
        }
    }

    @Test
    public void keyVaultWithAppServiceMSI() {
        final AppServiceTool appServiceTool = new AppServiceTool(access);

        final Map<String, String> appSettings = new HashMap<>();
        appSettings.put("AZURE_KEYVAULT_URI", vault.vaultUri());

        final WebApp appService = appServiceTool.createAppService(resourceGroupName, prefix, appSettings);

        // Grant System Assigned MSI access to key vault
        KeyVaultTool.grantSystemAssignedMSIAccessToKeyVault(vault,
                appService.systemAssignedManagedServiceIdentityPrincipalId());

        // Deploy zip
        // Add retry logic here to avoid Kudu's socket timeout issue.
        // More details: https://github.com/Microsoft/azure-maven-plugins/issues/339
        int retryCount = 0;
        final File zipFile = new File(TEST_KEYVAULT_APP_ZIP_PATH);
        while (retryCount < DEFAULT_MAX_RETRY_TIMES) {
            retryCount += 1;
            try {
                appService.zipDeploy(zipFile);
                log.info(String.format("Successfully deployed the artifact to https://%s",
                        appService.defaultHostName()));
                break;
            } catch (Exception e) {
                log.debug(
                        String.format("Exception occurred when deploying the zip package: %s, " +
                                "retrying immediately (%d/%d)", e.getMessage(), retryCount, DEFAULT_MAX_RETRY_TIMES));
            }
        }

        // Restart App Service
        log.info("restarting app service...");
        appService.restart();
        log.info("restarting app service finished...");

        try {
            Thread.sleep(60 * 1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final String resourceUrl = "https://" + appService.name() + ".azurewebsites.net" + "/get";
        // warm up
        final ResponseEntity<String> response = curlWithRetry(resourceUrl, 3, 120_000, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(KEY_VAULT_VALUE, response.getBody());
        log.info("--------------------->test app service with MSI over");
    }

    @Test
    public void keyVaultWithVirtualMachineMSI() throws Exception {
        final VirtualMachineTool vmTool = new VirtualMachineTool(access);

        // create virtual machine
        final VirtualMachine vm = vmTool.createVM(resourceGroupName, prefix, VM_USER_NAME, VM_USER_PASSWORD);
        final String host = vm.getPrimaryPublicIPAddress().ipAddress();

        // Grant System Assigned MSI access to key vault
        KeyVaultTool.grantSystemAssignedMSIAccessToKeyVault(vault,
                vm.systemAssignedManagedServiceIdentityPrincipalId());

        // Upload app.jar to virtual machine
        final File file = new File(TEST_KEYVAULT_APP_JAR_PATH);
        if (!file.exists()) {
            throw new FileNotFoundException("There's no file found on " + TEST_KEYVAULT_APP_JAR_PATH);
        }
        try (SSHShell sshShell = SSHShell.open(host, 22, VM_USER_NAME, VM_USER_PASSWORD);
             FileInputStream fis = new FileInputStream(file)) {

            log.info(String.format("Uploading jar file %s", TEST_KEYVAULT_APP_JAR_PATH));
            sshShell.upload(fis, TEST_KEY_VAULT_JAR_FILE_NAME, "", true, "4095");
        }

        // run java application
        final List<String> commands = new ArrayList<>();
        commands.add(String.format("cd /home/%s", VM_USER_NAME));
        commands.add(
                String.
                format("nohup java -jar -Xdebug " +
                                "-Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n " +
                                "-Dazure.keyvault.uri=%s %s &" +
                                " >/log.txt  2>&1"
                        , vault.vaultUri(),
                TEST_KEY_VAULT_JAR_FILE_NAME));
        vmTool.runCommandOnVM(vm, commands);

        final ResponseEntity<String> response = curlWithRetry(
                String.format("http://%s:8080/get", host),
                3,
                60_000,
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(KEY_VAULT_VALUE, response.getBody());
        log.info("key vault value is: {}", response.getBody());
        log.info("--------------------->test virtual machine with MSI over");
    }

    private static <T> ResponseEntity<T> curlWithRetry(String resourceUrl,
                                                    final int retryTimes,
                                                    int sleepMills,
                                                    Class<T> clazz) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ResponseEntity<T> response = ResponseEntity.of(Optional.empty());
        int rt = retryTimes;

        while (rt-- > 0 && httpStatus != HttpStatus.OK) {
            SdkContext.sleep(sleepMills);

            log.info("CURLing " + resourceUrl);

            try {
                response = restTemplate.getForEntity(resourceUrl, clazz);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            httpStatus = response.getStatusCode();
        }
        return response;
    }

    @SpringBootApplication
    public static class DumbApp {}
}
