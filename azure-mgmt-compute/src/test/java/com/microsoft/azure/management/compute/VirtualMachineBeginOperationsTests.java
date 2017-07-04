/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.PollingState;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.RestClient;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import rx.Single;
import rx.functions.Func1;

public class VirtualMachineBeginOperationsTests extends ComputeManagementTest {
    private static String RG_NAME = "";
    private static final Region REGION = Region.US_SOUTH_CENTRAL;
    private static final String VMNAME = "javavm";

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Ignore("Requires the runtime changes to be merged https://github.com/Azure/autorest-clientruntime-for-java/pull/176")
    @Test
    public void canBeginVirtualMachineCaptureAndPoll() throws Exception {
        // Create a virtual machine, power-off and generalize
        //
        VirtualMachine virtualMachine = createVirtualMachine();
        virtualMachine.powerOff();
        virtualMachine.generalize();

        // STEP-1
        // Gets the observable representing the deferred capture action
        //
        Single<PollingState<VirtualMachineCaptureResult>> ObservableState = virtualMachine.beginCaptureAsync("vhds", "capt", true);

        // STEP-2
        // Initiate the capture by running the deferred action and return immediately
        //
        PollingState<VirtualMachineCaptureResult> capturePollingState = ObservableState.toBlocking().value();

        Assert.assertNotNull(capturePollingState);
        Assert.assertNotNull(capturePollingState.status());
        // Capture is an LRO which is pollable using azure async operation uri
        Assert.assertNotNull(capturePollingState.azureAsyncOperationHeaderLink());

        // STEP-3
        // Poll capture long running operation (LRO)
        // Each intermediate LRO state will be emitted to the map function
        //
        PollingState<VirtualMachineCaptureResult> captureFinalState = virtualMachine.pollCaptureAsync(capturePollingState)
                .map(new Func1<PollingState<VirtualMachineCaptureResult>, PollingState<VirtualMachineCaptureResult>>() {
                    @Override
                    public PollingState<VirtualMachineCaptureResult> call(PollingState<VirtualMachineCaptureResult> pollingState) {
                        Assert.assertNotNull(pollingState.status());
                        return pollingState;
                    }
                })
                .toBlocking()
                .last();

        Assert.assertNotNull(captureFinalState);
        Assert.assertNotNull(captureFinalState.resource());

        // STEP-4
        // Retrieve the capture result
        //
        VirtualMachineCaptureResult captureFinalResult = captureFinalState.resource();
        Assert.assertNotNull(captureFinalResult);
        Assert.assertNotNull(captureFinalResult.template());
    }

    @Ignore("Requires the runtime changes to be merged https://github.com/Azure/autorest-clientruntime-for-java/pull/176")
    @Test
    public void canBeginVirtualMachineCaptureThenSerializeThenDeserializeAndPoll() throws Exception {
        // Create a virtual machine and generalize
        //
        VirtualMachine virtualMachine = createVirtualMachine();
        virtualMachine.powerOff();
        virtualMachine.generalize();

        // STEP-1
        // Gets the observable representing the deferred capture action
        //
        Single<PollingState<VirtualMachineCaptureResult>> ObservableState = virtualMachine.beginCaptureAsync("vhds", "capt", true);

        // STEP-2
        // Initiate the capture by running the deferred action and return immediately
        //
        PollingState<VirtualMachineCaptureResult> capturePollingState = ObservableState.toBlocking().value();

        Assert.assertNotNull(capturePollingState);
        Assert.assertNotNull(capturePollingState.status());
        // Capture is an LRO which is pollable using azure async operation uri
        Assert.assertNotNull(capturePollingState.azureAsyncOperationHeaderLink());

        // STEP-3
        // Serialize the polling state as json
        //
        String serializedPollingState = capturePollingState.serialize();

        // STEP-4
        // Create a PollingState<VirtualMachineCaptureResult> from the serialized json string
        //
        PollingState<VirtualMachineCaptureResult> deserializedPollingState = PollingState.createFromJSONString(serializedPollingState);

        // STEP-5
        // Poll capture long running operation (LRO)
        // Each intermediate LRO state will be emitted to the map function
        //
        PollingState<VirtualMachineCaptureResult> captureFinalState = virtualMachine.pollCaptureAsync(deserializedPollingState)
                .map(new Func1<PollingState<VirtualMachineCaptureResult>, PollingState<VirtualMachineCaptureResult>>() {
                    @Override
                    public PollingState<VirtualMachineCaptureResult> call(PollingState<VirtualMachineCaptureResult> pollingState) {
                        Assert.assertNotNull(pollingState.status());
                        return pollingState;
                    }
                })
                .toBlocking()
                .last();

        Assert.assertNotNull(captureFinalState);
        Assert.assertNotNull(captureFinalState.resource());

        // STEP-6
        // Retrieve the capture result
        //
        VirtualMachineCaptureResult captureFinalResult = captureFinalState.resource();
        Assert.assertNotNull(captureFinalResult);
        Assert.assertNotNull(captureFinalResult.template());
    }

    @Ignore("Requires the runtime changes to be merged https://github.com/Azure/autorest-clientruntime-for-java/pull/176")
    @Test
    public void canBeginVirtualMachineDeleteAndPoll() throws Exception {
        // Create virtual machine
        //
        VirtualMachine virtualMachine = createVirtualMachine();

        // STEP-1
        // Gets the observable representing the deferred delete action
        //
        Single<PollingState<Void>> ObservableState =computeManager.virtualMachines().beginDeleteByIdAsync(virtualMachine.id());

        // STEP-2
        // Initiate the delete by running the deferred action and return immediately
        //
        PollingState<Void> deletePollingState = ObservableState.toBlocking().value();

        Assert.assertNotNull(deletePollingState);
        Assert.assertNotNull(deletePollingState.status());
        // Delete is an LRO which is pollable using azure async operation uri
        Assert.assertNotNull(deletePollingState.azureAsyncOperationHeaderLink());

        // STEP-3
        // Poll capture long running operation (LRO)
        // Each intermediate LRO state will be emitted to the map function
        //
        PollingState<Void> deleteFinalState = computeManager.pollAsync(deletePollingState)
                .map(new Func1<PollingState<Void>, PollingState<Void>>() {
                    @Override
                    public PollingState<Void> call(PollingState<Void> pollingState) {
                        Assert.assertNotNull(pollingState.status());
                        return pollingState;
                    }
                })
                .toBlocking()
                .last();

        Assert.assertNotNull(deleteFinalState);
    }

    @Ignore("Requires the runtime changes to be merged https://github.com/Azure/autorest-clientruntime-for-java/pull/176")
    @Test
    public void canBeginVirtualMachineDeleteThenSerializeThenDeserializeAndPoll() throws Exception {
        VirtualMachine virtualMachine = createVirtualMachine();

        // STEP-1
        // Gets the observable representing the deferred delete action
        //
        Single<PollingState<Void>> ObservableState =computeManager.virtualMachines().beginDeleteByIdAsync(virtualMachine.id());

        // STEP-2
        // Initiate the delete by running the deferred action and return immediately
        //
        PollingState<Void> deletePollingState = ObservableState.toBlocking().value();

        Assert.assertNotNull(deletePollingState);
        Assert.assertNotNull(deletePollingState.status());
        // Delete is an LRO which is pollable using azure async operation uri
        Assert.assertNotNull(deletePollingState.azureAsyncOperationHeaderLink());

        // STEP-3
        // Serialize the polling state as json
        //
        String serializedPollingState = deletePollingState.serialize();

        // STEP-4
        // Create a PollingState<Void> from the serialized json string
        //
        PollingState<Void> deserializedPollingState = PollingState.createFromJSONString(serializedPollingState);

        // STEP-3
        // Poll capture long running operation (LRO)
        // Each intermediate LRO state will be emitted to the map function
        //
        PollingState<Void> deleteFinalState = computeManager.pollAsync(deserializedPollingState)
                .map(new Func1<PollingState<Void>, PollingState<Void>>() {
                    @Override
                    public PollingState<Void> call(PollingState<Void> pollingState) {
                        Assert.assertNotNull(pollingState.status());
                        return pollingState;
                    }
                })
                .toBlocking()
                .last();

        Assert.assertNotNull(deleteFinalState);
    }

    private VirtualMachine createVirtualMachine() {
        return computeManager.virtualMachines()
                .define(VMNAME)
                .withRegion(REGION)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_DATACENTER)
                .withAdminUsername("Foo12")
                .withAdminPassword("abc!@#F0orL")
                .withUnmanagedDisks()
                .withSize(VirtualMachineSizeTypes.STANDARD_D3)
                .withOSDiskCaching(CachingTypes.READ_WRITE)
                .withOSDiskName("javatest")
                .create();
    }
}
