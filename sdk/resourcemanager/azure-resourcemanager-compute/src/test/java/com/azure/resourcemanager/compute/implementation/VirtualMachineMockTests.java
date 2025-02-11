// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.compute.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.models.AzureCloud;
import com.azure.core.test.http.MockHttpResponse;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.io.IOException;
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
    private static final String HOST = "http://localhost:3000";
    private final StateHolder stateHolder = new StateHolder();

    @Test
    public void listByVmssByIdWithNextLinkEncoded() {

        ComputeManager computeManager = mockComputeManager();

        PagedIterable<VirtualMachine> virtualMachines = computeManager.virtualMachines()
            .listByVirtualMachineScaleSetId(
                "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/javacsmrg97796/providers/Microsoft.Compute/virtualMachineScaleSets/vmss035803b7");
        // 1 element per page, 2 pages in total
        Assertions.assertEquals(2, virtualMachines.stream().count());
        Assertions.assertTrue(stateHolder.firstPageRequested);
        Assertions.assertTrue(stateHolder.secondPageRequested);
    }

    private ComputeManager mockComputeManager() {
        HttpClient httpClient = mockHttpClient();
        AzureProfile mockProfile = new AzureProfile(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
            AzureCloud.AZURE_PUBLIC_CLOUD);
        ComputeManager computeManager
            = ComputeManager.authenticate(new HttpPipelineBuilder().httpClient(httpClient).build(), mockProfile);
        stateHolder.nextLinkUrl = String.format("%s%s?filter=%s", HOST, NEXT_LINK_PATH, QUERY);
        return computeManager;
    }

    private HttpClient mockHttpClient() {
        Map<String, Object> responseBody = new HashMap<>();
        Map<String, Object> vm;
        VirtualMachineInner vmInner = mockVmInner();
        try {
            vm = SERIALIZER.deserialize(SERIALIZER.serialize(vmInner, SerializerEncoding.JSON), Map.class,
                SerializerEncoding.JSON);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        vm.put("name", "vmName");
        return request -> {
            if (request.getUrl().getPath().contains("Microsoft.Compute/virtualMachines")) {
                // first page
                stateHolder.firstPageRequested = true;
                responseBody.put("value", Collections.singletonList(vm));
                responseBody.put("nextLink", stateHolder.nextLinkUrl);
                return successResponse(request, responseBody);
            } else if (request.getUrl().getPath().contains(NEXT_LINK_PATH)) {
                // next link page
                if (stateHolder.secondPageRequested) {
                    return successResponse(request, new HashMap<>());
                }
                stateHolder.secondPageRequested = true;
                // check the nextLink is actually encoded and is valid URI
                responseBody.put("value", Collections.singletonList(vm));
                return successResponse(request, responseBody);
            } else {
                return failedResponse(request, "Unexpected request: " + request.getUrl());
            }
        };
    }

    private static Mono<HttpResponse> failedResponse(HttpRequest request, String errorMessage) {
        return Mono.just(new MockHttpResponse(request, 400, errorMessage));
    }

    private static VirtualMachineInner mockVmInner() {
        return new VirtualMachineInner().withLocation("westus")
            .withHardwareProfile(new HardwareProfile().withVmSize(VirtualMachineSizeTypes.STANDARD_D1_V2))
            .withStorageProfile(new StorageProfile().withImageReference(new ImageReference().withSharedGalleryImageId(
                "/SharedGalleries/sharedGalleryName/Images/sharedGalleryImageName/Versions/sharedGalleryImageVersionName"))
                .withOsDisk(new OSDisk().withName("myVMosdisk")
                    .withCaching(CachingTypes.READ_WRITE)
                    .withCreateOption(DiskCreateOptionTypes.FROM_IMAGE)
                    .withManagedDisk(
                        new ManagedDiskParameters().withStorageAccountType(StorageAccountTypes.STANDARD_LRS))))
            .withOsProfile(new OSProfile().withComputerName("myVM")
                .withAdminUsername("{your-username}")
                .withAdminPassword("fakeTokenPlaceholder"))
            .withNetworkProfile(new NetworkProfile().withNetworkInterfaces(Arrays.asList(new NetworkInterfaceReference()
                .withId(
                    "/subscriptions/{subscription-id}/resourceGroups/myResourceGroup/providers/Microsoft.Network/networkInterfaces/{existing-nic-name}")
                .withPrimary(true))));
    }

    private Mono<HttpResponse> successResponse(HttpRequest request, Map<String, Object> responseBody) {
        return Mono.just(new MockHttpResponse(request, 200, responseBody));
    }

    private static class StateHolder {
        // dynamic nextLink url with mock server baseUrl
        private String nextLinkUrl;
        private boolean firstPageRequested;
        private boolean secondPageRequested;
    }
}
