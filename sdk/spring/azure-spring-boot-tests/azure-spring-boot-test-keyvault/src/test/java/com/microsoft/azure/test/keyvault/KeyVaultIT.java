// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.test.keyvault;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.compute.RunCommandInput;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.test.management.ClientSecretAccess;
import com.microsoft.azure.test.utils.AppRunner;
import com.microsoft.azure.test.utils.MavenBasedProject;
import com.microsoft.azure.test.utils.SSHShell;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class KeyVaultIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyVaultIT.class);
    private static final String AZURE_KEYVAULT_ENDPOINT = System.getenv("AZURE_KEYVAULT_ENDPOINT");
    private static final String KEY_VAULT_SECRET_VALUE = System.getenv("KEY_VAULT_SECRET_VALUE");
    private static final String KEY_VAULT_SECRET_NAME = System.getenv("KEY_VAULT_SECRET_NAME");
    private static final String SPRING_RESOURCE_GROUP = System.getenv("SPRING_RESOURCE_GROUP");
    private static final String APP_SERVICE_NAME = System.getenv("APP_SERVICE_NAME");
    private static final String VM_NAME = System.getenv("VM_NAME");
    private static final String VM_USER_USERNAME = System.getenv("VM_USER_USERNAME");
    private static final String VM_USER_PASSWORD = System.getenv("VM_USER_PASSWORD");
    private static final int DEFAULT_MAX_RETRY_TIMES = 3;
    private static final Azure AZURE;
    private static final ClientSecretAccess CLIENT_SECRET_ACCESS;
    private static final RestTemplate REST_TEMPLATE = new RestTemplate();

    static {
        CLIENT_SECRET_ACCESS = ClientSecretAccess.load();
        AZURE = Azure.authenticate(CLIENT_SECRET_ACCESS.credentials())
            .withSubscription(CLIENT_SECRET_ACCESS.subscription());
    }

    @Test
    public void keyVaultAsPropertySource() {
        try (AppRunner app = new AppRunner(DumbApp.class)) {
            app.property("azure.keyvault.enabled", "true");
            app.property("azure.keyvault.uri", AZURE_KEYVAULT_ENDPOINT);
            app.property("azure.keyvault.client-id", CLIENT_SECRET_ACCESS.clientId());
            app.property("azure.keyvault.client-key", CLIENT_SECRET_ACCESS.clientSecret());
            app.property("azure.keyvault.tenant-id", CLIENT_SECRET_ACCESS.tenantId());


            final ConfigurableApplicationContext dummy = app.start("dummy");
            final ConfigurableEnvironment environment = dummy.getEnvironment();
            final MutablePropertySources propertySources = environment.getPropertySources();
            for (final PropertySource<?> propertySource : propertySources) {
                System.out.println("name =  " + propertySource.getName() + "\nsource = " + propertySource
                    .getSource().getClass() + "\n");
            }

            assertEquals(KEY_VAULT_SECRET_VALUE, app.getProperty(KEY_VAULT_SECRET_NAME));
            LOGGER.info("--------------------->test over");
        }
    }

    @Test
    public void keyVaultAsPropertySourceWithSpecificKeys() {
        try (AppRunner app = new AppRunner(DumbApp.class)) {
            app.property("azure.keyvault.enabled", "true");
            app.property("azure.keyvault.uri", AZURE_KEYVAULT_ENDPOINT);
            app.property("azure.keyvault.client-id", CLIENT_SECRET_ACCESS.clientId());
            app.property("azure.keyvault.client-key", CLIENT_SECRET_ACCESS.clientSecret());
            app.property("azure.keyvault.tenant-id", CLIENT_SECRET_ACCESS.tenantId());

            app.property("azure.keyvault.secret.keys", KEY_VAULT_SECRET_NAME + " , azure-cosmosdb-key");
            LOGGER.info("====" + KEY_VAULT_SECRET_NAME + " , azure-cosmosdb-key");

            app.start();
            assertEquals(KEY_VAULT_SECRET_VALUE, app.getProperty(KEY_VAULT_SECRET_NAME));
            LOGGER.info("--------------------->test over");
        }
    }

    @Test
    public void keyVaultWithAppServiceMSI() {
        final WebApp webApp = AZURE
            .webApps()
            .getByResourceGroup(SPRING_RESOURCE_GROUP, APP_SERVICE_NAME);


        final MavenBasedProject app = new MavenBasedProject("../azure-spring-boot-test-application");
        app.packageUp();

        // Deploy zip
        // Add retry logic here to avoid Kudu's socket timeout issue.
        // More details: https://github.com/Microsoft/azure-maven-plugins/issues/339
        int retryCount = 0;
        final File zipFile = new File(app.zipFile());
        while (retryCount < DEFAULT_MAX_RETRY_TIMES) {
            retryCount += 1;
            try {
                webApp.zipDeploy(zipFile);
                LOGGER.info(String.format("Deployed the artifact to https://%s", webApp.defaultHostName()));
                break;
            } catch (Exception e) {
                LOGGER.error(String.format("Exception occurred when deploying the zip package: %s, "
                    + "retrying immediately (%d/%d)", e.getMessage(), retryCount, DEFAULT_MAX_RETRY_TIMES));
            }
        }

        // Restart App Service
        LOGGER.info("restarting app service...");
        webApp.restart();
        LOGGER.info("restarting app service finished...");

        final String resourceUrl = "https://" + webApp.name() + ".azurewebsites.net" + "/get";
        // warm up
        final ResponseEntity<String> response = curlWithRetry(resourceUrl, 3, 120_000, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(KEY_VAULT_SECRET_VALUE, response.getBody());
        LOGGER.info("--------------------->test app service with MSI over");
    }

    @Test
    @Ignore
    public void keyVaultWithVirtualMachineMSI() throws Exception {
        final VirtualMachine vm = AZURE.virtualMachines().getByResourceGroup(SPRING_RESOURCE_GROUP, VM_NAME);

        final String host = vm.getPrimaryPublicIPAddress().ipAddress();

        // Upload app.jar to virtual machine
        final MavenBasedProject app = new MavenBasedProject("../azure-spring-boot-test-application");
        app.packageUp();

        final File file = new File(app.artifact());

        if (!file.exists()) {
            throw new FileNotFoundException("There's no app.jar file found.");
        }
        try (SSHShell sshShell = SSHShell.open(host, 22, VM_USER_USERNAME, VM_USER_PASSWORD);
             FileInputStream fis = new FileInputStream(file)) {
            LOGGER.info("Uploading jar file...");
            sshShell.upload(fis, "app.jar", "", true, "4095");
        }

        // run java application
        final List<String> commands = new ArrayList<>();
        commands.add(String.format("cd /home/%s", VM_USER_USERNAME));
        commands.add(String.format("nohup java -jar -Xdebug "
                + "-Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=n "
                + "-Dazure.keyvault.uri=%s %s &"
                + " >/log.txt  2>&1",
            AZURE_KEYVAULT_ENDPOINT,
            "app.jar"));

        vm.runCommand(new RunCommandInput().withCommandId("RunShellScript").withScript(commands));

        final ResponseEntity<String> response = curlWithRetry(
            String.format("http://%s:8080/get", host),
            3,
            60_000,
            String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(KEY_VAULT_SECRET_VALUE, response.getBody());
        LOGGER.info("key vault value is: {}", response.getBody());
        LOGGER.info("--------------------->test virtual machine with MSI over");
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

            LOGGER.info("CURLing " + resourceUrl);

            try {
                response = REST_TEMPLATE.getForEntity(resourceUrl, clazz);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }

            httpStatus = response.getStatusCode();
        }
        return response;
    }

    @SpringBootApplication
    public static class DumbApp {

    }
}
