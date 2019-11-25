/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.hdinsight.v2018_06_01_preview.scenariotests;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.keyvault.KeyIdentifier;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.*;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.implementation.ClusterInner;
import com.microsoft.azure.management.keyvault.KeyPermissions;
import com.microsoft.azure.management.keyvault.Permissions;
import com.microsoft.azure.management.keyvault.SecretPermissions;
import com.microsoft.azure.management.msi.implementation.IdentityInner;
import com.microsoft.azure.management.storage.Kind;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ThrowableAssert;
import org.assertj.core.util.Lists;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.microsoft.azure.management.hdinsight.v2018_06_01_preview.utilities.Utilities.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ClusterTests extends HDInsightManagementTestBase {

    @Test
    public void testCreateHumboldtCluster() {
        String clusterName = generateRandomClusterName("hdisdk-humboldt");
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        validateCluster(clusterName, createParams, cluster);
    }

    @Test
    public void testCreateHumboldtClusterWithPremiumTier() {
        String clusterName = generateRandomClusterName("hdisdk-premium");
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        createParams.properties().withTier(Tier.PREMIUM);
        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        validateCluster(clusterName, createParams, cluster);
    }

    @Test
    public void testCreateWithEmptyExtendedParameters() {
        assertThatThrownBy(new ThrowableAssert.ThrowingCallable() {
            @Override
            public void call() {
                String clusterName = generateRandomClusterName("hdisdk-cluster");
                hdInsightManager.clusters()
                    .inner()
                    .create(resourceGroup.name(), clusterName, new ClusterCreateParametersExtended());
            }
        }).isInstanceOf(CloudException.class).hasMessage("The location property is required for this definition.");
    }

    @Test
    public void testCreateHumboldtClusterWithCustomVMSizes() {
        //Iterables.find
        String clusterName = generateRandomClusterName("hdisdk-customvmsizes");
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        Role headNode = findRoleByName(createParams, "headnode");
        headNode.hardwareProfile().withVmSize("ExtraLarge");
        Role zookeeperNode = findRoleByName(createParams, "zookeepernode");
        zookeeperNode.hardwareProfile().withVmSize("Medium");
        validateCluster(clusterName, createParams, cluster);
    }

    @Test
    public void testCreateLinuxSparkClusterWithComponentVersion() {
        String clusterName = generateRandomClusterName("hdisdk-sparkcomponentversions");
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        createParams.properties()
            .clusterDefinition()
            .withKind("Spark")
            .withComponentVersion(ImmutableMap.of(
                "Spark", "2.2"
            ));
        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        validateCluster(clusterName, createParams, cluster);
    }

    @Test
    public void testCreateKafkaClusterWithManagedDisks() {
        String clusterName = generateRandomClusterName("hdisdk-kafka");
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        createParams.properties().clusterDefinition().withKind("Kafka");
        Role workerNode = findRoleByName(createParams, "workernode");
        workerNode.withDataDisksGroups(Collections.singletonList(
            new DataDisksGroups().withDisksPerNode(8)
        ));
        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        validateCluster(clusterName, createParams, cluster);
    }

    @Test
    public void testCreateKafkaClusterWithDiskEncryption() {

        // This test cases requires Key Vault setup beforehand.
        // Key Vault setup requires tenant id, client id provided.
        // Skip this test if those info are not provided.
        if (isPartialRecordMode()) {
            return;
        }

        // create key vault with soft delete enabled
        vault = resourceManager.createVault(
            resourceGroup.name(),   // resource group name
            true     // enable soft delete
        );

        addTextReplacementRule(vault.properties().vaultUri() + "/", playbackUri + "/");

        // create managed identities for Azure resources.
        final IdentityInner msi = resourceManager.createManagedIdentity(resourceGroup.name());

        // add managed identity to vault
        Permissions requiredPermissions = new Permissions()
            .withKeys(Arrays.asList(KeyPermissions.GET, KeyPermissions.WRAP_KEY, KeyPermissions.UNWRAP_KEY))
            .withSecrets(Arrays.asList(SecretPermissions.GET, SecretPermissions.SET, SecretPermissions.DELETE));
        vault = setPermissions(vault, msi.principalId(), requiredPermissions);
        assertThat(vault).isNotNull();

        // create key
        KeyIdentifier keyIdentifier = resourceManager.generateVaultKey(vault, "hdijavakey1");

        // create HDInsight cluster with Kafka disk encryption
        String clusterName = generateRandomClusterName("hdisdk-kafka-byok");
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        createParams.properties().clusterDefinition().withKind("Kafka");
        Role workerNode = findRoleByName(createParams, "workernode");
        workerNode.withDataDisksGroups(Collections.singletonList(
            new DataDisksGroups().withDisksPerNode(8)
        ));
        createParams.withIdentity(new ClusterIdentity()
            .withType(ResourceIdentityType.USER_ASSIGNED)
            .withUserAssignedIdentities(ImmutableMap.of(
                msi.id(), new ClusterIdentityUserAssignedIdentitiesValue()
            ))
        );
        createParams.properties().withDiskEncryptionProperties(new DiskEncryptionProperties()
            .withVaultUri(keyIdentifier.vault())
            .withKeyName(keyIdentifier.name())
            .withKeyVersion(keyIdentifier.version())
            .withMsiResourceId(msi.id())
        );

        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        validateCluster(clusterName, createParams, cluster);

        // check disk encryption properties
        DiskEncryptionProperties diskEncryptionProperties = cluster.properties().diskEncryptionProperties();
        DiskEncryptionProperties diskEncryptionPropertyParams = createParams.properties().diskEncryptionProperties();
        assertThat(diskEncryptionProperties).isNotNull();
        assertThat(diskEncryptionProperties.vaultUri()).isEqualTo(diskEncryptionPropertyParams.vaultUri());
        assertThat(diskEncryptionProperties.keyName()).isEqualTo(diskEncryptionPropertyParams.keyName());
        assertThat(diskEncryptionProperties.msiResourceId()).isEqualToIgnoringCase(msi.id());

        KeyIdentifier secondKeyIdentifier = resourceManager.generateVaultKey(vault, "hdijavakey2");

        // rotate cluster key
        ClusterDiskEncryptionParameters rotateParams = new ClusterDiskEncryptionParameters()
            .withVaultUri(secondKeyIdentifier.vault())
            .withKeyName(secondKeyIdentifier.name())
            .withKeyVersion(secondKeyIdentifier.version());
        hdInsightManager.clusters().inner().rotateDiskEncryptionKey(resourceGroup.name(), clusterName, rotateParams);
        cluster = hdInsightManager.clusters().inner().getByResourceGroup(resourceGroup.name(), clusterName);
        diskEncryptionProperties = cluster.properties().diskEncryptionProperties();
        assertThat(diskEncryptionProperties).isNotNull();
        assertThat(diskEncryptionProperties.vaultUri()).isEqualTo(rotateParams.vaultUri());
        assertThat(diskEncryptionProperties.keyName()).isEqualTo(rotateParams.keyName());
        assertThat(diskEncryptionProperties.msiResourceId()).isEqualToIgnoringCase(msi.id());
    }

    @Test
    public void testCreateWithADLSv1() {

        // This test case require ADLS gen1 storage account created beforehand
        if (isPartialRecordMode()) {
            return;
        }

        String clusterName = generateRandomClusterName("hdisdk-adlgen1");
        ClusterCreateParametersExtended createParams = prepareClusterCreateParamsForADLSv1(
            region,
            tenantId,
            clusterName);

        // Add additional storage account
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        createParams.properties().storageProfile().storageaccounts().add(new StorageAccount()
            .withName(storageAccount.name() + STORAGE_BLOB_SERVICE_ENDPOINT_SUFFIX)
            .withKey(storageAccountKey)
            .withContainer(clusterName.toLowerCase())
            .withIsDefault(false)
        );
        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        validateCluster(clusterName, createParams, cluster);
    }

    @Test
    public void testCreateWithADLSv2() {
        com.microsoft.azure.management.storage.StorageAccount storageV2Account = resourceManager.createStorageAccount(
            resourceGroup.name(),          // resource group name
            "hdijavaadlsv2",    // name Prefix
            Kind.STORAGE_V2               // storage account kind
        );
        String clusterName = generateRandomClusterName("hdisdk-adlgen2");
        ClusterCreateParametersExtended createParams = prepareClusterCreateParamsForADLSv2(
            region,
            clusterName,
            storageV2Account,
            resourceManager.generateKey(storageV2Account)
        );

        // Add additional storage account
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        createParams.properties().storageProfile().storageaccounts().add(new StorageAccount()
            .withName(storageAccount.name() + STORAGE_BLOB_SERVICE_ENDPOINT_SUFFIX)
            .withKey(storageAccountKey)
            .withContainer(clusterName.toLowerCase())
            .withIsDefault(false)
        );
        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        validateCluster(clusterName, createParams, cluster);
    }

    @Test
    public void testCreateWithAdditionalStorageAccount() {
        com.microsoft.azure.management.storage.StorageAccount secondaryStorageAccount = resourceManager
            .createStorageAccount(
                resourceGroup.name(),                           // resource group name
                STORAGE_ACCOUNT_NAME_PREFIX + "2"     // name Prefix
            );
        String clusterName = generateRandomClusterName("hdisdk-additional");
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        createParams.properties().storageProfile().storageaccounts().add(new StorageAccount()
            .withName(secondaryStorageAccount.name() + STORAGE_BLOB_SERVICE_ENDPOINT_SUFFIX)
            .withKey(resourceManager.generateKey(secondaryStorageAccount))
            .withContainer(clusterName.toLowerCase())
            .withIsDefault(false)
        );
        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        validateCluster(clusterName, createParams, cluster);
    }

    @Test
    public void testCreateRServerCluster() {
        String clusterName = generateRandomClusterName("hdisdk-rserver");
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        createParams.properties().clusterDefinition().withKind("RServer");
        createParams.properties().computeProfile().roles().add(
            new Role().withName("edgenode")
                .withTargetInstanceCount(1)
                .withHardwareProfile(new HardwareProfile()
                    .withVmSize("Standard_D4_v2")
                )
                .withOsProfile(new OsProfile()
                    .withLinuxOperatingSystemProfile(
                        new LinuxOperatingSystemProfile()
                            .withUsername(SSH_USERNAME)
                            .withPassword(SSH_PASSWORD)
                    )
                )
        );
        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        validateCluster(clusterName, createParams, cluster);
    }

    @Test
    public void testCreateMLServicesCluster() {
        String clusterName = generateRandomClusterName("hdisdk-mlservices");
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        createParams.properties().withClusterVersion("3.6");
        createParams.properties().clusterDefinition().withKind("MLServices");
        createParams.properties().computeProfile().roles().add(
            new Role().withName("edgenode")
                .withTargetInstanceCount(1)
                .withHardwareProfile(new HardwareProfile()
                    .withVmSize("Standard_D4_v2")
                )
                .withOsProfile(new OsProfile()
                    .withLinuxOperatingSystemProfile(
                        new LinuxOperatingSystemProfile()
                            .withUsername(SSH_USERNAME)
                            .withPassword(SSH_PASSWORD)
                    )
                )
        );
        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        validateCluster(clusterName, createParams, cluster);
    }

    @Test
    public void testListClustersInResourceGroup() {
        String clusterName = generateRandomClusterName("hdisdk-cluster-rg1");
        PagedList<ClusterInner> clusterPage = hdInsightManager.clusters().inner().listByResourceGroup(resourceGroup.name());
        List<ClusterInner> clusterList = Lists.newArrayList(clusterPage);
        while (clusterPage.hasNextPage()) {
            clusterPage = hdInsightManager.clusters().inner()
                .listByResourceGroupNext(clusterPage.currentPage().nextPageLink());
            clusterList.addAll(clusterPage);
        }

        ClusterNameMatchedCondition clusterNameMatchedCondition = new ClusterNameMatchedCondition(clusterName);
        assertThat(clusterList).doesNotHave(clusterNameMatchedCondition);

        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);

        clusterList.clear();
        clusterPage = hdInsightManager.clusters().inner().listByResourceGroup(resourceGroup.name());
        clusterList.addAll(clusterPage);
        while (clusterPage.hasNextPage()) {
            clusterPage = hdInsightManager.clusters().inner()
                .listByResourceGroupNext(clusterPage.currentPage().nextPageLink());
            clusterList.addAll(clusterPage);
        }

        assertThat(clusterList).has(clusterNameMatchedCondition);
    }

    @Test
    @Ignore("Test case will list all clusters under a subscription.")
    public void testListClustersInSubscription() {
        String clusterName = generateRandomClusterName("hdisdk-cluster-sub");
        PagedList<ClusterInner> clusterPage = hdInsightManager.clusters().inner().list();
        List<ClusterInner> clusterList = Lists.newArrayList(clusterPage);
        while (clusterPage.hasNextPage()) {
            clusterPage = hdInsightManager.clusters().inner()
                .listByResourceGroupNext(clusterPage.currentPage().nextPageLink());
            clusterList.addAll(clusterPage);
        }

        ClusterNameMatchedCondition clusterNameMatchedCondition = new ClusterNameMatchedCondition(clusterName);
        assertThat(clusterList).doesNotHave(clusterNameMatchedCondition);

        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);

        clusterList.clear();
        clusterPage = hdInsightManager.clusters().inner().list();
        clusterList.addAll(clusterPage);
        while (clusterPage.hasNextPage()) {
            clusterPage = hdInsightManager.clusters().inner()
                .listNext(clusterPage.currentPage().nextPageLink());
            clusterList.addAll(clusterPage);
        }

        assertThat(clusterList).has(clusterNameMatchedCondition);
    }

    @Test
    public void testResizeCluster() {
        String clusterName = generateRandomClusterName("hdisdk-clusterresize");
        String storageAccountKey = resourceManager.generateKey(storageAccount);
        ClusterCreateParametersExtended createParams = prepareClusterCreateParams(region, clusterName, storageAccount, storageAccountKey);
        Role workerNodeParams = findRoleByName(createParams, "workernode");
        ClusterInner cluster = hdInsightManager.clusters().inner().create(resourceGroup.name(), clusterName, createParams);
        validateCluster(clusterName, createParams, cluster);

        cluster = hdInsightManager.clusters().inner().getByResourceGroup(resourceGroup.name(), clusterName);
        Role workerNode = findRoleByName(cluster, "workernode");
        assertThat(workerNode.targetInstanceCount()).isEqualTo(workerNodeParams.targetInstanceCount());

        hdInsightManager.clusters().inner().resize(
            resourceGroup.name(),
            clusterName,
            workerNodeParams.targetInstanceCount() + 1);
        cluster = hdInsightManager.clusters().inner().getByResourceGroup(resourceGroup.name(), clusterName);
        workerNode = findRoleByName(cluster, "workernode");
        assertThat(workerNode.targetInstanceCount()).isEqualTo(workerNodeParams.targetInstanceCount() + 1);
    }

    class ClusterNameMatchedCondition extends Condition<List<? extends ClusterInner>> {
        private final String clusterName;

        ClusterNameMatchedCondition(String clusterName) {
            this.clusterName = clusterName;
        }

        @Override
        public boolean matches(List<? extends ClusterInner> clusters) {
            for (ClusterInner cluster : clusters) {
                if (cluster.name().equals(clusterName)) {
                    return true;
                }
            }

            return false;
        }
    }
}
