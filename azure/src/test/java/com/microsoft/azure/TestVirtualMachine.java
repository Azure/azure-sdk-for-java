/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure;

import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachineSizeTypes;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.Test;
import rx.Subscriber;
import rx.functions.Action1;

public class TestVirtualMachine extends TestTemplate<VirtualMachine, VirtualMachines> {
    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = "vm" + this.testId;
        final VirtualMachine[] vms = new VirtualMachine[1];
        final SettableFuture<VirtualMachine> future = SettableFuture.create();
        virtualMachines.define(vmName)
                .withRegion(Region.US_EAST)
                .withNewResourceGroup()
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withAdminUserName("testuser")
                .withPassword("12NewPA$$w0rd!")
                .withSize(VirtualMachineSizeTypes.STANDARD_D1_V2)
                .createAsync()
                .subscribe(new Action1<VirtualMachine>() {
                    @Override
                    public void call(VirtualMachine virtualMachine) {
                        future.set(virtualMachine);
                    }
                });
        vms[0] = future.get();
        return vms[0];
    }

    @Override
    public VirtualMachine updateResource(VirtualMachine resource) throws Exception {
        resource = resource.update()
                .withSize(VirtualMachineSizeTypes.STANDARD_D3_V2)
                .withNewDataDisk(100)
                .apply();
        return resource;
    }

    @Override
    public void print(VirtualMachine virtualMachine) {
        TestUtils.print(virtualMachine);
    }

    @Test
    public void run() throws Exception {
        ApplicationTokenCredentials credentials = new ApplicationTokenCredentials(
                System.getenv("client-id"),
                System.getenv("domain"),
                System.getenv("secret"),
                null);

        Azure azure = Azure.configure()
                .withLogLevel(HttpLoggingInterceptor.Level.NONE)
                .authenticate(credentials)
                .withDefaultSubscription();
        runTest(azure.virtualMachines(), azure.resourceGroups());
    }
}
