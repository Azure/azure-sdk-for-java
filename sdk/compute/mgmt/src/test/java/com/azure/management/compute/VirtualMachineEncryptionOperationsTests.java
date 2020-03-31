/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.compute;

import com.azure.management.RestClient;
import com.azure.management.resources.fluentcore.arm.Region;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class VirtualMachineEncryptionOperationsTests extends ComputeManagementTest {
    private String RG_NAME = "";
    private Region REGION = Region.US_EAST;
    @Override
    protected void initializeClients(RestClient restClient, String defaultSubscription, String domain) {
        RG_NAME = generateRandomResourceName("vmencryptst", 18);
        super.initializeClients(restClient, defaultSubscription, domain);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().deleteByName(RG_NAME);
    }

    @Test
    @Disabled("Requires manually creating service principal and setting SP credentials in the test")
    public void canEncryptVirtualMachine() {
        // https://docs.microsoft.com/en-us/azure/security/azure-security-disk-encryption
        //
        // KeyVault Resource ID
        String keyVaultId = "KEY_VAULT_ID_HERE";
        // Azure AD service principal client (application) ID
        String aadClientId = "AAD_APPLICATION_ID_HERE";
        // Azure AD service principal client secret
        String aadSecret  = "AAD_CLIENT_SECRET_HERE";

        final String vmName1 = "myvm1";
        final String publicIpDnsLabel = generateRandomResourceName("pip", 20);
        final String uname = "juser";
        final String password = "123tEst!@|ac";
        VirtualMachine virtualMachine = computeManager.virtualMachines()
                .define(vmName1)
                    .withRegion(REGION)
                    .withNewResourceGroup(RG_NAME)
                    .withNewPrimaryNetwork("10.0.0.0/28")
                    .withPrimaryPrivateIPAddressDynamic()
                    .withNewPrimaryPublicIPAddress(publicIpDnsLabel)
                    .withLatestLinuxImage("RedHat", "RHEL", "7.2")
                    .withRootUsername(uname)
                    .withRootPassword(password)
                    .withSize(VirtualMachineSizeTypes.STANDARD_D5_V2)
                    .withOSDiskCaching(CachingTypes.READ_WRITE)
                .create();

        DiskVolumeEncryptionMonitor monitor1 = virtualMachine.diskEncryption().getMonitor();
        Assertions.assertNotNull(monitor1);
        Assertions.assertNotNull(monitor1.osDiskStatus());
        Assertions.assertNotNull(monitor1.dataDiskStatus());
        Assertions.assertTrue(monitor1.osDiskStatus().equals(EncryptionStatus.NOT_ENCRYPTED));
        Assertions.assertTrue(monitor1.dataDiskStatus().equals(EncryptionStatus.NOT_ENCRYPTED));
        DiskVolumeEncryptionMonitor monitor2 = virtualMachine
                .diskEncryption()
                .enable(keyVaultId, aadClientId, aadSecret);
        Assertions.assertNotNull(monitor2);
        Assertions.assertNotNull(monitor2.osDiskStatus());
        Assertions.assertNotNull(monitor2.dataDiskStatus());
        monitor1.refresh();
        Assertions.assertTrue(monitor1.osDiskStatus().equals(monitor2.osDiskStatus()));
        Assertions.assertTrue(monitor1.dataDiskStatus().equals(monitor2.dataDiskStatus()));
        monitor2.refresh();
        Assertions.assertTrue(monitor2.osDiskStatus().equals(EncryptionStatus.ENCRYPTION_INPROGRESS));
    }
}
