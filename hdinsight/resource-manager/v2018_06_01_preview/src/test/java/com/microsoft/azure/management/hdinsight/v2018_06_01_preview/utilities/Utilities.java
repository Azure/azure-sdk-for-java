/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.hdinsight.v2018_06_01_preview.utilities;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.microsoft.azure.arm.core.TestBase;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.*;
import com.microsoft.azure.management.hdinsight.v2018_06_01_preview.implementation.ClusterInner;
import org.assertj.core.api.Condition;

import java.util.List;
import java.util.Map;

import static com.microsoft.azure.management.hdinsight.v2018_06_01_preview.scenariotests.HDInsightManagementTestBase.*;
import static org.assertj.core.api.Assertions.assertThat;

public class Utilities {

    public static void validateCluster(String expectedClusterName, ClusterCreateParametersExtended expectedParameters, ClusterInner actualCluster) {
        assertThat(actualCluster.name()).isEqualTo(expectedClusterName);
        assertThat(actualCluster.properties().tier()).isEqualTo(expectedParameters.properties().tier());
        assertThat(actualCluster.etag()).isNotNull();
        assertThat(actualCluster.id()).endsWith(expectedClusterName);
        assertThat(actualCluster.properties().clusterState()).isEqualTo("Running");
        assertThat(actualCluster.type()).isEqualTo("Microsoft.HDInsight/clusters");
        assertThat(actualCluster.location()).isEqualTo(expectedParameters.location());
        assertThat(actualCluster.getTags()).isEqualTo(expectedParameters.tags());
        assertThat(actualCluster.properties().connectivityEndpoints()).has(new Condition<List<? extends ConnectivityEndpoint>>() {
            @Override
            public boolean matches(List<? extends ConnectivityEndpoint> connectivityEndpoints) {
                for (ConnectivityEndpoint connectivityEndpoint : connectivityEndpoints) {
                    if (connectivityEndpoint.name().equals("HTTPS")) {
                        return true;
                    }
                }

                return false;
            }
        });
        assertThat(actualCluster.properties().connectivityEndpoints()).has(new Condition<List<? extends ConnectivityEndpoint>>() {
            @Override
            public boolean matches(List<? extends ConnectivityEndpoint> connectivityEndpoints) {
                for (ConnectivityEndpoint connectivityEndpoint : connectivityEndpoints) {
                    if (connectivityEndpoint.name().equals("SSH")) {
                        return true;
                    }
                }

                return false;
            }
        });
        assertThat(actualCluster.properties().osType()).isEqualTo(expectedParameters.properties().osType());
        assertThat(actualCluster.properties().errors()).isNull();
        assertThat(actualCluster.properties().provisioningState()).isEqualTo(HDInsightClusterProvisioningState.SUCCEEDED);
        assertThat(actualCluster.properties().clusterDefinition().kind())
            .isEqualTo(expectedParameters.properties().clusterDefinition().kind());
        assertThat(actualCluster.properties().clusterVersion()).startsWith(expectedParameters.properties().clusterVersion());
        assertThat(actualCluster.properties().clusterDefinition().configurations()).isNull();
    }

    public static void validateHttpSettings(String expectedUserName, String expectedUserPassword, Map<String, String> actualHttpSettings) {
        assertThat(actualHttpSettings).isNotNull();
        assertThat(actualHttpSettings.get("restAuthCredential.isEnabled")).isEqualTo("true");
        assertThat(actualHttpSettings.get("restAuthCredential.username")).isEqualTo(expectedUserName);
        assertThat(actualHttpSettings.get("restAuthCredential.password")).isEqualTo(expectedUserPassword);
    }

    public static String generateRandomClusterName(String prefix) {
        return generateRandomClusterName(prefix, 35);
    }

    public static String generateRandomClusterName(String prefix, int maxLen) {
        return TestBase.generateRandomResourceName(prefix, maxLen);
    }

    public static Role findRoleByName(ClusterInner cluster, final String roleName) {
        return Iterables.find(
            cluster.properties()
                .computeProfile()
                .roles(),
            new Predicate<Role>() {
                @Override
                public boolean apply(Role role) {
                    return role.name().equals(roleName);
                }
            }
        );
    }

    public static Role findRoleByName(ClusterCreateParametersExtended cluster, final String roleName) {
        return Iterables.find(
            cluster.properties()
                .computeProfile()
                .roles(),
            new Predicate<Role>() {
                @Override
                public boolean apply(Role role) {
                    return role.name().equals(roleName);
                }
            }
        );
    }

    public static ClusterCreateParametersExtended prepareClusterCreateParamsForADLSv1(String region,
                                                                                      String tenantId,
                                                                                      String clusterName) {
        return prepareClusterCreateParamsForADLSv1(region, tenantId, clusterName, null);
    }

    public static ClusterCreateParametersExtended prepareClusterCreateParamsForADLSv1(String region,
                                                                                      String tenantId,
                                                                                      String clusterName,
                                                                                      ClusterCreateParametersExtended createParams) {
        ClusterCreateParametersExtended createParamsForADLSv1 = createParams != null ? createParams : prepareClusterCreateParams(region, clusterName);
        Map<String, Map<String, String>> configurations = (Map<String, Map<String, String>>) createParamsForADLSv1
            .properties().clusterDefinition().configurations();
        String clusterIdentity = "clusterIdentity";
        Map<String, String> clusterIdentityConfig = ImmutableMap.of(
            "clusterIdentity.applicationId", HDI_ADLS_CLIENT_ID,
            "clusterIdentity.certificate", CERT_CONTENT,
            "clusterIdentity.aadTenantId", "https://login.windows.net/" + tenantId,
            "clusterIdentity.resourceUri", "https://datalake.azure.net/",
            "clusterIdentity.certificatePassword", CERT_PASSWORD
        );
        configurations.put(clusterIdentity, clusterIdentityConfig);
        boolean isDefault = createParamsForADLSv1.properties().storageProfile().storageaccounts().isEmpty();
        if (isDefault) {
            String coreSite = "core-site";
            Map<String, String> coreConfig = ImmutableMap.of(
                "fs.defaultFS", "adl://home",
                "dfs.adls.home.hostname", HDI_ADLS_ACCOUNT_NAME + ".azuredatalakestore.net",
                "dfs.adls.home.mountpoint", ADLS_HOME_MOUNTPOINT
            );

            configurations.put(coreSite, coreConfig);
        }

        return createParamsForADLSv1;
    }

    public static ClusterCreateParametersExtended prepareClusterCreateParamsForADLSv2(String region,
                                                                                      String clusterName,
                                                                                      com.microsoft.azure.management.storage.StorageAccount storageAccount,
                                                                                      String storageAccountKey) {
        return prepareClusterCreateParamsForADLSv2(region, clusterName, storageAccount, storageAccountKey, null);
    }

    public static ClusterCreateParametersExtended prepareClusterCreateParamsForADLSv2(String region,
                                                                                      String clusterName,
                                                                                      com.microsoft.azure.management.storage.StorageAccount storageAccount,
                                                                                      String storageAccountKey,
                                                                                      ClusterCreateParametersExtended createParams) {
        ClusterCreateParametersExtended createParamsForADLSv2 = createParams != null ? createParams : prepareClusterCreateParams(region, clusterName);
        boolean isDefault = createParamsForADLSv2.properties().storageProfile().storageaccounts().isEmpty();
        createParamsForADLSv2.properties().storageProfile().storageaccounts().add(
            new StorageAccount()
                .withName(storageAccount.name() + STORAGE_ADLS_FILE_SYSTEM_ENDPOINT_SUFFIX)
                .withKey(storageAccountKey)
                .withFileSystem(clusterName.toLowerCase())
                .withIsDefault(isDefault)
        );

        return createParamsForADLSv2;
    }

    public static ClusterCreateParametersExtended prepareClusterCreateParams(String region, String clusterName) {
        return prepareClusterCreateParams(region, clusterName, null, null);
    }

    public static ClusterCreateParametersExtended prepareClusterCreateParams(String region,
                                                                             String clusterName,
                                                                             com.microsoft.azure.management.storage.StorageAccount storageAccount,
                                                                             String storageAccountKey) {
        List<StorageAccount> storageAccounts = Lists.newArrayList();
        if (storageAccount != null) {
            storageAccounts.add(
                new StorageAccount()
                    .withName(storageAccount.name() + STORAGE_BLOB_SERVICE_ENDPOINT_SUFFIX)
                    .withKey(storageAccountKey)
                    .withContainer(clusterName.toLowerCase())
                    .withIsDefault(true)
            );
        }

        return new ClusterCreateParametersExtended()
            .withLocation(region)
            .withProperties(new ClusterCreateProperties()
                .withClusterVersion("3.6")
                .withOsType(OSType.LINUX)
                .withTier(Tier.STANDARD)
                .withClusterDefinition(new ClusterDefinition()
                    .withKind("hadoop")
                    .withConfigurations(Maps.newHashMap(ImmutableMap.of(
                        "gateway", ImmutableMap.of(
                            "restAuthCredential.isEnabled", "true",
                            "restAuthCredential.username", CLUSTER_USERNAME,
                            "restAuthCredential.password", CLUSTER_PASSWORD
                        ))))
                )
                .withComputeProfile(new ComputeProfile()
                    .withRoles(Lists.newArrayList(
                        new Role().withName("headnode")
                            .withTargetInstanceCount(2)
                            .withHardwareProfile(new HardwareProfile()
                                .withVmSize("Large")
                            )
                            .withOsProfile(new OsProfile()
                                .withLinuxOperatingSystemProfile(
                                    new LinuxOperatingSystemProfile()
                                        .withUsername(SSH_USERNAME)
                                        .withPassword(SSH_PASSWORD)
                                )
                            ),
                        new Role().withName("workernode")
                            .withTargetInstanceCount(3)
                            .withHardwareProfile(new HardwareProfile()
                                .withVmSize("Large")
                            )
                            .withOsProfile(new OsProfile()
                                .withLinuxOperatingSystemProfile(
                                    new LinuxOperatingSystemProfile()
                                        .withUsername(SSH_USERNAME)
                                        .withPassword(SSH_PASSWORD)
                                )
                            ),
                        new Role().withName("zookeepernode")
                            .withTargetInstanceCount(3)
                            .withHardwareProfile(new HardwareProfile()
                                .withVmSize("Small")
                            )
                            .withOsProfile(new OsProfile()
                                .withLinuxOperatingSystemProfile(
                                    new LinuxOperatingSystemProfile()
                                        .withUsername(SSH_USERNAME)
                                        .withPassword(SSH_PASSWORD)
                                )
                            )
                    ))
                )
                .withStorageProfile(new StorageProfile()
                    .withStorageaccounts(storageAccounts)
                )
            );
    }
}
