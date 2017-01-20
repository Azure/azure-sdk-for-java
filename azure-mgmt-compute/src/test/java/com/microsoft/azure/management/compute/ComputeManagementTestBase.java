package com.microsoft.azure.management.compute;

import com.jcraft.jsch.JSchException;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.network.implementation.NetworkManager;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.rest.LogLevel;
import com.microsoft.rest.RestClient;
import org.junit.Assert;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class ComputeManagementTestBase {
    protected static ResourceManager resourceManager;
    protected static ComputeManager computeManager;
    protected static NetworkManager networkManager;
    protected static StorageManager storageManager;

    public static void createClients() {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("client-id"),
                System.getenv("domain"),
                System.getenv("secret"),
                AzureEnvironment.AZURE);

        RestClient restClient = new RestClient.Builder()
                .withBaseUrl(AzureEnvironment.AZURE, AzureEnvironment.Endpoint.RESOURCE_MANAGER)
                .withCredentials(credentials)
                .withLogLevel(LogLevel.BODY_AND_HEADERS)
                .build();

        resourceManager = ResourceManager
                .authenticate(restClient)
                .withSubscription(System.getenv("subscription-id"));

        computeManager = ComputeManager
                .authenticate(restClient, System.getenv("subscription-id"));

        networkManager = NetworkManager
                .authenticate(restClient, System.getenv("subscription-id"));

        storageManager = StorageManager
                .authenticate(restClient, System.getenv("subscription-id"));
    }

    protected void deprovisionLinuxVM(String host, int port, String userName, String password) {
        SSHShell shell = null;
        try {
            System.out.println("Trying to de-provision");
            shell = SSHShell.open(host, port, userName, password);
            List<String> deprovisionCommand = new ArrayList<>();
            deprovisionCommand.add("sudo waagent -deprovision+user --force");
            String output = shell.runCommands(deprovisionCommand);
            System.out.println(output);
        } catch (JSchException jSchException) {
            Assert.assertNull(jSchException.getMessage(), jSchException);
        } catch (IOException ioException) {
            Assert.assertNull(ioException.getMessage(), ioException);
        } catch (Exception exception) {
            Assert.assertNull(exception.getMessage(), exception);
        } finally {
            if (shell != null) {
                shell.close();
            }
        }
    }

    protected  void sleep(long milli) {
        try {
            Thread.sleep(milli);
        } catch (InterruptedException exception) {
        }
    }

    protected void writeToFile(String content) {
        try
        {
            String filename= "C:\\rgnames\\rg-names.txt";
            FileWriter fw = new FileWriter(filename,true);
            fw.write("\n" + content + "\n");
            fw.close();
        } catch(IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }
}
