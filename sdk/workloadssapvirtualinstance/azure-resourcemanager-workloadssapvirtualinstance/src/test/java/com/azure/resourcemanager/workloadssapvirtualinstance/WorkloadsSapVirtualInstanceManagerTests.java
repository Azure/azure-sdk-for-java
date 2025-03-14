// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.workloadssapvirtualinstance;

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
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.DeploymentWithOSConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.DiskConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.DiskSku;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.DiskSkuName;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.DiskVolumeConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.ImageReference;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.LinuxConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.ManagedResourcesNetworkAccessType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.NetworkConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.OSProfile;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.OsSapConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDatabaseType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapEnvironmentType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapProductType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapVirtualInstance;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapVirtualInstanceProperties;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SingleServerConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SshConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SshPublicKey;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.VirtualMachineConfiguration;
import org.junit.jupiter.api.Assertions;
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
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WorkloadsSapVirtualInstanceManagerTests extends TestProxyTestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST2;
    private String resourceGroupName = "rg" + randomPadding();
    private WorkloadsSapVirtualInstanceManager workloadsSapVirtualInstanceManager;
    private NetworkManager networkManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        resourceManager = ResourceManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        networkManager = NetworkManager.configure()
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        workloadsSapVirtualInstanceManager = WorkloadsSapVirtualInstanceManager.configure()
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        // use AZURE_RESOURCE_GROUP_NAME if run in LIVE CI
        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            resourceManager.resourceGroups().define(resourceGroupName).withRegion(REGION).create();
        }
    }

    @Test
    @LiveOnly
    public void testCreateSapVirtualInstance() throws NoSuchAlgorithmException, IOException {
        Network network = null;
        SapVirtualInstance sapVirtualInstance = null;
        try {
            String vnetName = "vnet" + randomPadding();
            String subNetName = "sub" + randomPadding();
            String instanceName = "instance" + randomPadding();
            Map<String, DiskVolumeConfiguration> diskVolumeConfigurationMap = new HashMap<>();
            diskVolumeConfigurationMap.put("hana/data",
                new DiskVolumeConfiguration().withCount(3L)
                    .withSizeGB(128L)
                    .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)));
            diskVolumeConfigurationMap.put("hana/log",
                new DiskVolumeConfiguration().withCount(3L)
                    .withSizeGB(128L)
                    .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)));
            diskVolumeConfigurationMap.put("hana/shared",
                new DiskVolumeConfiguration().withCount(1L)
                    .withSizeGB(256L)
                    .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)));
            diskVolumeConfigurationMap.put("usr/sap",
                new DiskVolumeConfiguration().withCount(1L)
                    .withSizeGB(128L)
                    .withSku(new DiskSku().withName(DiskSkuName.PREMIUM_LRS)));
            diskVolumeConfigurationMap.put("backup",
                new DiskVolumeConfiguration().withCount(2L)
                    .withSizeGB(256L)
                    .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)));
            diskVolumeConfigurationMap.put("os",
                new DiskVolumeConfiguration().withCount(1L)
                    .withSizeGB(64L)
                    .withSku(new DiskSku().withName(DiskSkuName.STANDARD_SSD_LRS)));
            // @embedmeStart
            network = networkManager.networks()
                .define(vnetName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withAddressSpace("10.0.0.0/16")
                .defineSubnet(subNetName)
                .withAddressPrefix("10.0.0.0/24")
                .disableNetworkPoliciesOnPrivateEndpoint()
                .attach()
                .create();

            sapVirtualInstance
                = workloadsSapVirtualInstanceManager.sapVirtualInstances()
                    .define("SBX")
                    .withRegion(REGION)
                    .withExistingResourceGroup(resourceGroupName)
                    .withProperties(
                        new SapVirtualInstanceProperties().withEnvironment(SapEnvironmentType.NON_PROD)
                            .withSapProduct(SapProductType.S4HANA)
                            .withManagedResourcesNetworkAccessType(ManagedResourcesNetworkAccessType.PUBLIC)
                            .withConfiguration(new DeploymentWithOSConfiguration().withAppLocation(REGION.name())
                                .withInfrastructureConfiguration(new SingleServerConfiguration()
                                    .withAppResourceGroup(REGION.name())
                                    .withNetworkConfiguration(new NetworkConfiguration().withIsSecondaryIpEnabled(true))
                                    .withDatabaseType(SapDatabaseType.HANA)
                                    .withSubnetId(network.subnets().get(subNetName).id())
                                    .withVirtualMachineConfiguration(new VirtualMachineConfiguration()
                                        .withVmSize("Standard_E32ds_v4")
                                        .withImageReference(new ImageReference().withVersion("latest")
                                            .withPublisher("RedHat")
                                            .withSku("86sapha-gen2")
                                            .withOffer("RHEL-SAP-HA"))
                                        .withOsProfile(new OSProfile().withAdminUsername("azureuser")
                                            .withOsConfiguration(
                                                new LinuxConfiguration().withSsh(new SshConfiguration().withPublicKeys(
                                                    Arrays.asList(new SshPublicKey().withKeyData(sshPublicKey())))))))
                                    .withDbDiskConfiguration(new DiskConfiguration()
                                        .withDiskVolumeConfigurations(diskVolumeConfigurationMap)))
                                .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("sap.contoso.com"))))
                    .create();
            // @embedmeEnd
            sapVirtualInstance.refresh();
            Assertions.assertEquals(instanceName, sapVirtualInstance.name());
            Assertions.assertEquals(instanceName,
                workloadsSapVirtualInstanceManager.sapVirtualInstances().getById(sapVirtualInstance.id()).name());
            Assertions.assertTrue(workloadsSapVirtualInstanceManager.sapVirtualInstances()
                .listByResourceGroup(resourceGroupName)
                .stream()
                .findAny()
                .isPresent());
        } finally {
            if (sapVirtualInstance != null) {
                workloadsSapVirtualInstanceManager.sapVirtualInstances().deleteById(sapVirtualInstance.id());
            }
            if (network != null) {
                networkManager.networks().deleteById(network.id());
            }
        }
    }

    @Override
    protected void afterTest() {
        if (!testEnv) {
            resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }

    private static String sshPublicKey() throws NoSuchAlgorithmException, IOException {
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
        return "ssh-rsa " + publicKeyEncoded;
    }
}
