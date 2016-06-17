package com.microsoft.azure;

import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.compute.KnownWindowsVirtualMachineImage;
import com.microsoft.azure.management.compute.VirtualMachine;
import com.microsoft.azure.management.compute.VirtualMachines;
import com.microsoft.azure.management.compute.implementation.api.VirtualMachineSizeTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;

public class TestVirtualMachine extends TestTemplate<VirtualMachine, VirtualMachines> {
    @Override
    public VirtualMachine createResource(VirtualMachines virtualMachines) throws Exception {
        final String vmName = "vm" + this.testId;
        final CountDownLatch latch = new CountDownLatch(1);
        final VirtualMachine[] vms = new VirtualMachine[1];
        virtualMachines.define(vmName)
                .withRegion(Region.US_EAST)
                .withNewGroup()
                .withNewPrimaryNetwork("10.0.0.0/28")
                .withPrimaryPrivateIpAddressDynamic()
                .withoutPrimaryPublicIpAddress()
                .withPopularWindowsImage(KnownWindowsVirtualMachineImage.WINDOWS_SERVER_2012_R2_DATACENTER)
                .withAdminUserName("testuser")
                .withPassword("12NewPA$$w0rd!")
                .withSize(VirtualMachineSizeTypes.STANDARD_D1_V2)
                .createAsync(new ServiceCallback<VirtualMachine>() {
                    @Override
                    public void failure(Throwable t) {
                        fail();
                    }

                    @Override
                    public void success(ServiceResponse<VirtualMachine> result) {
                        vms[0] = result.getBody();
                        latch.countDown();
                    }
                });
        latch.await(12, TimeUnit.MINUTES);
        return vms[0];
    }

    @Override
    public VirtualMachine updateResource(VirtualMachine resource) throws Exception {
        return null;
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
                .withLogLevel(HttpLoggingInterceptor.Level.BODY)
                .authenticate(credentials)
                .withDefaultSubscription();
        runTest(azure.virtualMachines(), azure.resourceGroups());
    }
}
