package com.microsoft.azure.management.compute;

import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.RestClient;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.implementation.ComputeManagementClientImpl;
import com.microsoft.azure.management.compute.implementation.VirtualMachineExtensionImageInner;
import com.microsoft.rest.ServiceResponse;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.Test;

import java.io.File;
import java.util.List;

public class VirtualMachineExtensionImageOperationsTests {
    @Test
    public void firstTest() throws Exception {
        RestClient.Builder.Buildable restClientBuilder = AzureEnvironment.AZURE.newRestClientBuilder();
        RestClient restClient = restClientBuilder
                .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                .withCredentials(ApplicationTokenCredentials.fromFile(new File("c:\\my.azureauth")))
                .build();

        ComputeManagementClientImpl computeClient = new ComputeManagementClientImpl(restClient).withSubscriptionId("1c638cf4-608f-4ee6-b680-c329e824c3a8");
        try {
            //ServiceResponse<VirtualMachineExtensionImageInner> response =computeClient.virtualMachineExtensionImages()
            //        .get("eastus", "Microsoft.Azure.Extensions", "DockerExtension", "1.2.0");

            ServiceResponse<VirtualMachineExtensionImageInner> response =computeClient.virtualMachineExtensionImages()
                    .get("eastus", "Microsoft.OSTCExtensions", "LinuxDiagnostic", "2.3.9007");

            VirtualMachineExtensionImageInner ext =  response.getBody();

            ServiceResponse<List<VirtualMachineExtensionImageInner>> response1 =computeClient.virtualMachineExtensionImages()
                    .listTypes("eastus", "Microsoft.OSTCExtensions");

            ServiceResponse<List<VirtualMachineExtensionImageInner>> response2 =computeClient.virtualMachineExtensionImages()
                    .listVersions("eastus", "Microsoft.OSTCExtensions", "LinuxDiagnostic");


        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
