# Code snippets and samples


## Applications

- [Create](#applications_create)
- [Delete](#applications_delete)
- [Get](#applications_get)
- [GetAzureAsyncOperationStatus](#applications_getazureasyncoperationstatus)
- [ListByCluster](#applications_listbycluster)

## Clusters

- [Create](#clusters_create)
- [Delete](#clusters_delete)
- [ExecuteScriptActions](#clusters_executescriptactions)
- [GetAzureAsyncOperationStatus](#clusters_getazureasyncoperationstatus)
- [GetByResourceGroup](#clusters_getbyresourcegroup)
- [GetGatewaySettings](#clusters_getgatewaysettings)
- [List](#clusters_list)
- [ListByResourceGroup](#clusters_listbyresourcegroup)
- [Resize](#clusters_resize)
- [RotateDiskEncryptionKey](#clusters_rotatediskencryptionkey)
- [Update](#clusters_update)
- [UpdateAutoScaleConfiguration](#clusters_updateautoscaleconfiguration)
- [UpdateGatewaySettings](#clusters_updategatewaysettings)
- [UpdateIdentityCertificate](#clusters_updateidentitycertificate)

## Configurations

- [Get](#configurations_get)
- [List](#configurations_list)
- [Update](#configurations_update)

## Extensions

- [Create](#extensions_create)
- [Delete](#extensions_delete)
- [DisableAzureMonitor](#extensions_disableazuremonitor)
- [DisableAzureMonitorAgent](#extensions_disableazuremonitoragent)
- [DisableMonitoring](#extensions_disablemonitoring)
- [EnableAzureMonitor](#extensions_enableazuremonitor)
- [EnableAzureMonitorAgent](#extensions_enableazuremonitoragent)
- [EnableMonitoring](#extensions_enablemonitoring)
- [Get](#extensions_get)
- [GetAzureAsyncOperationStatus](#extensions_getazureasyncoperationstatus)
- [GetAzureMonitorAgentStatus](#extensions_getazuremonitoragentstatus)
- [GetAzureMonitorStatus](#extensions_getazuremonitorstatus)
- [GetMonitoringStatus](#extensions_getmonitoringstatus)

## Locations

- [CheckNameAvailability](#locations_checknameavailability)
- [GetAzureAsyncOperationStatus](#locations_getazureasyncoperationstatus)
- [GetCapabilities](#locations_getcapabilities)
- [ListBillingSpecs](#locations_listbillingspecs)
- [ListUsages](#locations_listusages)
- [ValidateClusterCreateRequest](#locations_validateclustercreaterequest)

## Operations

- [List](#operations_list)

## PrivateEndpointConnections

- [CreateOrUpdate](#privateendpointconnections_createorupdate)
- [Delete](#privateendpointconnections_delete)
- [Get](#privateendpointconnections_get)
- [ListByCluster](#privateendpointconnections_listbycluster)

## PrivateLinkResources

- [Get](#privatelinkresources_get)
- [ListByCluster](#privatelinkresources_listbycluster)

## ScriptActions

- [Delete](#scriptactions_delete)
- [GetExecutionAsyncOperationStatus](#scriptactions_getexecutionasyncoperationstatus)
- [GetExecutionDetail](#scriptactions_getexecutiondetail)
- [ListByCluster](#scriptactions_listbycluster)

## ScriptExecutionHistory

- [ListByCluster](#scriptexecutionhistory_listbycluster)
- [Promote](#scriptexecutionhistory_promote)

## VirtualMachines

- [GetAsyncOperationStatus](#virtualmachines_getasyncoperationstatus)
- [ListHosts](#virtualmachines_listhosts)
- [RestartHosts](#virtualmachines_restarthosts)
### Applications_Create

```java
import com.azure.resourcemanager.hdinsight.models.ApplicationGetHttpsEndpoint;
import com.azure.resourcemanager.hdinsight.models.ApplicationProperties;
import com.azure.resourcemanager.hdinsight.models.ComputeProfile;
import com.azure.resourcemanager.hdinsight.models.HardwareProfile;
import com.azure.resourcemanager.hdinsight.models.Role;
import com.azure.resourcemanager.hdinsight.models.RuntimeScriptAction;
import java.util.Arrays;

/**
 * Samples for Applications Create.
 */
public final class ApplicationsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * CreateApplication.json
     */
    /**
     * Sample code: Create Application.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void createApplication(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.applications()
            .define("hue")
            .withExistingCluster("rg1", "cluster1")
            .withProperties(new ApplicationProperties()
                .withComputeProfile(new ComputeProfile().withRoles(Arrays.asList(new Role().withName("edgenode")
                    .withTargetInstanceCount(1)
                    .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D12_v2")))))
                .withInstallScriptActions(Arrays.asList(new RuntimeScriptAction().withName("app-install-app1")
                    .withUri("https://.../install.sh")
                    .withParameters("-version latest -port 20000")
                    .withRoles(Arrays.asList("edgenode"))))
                .withUninstallScriptActions(Arrays.asList())
                .withHttpsEndpoints(
                    Arrays.asList(new ApplicationGetHttpsEndpoint().withAccessModes(Arrays.asList("WebPage"))
                        .withDestinationPort(20000)
                        .withSubDomainSuffix("dss")))
                .withApplicationType("CustomApplication")
                .withErrors(Arrays.asList()))
            .create();
    }
}
```

### Applications_Delete

```java
/**
 * Samples for Applications Delete.
 */
public final class ApplicationsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * DeleteApplication.json
     */
    /**
     * Sample code: Delete Application from HDInsight cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        deleteApplicationFromHDInsightCluster(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.applications().delete("rg1", "cluster1", "hue", com.azure.core.util.Context.NONE);
    }
}
```

### Applications_Get

```java
/**
 * Samples for Applications Get.
 */
public final class ApplicationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetApplicationInProgress.json
     */
    /**
     * Sample code: Get application on HDInsight cluster creation in progress.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getApplicationOnHDInsightClusterCreationInProgress(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.applications().getWithResponse("rg1", "cluster1", "app", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetApplicationCreated.json
     */
    /**
     * Sample code: Get application on HDInsight cluster successfully created.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getApplicationOnHDInsightClusterSuccessfullyCreated(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.applications().getWithResponse("rg1", "cluster1", "app", com.azure.core.util.Context.NONE);
    }
}
```

### Applications_GetAzureAsyncOperationStatus

```java
/**
 * Samples for Applications GetAzureAsyncOperationStatus.
 */
public final class ApplicationsGetAzureAsyncOperationStatusSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetApplicationCreationAsyncOperationStatus.json
     */
    /**
     * Sample code: Get the azure async operation status.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getTheAzureAsyncOperationStatus(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.applications()
            .getAzureAsyncOperationStatusWithResponse("rg1", "cluster1", "app", "CF938302-6B4D-44A0-A6D2-C0D67E847AEC",
                com.azure.core.util.Context.NONE);
    }
}
```

### Applications_ListByCluster

```java
/**
 * Samples for Applications ListByCluster.
 */
public final class ApplicationsListByClusterSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetAllApplications.json
     */
    /**
     * Sample code: Get All Applications for an HDInsight cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        getAllApplicationsForAnHDInsightCluster(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.applications().listByCluster("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Create

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.hdinsight.models.Autoscale;
import com.azure.resourcemanager.hdinsight.models.AutoscaleRecurrence;
import com.azure.resourcemanager.hdinsight.models.AutoscaleSchedule;
import com.azure.resourcemanager.hdinsight.models.AutoscaleTimeAndCapacity;
import com.azure.resourcemanager.hdinsight.models.ClientGroupInfo;
import com.azure.resourcemanager.hdinsight.models.ClusterCreateProperties;
import com.azure.resourcemanager.hdinsight.models.ClusterDefinition;
import com.azure.resourcemanager.hdinsight.models.ComputeIsolationProperties;
import com.azure.resourcemanager.hdinsight.models.ComputeProfile;
import com.azure.resourcemanager.hdinsight.models.DataDisksGroups;
import com.azure.resourcemanager.hdinsight.models.DaysOfWeek;
import com.azure.resourcemanager.hdinsight.models.DirectoryType;
import com.azure.resourcemanager.hdinsight.models.DiskEncryptionProperties;
import com.azure.resourcemanager.hdinsight.models.EncryptionInTransitProperties;
import com.azure.resourcemanager.hdinsight.models.HardwareProfile;
import com.azure.resourcemanager.hdinsight.models.IpTag;
import com.azure.resourcemanager.hdinsight.models.KafkaRestProperties;
import com.azure.resourcemanager.hdinsight.models.LinuxOperatingSystemProfile;
import com.azure.resourcemanager.hdinsight.models.NetworkProperties;
import com.azure.resourcemanager.hdinsight.models.OsProfile;
import com.azure.resourcemanager.hdinsight.models.OSType;
import com.azure.resourcemanager.hdinsight.models.PrivateLink;
import com.azure.resourcemanager.hdinsight.models.ResourceProviderConnection;
import com.azure.resourcemanager.hdinsight.models.Role;
import com.azure.resourcemanager.hdinsight.models.SecurityProfile;
import com.azure.resourcemanager.hdinsight.models.SshProfile;
import com.azure.resourcemanager.hdinsight.models.SshPublicKey;
import com.azure.resourcemanager.hdinsight.models.StorageAccount;
import com.azure.resourcemanager.hdinsight.models.StorageProfile;
import com.azure.resourcemanager.hdinsight.models.Tier;
import com.azure.resourcemanager.hdinsight.models.VirtualNetworkProfile;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Clusters Create.
 */
public final class ClustersCreateSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * CreateLinuxHadoopSshPassword.json
     */
    /**
     * Sample code: Create Hadoop on Linux cluster with SSH password.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void createHadoopOnLinuxClusterWithSSHPassword(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) throws IOException {
        manager.clusters()
            .define("cluster1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withProperties(new ClusterCreateProperties().withClusterVersion("3.5")
                .withOsType(OSType.LINUX)
                .withTier(Tier.STANDARD)
                .withClusterDefinition(new ClusterDefinition().withKind("Hadoop")
                    .withConfigurations(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"gateway\":{\"restAuthCredential.isEnabled\":\"true\",\"restAuthCredential.password\":\"**********\",\"restAuthCredential.username\":\"admin\"}}",
                            Object.class, SerializerEncoding.JSON)))
                .withComputeProfile(new ComputeProfile().withRoles(Arrays.asList(
                    new Role().withName("headnode")
                        .withMinInstanceCount(1)
                        .withTargetInstanceCount(2)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D3_V2"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))),
                    new Role().withName("workernode")
                        .withMinInstanceCount(1)
                        .withTargetInstanceCount(4)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D3_V2"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))),
                    new Role().withName("zookeepernode")
                        .withMinInstanceCount(1)
                        .withTargetInstanceCount(3)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Small"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))))))
                .withStorageProfile(new StorageProfile()
                    .withStorageaccounts(Arrays.asList(new StorageAccount().withName("mystorage.blob.core.windows.net")
                        .withIsDefault(true)
                        .withContainer("containername")
                        .withKey("fakeTokenPlaceholder")
                        .withEnableSecureChannel(true)))))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * CreateKafkaClusterWithKafkaRestProxy.json
     */
    /**
     * Sample code: Create Kafka cluster with Kafka Rest Proxy.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void createKafkaClusterWithKafkaRestProxy(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) throws IOException {
        manager.clusters()
            .define("cluster1")
            .withExistingResourceGroup("rg1")
            .withProperties(new ClusterCreateProperties().withClusterVersion("4.0")
                .withOsType(OSType.LINUX)
                .withTier(Tier.STANDARD)
                .withClusterDefinition(new ClusterDefinition().withKind("kafka")
                    .withComponentVersion(mapOf("Kafka", "2.1"))
                    .withConfigurations(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"gateway\":{\"restAuthCredential.isEnabled\":true,\"restAuthCredential.password\":\"**********\",\"restAuthCredential.username\":\"admin\"}}",
                            Object.class, SerializerEncoding.JSON)))
                .withKafkaRestProperties(new KafkaRestProperties()
                    .withClientGroupInfo(new ClientGroupInfo().withGroupName("Kafka security group name")
                        .withGroupId("00000000-0000-0000-0000-111111111111")))
                .withComputeProfile(new ComputeProfile().withRoles(Arrays.asList(
                    new Role().withName("headnode")
                        .withTargetInstanceCount(2)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Large"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))),
                    new Role().withName("workernode")
                        .withTargetInstanceCount(3)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Large"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder")))
                        .withDataDisksGroups(Arrays.asList(new DataDisksGroups().withDisksPerNode(8))),
                    new Role().withName("zookeepernode")
                        .withTargetInstanceCount(3)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Small"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))),
                    new Role().withName("kafkamanagementnode")
                        .withTargetInstanceCount(2)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D4_v2"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("kafkauser")
                                .withPassword("fakeTokenPlaceholder"))))))
                .withStorageProfile(new StorageProfile()
                    .withStorageaccounts(Arrays.asList(new StorageAccount().withName("mystorage.blob.core.windows.net")
                        .withIsDefault(true)
                        .withContainer("containername")
                        .withKey("fakeTokenPlaceholder")
                        .withEnableSecureChannel(true)))))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * CreateHDInsightClusterWithAutoscaleConfig.json
     */
    /**
     * Sample code: Create HDInsight cluster with Autoscale configuration.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void createHDInsightClusterWithAutoscaleConfiguration(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) throws IOException {
        manager.clusters()
            .define("cluster1")
            .withExistingResourceGroup("rg1")
            .withProperties(new ClusterCreateProperties().withClusterVersion("3.6")
                .withOsType(OSType.LINUX)
                .withTier(Tier.STANDARD)
                .withClusterDefinition(new ClusterDefinition().withKind("hadoop")
                    .withComponentVersion(mapOf("Hadoop", "2.7"))
                    .withConfigurations(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"gateway\":{\"restAuthCredential.isEnabled\":true,\"restAuthCredential.password\":\"**********\",\"restAuthCredential.username\":\"admin\"}}",
                            Object.class, SerializerEncoding.JSON)))
                .withComputeProfile(
                    new ComputeProfile()
                        .withRoles(Arrays.asList(new Role().withName("workernode")
                            .withTargetInstanceCount(4)
                            .withAutoscaleConfiguration(new Autoscale().withRecurrence(new AutoscaleRecurrence()
                                .withTimeZone("China Standard Time")
                                .withSchedule(Arrays.asList(
                                    new AutoscaleSchedule()
                                        .withDays(Arrays.asList(DaysOfWeek.MONDAY, DaysOfWeek.TUESDAY,
                                            DaysOfWeek.WEDNESDAY, DaysOfWeek.THURSDAY, DaysOfWeek.FRIDAY))
                                        .withTimeAndCapacity(new AutoscaleTimeAndCapacity().withTime("09:00")
                                            .withMinInstanceCount(3)
                                            .withMaxInstanceCount(3)),
                                    new AutoscaleSchedule()
                                        .withDays(Arrays.asList(DaysOfWeek.MONDAY, DaysOfWeek.TUESDAY,
                                            DaysOfWeek.WEDNESDAY, DaysOfWeek.THURSDAY, DaysOfWeek.FRIDAY))
                                        .withTimeAndCapacity(
                                            new AutoscaleTimeAndCapacity().withTime("18:00")
                                                .withMinInstanceCount(6)
                                                .withMaxInstanceCount(6)),
                                    new AutoscaleSchedule()
                                        .withDays(Arrays.asList(DaysOfWeek.SATURDAY, DaysOfWeek.SUNDAY))
                                        .withTimeAndCapacity(new AutoscaleTimeAndCapacity().withTime("09:00")
                                            .withMinInstanceCount(2)
                                            .withMaxInstanceCount(2)),
                                    new AutoscaleSchedule()
                                        .withDays(Arrays.asList(DaysOfWeek.SATURDAY, DaysOfWeek.SUNDAY))
                                        .withTimeAndCapacity(new AutoscaleTimeAndCapacity().withTime("18:00")
                                            .withMinInstanceCount(4)
                                            .withMaxInstanceCount(4))))))
                            .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D4_V2"))
                            .withOsProfile(new OsProfile().withLinuxOperatingSystemProfile(
                                new LinuxOperatingSystemProfile().withUsername("sshuser")
                                    .withPassword("fakeTokenPlaceholder")))
                            .withScriptActions(Arrays.asList()))))
                .withStorageProfile(new StorageProfile()
                    .withStorageaccounts(Arrays.asList(new StorageAccount().withName("mystorage.blob.core.windows.net")
                        .withIsDefault(true)
                        .withContainer("hdinsight-autoscale-tes-2019-06-18t05-49-16-591z")
                        .withKey("fakeTokenPlaceholder")
                        .withEnableSecureChannel(true)))))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * CreateLinuxHadoopSshPublicKey.json
     */
    /**
     * Sample code: Create Hadoop on Linux cluster with SSH public key.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void createHadoopOnLinuxClusterWithSSHPublicKey(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) throws IOException {
        manager.clusters()
            .define("cluster1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withProperties(new ClusterCreateProperties().withClusterVersion("3.5")
                .withOsType(OSType.LINUX)
                .withTier(Tier.STANDARD)
                .withClusterDefinition(new ClusterDefinition().withKind("Hadoop")
                    .withConfigurations(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"gateway\":{\"restAuthCredential.isEnabled\":true,\"restAuthCredential.password\":\"**********\",\"restAuthCredential.username\":\"admin\"}}",
                            Object.class, SerializerEncoding.JSON)))
                .withComputeProfile(
                    new ComputeProfile()
                        .withRoles(
                            Arrays
                                .asList(
                                    new Role().withName("headnode")
                                        .withMinInstanceCount(1)
                                        .withTargetInstanceCount(2)
                                        .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D3_V2"))
                                        .withOsProfile(new OsProfile().withLinuxOperatingSystemProfile(
                                            new LinuxOperatingSystemProfile().withUsername("sshuser")
                                                .withSshProfile(new SshProfile().withPublicKeys(Arrays
                                                    .asList(new SshPublicKey().withCertificateData("**********")))))),
                                    new Role().withName("workernode")
                                        .withMinInstanceCount(1)
                                        .withTargetInstanceCount(4)
                                        .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D3_V2"))
                                        .withOsProfile(new OsProfile().withLinuxOperatingSystemProfile(
                                            new LinuxOperatingSystemProfile().withUsername("sshuser")
                                                .withPassword("fakeTokenPlaceholder"))),
                                    new Role().withName("zookeepernode")
                                        .withMinInstanceCount(1)
                                        .withTargetInstanceCount(3)
                                        .withHardwareProfile(new HardwareProfile().withVmSize("Small"))
                                        .withOsProfile(new OsProfile().withLinuxOperatingSystemProfile(
                                            new LinuxOperatingSystemProfile().withUsername("sshuser")
                                                .withPassword("fakeTokenPlaceholder"))))))
                .withStorageProfile(new StorageProfile()
                    .withStorageaccounts(Arrays.asList(new StorageAccount().withName("mystorage.blob.core.windows.net")
                        .withIsDefault(true)
                        .withContainer("containername")
                        .withKey("fakeTokenPlaceholder")
                        .withEnableSecureChannel(true)))))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * CreateHDInsightClusterWithAvailabilityZones.json
     */
    /**
     * Sample code: Create cluster with availability zones.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void createClusterWithAvailabilityZones(com.azure.resourcemanager.hdinsight.HDInsightManager manager)
        throws IOException {
        manager.clusters()
            .define("cluster1")
            .withExistingResourceGroup("rg1")
            .withZones(Arrays.asList("1"))
            .withProperties(new ClusterCreateProperties().withClusterVersion("3.6")
                .withOsType(OSType.LINUX)
                .withClusterDefinition(new ClusterDefinition().withKind("hadoop")
                    .withConfigurations(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"ambari-conf\":{\"database-name\":\"{ambari database name}\",\"database-server\":\"{sql server name}.database.windows.net\",\"database-user-name\":\"**********\",\"database-user-password\":\"**********\"},\"gateway\":{\"restAuthCredential.isEnabled\":true,\"restAuthCredential.password\":\"**********\",\"restAuthCredential.username\":\"admin\"},\"hive-env\":{\"hive_database\":\"Existing MSSQL Server database with SQL authentication\",\"hive_database_name\":\"{hive metastore name}\",\"hive_database_type\":\"mssql\",\"hive_existing_mssql_server_database\":\"{hive metastore name}\",\"hive_existing_mssql_server_host\":\"{sql server name}.database.windows.net\",\"hive_hostname\":\"{sql server name}.database.windows.net\"},\"hive-site\":{\"javax.jdo.option.ConnectionDriverName\":\"com.microsoft.sqlserver.jdbc.SQLServerDriver\",\"javax.jdo.option.ConnectionPassword\":\"**********!\",\"javax.jdo.option.ConnectionURL\":\"jdbc:sqlserver://{sql server name}.database.windows.net;database={hive metastore name};encrypt=true;trustServerCertificate=true;create=false;loginTimeout=300;sendStringParametersAsUnicode=true;prepareSQL=0\",\"javax.jdo.option.ConnectionUserName\":\"**********\"},\"oozie-env\":{\"oozie_database\":\"Existing MSSQL Server database with SQL authentication\",\"oozie_database_name\":\"{oozie metastore name}\",\"oozie_database_type\":\"mssql\",\"oozie_existing_mssql_server_database\":\"{oozie metastore name}\",\"oozie_existing_mssql_server_host\":\"{sql server name}.database.windows.net\",\"oozie_hostname\":\"{sql server name}.database.windows.net\"},\"oozie-site\":{\"oozie.db.schema.name\":\"oozie\",\"oozie.service.JPAService.jdbc.driver\":\"com.microsoft.sqlserver.jdbc.SQLServerDriver\",\"oozie.service.JPAService.jdbc.password\":\"**********\",\"oozie.service.JPAService.jdbc.url\":\"jdbc:sqlserver://{sql server name}.database.windows.net;database={oozie metastore name};encrypt=true;trustServerCertificate=true;create=false;loginTimeout=300;sendStringParametersAsUnicode=true;prepareSQL=0\",\"oozie.service.JPAService.jdbc.username\":\"**********\"}}",
                            Object.class, SerializerEncoding.JSON)))
                .withComputeProfile(new ComputeProfile().withRoles(Arrays.asList(
                    new Role().withName("headnode")
                        .withTargetInstanceCount(2)
                        .withHardwareProfile(new HardwareProfile().withVmSize("standard_d3"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder")
                                .withSshProfile(new SshProfile().withPublicKeys(
                                    Arrays.asList(new SshPublicKey().withCertificateData("**********"))))))
                        .withVirtualNetworkProfile(new VirtualNetworkProfile().withId(
                            "/subscriptions/subId/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/vnetname")
                            .withSubnet(
                                "/subscriptions/subId/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/vnetname/subnets/vnetsubnet")),
                    new Role().withName("workernode")
                        .withTargetInstanceCount(2)
                        .withHardwareProfile(new HardwareProfile().withVmSize("standard_d3"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder")
                                .withSshProfile(new SshProfile().withPublicKeys(
                                    Arrays.asList(new SshPublicKey().withCertificateData("**********"))))))
                        .withVirtualNetworkProfile(new VirtualNetworkProfile().withId(
                            "/subscriptions/subId/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/vnetname")
                            .withSubnet(
                                "/subscriptions/subId/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/vnetname/subnets/vnetsubnet")))))
                .withStorageProfile(
                    new StorageProfile().withStorageaccounts(Arrays.asList(new StorageAccount().withName("mystorage")
                        .withIsDefault(true)
                        .withContainer("containername")
                        .withKey("fakeTokenPlaceholder")
                        .withEnableSecureChannel(true)))))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * CreateLinuxHadoopAdlsGen2.json
     */
    /**
     * Sample code: Create Hadoop cluster with Azure Data Lake Storage Gen 2.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void createHadoopClusterWithAzureDataLakeStorageGen2(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) throws IOException {
        manager.clusters()
            .define("cluster1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withProperties(new ClusterCreateProperties().withClusterVersion("3.6")
                .withOsType(OSType.LINUX)
                .withTier(Tier.STANDARD)
                .withClusterDefinition(new ClusterDefinition().withKind("Hadoop")
                    .withConfigurations(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"gateway\":{\"restAuthCredential.isEnabled\":\"true\",\"restAuthCredential.password\":\"**********\",\"restAuthCredential.username\":\"admin\"}}",
                            Object.class, SerializerEncoding.JSON)))
                .withComputeProfile(new ComputeProfile().withRoles(Arrays.asList(
                    new Role().withName("headnode")
                        .withMinInstanceCount(1)
                        .withTargetInstanceCount(2)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D3_V2"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))),
                    new Role().withName("workernode")
                        .withMinInstanceCount(1)
                        .withTargetInstanceCount(4)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D3_V2"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))),
                    new Role().withName("zookeepernode")
                        .withMinInstanceCount(1)
                        .withTargetInstanceCount(3)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Small"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))))))
                .withStorageProfile(new StorageProfile()
                    .withStorageaccounts(Arrays.asList(new StorageAccount().withName("mystorage.dfs.core.windows.net")
                        .withIsDefault(true)
                        .withFileSystem("default")
                        .withKey("fakeTokenPlaceholder")
                        .withEnableSecureChannel(true)))))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * CreateLinuxHadoopSecureHadoop.json
     */
    /**
     * Sample code: Create Secure Hadoop cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void createSecureHadoopCluster(com.azure.resourcemanager.hdinsight.HDInsightManager manager)
        throws IOException {
        manager.clusters()
            .define("cluster1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withProperties(new ClusterCreateProperties().withClusterVersion("3.5")
                .withOsType(OSType.LINUX)
                .withTier(Tier.PREMIUM)
                .withClusterDefinition(new ClusterDefinition().withKind("Hadoop")
                    .withConfigurations(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"gateway\":{\"restAuthCredential.isEnabled\":true,\"restAuthCredential.password\":\"**********\",\"restAuthCredential.username\":\"admin\"}}",
                            Object.class, SerializerEncoding.JSON)))
                .withSecurityProfile(new SecurityProfile().withDirectoryType(DirectoryType.ACTIVE_DIRECTORY)
                    .withDomain("DomainName")
                    .withOrganizationalUnitDN("OU=Hadoop,DC=hdinsight,DC=test")
                    .withLdapsUrls(Arrays.asList("ldaps://10.10.0.4:636"))
                    .withDomainUsername("DomainUsername")
                    .withDomainUserPassword("fakeTokenPlaceholder")
                    .withClusterUsersGroupDNs(Arrays.asList("hdiusers")))
                .withComputeProfile(new ComputeProfile().withRoles(Arrays.asList(
                    new Role().withName("headnode")
                        .withMinInstanceCount(1)
                        .withTargetInstanceCount(2)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D3_V2"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder")
                                .withSshProfile(new SshProfile().withPublicKeys(
                                    Arrays.asList(new SshPublicKey().withCertificateData("**********"))))))
                        .withVirtualNetworkProfile(new VirtualNetworkProfile().withId(
                            "/subscriptions/subId/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/vnetname")
                            .withSubnet(
                                "/subscriptions/subId/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/vnetname/subnets/vnetsubnet"))
                        .withScriptActions(Arrays.asList()),
                    new Role().withName("workernode")
                        .withMinInstanceCount(1)
                        .withTargetInstanceCount(4)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D3_V2"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder")
                                .withSshProfile(new SshProfile().withPublicKeys(
                                    Arrays.asList(new SshPublicKey().withCertificateData("**********"))))))
                        .withVirtualNetworkProfile(new VirtualNetworkProfile().withId(
                            "/subscriptions/subId/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/vnetname")
                            .withSubnet(
                                "/subscriptions/subId/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/vnetname/subnets/vnetsubnet"))
                        .withScriptActions(Arrays.asList()),
                    new Role().withName("zookeepernode")
                        .withMinInstanceCount(1)
                        .withTargetInstanceCount(3)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Small"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder")
                                .withSshProfile(new SshProfile().withPublicKeys(
                                    Arrays.asList(new SshPublicKey().withCertificateData("**********"))))))
                        .withVirtualNetworkProfile(new VirtualNetworkProfile().withId(
                            "/subscriptions/subId/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/vnetname")
                            .withSubnet(
                                "/subscriptions/subId/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/vnetname/subnets/vnetsubnet"))
                        .withScriptActions(Arrays.asList()))))
                .withStorageProfile(new StorageProfile()
                    .withStorageaccounts(Arrays.asList(new StorageAccount().withName("mystorage.blob.core.windows.net")
                        .withIsDefault(true)
                        .withContainer("containername")
                        .withKey("fakeTokenPlaceholder")
                        .withEnableSecureChannel(true)))))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * CreateLinuxSparkSshPassword.json
     */
    /**
     * Sample code: Create Spark on Linux Cluster with SSH password.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void createSparkOnLinuxClusterWithSSHPassword(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) throws IOException {
        manager.clusters()
            .define("cluster1")
            .withExistingResourceGroup("rg1")
            .withTags(mapOf("key1", "fakeTokenPlaceholder"))
            .withProperties(new ClusterCreateProperties().withClusterVersion("3.5")
                .withOsType(OSType.LINUX)
                .withTier(Tier.STANDARD)
                .withClusterDefinition(new ClusterDefinition().withKind("Spark")
                    .withComponentVersion(mapOf("Spark", "2.0"))
                    .withConfigurations(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"gateway\":{\"restAuthCredential.isEnabled\":true,\"restAuthCredential.password\":\"**********\",\"restAuthCredential.username\":\"admin\"}}",
                            Object.class, SerializerEncoding.JSON)))
                .withComputeProfile(new ComputeProfile().withRoles(Arrays.asList(
                    new Role().withName("headnode")
                        .withMinInstanceCount(1)
                        .withTargetInstanceCount(2)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D12_V2"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))),
                    new Role().withName("workernode")
                        .withMinInstanceCount(1)
                        .withTargetInstanceCount(4)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D4_V2"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))))))
                .withStorageProfile(new StorageProfile()
                    .withStorageaccounts(Arrays.asList(new StorageAccount().withName("mystorage.blob.core.windows.net")
                        .withIsDefault(true)
                        .withContainer("containername")
                        .withKey("fakeTokenPlaceholder")
                        .withEnableSecureChannel(true)))))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * CreateHDInsightClusterWithCustomNetworkProperties.json
     */
    /**
     * Sample code: Create cluster with network properties.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void createClusterWithNetworkProperties(com.azure.resourcemanager.hdinsight.HDInsightManager manager)
        throws IOException {
        manager.clusters()
            .define("cluster1")
            .withExistingResourceGroup("rg1")
            .withProperties(new ClusterCreateProperties().withClusterVersion("3.6")
                .withOsType(OSType.LINUX)
                .withClusterDefinition(new ClusterDefinition().withKind("hadoop")
                    .withConfigurations(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"gateway\":{\"restAuthCredential.isEnabled\":true,\"restAuthCredential.password\":\"**********\",\"restAuthCredential.username\":\"admin\"}}",
                            Object.class, SerializerEncoding.JSON)))
                .withComputeProfile(new ComputeProfile().withRoles(Arrays.asList(
                    new Role().withName("headnode")
                        .withTargetInstanceCount(2)
                        .withHardwareProfile(new HardwareProfile().withVmSize("standard_d3"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder")
                                .withSshProfile(new SshProfile().withPublicKeys(
                                    Arrays.asList(new SshPublicKey().withCertificateData("**********"))))))
                        .withVirtualNetworkProfile(new VirtualNetworkProfile().withId(
                            "/subscriptions/subId/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/vnetname")
                            .withSubnet(
                                "/subscriptions/subId/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/vnetname/subnets/vnetsubnet")),
                    new Role().withName("workernode")
                        .withTargetInstanceCount(2)
                        .withHardwareProfile(new HardwareProfile().withVmSize("standard_d3"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder")
                                .withSshProfile(new SshProfile().withPublicKeys(
                                    Arrays.asList(new SshPublicKey().withCertificateData("**********"))))))
                        .withVirtualNetworkProfile(new VirtualNetworkProfile().withId(
                            "/subscriptions/subId/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/vnetname")
                            .withSubnet(
                                "/subscriptions/subId/resourceGroups/rg/providers/Microsoft.Network/virtualNetworks/vnetname/subnets/vnetsubnet")))))
                .withStorageProfile(
                    new StorageProfile().withStorageaccounts(Arrays.asList(new StorageAccount().withName("mystorage")
                        .withIsDefault(true)
                        .withContainer("containername")
                        .withKey("fakeTokenPlaceholder")
                        .withEnableSecureChannel(true))))
                .withNetworkProperties(
                    new NetworkProperties().withResourceProviderConnection(ResourceProviderConnection.OUTBOUND)
                        .withPrivateLink(PrivateLink.ENABLED)
                        .withPublicIpTag(new IpTag().withIpTagType("FirstPartyUsage").withTag("/<TagName>"))))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * CreateHDInsightClusterWithTLS12.json
     */
    /**
     * Sample code: Create cluster with TLS 1.2.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void createClusterWithTLS12(com.azure.resourcemanager.hdinsight.HDInsightManager manager)
        throws IOException {
        manager.clusters()
            .define("cluster1")
            .withExistingResourceGroup("rg1")
            .withProperties(new ClusterCreateProperties().withClusterVersion("3.6")
                .withOsType(OSType.LINUX)
                .withTier(Tier.STANDARD)
                .withClusterDefinition(new ClusterDefinition().withKind("Hadoop")
                    .withConfigurations(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"gateway\":{\"restAuthCredential.isEnabled\":true,\"restAuthCredential.password\":\"**********\",\"restAuthCredential.username\":\"admin\"}}",
                            Object.class, SerializerEncoding.JSON)))
                .withComputeProfile(new ComputeProfile().withRoles(Arrays.asList(
                    new Role().withName("headnode")
                        .withTargetInstanceCount(2)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Large"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))),
                    new Role().withName("workernode")
                        .withTargetInstanceCount(3)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Large"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))),
                    new Role().withName("zookeepernode")
                        .withTargetInstanceCount(3)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Small"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))))))
                .withStorageProfile(new StorageProfile()
                    .withStorageaccounts(Arrays.asList(new StorageAccount().withName("mystorage.blob.core.windows.net")
                        .withIsDefault(true)
                        .withContainer("default8525")
                        .withKey("fakeTokenPlaceholder")
                        .withEnableSecureChannel(true))))
                .withMinSupportedTlsVersion("1.2"))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * CreateHDInsightClusterWithEncryptionAtHost.json
     */
    /**
     * Sample code: Create cluster with encryption at host.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void createClusterWithEncryptionAtHost(com.azure.resourcemanager.hdinsight.HDInsightManager manager)
        throws IOException {
        manager.clusters()
            .define("cluster1")
            .withExistingResourceGroup("rg1")
            .withProperties(new ClusterCreateProperties().withClusterVersion("3.6")
                .withOsType(OSType.LINUX)
                .withTier(Tier.STANDARD)
                .withClusterDefinition(new ClusterDefinition().withKind("Hadoop")
                    .withConfigurations(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"gateway\":{\"restAuthCredential.isEnabled\":true,\"restAuthCredential.password\":\"**********\",\"restAuthCredential.username\":\"admin\"}}",
                            Object.class, SerializerEncoding.JSON)))
                .withComputeProfile(new ComputeProfile().withRoles(Arrays.asList(
                    new Role().withName("headnode")
                        .withTargetInstanceCount(2)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Standard_DS14_v2"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))),
                    new Role().withName("workernode")
                        .withTargetInstanceCount(3)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Standard_DS14_v2"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))),
                    new Role().withName("zookeepernode")
                        .withTargetInstanceCount(3)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Standard_DS14_v2"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))))))
                .withStorageProfile(new StorageProfile()
                    .withStorageaccounts(Arrays.asList(new StorageAccount().withName("mystorage.blob.core.windows.net")
                        .withIsDefault(true)
                        .withContainer("default8525")
                        .withKey("fakeTokenPlaceholder")
                        .withEnableSecureChannel(true))))
                .withDiskEncryptionProperties(new DiskEncryptionProperties().withEncryptionAtHost(true)))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * CreateHDInsightClusterWithEncryptionInTransit.json
     */
    /**
     * Sample code: Create cluster with encryption in transit.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void createClusterWithEncryptionInTransit(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) throws IOException {
        manager.clusters()
            .define("cluster1")
            .withExistingResourceGroup("rg1")
            .withProperties(new ClusterCreateProperties().withClusterVersion("3.6")
                .withOsType(OSType.LINUX)
                .withTier(Tier.STANDARD)
                .withClusterDefinition(new ClusterDefinition().withKind("Hadoop")
                    .withConfigurations(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"gateway\":{\"restAuthCredential.isEnabled\":true,\"restAuthCredential.password\":\"**********\",\"restAuthCredential.username\":\"admin\"}}",
                            Object.class, SerializerEncoding.JSON)))
                .withComputeProfile(new ComputeProfile().withRoles(Arrays.asList(
                    new Role().withName("headnode")
                        .withTargetInstanceCount(2)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Large"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))),
                    new Role().withName("workernode")
                        .withTargetInstanceCount(3)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Large"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))),
                    new Role().withName("zookeepernode")
                        .withTargetInstanceCount(3)
                        .withHardwareProfile(new HardwareProfile().withVmSize("Small"))
                        .withOsProfile(new OsProfile()
                            .withLinuxOperatingSystemProfile(new LinuxOperatingSystemProfile().withUsername("sshuser")
                                .withPassword("fakeTokenPlaceholder"))))))
                .withStorageProfile(new StorageProfile()
                    .withStorageaccounts(Arrays.asList(new StorageAccount().withName("mystorage.blob.core.windows.net")
                        .withIsDefault(true)
                        .withContainer("default8525")
                        .withKey("fakeTokenPlaceholder")
                        .withEnableSecureChannel(true))))
                .withEncryptionInTransitProperties(
                    new EncryptionInTransitProperties().withIsEncryptionInTransitEnabled(true)))
            .create();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * CreateHDInsightClusterWithComputeIsolationProperties.json
     */
    /**
     * Sample code: Create cluster with compute isolation properties.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void createClusterWithComputeIsolationProperties(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) throws IOException {
        manager.clusters()
            .define("cluster1")
            .withExistingResourceGroup("rg1")
            .withProperties(new ClusterCreateProperties().withClusterVersion("3.6")
                .withOsType(OSType.LINUX)
                .withClusterDefinition(new ClusterDefinition().withKind("hadoop")
                    .withConfigurations(SerializerFactory.createDefaultManagementSerializerAdapter()
                        .deserialize(
                            "{\"gateway\":{\"restAuthCredential.isEnabled\":true,\"restAuthCredential.password\":\"**********\",\"restAuthCredential.username\":\"admin\"}}",
                            Object.class, SerializerEncoding.JSON)))
                .withComputeProfile(
                    new ComputeProfile()
                        .withRoles(
                            Arrays
                                .asList(
                                    new Role().withName("headnode")
                                        .withTargetInstanceCount(2)
                                        .withHardwareProfile(new HardwareProfile().withVmSize("standard_d3"))
                                        .withOsProfile(new OsProfile().withLinuxOperatingSystemProfile(
                                            new LinuxOperatingSystemProfile().withUsername("sshuser")
                                                .withPassword("fakeTokenPlaceholder")
                                                .withSshProfile(new SshProfile().withPublicKeys(Arrays
                                                    .asList(new SshPublicKey().withCertificateData("**********")))))),
                                    new Role().withName("workernode")
                                        .withTargetInstanceCount(2)
                                        .withHardwareProfile(new HardwareProfile().withVmSize("standard_d3"))
                                        .withOsProfile(new OsProfile().withLinuxOperatingSystemProfile(
                                            new LinuxOperatingSystemProfile().withUsername("sshuser")
                                                .withPassword("fakeTokenPlaceholder")
                                                .withSshProfile(new SshProfile().withPublicKeys(Arrays
                                                    .asList(new SshPublicKey().withCertificateData("**********")))))))))
                .withStorageProfile(
                    new StorageProfile().withStorageaccounts(Arrays.asList(new StorageAccount().withName("mystorage")
                        .withIsDefault(true)
                        .withContainer("containername")
                        .withKey("fakeTokenPlaceholder")
                        .withEnableSecureChannel(true))))
                .withComputeIsolationProperties(new ComputeIsolationProperties().withEnableComputeIsolation(true)))
            .create();
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### Clusters_Delete

```java
/**
 * Samples for Clusters Delete.
 */
public final class ClustersDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * DeleteLinuxHadoopCluster.json
     */
    /**
     * Sample code: Delete Hadoop on Linux cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void deleteHadoopOnLinuxCluster(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.clusters().delete("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ExecuteScriptActions

```java
import com.azure.resourcemanager.hdinsight.models.ExecuteScriptActionParameters;
import com.azure.resourcemanager.hdinsight.models.RuntimeScriptAction;
import java.util.Arrays;

/**
 * Samples for Clusters ExecuteScriptActions.
 */
public final class ClustersExecuteScriptActionsSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * PostExecuteScriptAction.json
     */
    /**
     * Sample code: Execute script action on HDInsight cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        executeScriptActionOnHDInsightCluster(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.clusters()
            .executeScriptActions("rg1", "cluster1",
                new ExecuteScriptActionParameters()
                    .withScriptActions(Arrays.asList(new RuntimeScriptAction().withName("Test")
                        .withUri("http://testurl.com/install.ssh")
                        .withParameters("")
                        .withRoles(Arrays.asList("headnode", "workernode"))))
                    .withPersistOnSuccess(false),
                com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_GetAzureAsyncOperationStatus

```java
/**
 * Samples for Clusters GetAzureAsyncOperationStatus.
 */
public final class ClustersGetAzureAsyncOperationStatusSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetClusterCreatingAsyncOperationStatus.json
     */
    /**
     * Sample code: Get Async Operation Status of Creating Cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        getAsyncOperationStatusOfCreatingCluster(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.clusters()
            .getAzureAsyncOperationStatusWithResponse("rg1", "cluster1", "CF938302-6B4D-44A0-A6D2-C0D67E847AEC",
                com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_GetByResourceGroup

```java
/**
 * Samples for Clusters GetByResourceGroup.
 */
public final class ClustersGetByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetLinuxHadoopCluster.json
     */
    /**
     * Sample code: Get Hadoop on Linux cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getHadoopOnLinuxCluster(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.clusters().getByResourceGroupWithResponse("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetLinuxSparkCluster.json
     */
    /**
     * Sample code: Get Spark on Linux cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getSparkOnLinuxCluster(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.clusters().getByResourceGroupWithResponse("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_GetGatewaySettings

```java
/**
 * Samples for Clusters GetGatewaySettings.
 */
public final class ClustersGetGatewaySettingsSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * HDI_Clusters_GetGatewaySettings.json
     */
    /**
     * Sample code: Get HTTP settings.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getHTTPSettings(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.clusters().getGatewaySettingsWithResponse("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_List

```java
/**
 * Samples for Clusters List.
 */
public final class ClustersListSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetLinuxHadoopAllClusters.json
     */
    /**
     * Sample code: Get All Hadoop on Linux clusters.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getAllHadoopOnLinuxClusters(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.clusters().list(com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_ListByResourceGroup

```java
/**
 * Samples for Clusters ListByResourceGroup.
 */
public final class ClustersListByResourceGroupSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetLinuxHadoopAllClustersInResourceGroup.json
     */
    /**
     * Sample code: Get All Hadoop on Linux clusters in a resource group.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        getAllHadoopOnLinuxClustersInAResourceGroup(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.clusters().listByResourceGroup("rg1", com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Resize

```java
import com.azure.resourcemanager.hdinsight.models.ClusterResizeParameters;
import com.azure.resourcemanager.hdinsight.models.RoleName;

/**
 * Samples for Clusters Resize.
 */
public final class ClustersResizeSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * ResizeLinuxHadoopCluster.json
     */
    /**
     * Sample code: Resize the worker nodes for a Hadoop on Linux cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        resizeTheWorkerNodesForAHadoopOnLinuxCluster(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.clusters()
            .resize("rg1", "cluster1", RoleName.WORKERNODE, new ClusterResizeParameters().withTargetInstanceCount(10),
                com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_RotateDiskEncryptionKey

```java
import com.azure.resourcemanager.hdinsight.models.ClusterDiskEncryptionParameters;

/**
 * Samples for Clusters RotateDiskEncryptionKey.
 */
public final class ClustersRotateDiskEncryptionKeySamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * RotateLinuxHadoopClusterDiskEncryptionKey.json
     */
    /**
     * Sample code: Rotate disk encryption key of the specified HDInsight cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void rotateDiskEncryptionKeyOfTheSpecifiedHDInsightCluster(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.clusters()
            .rotateDiskEncryptionKey("rg1", "cluster1",
                new ClusterDiskEncryptionParameters().withVaultUri("https://newkeyvault.vault.azure.net/")
                    .withKeyName("fakeTokenPlaceholder")
                    .withKeyVersion("fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_Update

```java
import com.azure.resourcemanager.hdinsight.models.Cluster;
import com.azure.resourcemanager.hdinsight.models.ClusterIdentity;
import com.azure.resourcemanager.hdinsight.models.ResourceIdentityType;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Clusters Update.
 */
public final class ClustersUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * PatchLinuxHadoopCluster.json
     */
    /**
     * Sample code: Patch HDInsight Linux clusters.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void patchHDInsightLinuxClusters(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        Cluster resource = manager.clusters()
            .getByResourceGroupWithResponse("rg1", "cluster1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update().withTags(mapOf("key1", "fakeTokenPlaceholder", "key2", "fakeTokenPlaceholder")).apply();
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * PatchLinuxHadoopClusterWithSystemMSI.json
     */
    /**
     * Sample code: Patch HDInsight Linux clusters with system assigned MSI.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        patchHDInsightLinuxClustersWithSystemAssignedMSI(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        Cluster resource = manager.clusters()
            .getByResourceGroupWithResponse("rg1", "cluster1", com.azure.core.util.Context.NONE)
            .getValue();
        resource.update()
            .withTags(mapOf("key1", "fakeTokenPlaceholder", "key2", "fakeTokenPlaceholder"))
            .withIdentity(new ClusterIdentity().withType(ResourceIdentityType.SYSTEM_ASSIGNED))
            .apply();
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### Clusters_UpdateAutoScaleConfiguration

```java
import com.azure.resourcemanager.hdinsight.models.Autoscale;
import com.azure.resourcemanager.hdinsight.models.AutoscaleCapacity;
import com.azure.resourcemanager.hdinsight.models.AutoscaleConfigurationUpdateParameter;
import com.azure.resourcemanager.hdinsight.models.AutoscaleRecurrence;
import com.azure.resourcemanager.hdinsight.models.AutoscaleSchedule;
import com.azure.resourcemanager.hdinsight.models.AutoscaleTimeAndCapacity;
import com.azure.resourcemanager.hdinsight.models.DaysOfWeek;
import com.azure.resourcemanager.hdinsight.models.RoleName;
import java.util.Arrays;

/**
 * Samples for Clusters UpdateAutoScaleConfiguration.
 */
public final class ClustersUpdateAutoScaleConfigurationSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * EnableOrUpdateAutoScaleWithLoadBasedConfiguration.json
     */
    /**
     * Sample code: Enable or Update Autoscale with the load based configuration for HDInsight cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void enableOrUpdateAutoscaleWithTheLoadBasedConfigurationForHDInsightCluster(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.clusters()
            .updateAutoScaleConfiguration("rg1", "cluster1", RoleName.WORKERNODE,
                new AutoscaleConfigurationUpdateParameter().withAutoscale(new Autoscale()
                    .withCapacity(new AutoscaleCapacity().withMinInstanceCount(3).withMaxInstanceCount(5))),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * DisableClusterAutoScale.json
     */
    /**
     * Sample code: Disable Autoscale for the HDInsight cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        disableAutoscaleForTheHDInsightCluster(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.clusters()
            .updateAutoScaleConfiguration("rg1", "cluster1", RoleName.WORKERNODE,
                new AutoscaleConfigurationUpdateParameter(), com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * EnableOrUpdateAutoScaleWithScheduleBasedConfiguration.json
     */
    /**
     * Sample code: Enable or Update Autoscale with the schedule based configuration for HDInsight cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void enableOrUpdateAutoscaleWithTheScheduleBasedConfigurationForHDInsightCluster(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.clusters()
            .updateAutoScaleConfiguration("rg1", "cluster1", RoleName.WORKERNODE,
                new AutoscaleConfigurationUpdateParameter().withAutoscale(
                    new Autoscale().withRecurrence(new AutoscaleRecurrence().withTimeZone("China Standard Time")
                        .withSchedule(Arrays.asList(new AutoscaleSchedule().withDays(Arrays.asList(DaysOfWeek.THURSDAY))
                            .withTimeAndCapacity(new AutoscaleTimeAndCapacity().withTime("16:00")
                                .withMinInstanceCount(4)
                                .withMaxInstanceCount(4)))))),
                com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_UpdateGatewaySettings

```java
import com.azure.resourcemanager.hdinsight.models.UpdateGatewaySettingsParameters;

/**
 * Samples for Clusters UpdateGatewaySettings.
 */
public final class ClustersUpdateGatewaySettingsSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * HDI_Clusters_UpdateGatewaySettings_Enable.json
     */
    /**
     * Sample code: Enable HTTP connectivity.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void enableHTTPConnectivity(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.clusters()
            .updateGatewaySettings("rg1", "cluster1", new UpdateGatewaySettingsParameters(),
                com.azure.core.util.Context.NONE);
    }
}
```

### Clusters_UpdateIdentityCertificate

```java
import com.azure.resourcemanager.hdinsight.models.UpdateClusterIdentityCertificateParameters;

/**
 * Samples for Clusters UpdateIdentityCertificate.
 */
public final class ClustersUpdateIdentityCertificateSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * HDI_Clusters_UpdateClusterIdentityCertificate.json
     */
    /**
     * Sample code: Update cluster identity certificate.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void updateClusterIdentityCertificate(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.clusters()
            .updateIdentityCertificate("rg1", "cluster1",
                new UpdateClusterIdentityCertificateParameters().withApplicationId("applicationId")
                    .withCertificate("base64encodedcertificate")
                    .withCertificatePassword("fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_Get

```java
/**
 * Samples for Configurations Get.
 */
public final class ConfigurationsGetSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * HDI_Configurations_Get.json
     */
    /**
     * Sample code: Get Core site settings.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getCoreSiteSettings(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.configurations().getWithResponse("rg1", "cluster1", "core-site", com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_List

```java
/**
 * Samples for Configurations List.
 */
public final class ConfigurationsListSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * HDI_Configurations_List.json
     */
    /**
     * Sample code: Get all configuration information.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getAllConfigurationInformation(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.configurations().listWithResponse("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Configurations_Update

```java
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Configurations Update.
 */
public final class ConfigurationsUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * ChangeHttpConnectivityEnable.json
     */
    /**
     * Sample code: Enable HTTP connectivity.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void enableHTTPConnectivity(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.configurations()
            .update("rg1", "cluster1", "gateway",
                mapOf("restAuthCredential.isEnabled", "fakeTokenPlaceholder", "restAuthCredential.password",
                    "fakeTokenPlaceholder", "restAuthCredential.username", "fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }

    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * ChangeHttpConnectivityDisable.json
     */
    /**
     * Sample code: Disable HTTP connectivity.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void disableHTTPConnectivity(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.configurations()
            .update("rg1", "cluster1", "gateway", mapOf("restAuthCredential.isEnabled", "fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### Extensions_Create

```java
import com.azure.resourcemanager.hdinsight.models.Extension;

/**
 * Samples for Extensions Create.
 */
public final class ExtensionsCreateSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/CreateExtension.
     * json
     */
    /**
     * Sample code: Create a monitoring extension on Hadoop Linux cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        createAMonitoringExtensionOnHadoopLinuxCluster(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.extensions()
            .create("rg1", "cluster1", "clustermonitoring",
                new Extension().withWorkspaceId("a2090ead-8c9f-4fba-b70e-533e3e003163")
                    .withPrimaryKey("fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_Delete

```java
/**
 * Samples for Extensions Delete.
 */
public final class ExtensionsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/DeleteExtension.
     * json
     */
    /**
     * Sample code: Delete an extension.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void deleteAnExtension(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.extensions().delete("rg1", "cluster1", "clustermonitoring", com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_DisableAzureMonitor

```java
/**
 * Samples for Extensions DisableAzureMonitor.
 */
public final class ExtensionsDisableAzureMonitorSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * DisableLinuxClusterAzureMonitor.json
     */
    /**
     * Sample code: Disable azure monitor.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void disableAzureMonitor(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.extensions().disableAzureMonitor("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_DisableAzureMonitorAgent

```java
/**
 * Samples for Extensions DisableAzureMonitorAgent.
 */
public final class ExtensionsDisableAzureMonitorAgentSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * DisableLinuxClusterAzureMonitorAgent.json
     */
    /**
     * Sample code: Disable azure monitor agent.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void disableAzureMonitorAgent(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.extensions().disableAzureMonitorAgent("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_DisableMonitoring

```java
/**
 * Samples for Extensions DisableMonitoring.
 */
public final class ExtensionsDisableMonitoringSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * DisableLinuxClusterMonitoring.json
     */
    /**
     * Sample code: Disable cluster monitoring.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void disableClusterMonitoring(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.extensions().disableMonitoring("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_EnableAzureMonitor

```java
import com.azure.resourcemanager.hdinsight.models.AzureMonitorRequest;

/**
 * Samples for Extensions EnableAzureMonitor.
 */
public final class ExtensionsEnableAzureMonitorSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * EnableLinuxClusterAzureMonitor.json
     */
    /**
     * Sample code: Enable azure monitor.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void enableAzureMonitor(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.extensions()
            .enableAzureMonitor("rg1", "cluster1",
                new AzureMonitorRequest().withWorkspaceId("a2090ead-8c9f-4fba-b70e-533e3e003163")
                    .withPrimaryKey("fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_EnableAzureMonitorAgent

```java
import com.azure.resourcemanager.hdinsight.models.AzureMonitorRequest;

/**
 * Samples for Extensions EnableAzureMonitorAgent.
 */
public final class ExtensionsEnableAzureMonitorAgentSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * EnableLinuxClusterAzureMonitorAgent.json
     */
    /**
     * Sample code: Enable azure monitoring agent.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void enableAzureMonitoringAgent(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.extensions()
            .enableAzureMonitorAgent("rg1", "cluster1",
                new AzureMonitorRequest().withWorkspaceId("a2090ead-8c9f-4fba-b70e-533e3e003163")
                    .withPrimaryKey("fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_EnableMonitoring

```java
import com.azure.resourcemanager.hdinsight.models.ClusterMonitoringRequest;

/**
 * Samples for Extensions EnableMonitoring.
 */
public final class ExtensionsEnableMonitoringSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * EnableLinuxClusterMonitoring.json
     */
    /**
     * Sample code: Enable cluster monitoring.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void enableClusterMonitoring(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.extensions()
            .enableMonitoring("rg1", "cluster1",
                new ClusterMonitoringRequest().withWorkspaceId("a2090ead-8c9f-4fba-b70e-533e3e003163")
                    .withPrimaryKey("fakeTokenPlaceholder"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_Get

```java
/**
 * Samples for Extensions Get.
 */
public final class ExtensionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/GetExtension.
     * json
     */
    /**
     * Sample code: Get an extension.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getAnExtension(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.extensions().getWithResponse("rg1", "cluster1", "clustermonitoring", com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_GetAzureAsyncOperationStatus

```java
/**
 * Samples for Extensions GetAzureAsyncOperationStatus.
 */
public final class ExtensionsGetAzureAsyncOperationStatusSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetExtensionCreationAsyncOperationStatus.json
     */
    /**
     * Sample code: Gets the azure async operation status.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getsTheAzureAsyncOperationStatus(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.extensions()
            .getAzureAsyncOperationStatusWithResponse("rg1", "cluster1", "azuremonitor",
                "CF938302-6B4D-44A0-A6D2-C0D67E847AEC", com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_GetAzureMonitorAgentStatus

```java
/**
 * Samples for Extensions GetAzureMonitorAgentStatus.
 */
public final class ExtensionsGetAzureMonitorAgentStatusSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetLinuxClusterAzureMonitorAgentStatus.json
     */
    /**
     * Sample code: Get azure monitor agent status.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getAzureMonitorAgentStatus(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.extensions()
            .getAzureMonitorAgentStatusWithResponse("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_GetAzureMonitorStatus

```java
/**
 * Samples for Extensions GetAzureMonitorStatus.
 */
public final class ExtensionsGetAzureMonitorStatusSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetLinuxClusterAzureMonitorStatus.json
     */
    /**
     * Sample code: Get azure monitor status.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getAzureMonitorStatus(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.extensions().getAzureMonitorStatusWithResponse("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Extensions_GetMonitoringStatus

```java
/**
 * Samples for Extensions GetMonitoringStatus.
 */
public final class ExtensionsGetMonitoringStatusSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetLinuxClusterMonitoringStatus.json
     */
    /**
     * Sample code: Get cluster monitoring status.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getClusterMonitoringStatus(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.extensions().getMonitoringStatusWithResponse("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### Locations_CheckNameAvailability

```java
import com.azure.resourcemanager.hdinsight.models.NameAvailabilityCheckRequestParameters;

/**
 * Samples for Locations CheckNameAvailability.
 */
public final class LocationsCheckNameAvailabilitySamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * HDI_Locations_CheckClusterNameAvailability.json
     */
    /**
     * Sample code: Get the subscription usages for specific location.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        getTheSubscriptionUsagesForSpecificLocation(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.locations()
            .checkNameAvailabilityWithResponse("westus",
                new NameAvailabilityCheckRequestParameters().withName("test123").withType("clusters"),
                com.azure.core.util.Context.NONE);
    }
}
```

### Locations_GetAzureAsyncOperationStatus

```java
/**
 * Samples for Locations GetAzureAsyncOperationStatus.
 */
public final class LocationsGetAzureAsyncOperationStatusSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * HDI_Locations_GetAsyncOperationStatus.json
     */
    /**
     * Sample code: Gets the azure async operation status.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getsTheAzureAsyncOperationStatus(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.locations()
            .getAzureAsyncOperationStatusWithResponse("East US 2", "8a0348f4-8a85-4ec2-abe0-03b26104a9a0-0",
                com.azure.core.util.Context.NONE);
    }
}
```

### Locations_GetCapabilities

```java
/**
 * Samples for Locations GetCapabilities.
 */
public final class LocationsGetCapabilitiesSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetHDInsightCapabilities.json
     */
    /**
     * Sample code: Get the subscription capabilities for specific location.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getTheSubscriptionCapabilitiesForSpecificLocation(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.locations().getCapabilitiesWithResponse("West US", com.azure.core.util.Context.NONE);
    }
}
```

### Locations_ListBillingSpecs

```java
/**
 * Samples for Locations ListBillingSpecs.
 */
public final class LocationsListBillingSpecsSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * HDI_Locations_ListBillingSpecs.json
     */
    /**
     * Sample code: Get the subscription billingSpecs for the specified location.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getTheSubscriptionBillingSpecsForTheSpecifiedLocation(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.locations().listBillingSpecsWithResponse("East US 2", com.azure.core.util.Context.NONE);
    }
}
```

### Locations_ListUsages

```java
/**
 * Samples for Locations ListUsages.
 */
public final class LocationsListUsagesSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetHDInsightUsages.json
     */
    /**
     * Sample code: Get the subscription usages for specific location.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        getTheSubscriptionUsagesForSpecificLocation(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.locations().listUsagesWithResponse("West US", com.azure.core.util.Context.NONE);
    }
}
```

### Locations_ValidateClusterCreateRequest

```java
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.resourcemanager.hdinsight.models.ClusterCreateProperties;
import com.azure.resourcemanager.hdinsight.models.ClusterCreateRequestValidationParameters;
import com.azure.resourcemanager.hdinsight.models.ClusterDefinition;
import com.azure.resourcemanager.hdinsight.models.ComputeProfile;
import com.azure.resourcemanager.hdinsight.models.HardwareProfile;
import com.azure.resourcemanager.hdinsight.models.LinuxOperatingSystemProfile;
import com.azure.resourcemanager.hdinsight.models.OsProfile;
import com.azure.resourcemanager.hdinsight.models.OSType;
import com.azure.resourcemanager.hdinsight.models.Role;
import com.azure.resourcemanager.hdinsight.models.StorageAccount;
import com.azure.resourcemanager.hdinsight.models.StorageProfile;
import com.azure.resourcemanager.hdinsight.models.Tier;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Samples for Locations ValidateClusterCreateRequest.
 */
public final class LocationsValidateClusterCreateRequestSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * HDI_Locations_ValidateClusterCreateRequest.json
     */
    /**
     * Sample code: Get the subscription usages for specific location.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getTheSubscriptionUsagesForSpecificLocation(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) throws IOException {
        manager.locations()
            .validateClusterCreateRequestWithResponse("southcentralus", new ClusterCreateRequestValidationParameters()
                .withLocation("southcentralus")
                .withTags(mapOf())
                .withProperties(new ClusterCreateProperties().withClusterVersion("4.0")
                    .withOsType(OSType.LINUX)
                    .withTier(Tier.STANDARD)
                    .withClusterDefinition(new ClusterDefinition().withKind("spark")
                        .withComponentVersion(mapOf("Spark", "2.4"))
                        .withConfigurations(SerializerFactory.createDefaultManagementSerializerAdapter()
                            .deserialize(
                                "{\"gateway\":{\"restAuthCredential.isEnabled\":true,\"restAuthCredential.password\":\"**********\",\"restAuthCredential.username\":\"admin\"}}",
                                Object.class, SerializerEncoding.JSON)))
                    .withComputeProfile(new ComputeProfile().withRoles(Arrays.asList(
                        new Role().withName("headnode")
                            .withMinInstanceCount(1)
                            .withTargetInstanceCount(2)
                            .withHardwareProfile(new HardwareProfile().withVmSize("Standard_E8_V3"))
                            .withOsProfile(new OsProfile().withLinuxOperatingSystemProfile(
                                new LinuxOperatingSystemProfile().withUsername("sshuser")
                                    .withPassword("fakeTokenPlaceholder")))
                            .withScriptActions(Arrays.asList()),
                        new Role().withName("workernode")
                            .withTargetInstanceCount(4)
                            .withHardwareProfile(new HardwareProfile().withVmSize("Standard_E8_V3"))
                            .withOsProfile(new OsProfile().withLinuxOperatingSystemProfile(
                                new LinuxOperatingSystemProfile().withUsername("sshuser")
                                    .withPassword("fakeTokenPlaceholder")))
                            .withScriptActions(Arrays.asList()),
                        new Role().withName("zookeepernode")
                            .withMinInstanceCount(1)
                            .withTargetInstanceCount(3)
                            .withHardwareProfile(new HardwareProfile().withVmSize("Standard_D13_V2"))
                            .withOsProfile(new OsProfile().withLinuxOperatingSystemProfile(
                                new LinuxOperatingSystemProfile().withUsername("sshuser")
                                    .withPassword("fakeTokenPlaceholder")))
                            .withScriptActions(Arrays.asList()))))
                    .withStorageProfile(new StorageProfile().withStorageaccounts(Arrays.asList(new StorageAccount()
                        .withName("storagename.blob.core.windows.net")
                        .withIsDefault(true)
                        .withContainer("contianername")
                        .withKey("fakeTokenPlaceholder")
                        .withResourceId(
                            "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/rg1/providers/Microsoft.Storage/storageAccounts/storagename")
                        .withEnableSecureChannel(true))))
                    .withMinSupportedTlsVersion("1.2"))
                .withName("testclustername")
                .withType("Microsoft.HDInsight/clusters")
                .withTenantId("00000000-0000-0000-0000-000000000000")
                .withFetchAaddsResource(false), com.azure.core.util.Context.NONE);
    }

    // Use "Map.of" if available
    @SuppressWarnings("unchecked")
    private static <T> Map<String, T> mapOf(Object... inputs) {
        Map<String, T> map = new HashMap<>();
        for (int i = 0; i < inputs.length; i += 2) {
            String key = (String) inputs[i];
            T value = (T) inputs[i + 1];
            map.put(key, value);
        }
        return map;
    }
}
```

### Operations_List

```java
/**
 * Samples for Operations List.
 */
public final class OperationsListSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * ListHDInsightOperations.json
     */
    /**
     * Sample code: Lists all of the available operations.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void listsAllOfTheAvailableOperations(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_CreateOrUpdate

```java
import com.azure.resourcemanager.hdinsight.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.hdinsight.models.PrivateLinkServiceConnectionStatus;

/**
 * Samples for PrivateEndpointConnections CreateOrUpdate.
 */
public final class PrivateEndpointConnectionsCreateOrUpdateSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * ApprovePrivateEndpointConnection.json
     */
    /**
     * Sample code: Approve a private endpoint connection manually.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        approveAPrivateEndpointConnectionManually(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.privateEndpointConnections()
            .define("testprivateep.b3bf5fed-9b12-4560-b7d0-2abe1bba07e2")
            .withExistingCluster("rg1", "cluster1")
            .withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateLinkServiceConnectionStatus.APPROVED)
                    .withDescription("update it from pending to approved.")
                    .withActionsRequired("None"))
            .create();
    }
}
```

### PrivateEndpointConnections_Delete

```java
/**
 * Samples for PrivateEndpointConnections Delete.
 */
public final class PrivateEndpointConnectionsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * DeletePrivateEndpointConnection.json
     */
    /**
     * Sample code: Delete specific private endpoint connection for a specific HDInsight cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void deleteSpecificPrivateEndpointConnectionForASpecificHDInsightCluster(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.privateEndpointConnections()
            .delete("rg1", "cluster1", "testprivateep.b3bf5fed-9b12-4560-b7d0-2abe1bba07e2",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_Get

```java
/**
 * Samples for PrivateEndpointConnections Get.
 */
public final class PrivateEndpointConnectionsGetSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetPrivateEndpointConnection.json
     */
    /**
     * Sample code: Get specific private endpoint connection for a specific HDInsight cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getSpecificPrivateEndpointConnectionForASpecificHDInsightCluster(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.privateEndpointConnections()
            .getWithResponse("rg1", "cluster1", "testprivateep.b3bf5fed-9b12-4560-b7d0-2abe1bba07e2",
                com.azure.core.util.Context.NONE);
    }
}
```

### PrivateEndpointConnections_ListByCluster

```java
/**
 * Samples for PrivateEndpointConnections ListByCluster.
 */
public final class PrivateEndpointConnectionsListByClusterSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetAllPrivateEndpointConnectionsInCluster.json
     */
    /**
     * Sample code: Get all private endpoint connections for a specific HDInsight cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getAllPrivateEndpointConnectionsForASpecificHDInsightCluster(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.privateEndpointConnections().listByCluster("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_Get

```java
/**
 * Samples for PrivateLinkResources Get.
 */
public final class PrivateLinkResourcesGetSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetPrivateLinkResource.json
     */
    /**
     * Sample code: Get specific private link resource in a specific HDInsight cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getSpecificPrivateLinkResourceInASpecificHDInsightCluster(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.privateLinkResources().getWithResponse("rg1", "cluster1", "gateway", com.azure.core.util.Context.NONE);
    }
}
```

### PrivateLinkResources_ListByCluster

```java
/**
 * Samples for PrivateLinkResources ListByCluster.
 */
public final class PrivateLinkResourcesListByClusterSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetAllPrivateLinkResourcesInCluster.json
     */
    /**
     * Sample code: Get all private link resources in a specific HDInsight cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getAllPrivateLinkResourcesInASpecificHDInsightCluster(
        com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.privateLinkResources().listByClusterWithResponse("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### ScriptActions_Delete

```java
/**
 * Samples for ScriptActions Delete.
 */
public final class ScriptActionsDeleteSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * DeleteScriptAction.json
     */
    /**
     * Sample code: Delete a script action on HDInsight cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        deleteAScriptActionOnHDInsightCluster(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.scriptActions().deleteWithResponse("rg1", "cluster1", "scriptName", com.azure.core.util.Context.NONE);
    }
}
```

### ScriptActions_GetExecutionAsyncOperationStatus

```java
/**
 * Samples for ScriptActions GetExecutionAsyncOperationStatus.
 */
public final class ScriptActionsGetExecutionAsyncOperationStatusSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetScriptExecutionAsyncOperationStatus.json
     */
    /**
     * Sample code: Gets the async execution operation status.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        getsTheAsyncExecutionOperationStatus(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.scriptActions()
            .getExecutionAsyncOperationStatusWithResponse("rg1", "cluster1", "CF938302-6B4D-44A0-A6D2-C0D67E847AEC",
                com.azure.core.util.Context.NONE);
    }
}
```

### ScriptActions_GetExecutionDetail

```java
/**
 * Samples for ScriptActions GetExecutionDetail.
 */
public final class ScriptActionsGetExecutionDetailSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetScriptActionById.json
     */
    /**
     * Sample code: Get script execution history by script id.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        getScriptExecutionHistoryByScriptId(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.scriptActions()
            .getExecutionDetailWithResponse("rg1", "cluster1", "391145124054712", com.azure.core.util.Context.NONE);
    }
}
```

### ScriptActions_ListByCluster

```java
/**
 * Samples for ScriptActions ListByCluster.
 */
public final class ScriptActionsListByClusterSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetLinuxHadoopScriptAction.json
     */
    /**
     * Sample code: List all persisted script actions for the given cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        listAllPersistedScriptActionsForTheGivenCluster(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.scriptActions().listByCluster("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### ScriptExecutionHistory_ListByCluster

```java
/**
 * Samples for ScriptExecutionHistory ListByCluster.
 */
public final class ScriptExecutionHistoryListByClusterSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetScriptExecutionHistory.json
     */
    /**
     * Sample code: Get Script Execution History List.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getScriptExecutionHistoryList(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.scriptExecutionHistories().listByCluster("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### ScriptExecutionHistory_Promote

```java
/**
 * Samples for ScriptExecutionHistory Promote.
 */
public final class ScriptExecutionHistoryPromoteSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * PromoteLinuxHadoopScriptAction.json
     */
    /**
     * Sample code: Promote a script action on HDInsight cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        promoteAScriptActionOnHDInsightCluster(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.scriptExecutionHistories()
            .promoteWithResponse("rg1", "cluster1", "391145124054712", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_GetAsyncOperationStatus

```java
/**
 * Samples for VirtualMachines GetAsyncOperationStatus.
 */
public final class VirtualMachinesGetAsyncOperationStatusSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetRestartHostsAsyncOperationStatus.json
     */
    /**
     * Sample code: Gets the async operation status of restarting host.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        getsTheAsyncOperationStatusOfRestartingHost(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.virtualMachines()
            .getAsyncOperationStatusWithResponse("rg1", "cluster1", "CF938302-6B4D-44A0-A6D2-C0D67E847AEC",
                com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_ListHosts

```java
/**
 * Samples for VirtualMachines ListHosts.
 */
public final class VirtualMachinesListHostsSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * GetClusterVirtualMachines.json
     */
    /**
     * Sample code: Get All hosts in the cluster.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void getAllHostsInTheCluster(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.virtualMachines().listHostsWithResponse("rg1", "cluster1", com.azure.core.util.Context.NONE);
    }
}
```

### VirtualMachines_RestartHosts

```java
import java.util.Arrays;

/**
 * Samples for VirtualMachines RestartHosts.
 */
public final class VirtualMachinesRestartHostsSamples {
    /*
     * x-ms-original-file:
     * specification/hdinsight/resource-manager/Microsoft.HDInsight/preview/2024-08-01-preview/examples/
     * RestartVirtualMachinesOperation.json
     */
    /**
     * Sample code: Restarts the specified HDInsight cluster hosts.
     * 
     * @param manager Entry point to HDInsightManager.
     */
    public static void
        restartsTheSpecifiedHDInsightClusterHosts(com.azure.resourcemanager.hdinsight.HDInsightManager manager) {
        manager.virtualMachines()
            .restartHosts("rg1", "cluster1", Arrays.asList("gateway1", "gateway3"), com.azure.core.util.Context.NONE);
    }
}
```

