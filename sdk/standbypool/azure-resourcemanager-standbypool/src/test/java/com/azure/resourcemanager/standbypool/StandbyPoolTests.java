// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.standbypool;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.test.utils.TestUtilities;
import com.azure.resourcemanager.compute.ComputeManager;
import com.azure.resourcemanager.compute.models.KnownLinuxVirtualMachineImage;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSet;
import com.azure.resourcemanager.compute.models.VirtualMachineScaleSetSkuTypes;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.standbypool.models.StandbyVirtualMachinePoolElasticityProfile;
import com.azure.resourcemanager.standbypool.models.StandbyVirtualMachinePoolResource;
import com.azure.resourcemanager.standbypool.models.StandbyVirtualMachinePoolResourceProperties;
import com.azure.resourcemanager.standbypool.models.VirtualMachineState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Random;

public class StandbyPoolTests extends TestProxyTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(StandbyPoolTests.class);

    private boolean testEnv;

    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST2;
    private String resourceGroupName = "rg" + randomPadding();
    private StandbyPoolManager standbyPoolManager;
    private ComputeManager computeManager;

    @Override
    public void beforeTest() {
        final TokenCredential credential = TestUtilities.getTokenCredentialForTest(getTestMode());
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        standbyPoolManager = StandbyPoolManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        computeManager = ComputeManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        // use AZURE_RESOURCE_GROUP_NAME if run in LIVE CI
        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            computeManager.resourceManager().resourceGroups().define(resourceGroupName).withRegion(REGION).create();
        }
    }

    @Override
    protected void afterTest() {
        if (!testEnv) {
            computeManager.resourceManager().resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    @Test
    @LiveOnly
    public void testStandByVirtualMachinePool() {
        // live test subscription is having issue creating VMSS resource. Only check API availability here.
        standbyPoolManager.standbyVirtualMachinePools().list().stream().count();
        // need to register preview feature in live test subscription
        // standbyPoolManager.standbyContainerGroupPools().list().stream().count();
    }

    @Test
    @LiveOnly
    @Disabled("Live test subscription only has available VMSS/VirtualMachine SKUs in centraleuap region. Network resource doesn't support this region.")
    public void testStandbyVirtualMachinePool() {
        String poolName = "pool" + randomPadding();
        StandbyVirtualMachinePoolResource standbyVirtualMachinePool = null;
        VirtualMachineScaleSet virtualMachineScaleSet = null;
        Network virtualNetwork = null;
        try {
            // @embedmeStart
            // reference https://learn.microsoft.com/azure/virtual-machine-scale-sets/standby-pools-create

            // Create virtual network and virtual machine scale set
            virtualNetwork = this.computeManager.networkManager()
                .networks()
                .define("vmssvnet")
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withAddressSpace("10.0.0.0/27")
                .withSubnet("default", "10.0.0.0/27")
                .create();

            virtualMachineScaleSet = computeManager.virtualMachineScaleSets()
                .define("vmss")
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withFlexibleOrchestrationMode()
                .withSku(VirtualMachineScaleSetSkuTypes.STANDARD_A0)
                .withExistingPrimaryNetworkSubnet(virtualNetwork, "default")
                .withoutPrimaryInternetFacingLoadBalancer()
                .withoutPrimaryInternalLoadBalancer()
                .withPopularLinuxImage(KnownLinuxVirtualMachineImage.UBUNTU_SERVER_18_04_LTS)
                .withRootUsername("Foo12")
                .withSsh(sshPublicKey())
                .withVirtualMachinePublicIp()
                .withCapacity(3L)
                .create();

            // create standby virtual machine pool
            standbyVirtualMachinePool = standbyPoolManager.standbyVirtualMachinePools()
                .define(poolName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withProperties(new StandbyVirtualMachinePoolResourceProperties()
                    .withAttachedVirtualMachineScaleSetId(virtualMachineScaleSet.id())
                    .withVirtualMachineState(VirtualMachineState.DEALLOCATED)
                    .withElasticityProfile(new StandbyVirtualMachinePoolElasticityProfile().withMaxReadyCapacity(3L)
                        .withMinReadyCapacity(1L)))
                .create();
            // @embedmeEnd
            standbyVirtualMachinePool.refresh();
            Assertions.assertEquals(poolName, standbyVirtualMachinePool.name());
            Assertions.assertTrue(
                standbyPoolManager.standbyVirtualMachinePools().listByResourceGroup(resourceGroupName).stream().count()
                    > 0);
        } finally {
            if (standbyVirtualMachinePool != null) {
                standbyPoolManager.standbyVirtualMachinePools().deleteById(standbyVirtualMachinePool.id());

                computeManager.virtualMachineScaleSets().deleteById(virtualMachineScaleSet.id());

                computeManager.networkManager().networks().deleteById(virtualNetwork.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }

    private static String sshPublicKey() {
        String sshPublicKey;
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair pair = keyGen.generateKeyPair();
            PublicKey publicKey = pair.getPublic();

            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            ByteArrayOutputStream byteOs = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(byteOs);
            dos.writeInt("ssh-rsa".getBytes(StandardCharsets.US_ASCII).length);
            dos.write("ssh-rsa".getBytes(StandardCharsets.US_ASCII));
            dos.writeInt(rsaPublicKey.getPublicExponent().toByteArray().length);
            dos.write(rsaPublicKey.getPublicExponent().toByteArray());
            dos.writeInt(rsaPublicKey.getModulus().toByteArray().length);
            dos.write(rsaPublicKey.getModulus().toByteArray());
            String publicKeyEncoded
                = new String(Base64.getEncoder().encode(byteOs.toByteArray()), StandardCharsets.US_ASCII);
            sshPublicKey = "ssh-rsa " + publicKeyEncoded;
        } catch (NoSuchAlgorithmException | IOException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("failed to generate ssh key", e));
        }
        return sshPublicKey;
    }
}
