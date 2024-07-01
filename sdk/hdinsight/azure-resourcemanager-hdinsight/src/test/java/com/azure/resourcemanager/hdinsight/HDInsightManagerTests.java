// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.hdinsight;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.hdinsight.models.Cluster;
import com.azure.resourcemanager.hdinsight.models.ClusterCreateProperties;
import com.azure.resourcemanager.hdinsight.models.ClusterDefinition;
import com.azure.resourcemanager.hdinsight.models.ComputeIsolationProperties;
import com.azure.resourcemanager.hdinsight.models.ComputeProfile;
import com.azure.resourcemanager.hdinsight.models.EncryptionInTransitProperties;
import com.azure.resourcemanager.hdinsight.models.HardwareProfile;
import com.azure.resourcemanager.hdinsight.models.LinuxOperatingSystemProfile;
import com.azure.resourcemanager.hdinsight.models.OSType;
import com.azure.resourcemanager.hdinsight.models.OsProfile;
import com.azure.resourcemanager.hdinsight.models.Role;
import com.azure.resourcemanager.hdinsight.models.StorageAccount;
import com.azure.resourcemanager.hdinsight.models.StorageProfile;
import com.azure.resourcemanager.hdinsight.models.Tier;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.models.BlobContainer;
import com.azure.resourcemanager.storage.models.MinimumTlsVersion;
import com.azure.resourcemanager.storage.models.PublicAccess;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HDInsightManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST;
    private String resourceGroupName = "rg" + randomPadding();
    private HDInsightManager hdInsightManager;
    private StorageManager storageManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        hdInsightManager = HDInsightManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        storageManager = StorageManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        resourceManager = ResourceManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

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
    public void testCreateCluster() {
        Cluster cluster = null;
        String randomPadding = randomPadding();
        try {
            String clusterName = "cluster" + randomPadding;
            String storageName = "storage" + randomPadding;
            String containerName = "container" + randomPadding;
            String strPassword = "Pa$s" + randomPadding;

            OsProfile osProfile = new OsProfile()
                .withLinuxOperatingSystemProfile(
                    new LinuxOperatingSystemProfile()
                        .withUsername("sshuser")
                        .withPassword(strPassword));

            Map<String, Map<String, String>> clusterDefinition = new HashMap<>(1);
            Map<String, String> clusterProperties = new HashMap<>(3);
            clusterProperties.put("restAuthCredential.isEnabled", "true");
            clusterProperties.put("restAuthCredential.username", "admin");
            clusterProperties.put("restAuthCredential.password", strPassword);
            clusterDefinition.put("gateway", Collections.unmodifiableMap(clusterProperties));

            // @embedmeStart
            com.azure.resourcemanager.storage.models.StorageAccount storageAccount =
                storageManager.storageAccounts().define(storageName)
                    .withRegion(REGION)
                    .withExistingResourceGroup(resourceGroupName)
                    .withSku(StorageAccountSkuType.STANDARD_LRS)
                    .withMinimumTlsVersion(MinimumTlsVersion.TLS1_0)
                    .withAccessFromAzureServices()
                    .withAccessFromAllNetworks()
                    .create();

            BlobContainer blobContainer = storageManager.blobContainers()
                .defineContainer(containerName)
                .withExistingStorageAccount(storageAccount)
                .withPublicAccess(PublicAccess.NONE)
                .create();

            cluster = hdInsightManager.clusters()
                .define(clusterName)
                .withExistingResourceGroup(resourceGroupName)
                .withRegion(REGION)
                .withProperties(
                    new ClusterCreateProperties()
                        .withClusterVersion("4.0.3000.1")
                        .withOsType(OSType.LINUX)
                        .withClusterDefinition(
                            new ClusterDefinition()
                                .withKind("SPARK")
                                .withConfigurations(Collections.unmodifiableMap(clusterDefinition)))
                        .withComputeProfile(
                            new ComputeProfile()
                                .withRoles(
                                    Arrays.asList(
                                        new Role().withName("headnode")
                                            .withTargetInstanceCount(2)
                                            .withHardwareProfile(new HardwareProfile().withVmSize("standard_e8_v3"))
                                            .withOsProfile(osProfile)
                                            .withEncryptDataDisks(false),
                                        new Role().withName("workernode")
                                            .withTargetInstanceCount(4)
                                            .withHardwareProfile(new HardwareProfile().withVmSize("standard_e8_v3"))
                                            .withOsProfile(osProfile)
                                            .withEncryptDataDisks(false),
                                        new Role().withName("zookeepernode")
                                            .withTargetInstanceCount(3)
                                            .withHardwareProfile(new HardwareProfile().withVmSize("standard_a2_v2"))
                                            .withOsProfile(osProfile)
                                            .withEncryptDataDisks(false)
                                    )))
                        .withTier(Tier.STANDARD)
                        .withEncryptionInTransitProperties(
                            new EncryptionInTransitProperties()
                                .withIsEncryptionInTransitEnabled(false))
                        .withStorageProfile(
                            new StorageProfile()
                                .withStorageaccounts(
                                    Arrays.asList(
                                        new StorageAccount()
                                            .withName(storageName + ".blob.core.windows.net")
                                            .withResourceId(storageAccount.id())
                                            .withContainer(blobContainer.name())
                                            .withIsDefault(true)
                                            .withKey(storageAccount.getKeys().iterator().next().value()))
                                ))
                        .withMinSupportedTlsVersion("1.2")
                        .withComputeIsolationProperties(
                            new ComputeIsolationProperties()
                                .withEnableComputeIsolation(false))
                )
                .create();
            // @embedmeEnd
            cluster.refresh();
            Assertions.assertEquals(cluster.name(), clusterName);
            Assertions.assertEquals(cluster.name(), hdInsightManager.clusters().getById(cluster.id()).name());
            Assertions.assertTrue(hdInsightManager.clusters().list().stream().count() > 0);
        } finally {
            if (cluster != null) {
                hdInsightManager.clusters().deleteById(cluster.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
