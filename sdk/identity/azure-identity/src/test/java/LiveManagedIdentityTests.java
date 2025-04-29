// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.implementation.Retry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class LiveManagedIdentityTests {
    private static final ClientLogger LOGGER = new ClientLogger(LiveManagedIdentityTests.class);

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
    @Retry(maxRetries = 3)
    public void testManagedIdentityFuncDeployment() {

        HttpClient client = HttpClient.createDefault();
        String functionUrl = "https://" + System.getenv("IDENTITY_FUNCTION_NAME") + ".azurewebsites.net/api/mitest";
        HttpRequest request = new HttpRequest(HttpMethod.GET, functionUrl);
        try (HttpResponse httpResponse = client.send(request).block()) {
            if (httpResponse.getStatusCode() != 200) {
                fail("Failed to get response from function app");
            }
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
    @Retry(maxRetries = 3)
    public void testManagedIdentityWebAppDeployment() {
        HttpClient client = HttpClient.createDefault();
        String functionUrl = "https://" + System.getenv("IDENTITY_WEBAPP_NAME") + ".azurewebsites.net/mitest";
        ClientLogger logger = new ClientLogger(LiveManagedIdentityTests.class);
        logger.log(LogLevel.INFORMATIONAL, () -> "webappURL: " + functionUrl);
        HttpRequest request = new HttpRequest(HttpMethod.GET, functionUrl);
        try (HttpResponse httpResponse = client.send(request).block()) {
            if (httpResponse.getStatusCode() != 200) {
                fail("Failed to get response from web app");
            }
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
    @EnabledIfSystemProperty(named = "os.name", matches = "Linux")
    @Timeout(value = 15, unit = TimeUnit.MINUTES)
    @Retry(maxRetries = 3)
    public void testManagedIdentityAksDeployment() {
        LOGGER.log(LogLevel.INFORMATIONAL, () -> "Environment: " + System.getenv("IDENTITY_ENVIRONMENT"));
        String os = System.getProperty("os.name");
        LOGGER.log(LogLevel.INFORMATIONAL, () -> "OS: " + os);
        //Setup Env
        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        String podName = configuration.get("IDENTITY_AKS_POD_NAME");
        String pathCommand = os.contains("Windows") ? "where" : "which";

        String kubectlPath = runCommand(pathCommand, "kubectl").trim();

        String podOutput = runCommand(kubectlPath, "get", "pods", "-o", "jsonpath='{.items[0].metadata.name}'");
        assertTrue(podOutput.contains(podName), "Pod name not found in the output");

        String output = runCommand(kubectlPath, "exec", "-it", podName, "--", "java", "-jar", "/identity-test.jar");

        assertTrue(output.contains("Successfully retrieved managed identity tokens"),
            "Failed to get response from AKS");
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
    @EnabledIfEnvironmentVariable(named = "IDENTITY_ENVIRONMENT", matches = "AzureCloud")
    @EnabledIfSystemProperty(named = "os.name", matches = "Linux")
    @Timeout(value = 15, unit = TimeUnit.MINUTES)
    @Retry(maxRetries = 3)
    public void testManagedIdentityVmDeployment() {
        LOGGER.log(LogLevel.INFORMATIONAL, () -> "Environment: " + System.getenv("IDENTITY_ENVIRONMENT"));
        String os = System.getProperty("os.name");
        LOGGER.log(LogLevel.INFORMATIONAL, () -> "OS: " + os);
        //Setup Env
        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        String spClientId = configuration.get("AZURESUBSCRIPTION_CLIENT_ID");
        String oidc = configuration.get("AZ_OIDC_TOKEN");
        String tenantId = configuration.get("AZURESUBSCRIPTION_TENANT_ID");
        String resourceGroup = configuration.get("IDENTITY_RESOURCE_GROUP");
        String subscriptionId = configuration.get("IDENTITY_SUBSCRIPTION_ID");
        String vmName = configuration.get("IDENTITY_VM_NAME");
        String storageAcccountName = configuration.get("IDENTITY_STORAGE_NAME_1");

        boolean isWindows = os.contains("Windows");

        String azPath = runCommand(isWindows ? "where" : "which", "az").trim();
        azPath = isWindows ? extractAzCmdPath(azPath) : azPath;

        runCommand(azPath, "login", "--federated-token", oidc, "--service-principal", "-u", spClientId, "--tenant",
            tenantId);
        runCommand(azPath, "account", "set", "--subscription", subscriptionId);

        String storageKey = runCommand(azPath, "storage", "account", "keys", "list", "--account-name",
            storageAcccountName, "--resource-group", resourceGroup, "--query", "[0].value", "--output", "tsv").trim();

        String expiry = LocalDate.now().plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String sasToken = runCommand(azPath, "storage", "blob", "generate-sas", "--account-name", storageAcccountName,
            "--account-key", "\"" + storageKey + "\"", "--container-name", "vmcontainer", "--name", "testfile.jar",
            "--permissions", "r", "--expiry", expiry, "--https-only", "--output", "tsv").trim();

        String vmBlob = String.format("https://%s.blob.core.windows.net/vmcontainer/testfile.jar?%s",
            storageAcccountName, sasToken);
        String script = String.format("curl \'%s\' -o ./testfile.jar && java -jar ./testfile.jar", vmBlob);

        LOGGER.log(LogLevel.INFORMATIONAL, () -> "Script: " + script);

        String output = runCommand(azPath, "vm", "run-command", "invoke", "-n", vmName, "-g", resourceGroup,
            "--command-id", "RunShellScript", "--scripts", script);

        assertTrue(output.contains("Successfully retrieved managed identity tokens"), "Failed to get response from VM");
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
    @Retry(maxRetries = 3)
    public void callGraphWithClientSecret() {
        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        String multiTenantId = "54826b22-38d6-4fb2-bad9-b7b93a3e9c5a";
        String multiClientId = "4fc2b07b-9d91-4a4a-86e0-96d5b9145075";
        String multiClientSecret = configuration.get("AZURE_IDENTITY_MULTI_TENANT_CLIENT_SECRET");

        ClientSecretCredential credential = new ClientSecretCredentialBuilder().tenantId(multiTenantId)
            .clientId(multiClientId)
            .clientSecret(multiClientSecret)
            .build();

        AccessToken accessToken
            = credential.getTokenSync(new TokenRequestContext().addScopes("https://graph.microsoft.com/.default"));

        assertTrue(accessToken != null, "Failed to get access token");
    }

    private String runCommand(String... args) {
        try {
            StringBuilder command = new StringBuilder();
            for (String arg : args) {
                command.append(arg).append(" ");
            }
            LOGGER.log(LogLevel.INFORMATIONAL, () -> "Running command: " + command);
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            process.waitFor();
            // Read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }
            LOGGER.log(LogLevel.INFORMATIONAL, () -> "Output:" + System.lineSeparator() + output);
            return output.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String extractAzCmdPath(String output) {
        String[] lines = output.split("\\r?\\n");
        for (String line : lines) {
            if (line.endsWith(".cmd")) {
                return line;
            }
        }
        return output;
    }
}
