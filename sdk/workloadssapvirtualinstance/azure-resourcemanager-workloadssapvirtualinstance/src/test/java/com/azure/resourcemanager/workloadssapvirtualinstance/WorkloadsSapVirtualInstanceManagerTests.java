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
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.Provider;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.DeploymentWithOSConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.ImageReference;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.LinuxConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.ManagedResourcesNetworkAccessType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.OSProfile;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.OsSapConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapDatabaseType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapEnvironmentType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapProductType;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapVirtualInstance;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SapVirtualInstanceProperties;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SingleServerConfiguration;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.SshKeyPair;
import com.azure.resourcemanager.workloadssapvirtualinstance.models.VirtualMachineConfiguration;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WorkloadsSapVirtualInstanceManagerTests extends TestProxyTestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST2;
    private String resourceGroupName = "rg" + randomPadding();
    private WorkloadsSapVirtualInstanceManager workloadsSapVirtualInstanceManager = null;
    private NetworkManager networkManager = null;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        workloadsSapVirtualInstanceManager = WorkloadsSapVirtualInstanceManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        networkManager = NetworkManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        resourceManager = ResourceManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        canRegisterProviders(Arrays.asList("Microsoft.Workloads"));

        // use AZURE_RESOURCE_GROUP_NAME if run in LIVE CI
        String testResourceGroup = Configuration.getGlobalConfiguration().get("AZURE_RESOURCE_GROUP_NAME");
        testEnv = !CoreUtils.isNullOrEmpty(testResourceGroup);
        if (testEnv) {
            resourceGroupName = testResourceGroup;
        } else {
            resourceManager.resourceGroups()
                .define(resourceGroupName)
                .withRegion(REGION)
                .create();
        }
    }

    @Override
    protected void afterTest() {
        if (!testEnv) {
            resourceManager.resourceGroups().beginDeleteByName(resourceGroupName);
        }
    }

    @Test
    @LiveOnly
    public void testCreateSapVirtualInstance() throws JSchException, UnsupportedEncodingException {
        SapVirtualInstance instance = null;
        try {
            String vnetName = "vnet" + randomPadding();
            String adminUser = "user" + randomPadding();
            SshPublicPrivateKey sshPublicPrivateKey = generateSSHKeys("", "");

            // @embedmeStart
            Network network = networkManager.networks()
                .define(vnetName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withAddressSpace("10.0.0.0/16")
                .withSubnet("appsubnet", "10.0.1.0/24")
                .create();

            instance = workloadsSapVirtualInstanceManager
                .sapVirtualInstances()
                .define("P01")
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withProperties(
                    new SapVirtualInstanceProperties()
                        .withSapProduct(SapProductType.S4HANA)
                        .withEnvironment(SapEnvironmentType.NON_PROD)
                        .withManagedResourcesNetworkAccessType(ManagedResourcesNetworkAccessType.PUBLIC)
                        .withConfiguration(
                            new DeploymentWithOSConfiguration()
                                .withAppLocation(REGION.name())
                                .withInfrastructureConfiguration(
                                    new SingleServerConfiguration()
                                        .withAppResourceGroup(resourceGroupName)
                                        .withDatabaseType(SapDatabaseType.HANA)
                                        .withSubnetId(network.subnets().get("appsubnet").id())
                                        .withVirtualMachineConfiguration(
                                            new VirtualMachineConfiguration()
                                                .withVmSize("Standard_E32ds_v4")
                                                .withImageReference(
                                                    new ImageReference()
                                                        .withPublisher("RedHat")
                                                        .withOffer("RHEL-SAP-HA")
                                                        .withSku("86sapha-gen2")
                                                        .withVersion("latest")
                                                )
                                                .withOsProfile(
                                                    new OSProfile()
                                                        .withAdminUsername(adminUser)
                                                        .withOsConfiguration(
                                                            new LinuxConfiguration()
                                                                .withDisablePasswordAuthentication(true)
                                                                .withSshKeyPair(
                                                                    new SshKeyPair()
                                                                        .withPublicKey(sshPublicPrivateKey.getSshPublicKey())
                                                                        .withPrivateKey(sshPublicPrivateKey.getSshPrivateKey()))
                                                        )
                                                )
                                        )
                                )
                                .withOsSapConfiguration(new OsSapConfiguration().withSapFqdn("sap.contoso.com"))
                        )
                )
                .create();
            // @embedmeEnd
            instance.refresh();
            Assertions.assertEquals("P01", instance.name());
            Assertions.assertEquals("P01", workloadsSapVirtualInstanceManager.sapVirtualInstances().getById(instance.id()).name());
            Assertions.assertTrue(workloadsSapVirtualInstanceManager.sapVirtualInstances().listByResourceGroup(resourceGroupName).stream().findAny().isPresent());
        } finally {
            if (instance != null) {
                workloadsSapVirtualInstanceManager.sapCentralInstances().deleteById(instance.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }

    /**
     * Automatically generate SSH keys.
     *
     * @param passPhrase the byte array content to be uploaded
     * @param comment the name of the file for which the content will be saved into
     * @return SSH public and private key
     * @throws JSchException exception thrown
     */
    public static SshPublicPrivateKey generateSSHKeys(String passPhrase, String comment) throws JSchException, UnsupportedEncodingException {
        JSch jsch = new JSch();
        KeyPair keyPair = KeyPair.genKeyPair(jsch, KeyPair.RSA);
        ByteArrayOutputStream privateKeyBuff = new ByteArrayOutputStream(2048);
        ByteArrayOutputStream publicKeyBuff = new ByteArrayOutputStream(2048);

        keyPair.writePublicKey(publicKeyBuff, (comment != null) ? comment : "SSHCerts");

        if (StringUtils.isBlank(passPhrase)) {
            keyPair.writePrivateKey(privateKeyBuff);
        } else {
            keyPair.writePrivateKey(privateKeyBuff, passPhrase.getBytes(StandardCharsets.UTF_8));
        }

        return new SshPublicPrivateKey(privateKeyBuff.toString("UTF-8"), publicKeyBuff.toString("UTF-8"));
    }

    /**
     * Internal class to retain the generate SSH keys.
     */
    public static class SshPublicPrivateKey {
        private final String sshPublicKey;
        private final String sshPrivateKey;

        /**
         * Constructor.
         *
         * @param sshPrivateKey SSH private key
         * @param sshPublicKey SSH public key
         */
        public SshPublicPrivateKey(String sshPrivateKey, String sshPublicKey) {
            this.sshPrivateKey = sshPrivateKey;
            this.sshPublicKey = sshPublicKey;
        }

        /**
         * Get SSH public key.
         *
         * @return public key
         */
        public String getSshPublicKey() {
            return sshPublicKey;
        }

        /**
         * Get SSH private key.
         *
         * @return private key
         */
        public String getSshPrivateKey() {
            return sshPrivateKey;
        }
    }

    /**
     * Check and register service resources
     *
     * @param providerNamespaces the resource provider names
     */
    private void canRegisterProviders(List<String> providerNamespaces) {
        providerNamespaces.forEach(providerNamespace -> {
            Provider provider = resourceManager.providers().getByName(providerNamespace);
            if (!"Registered".equalsIgnoreCase(provider.registrationState())
                && !"Registering".equalsIgnoreCase(provider.registrationState())) {
                provider = resourceManager.providers().register(providerNamespace);
            }
            while (!"Registered".equalsIgnoreCase(provider.registrationState())) {
                ResourceManagerUtils.sleep(Duration.ofSeconds(5));
                provider = resourceManager.providers().getByName(provider.namespace());
            }
        });
    }
}
