// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.fluent.models.VirtualMachineInner;
import com.azure.resourcemanager.compute.models.CachingTypes;
import com.azure.resourcemanager.compute.models.DiskCreateOptionTypes;
import com.azure.resourcemanager.compute.models.HardwareProfile;
import com.azure.resourcemanager.compute.models.ImageReference;
import com.azure.resourcemanager.compute.models.ManagedDiskParameters;
import com.azure.resourcemanager.compute.models.NetworkInterfaceReference;
import com.azure.resourcemanager.compute.models.NetworkProfile;
import com.azure.resourcemanager.compute.models.OSDisk;
import com.azure.resourcemanager.compute.models.OSProfile;
import com.azure.resourcemanager.compute.models.StorageAccountTypes;
import com.azure.resourcemanager.compute.models.StorageProfile;
import com.azure.resourcemanager.compute.models.VirtualMachine;
import com.azure.resourcemanager.compute.models.VirtualMachineSizeTypes;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirtualMachineMockTests {
    private static final String NEXT_LINK_PATH = "/nextLink";
    // query contains unescaped space
    private static final String QUERY = "'virtualMachineScaleSet/id' eq 'id'";
    private static final SerializerAdapter SERIALIZER = SerializerFactory.createDefaultManagementSerializerAdapter();
    private final StateHolder stateHolder = new StateHolder();

    @Test
    public void listByVmssByIdWithNextLinkEncoded() {
        WireMockServer mockServer = startMockServer();

        try {
            ComputeManager computeManager = mockComputeManager(mockServer);

            PagedIterable<VirtualMachine> virtualMachines = computeManager.virtualMachines().listByVirtualMachineScaleSetId("/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/javacsmrg97796/providers/Microsoft.Compute/virtualMachineScaleSets/vmss035803b7");
            // 1 element per page, 2 pages in total
            Assertions.assertEquals(2, virtualMachines.stream().count());
            Assertions.assertTrue(stateHolder.firstPageRequested);
            Assertions.assertTrue(stateHolder.secondPageRequested);
        } finally {
            if (mockServer.isRunning()) {
                mockServer.shutdown();
            }
        }
    }

    private ComputeManager mockComputeManager(WireMockServer mockServer) {
        Map<String, String> environment = new HashMap<>();
        environment.put("resourceManagerEndpointUrl", mockServer.baseUrl());
        // just to mitigate npe, no actual use here
        environment.put("microsoftGraphResourceId", mockServer.baseUrl());
        AzureProfile mockProfile = new AzureProfile(UUID.randomUUID().toString(), UUID.randomUUID().toString(), new AzureEnvironment(environment));
        ComputeManager computeManager = ComputeManager.authenticate(new HttpPipelineBuilder().build(), mockProfile);
        return computeManager;
    }

    private WireMockServer startMockServer() {
        ResponseTransformer transformer = new ResponseTransformer() {
            @Override
            public Response transform(Request request, Response response, FileSource fileSource, Parameters parameters) {
                Map<String, Object> responseBody = new HashMap<>();
                Map<String, Object> vm;
                VirtualMachineInner vmInner = mockVmInner();
                try {
                    vm = SERIALIZER.deserialize(SERIALIZER.serialize(vmInner, SerializerEncoding.JSON), Map.class, SerializerEncoding.JSON);
                } catch (IOException e) {
                    return failedResponse(e.getMessage());
                }
                vm.put("name", "vmName");
                if (request.getUrl().contains("Microsoft.Compute/virtualMachines")) {
                    // first page
                    stateHolder.firstPageRequested = true;
                    responseBody.put("value", Collections.singletonList(vm));
                    responseBody.put("nextLink", stateHolder.nextLinkUrl);
                    return successResponse(responseBody);
                } else if (request.getUrl().contains(NEXT_LINK_PATH)) {
                    // next link page
                    stateHolder.secondPageRequested = true;
                    // check the nextLink is actually encoded and is valid URI
                    try {
                        new URI(request.getUrl());
                        responseBody.put("value", Collections.singletonList(vm));
                        return successResponse(responseBody);
                    } catch (URISyntaxException e) {
                        return failedResponse("Next link not encoded: " + request.getUrl());
                    }
                } else {
                    return failedResponse("Unexpected request: " + request.getUrl());
                }
            }

            @Override
            public String getName() {
                return "listByVmssId";
            }
        };

        WireMockServer mockServer = new WireMockServer(WireMockConfiguration
            .options()
            .dynamicPort()
            .extensions(transformer)
            .disableRequestJournal());
        mockServer.stubFor(WireMock.any(WireMock.anyUrl()).willReturn(WireMock.aResponse()));
        mockServer.start();
        stateHolder.nextLinkUrl = String.format("%s%s?filter=%s", mockServer.baseUrl(), NEXT_LINK_PATH, QUERY);
        return mockServer;
    }

    private Response failedResponse(String errorMessage) {
        return new Response.Builder()
            .status(400)
            .body(errorMessage)
            .build();
    }

    private VirtualMachineInner mockVmInner() {
        return new VirtualMachineInner()
            .withLocation("westus")
            .withHardwareProfile(new HardwareProfile().withVmSize(VirtualMachineSizeTypes.STANDARD_D1_V2))
            .withStorageProfile(
                new StorageProfile()
                    .withImageReference(
                        new ImageReference()
                            .withSharedGalleryImageId(
                                "/SharedGalleries/sharedGalleryName/Images/sharedGalleryImageName/Versions/sharedGalleryImageVersionName"))
                    .withOsDisk(
                        new OSDisk()
                            .withName("myVMosdisk")
                            .withCaching(CachingTypes.READ_WRITE)
                            .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                            .withManagedDisk(
                                new ManagedDiskParameters()
                                    .withStorageAccountType(StorageAccountTypes.STANDARD_LRS))))
            .withOsProfile(
                new OSProfile()
                    .withComputerName("myVM")
                    .withAdminUsername("{your-username}")
                    .withAdminPassword("fakeTokenPlaceholder"))
            .withNetworkProfile(
                new NetworkProfile()
                    .withNetworkInterfaces(
                        Arrays
                            .asList(
                                new NetworkInterfaceReference()
                                    .withId(
                                        "/subscriptions/{subscription-id}/resourceGroups/myResourceGroup/providers/Microsoft.Network/networkInterfaces/{existing-nic-name}")
                                    .withPrimary(true))));
    }

    private Response successResponse(Map<String, Object> responseBody) {
        try {
            return new Response.Builder()
                .status(200)
                .body(SERIALIZER.serialize(responseBody, SerializerEncoding.JSON))
                .build();
        } catch (IOException e) {
            return new Response.Builder()
                .status(400)
                .body("Mock server error: " + e.getMessage())
                .build();
        }
    }

    private static class StateHolder {
        // dynamic nextLink url with mock server baseUrl
        private String nextLinkUrl;
        private boolean firstPageRequested;
        private boolean secondPageRequested;
    }
}
