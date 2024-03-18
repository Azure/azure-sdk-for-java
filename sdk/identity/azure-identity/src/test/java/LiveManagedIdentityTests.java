// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.TestBase;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperties;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class LiveManagedIdentityTests extends TestBase {

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
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
    @EnabledIfSystemProperty(named = "os.name", matches = "Linux|Windows")
    public void testManagedIdentityAksDeployment() {

        String os = System.getProperty("os.name");
        System.out.println("OS: " + os);
        //Setup Env
        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        String spClientId = configuration.get("IDENTITY_CLIENT_ID");
        String secret = configuration.get("IDENTITY_CLIENT_SECRET");
        String tenantId = configuration.get("IDENTITY_TENANT_ID");
        String resourceGroup = configuration.get("IDENTITY_RESOURCE_GROUP");
        String aksCluster = configuration.get("IDENTITY_AKS_CLUSTER_NAME");
        String subscriptionId = configuration.get("IDENTITY_SUBSCRIPTION_ID");
        String podName = configuration.get("IDENTITY_AKS_POD_NAME");
        String pathCommand = os.contains("Windows") ? "where" : "which";

        String azPath = runCommand(pathCommand, "az").trim();
        String kubectlPath = runCommand(pathCommand, "kubectl").trim();

        runCommand(azPath, "login",  "--service-principal", "-u", spClientId, "-p", secret, "--tenant", tenantId);
        runCommand(azPath, "account", "set", "--subscription", subscriptionId);
        runCommand(azPath, "aks", "get-credentials", "--resource-group", resourceGroup, "--name", aksCluster,
            "--overwrite-existing");

        String podOutput = runCommand(kubectlPath, "get", "pods", "-o", "jsonpath='{.items[0].metadata.name}'");
        assertTrue(podOutput.contains(podName), "Pod name not found in the output");

        String output = runCommand(kubectlPath, "exec", "-it", podName, "--", "java", "-jar", "/identity-test.jar");
        Assertions.assertTrue(output.contains("Successfully retrieved managed identity tokens"), "Failed to get response from AKS");
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "AZURE_TEST_MODE", matches = "LIVE")
    public void testManagedIdentityVmDeployment() {

        String os = System.getProperty("os.name");
        System.out.println("OS: " + os);
        //Setup Env
        Configuration configuration = Configuration.getGlobalConfiguration().clone();

        String spClientId = configuration.get("IDENTITY_CLIENT_ID");
        String secret = configuration.get("IDENTITY_CLIENT_SECRET");
        String tenantId = configuration.get("IDENTITY_TENANT_ID");
        String resourceGroup = configuration.get("IDENTITY_RESOURCE_GROUP");
        String subscriptionId = configuration.get("IDENTITY_SUBSCRIPTION_ID");
        String vmName = configuration.get("IDENTITY_VM_NAME");
        String storageAcccountName = configuration.get("IDENTITY_STORAGE_NAME_1");

        boolean isWindows = os.contains("Windows");

        String azPath = runCommand(isWindows ? "where" : "which", "az").trim();
        azPath = isWindows ? extractAzCmdPath(azPath) : azPath;

        runCommand(azPath, "login",  "--service-principal", "-u", spClientId, "-p", secret, "--tenant", tenantId);
        runCommand(azPath, "account", "set", "--subscription", subscriptionId);

        String vmBlob = String.format("https://%s.blob.core.windows.net/vmcontainer/testfile.jar", storageAcccountName);
        String script = String.format("curl '%s' -o ./testfile.jar && java -jar ./testfile.jar", vmBlob);

        String output = runCommand(azPath, "vm", "run-command", "invoke", "-n", vmName, "-g", resourceGroup,
            "--command-id", "RunShellScript", "--scripts", script);

        Assertions.assertTrue(output.contains("Successfully retrieved managed identity tokens"),
            "Failed to get response from AKS");
    }

    private String runCommand(String... args) {
        try {
            StringBuilder command = new StringBuilder();
            for (String arg : args) {
                command.append(arg).append(" ");
            }
            System.out.println("Running command: " + command);
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            // Set timeout
            boolean finished = process.waitFor(300, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                throw new RuntimeException("Process execution timeout");
            }
            // Read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }
            System.out.println("Output:" + System.lineSeparator() + output);
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
