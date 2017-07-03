/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.resources.fluentcore.arm.DeleteOperationMonitor;
import com.microsoft.azure.management.resources.fluentcore.arm.DeletePollingState;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.RestClient;
import org.junit.Ignore;
import org.junit.Test;
import rx.Observable;

public class VirtualMachineBeginDeleteTests extends ComputeManagementTest {
    private static String RG_NAME = "";
    private static Region region = Region.US_WEST_CENTRAL;

    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("javacsmrg", 15);
        super.initializeClients(restClient, defaultSubscription, domain);
    }
    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    @Ignore
    public void canMonitorVirtualMachineDeletion() throws Exception {
        final String vmName = generateRandomResourceName("vm-", 10);

        VirtualMachine virtualMachine = computeManager.virtualMachines().define(vmName)
                .withRegion(region)
                .withNewResourceGroup(RG_NAME)
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIPAddressDynamic()
                .withoutPrimaryPublicIPAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withAdminUsername("tester")
                .withAdminPassword("Abcdef.123456!")
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                .create();

        // make delete request
        //
        Observable<DeleteOperationMonitor> monitorObservable = virtualMachine.beginDeleteAsync();
        // Gets the monitor (This will fire delete request and return immediately as the initial response)
        //
        DeleteOperationMonitor monitor = monitorObservable.toBlocking().last();
        // Gets the progress tracking observable from monitor
        //
        Observable<DeletePollingState> pollingObservable = monitor.toObservable();
        // wait for next 3 polling state and get the last one
        //
        DeletePollingState savedState = pollingObservable.take(2).toBlocking().last();
        //TODO: Save the state to file or db.
        //
        // Creates monitor object from the saved state
        //
        monitorObservable = DeleteOperationMonitor.fromPollingState(computeManager.inner().restClient(), savedState);
        // Gets the monitor (This will NOT initiate any network calls)
        //
        monitor = monitorObservable.toBlocking().last();
        // Synchronously wait for delete to complete or fail
        //
        monitor.toObservable().toBlocking().last();
    }
}
