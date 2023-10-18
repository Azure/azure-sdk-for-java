# Code snippets and samples


## AcssBackupConnections

- [Create](#acssbackupconnections_create)
- [Delete](#acssbackupconnections_delete)
- [Get](#acssbackupconnections_get)
- [List](#acssbackupconnections_list)
- [Update](#acssbackupconnections_update)

## Connectors

- [Create](#connectors_create)
- [Delete](#connectors_delete)
- [GetByResourceGroup](#connectors_getbyresourcegroup)
- [List](#connectors_list)
- [ListByResourceGroup](#connectors_listbyresourcegroup)
- [Update](#connectors_update)

## Operations

- [List](#operations_list)
### AcssBackupConnections_Create

```java
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.AzureIaaSvmProtectionPolicy;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.AzureVmWorkloadProtectionPolicy;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.DBBackupPolicyProperties;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.DailyRetentionSchedule;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.DayOfWeek;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.DiskExclusionProperties;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.ExistingRecoveryServicesVault;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.HanaBackupData;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.IaasvmPolicyType;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.InstantRPAdditionalDetails;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.LogSchedulePolicy;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.LongTermRetentionPolicy;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.MonthOfYear;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.MonthlyRetentionSchedule;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.NewRecoveryServicesVault;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.PolicyType;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.RetentionDuration;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.RetentionDurationType;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.RetentionScheduleFormat;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.ScheduleRunType;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.Settings;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.SimpleRetentionPolicy;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.SimpleSchedulePolicy;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.SnapshotBackupAdditionalDetails;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.SqlBackupData;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.SslConfiguration;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.SslCryptoProvider;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.SubProtectionPolicy;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.TieringMode;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.TieringPolicy;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.UserAssignedIdentityProperties;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.UserAssignedManagedIdentityDetails;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.VMBackupData;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.VMBackupPolicyProperties;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.WeekOfMonth;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.WeeklyRetentionFormat;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.WeeklyRetentionSchedule;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.WorkloadType;
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.YearlyRetentionSchedule;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/** Samples for AcssBackupConnections Create. */
public final class AcssBackupConnection {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/ACSSBackup/SQL_NewPolicy.json
     */
    /**
     * Sample code: Create a SQL backup connection with a new backup policy.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createASQLBackupConnectionWithANewBackupPolicy(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        manager
            .acssBackupConnections()
            .define("dbBackup")
            .withRegion("westcentralus")
            .withExistingConnector("test-rg", "C1")
            .withTags(mapOf())
            .withBackupData(
                new SqlBackupData()
                    .withRecoveryServicesVault(
                        new NewRecoveryServicesVault().withName("test-vault").withResourceGroup("test-rg"))
                    .withBackupPolicy(
                        new DBBackupPolicyProperties()
                            .withName("defaultSqlPolicy")
                            .withProperties(
                                new AzureVmWorkloadProtectionPolicy()
                                    .withProtectedItemsCount(0)
                                    .withWorkLoadType(WorkloadType.SQLDATA_BASE)
                                    .withSettings(
                                        new Settings()
                                            .withTimeZone("UTC")
                                            .withIssqlcompression(true)
                                            .withIsCompression(true))
                                    .withSubProtectionPolicy(
                                        Arrays
                                            .asList(
                                                new SubProtectionPolicy()
                                                    .withPolicyType(PolicyType.FULL)
                                                    .withSchedulePolicy(
                                                        new SimpleSchedulePolicy()
                                                            .withScheduleRunFrequency(ScheduleRunType.WEEKLY)
                                                            .withScheduleRunDays(Arrays.asList(DayOfWeek.SUNDAY))
                                                            .withScheduleRunTimes(
                                                                Arrays
                                                                    .asList(
                                                                        OffsetDateTime
                                                                            .parse("2022-11-29T19:30:00.000Z"))))
                                                    .withRetentionPolicy(
                                                        new LongTermRetentionPolicy()
                                                            .withWeeklySchedule(
                                                                new WeeklyRetentionSchedule()
                                                                    .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                                                    .withRetentionTimes(
                                                                        Arrays
                                                                            .asList(
                                                                                OffsetDateTime
                                                                                    .parse("2022-11-29T19:30:00.000Z")))
                                                                    .withRetentionDuration(
                                                                        new RetentionDuration()
                                                                            .withCount(104)
                                                                            .withDurationType(
                                                                                RetentionDurationType.WEEKS)))
                                                            .withMonthlySchedule(
                                                                new MonthlyRetentionSchedule()
                                                                    .withRetentionScheduleFormatType(
                                                                        RetentionScheduleFormat.WEEKLY)
                                                                    .withRetentionScheduleWeekly(
                                                                        new WeeklyRetentionFormat()
                                                                            .withDaysOfTheWeek(
                                                                                Arrays.asList(DayOfWeek.SUNDAY))
                                                                            .withWeeksOfTheMonth(
                                                                                Arrays.asList(WeekOfMonth.FIRST)))
                                                                    .withRetentionTimes(
                                                                        Arrays
                                                                            .asList(
                                                                                OffsetDateTime
                                                                                    .parse("2022-11-29T19:30:00.000Z")))
                                                                    .withRetentionDuration(
                                                                        new RetentionDuration()
                                                                            .withCount(60)
                                                                            .withDurationType(
                                                                                RetentionDurationType.MONTHS)))
                                                            .withYearlySchedule(
                                                                new YearlyRetentionSchedule()
                                                                    .withRetentionScheduleFormatType(
                                                                        RetentionScheduleFormat.WEEKLY)
                                                                    .withMonthsOfYear(
                                                                        Arrays.asList(MonthOfYear.JANUARY))
                                                                    .withRetentionScheduleWeekly(
                                                                        new WeeklyRetentionFormat()
                                                                            .withDaysOfTheWeek(
                                                                                Arrays.asList(DayOfWeek.SUNDAY))
                                                                            .withWeeksOfTheMonth(
                                                                                Arrays.asList(WeekOfMonth.FIRST)))
                                                                    .withRetentionTimes(
                                                                        Arrays
                                                                            .asList(
                                                                                OffsetDateTime
                                                                                    .parse("2022-11-29T19:30:00.000Z")))
                                                                    .withRetentionDuration(
                                                                        new RetentionDuration()
                                                                            .withCount(10)
                                                                            .withDurationType(
                                                                                RetentionDurationType.YEARS))))
                                                    .withTieringPolicy(
                                                        mapOf(
                                                            "ArchivedRP",
                                                            new TieringPolicy()
                                                                .withTieringMode(TieringMode.TIER_AFTER)
                                                                .withDuration(45)
                                                                .withDurationType(RetentionDurationType.DAYS))),
                                                new SubProtectionPolicy()
                                                    .withPolicyType(PolicyType.DIFFERENTIAL)
                                                    .withSchedulePolicy(
                                                        new SimpleSchedulePolicy()
                                                            .withScheduleRunFrequency(ScheduleRunType.WEEKLY)
                                                            .withScheduleRunDays(Arrays.asList(DayOfWeek.MONDAY))
                                                            .withScheduleRunTimes(
                                                                Arrays
                                                                    .asList(
                                                                        OffsetDateTime.parse("2022-09-29T02:00:00Z")))
                                                            .withScheduleWeeklyFrequency(0))
                                                    .withRetentionPolicy(
                                                        new SimpleRetentionPolicy()
                                                            .withRetentionDuration(
                                                                new RetentionDuration()
                                                                    .withCount(30)
                                                                    .withDurationType(RetentionDurationType.DAYS))),
                                                new SubProtectionPolicy()
                                                    .withPolicyType(PolicyType.LOG)
                                                    .withSchedulePolicy(
                                                        new LogSchedulePolicy().withScheduleFrequencyInMins(120))
                                                    .withRetentionPolicy(
                                                        new SimpleRetentionPolicy()
                                                            .withRetentionDuration(
                                                                new RetentionDuration()
                                                                    .withCount(20)
                                                                    .withDurationType(RetentionDurationType.DAYS))))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/ACSSBackup/VM_NewPolicy.json
     */
    /**
     * Sample code: Create a VM backup connection with a new backup policy with tieringPolicy.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAVMBackupConnectionWithANewBackupPolicyWithTieringPolicy(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        manager
            .acssBackupConnections()
            .define("vmBackup")
            .withRegion("westcentralus")
            .withExistingConnector("test-rg", "C1")
            .withTags(mapOf())
            .withBackupData(
                new VMBackupData()
                    .withRecoveryServicesVault(
                        new NewRecoveryServicesVault().withName("test-vault").withResourceGroup("test-rg"))
                    .withBackupPolicy(
                        new VMBackupPolicyProperties()
                            .withName("defaultVmPolicy")
                            .withProperties(
                                new AzureIaaSvmProtectionPolicy()
                                    .withProtectedItemsCount(0)
                                    .withInstantRPDetails(
                                        new InstantRPAdditionalDetails()
                                            .withAzureBackupRGNamePrefix("dasas")
                                            .withAzureBackupRGNameSuffix("a"))
                                    .withSchedulePolicy(
                                        new SimpleSchedulePolicy()
                                            .withScheduleRunFrequency(ScheduleRunType.DAILY)
                                            .withScheduleRunTimes(
                                                Arrays.asList(OffsetDateTime.parse("2022-11-29T19:30:00.000Z"))))
                                    .withRetentionPolicy(
                                        new LongTermRetentionPolicy()
                                            .withDailySchedule(
                                                new DailyRetentionSchedule()
                                                    .withRetentionTimes(
                                                        Arrays.asList(OffsetDateTime.parse("2022-11-29T19:30:00.000Z")))
                                                    .withRetentionDuration(
                                                        new RetentionDuration()
                                                            .withCount(30)
                                                            .withDurationType(RetentionDurationType.DAYS)))
                                            .withWeeklySchedule(
                                                new WeeklyRetentionSchedule()
                                                    .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                                    .withRetentionTimes(
                                                        Arrays.asList(OffsetDateTime.parse("2022-11-29T19:30:00.000Z")))
                                                    .withRetentionDuration(
                                                        new RetentionDuration()
                                                            .withCount(12)
                                                            .withDurationType(RetentionDurationType.WEEKS)))
                                            .withMonthlySchedule(
                                                new MonthlyRetentionSchedule()
                                                    .withRetentionScheduleFormatType(RetentionScheduleFormat.WEEKLY)
                                                    .withRetentionScheduleWeekly(
                                                        new WeeklyRetentionFormat()
                                                            .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                                            .withWeeksOfTheMonth(Arrays.asList(WeekOfMonth.FIRST)))
                                                    .withRetentionTimes(
                                                        Arrays.asList(OffsetDateTime.parse("2022-11-29T19:30:00.000Z")))
                                                    .withRetentionDuration(
                                                        new RetentionDuration()
                                                            .withCount(60)
                                                            .withDurationType(RetentionDurationType.MONTHS)))
                                            .withYearlySchedule(
                                                new YearlyRetentionSchedule()
                                                    .withRetentionScheduleFormatType(RetentionScheduleFormat.WEEKLY)
                                                    .withMonthsOfYear(Arrays.asList(MonthOfYear.JANUARY))
                                                    .withRetentionScheduleWeekly(
                                                        new WeeklyRetentionFormat()
                                                            .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                                            .withWeeksOfTheMonth(Arrays.asList(WeekOfMonth.FIRST)))
                                                    .withRetentionTimes(
                                                        Arrays.asList(OffsetDateTime.parse("2022-11-29T19:30:00.000Z")))
                                                    .withRetentionDuration(
                                                        new RetentionDuration()
                                                            .withCount(10)
                                                            .withDurationType(RetentionDurationType.YEARS))))
                                    .withTieringPolicy(
                                        mapOf(
                                            "ArchivedRP",
                                            new TieringPolicy()
                                                .withTieringMode(TieringMode.TIER_AFTER)
                                                .withDuration(3)
                                                .withDurationType(RetentionDurationType.MONTHS)))
                                    .withInstantRpRetentionRangeInDays(2)
                                    .withTimeZone("UTC")
                                    .withPolicyType(IaasvmPolicyType.V1)))
                    .withDiskExclusionProperties(
                        new DiskExclusionProperties().withDiskLunList(Arrays.asList()).withIsInclusionList(true)))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/ACSSBackup/DB_New_Create.json
     */
    /**
     * Sample code: Create a db backup connection with a new backup policy.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createADbBackupConnectionWithANewBackupPolicy(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        manager
            .acssBackupConnections()
            .define("dbBackup")
            .withRegion("westcentralus")
            .withExistingConnector("test-rg", "C1")
            .withTags(mapOf())
            .withBackupData(
                new SqlBackupData()
                    .withRecoveryServicesVault(
                        new ExistingRecoveryServicesVault()
                            .withId(
                                "/subscriptions/6d875e77-e412-4d7d-9af4-8895278b4443/resourceGroups/test-rg/providers/Microsoft.RecoveryServices/vaults/test-vault"))
                    .withBackupPolicy(
                        new DBBackupPolicyProperties()
                            .withName("defaultDbPolicy")
                            .withProperties(
                                new AzureVmWorkloadProtectionPolicy()
                                    .withProtectedItemsCount(0)
                                    .withWorkLoadType(WorkloadType.SQLDATA_BASE)
                                    .withSettings(
                                        new Settings()
                                            .withTimeZone("UTC")
                                            .withIssqlcompression(false)
                                            .withIsCompression(false))
                                    .withSubProtectionPolicy(
                                        Arrays
                                            .asList(
                                                new SubProtectionPolicy()
                                                    .withPolicyType(PolicyType.FULL)
                                                    .withSchedulePolicy(
                                                        new SimpleSchedulePolicy()
                                                            .withScheduleRunFrequency(ScheduleRunType.DAILY)
                                                            .withScheduleRunTimes(
                                                                Arrays
                                                                    .asList(
                                                                        OffsetDateTime.parse("2018-01-10T18:30:00Z")))
                                                            .withScheduleWeeklyFrequency(0))
                                                    .withRetentionPolicy(
                                                        new LongTermRetentionPolicy()
                                                            .withDailySchedule(
                                                                new DailyRetentionSchedule()
                                                                    .withRetentionTimes(
                                                                        Arrays
                                                                            .asList(
                                                                                OffsetDateTime
                                                                                    .parse("2018-01-10T18:30:00Z")))
                                                                    .withRetentionDuration(
                                                                        new RetentionDuration()
                                                                            .withCount(30)
                                                                            .withDurationType(
                                                                                RetentionDurationType.DAYS)))),
                                                new SubProtectionPolicy()
                                                    .withPolicyType(PolicyType.LOG)
                                                    .withSchedulePolicy(
                                                        new LogSchedulePolicy().withScheduleFrequencyInMins(60))
                                                    .withRetentionPolicy(
                                                        new SimpleRetentionPolicy()
                                                            .withRetentionDuration(
                                                                new RetentionDuration()
                                                                    .withCount(30)
                                                                    .withDurationType(RetentionDurationType.DAYS))))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/ACSSBackup/HANA_NewPolicy.json
     */
    /**
     * Sample code: Create a HANA backup connection with a new backup policy.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAHANABackupConnectionWithANewBackupPolicy(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        manager
            .acssBackupConnections()
            .define("dbBackup")
            .withRegion("westcentralus")
            .withExistingConnector("test-rg", "C1")
            .withTags(mapOf())
            .withBackupData(
                new HanaBackupData()
                    .withRecoveryServicesVault(
                        new NewRecoveryServicesVault().withName("test-vault").withResourceGroup("test-rg"))
                    .withSslConfiguration(
                        new SslConfiguration()
                            .withSslKeyStore("fakeTokenPlaceholder")
                            .withSslTrustStore("sapsrv.pse")
                            .withSslHostnameInCertificate("hostname")
                            .withSslCryptoProvider(SslCryptoProvider.COMMONCRYPTO))
                    .withBackupPolicy(
                        new DBBackupPolicyProperties()
                            .withName("defaultHanaPolicy")
                            .withProperties(
                                new AzureVmWorkloadProtectionPolicy()
                                    .withProtectedItemsCount(0)
                                    .withWorkLoadType(WorkloadType.SAPHANA_DATABASE)
                                    .withSettings(
                                        new Settings()
                                            .withTimeZone("UTC")
                                            .withIssqlcompression(false)
                                            .withIsCompression(false))
                                    .withSubProtectionPolicy(
                                        Arrays
                                            .asList(
                                                new SubProtectionPolicy()
                                                    .withPolicyType(PolicyType.FULL)
                                                    .withSchedulePolicy(
                                                        new SimpleSchedulePolicy()
                                                            .withScheduleRunFrequency(ScheduleRunType.WEEKLY)
                                                            .withScheduleRunDays(Arrays.asList(DayOfWeek.SUNDAY))
                                                            .withScheduleRunTimes(
                                                                Arrays
                                                                    .asList(
                                                                        OffsetDateTime
                                                                            .parse("2022-11-29T19:30:00.000Z"))))
                                                    .withRetentionPolicy(
                                                        new LongTermRetentionPolicy()
                                                            .withWeeklySchedule(
                                                                new WeeklyRetentionSchedule()
                                                                    .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                                                    .withRetentionTimes(
                                                                        Arrays
                                                                            .asList(
                                                                                OffsetDateTime
                                                                                    .parse("2022-11-29T19:30:00.000Z")))
                                                                    .withRetentionDuration(
                                                                        new RetentionDuration()
                                                                            .withCount(104)
                                                                            .withDurationType(
                                                                                RetentionDurationType.WEEKS)))
                                                            .withMonthlySchedule(
                                                                new MonthlyRetentionSchedule()
                                                                    .withRetentionScheduleFormatType(
                                                                        RetentionScheduleFormat.WEEKLY)
                                                                    .withRetentionScheduleWeekly(
                                                                        new WeeklyRetentionFormat()
                                                                            .withDaysOfTheWeek(
                                                                                Arrays.asList(DayOfWeek.SUNDAY))
                                                                            .withWeeksOfTheMonth(
                                                                                Arrays.asList(WeekOfMonth.FIRST)))
                                                                    .withRetentionTimes(
                                                                        Arrays
                                                                            .asList(
                                                                                OffsetDateTime
                                                                                    .parse("2022-11-29T19:30:00.000Z")))
                                                                    .withRetentionDuration(
                                                                        new RetentionDuration()
                                                                            .withCount(60)
                                                                            .withDurationType(
                                                                                RetentionDurationType.MONTHS)))
                                                            .withYearlySchedule(
                                                                new YearlyRetentionSchedule()
                                                                    .withRetentionScheduleFormatType(
                                                                        RetentionScheduleFormat.WEEKLY)
                                                                    .withMonthsOfYear(
                                                                        Arrays.asList(MonthOfYear.JANUARY))
                                                                    .withRetentionScheduleWeekly(
                                                                        new WeeklyRetentionFormat()
                                                                            .withDaysOfTheWeek(
                                                                                Arrays.asList(DayOfWeek.SUNDAY))
                                                                            .withWeeksOfTheMonth(
                                                                                Arrays.asList(WeekOfMonth.FIRST)))
                                                                    .withRetentionTimes(
                                                                        Arrays
                                                                            .asList(
                                                                                OffsetDateTime
                                                                                    .parse("2022-11-29T19:30:00.000Z")))
                                                                    .withRetentionDuration(
                                                                        new RetentionDuration()
                                                                            .withCount(10)
                                                                            .withDurationType(
                                                                                RetentionDurationType.YEARS))))
                                                    .withTieringPolicy(
                                                        mapOf(
                                                            "ArchivedRP",
                                                            new TieringPolicy()
                                                                .withTieringMode(TieringMode.DO_NOT_TIER))),
                                                new SubProtectionPolicy()
                                                    .withPolicyType(PolicyType.DIFFERENTIAL)
                                                    .withSchedulePolicy(
                                                        new SimpleSchedulePolicy()
                                                            .withScheduleRunFrequency(ScheduleRunType.WEEKLY)
                                                            .withScheduleRunDays(Arrays.asList(DayOfWeek.MONDAY))
                                                            .withScheduleRunTimes(
                                                                Arrays
                                                                    .asList(
                                                                        OffsetDateTime.parse("2022-09-29T02:00:00Z")))
                                                            .withScheduleWeeklyFrequency(0))
                                                    .withRetentionPolicy(
                                                        new SimpleRetentionPolicy()
                                                            .withRetentionDuration(
                                                                new RetentionDuration()
                                                                    .withCount(30)
                                                                    .withDurationType(RetentionDurationType.DAYS))),
                                                new SubProtectionPolicy()
                                                    .withPolicyType(PolicyType.LOG)
                                                    .withSchedulePolicy(
                                                        new LogSchedulePolicy().withScheduleFrequencyInMins(120))
                                                    .withRetentionPolicy(
                                                        new SimpleRetentionPolicy()
                                                            .withRetentionDuration(
                                                                new RetentionDuration()
                                                                    .withCount(20)
                                                                    .withDurationType(RetentionDurationType.DAYS)))))))
                    .withHdbuserstoreKeyName("fakeTokenPlaceholder")
                    .withInstanceNumber("00")
                    .withDbInstanceSnapshotBackupPolicy(
                        new DBBackupPolicyProperties()
                            .withName("defaultDbInstanceSnapshotPolicy")
                            .withProperties(
                                new AzureVmWorkloadProtectionPolicy()
                                    .withWorkLoadType(WorkloadType.SAPHANA_DBINSTANCE)
                                    .withSettings(
                                        new Settings()
                                            .withTimeZone("UTC")
                                            .withIssqlcompression(false)
                                            .withIsCompression(false))
                                    .withSubProtectionPolicy(
                                        Arrays
                                            .asList(
                                                new SubProtectionPolicy()
                                                    .withPolicyType(PolicyType.SNAPSHOT_FULL)
                                                    .withSchedulePolicy(
                                                        new SimpleSchedulePolicy()
                                                            .withScheduleRunFrequency(ScheduleRunType.DAILY)
                                                            .withScheduleRunTimes(
                                                                Arrays
                                                                    .asList(
                                                                        OffsetDateTime
                                                                            .parse("2023-09-18T06:30:00.000Z"))))
                                                    .withSnapshotBackupAdditionalDetails(
                                                        new SnapshotBackupAdditionalDetails()
                                                            .withInstantRpRetentionRangeInDays(1)
                                                            .withInstantRPDetails("test-rg")
                                                            .withUserAssignedManagedIdentityDetails(
                                                                new UserAssignedManagedIdentityDetails()
                                                                    .withIdentityArmId(
                                                                        "/subscriptions/6d875e77-e412-4d7d-9af4-8895278b4443/resourcegroups/test-rg/providers/Microsoft.ManagedIdentity/userAssignedIdentities/testMsi")
                                                                    .withIdentityName("testMsi")
                                                                    .withUserAssignedIdentityProperties(
                                                                        new UserAssignedIdentityProperties()
                                                                            .withClientId(
                                                                                "c3a877cf-51f8-4031-8f17-ab562d1e7737")
                                                                            .withPrincipalId(
                                                                                "2f5834bd-4b86-4d85-a8df-6dd829a6418c")))))))))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/ACSSBackup/VM_Existing_Create.json
     */
    /**
     * Sample code: Create a vm backup connection with an existing backup policy.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAVmBackupConnectionWithAnExistingBackupPolicy(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        manager
            .acssBackupConnections()
            .define("vmBackup")
            .withRegion("westcentralus")
            .withExistingConnector("test-rg", "C1")
            .withTags(mapOf())
            .withBackupData(
                new VMBackupData()
                    .withRecoveryServicesVault(
                        new ExistingRecoveryServicesVault()
                            .withId(
                                "/subscriptions/6d875e77-e412-4d7d-9af4-8895278b4443/resourceGroups/test-rg/providers/Microsoft.RecoveryServices/vaults/test-vault"))
                    .withBackupPolicy(new VMBackupPolicyProperties().withName("defaultVmPolicy"))
                    .withDiskExclusionProperties(
                        new DiskExclusionProperties().withDiskLunList(Arrays.asList()).withIsInclusionList(true)))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/ACSSBackup/DB_Existing_Create.json
     */
    /**
     * Sample code: Create a db backup connection with an existing backup policy.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createADbBackupConnectionWithAnExistingBackupPolicy(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        manager
            .acssBackupConnections()
            .define("dbBackup")
            .withRegion("westcentralus")
            .withExistingConnector("test-rg", "C1")
            .withTags(mapOf())
            .withBackupData(
                new SqlBackupData()
                    .withRecoveryServicesVault(
                        new ExistingRecoveryServicesVault()
                            .withId(
                                "/subscriptions/6d875e77-e412-4d7d-9af4-8895278b4443/resourceGroups/test-rg/providers/Microsoft.RecoveryServices/vaults/test-vault"))
                    .withBackupPolicy(new DBBackupPolicyProperties().withName("defaultDbPolicy")))
            .create();
    }

    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/ACSSBackup/VM_New_Create.json
     */
    /**
     * Sample code: Create a vm backup connection with a new backup policy.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAVmBackupConnectionWithANewBackupPolicy(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        manager
            .acssBackupConnections()
            .define("vmBackup")
            .withRegion("westcentralus")
            .withExistingConnector("test-rg", "C1")
            .withTags(mapOf())
            .withBackupData(
                new VMBackupData()
                    .withRecoveryServicesVault(
                        new NewRecoveryServicesVault().withName("test-vault").withResourceGroup("test-rg"))
                    .withBackupPolicy(
                        new VMBackupPolicyProperties()
                            .withName("defaultVmPolicy")
                            .withProperties(
                                new AzureIaaSvmProtectionPolicy()
                                    .withProtectedItemsCount(0)
                                    .withInstantRPDetails(new InstantRPAdditionalDetails())
                                    .withSchedulePolicy(
                                        new SimpleSchedulePolicy()
                                            .withScheduleRunFrequency(ScheduleRunType.DAILY)
                                            .withScheduleRunTimes(
                                                Arrays.asList(OffsetDateTime.parse("2018-01-10T18:30:00Z")))
                                            .withScheduleWeeklyFrequency(0))
                                    .withRetentionPolicy(
                                        new LongTermRetentionPolicy()
                                            .withDailySchedule(
                                                new DailyRetentionSchedule()
                                                    .withRetentionTimes(
                                                        Arrays.asList(OffsetDateTime.parse("2018-01-10T18:30:00Z")))
                                                    .withRetentionDuration(
                                                        new RetentionDuration()
                                                            .withCount(30)
                                                            .withDurationType(RetentionDurationType.DAYS))))
                                    .withInstantRpRetentionRangeInDays(2)))
                    .withDiskExclusionProperties(
                        new DiskExclusionProperties().withDiskLunList(Arrays.asList()).withIsInclusionList(true)))
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

### AcssBackupConnections_Delete

```java
/** Samples for AcssBackupConnections Delete. */
public final class AcssBackupConnection {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/ACSSBackup/Delete.json
     */
    /**
     * Sample code: Delete a backup connection resource of virtual instance for SAP.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void deleteABackupConnectionResourceOfVirtualInstanceForSAP(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        manager.acssBackupConnections().delete("test-rg", "C1", "vmBackup", com.azure.core.util.Context.NONE);
    }
}
```

### AcssBackupConnections_Get

```java
/** Samples for AcssBackupConnections Get. */
public final class AcssBackupConnection {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/ACSSBackup/Get.json
     */
    /**
     * Sample code: Get the backup connection resource of virtual instance for SAP.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getTheBackupConnectionResourceOfVirtualInstanceForSAP(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        manager.acssBackupConnections().getWithResponse("test-rg", "C1", "vmBackup", com.azure.core.util.Context.NONE);
    }
}
```

### AcssBackupConnections_List

```java
/** Samples for AcssBackupConnections List. */
public final class AcssBackupConnection {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/ACSSBackup/List.json
     */
    /**
     * Sample code: List the backup connection resources of virtual instance for SAP under the given connector resource.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void listTheBackupConnectionResourcesOfVirtualInstanceForSAPUnderTheGivenConnectorResource(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        manager.acssBackupConnections().list("test-rg", "C1", com.azure.core.util.Context.NONE);
    }
}
```

### AcssBackupConnections_Update

```java
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.AcssBackupConnection;
import java.util.HashMap;
import java.util.Map;

/** Samples for AcssBackupConnections Update. */
public final class AcssBackupConnection {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/ACSSBackup/Update.json
     */
    /**
     * Sample code: Update an backup connection resource of virtual instance for SAP.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void updateAnBackupConnectionResourceOfVirtualInstanceForSAP(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        AcssBackupConnection resource =
            manager
                .acssBackupConnections()
                .getWithResponse("test-rg", "C1", "vmBackup", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1")).apply();
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

### Connectors_Create

```java
import java.util.HashMap;
import java.util.Map;

/** Samples for Connectors Create. */
public final class ConnectorsCreateSamp {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/connectors/Create.json
     */
    /**
     * Sample code: Create a connector resource.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void createAConnectorResource(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        manager
            .connectors()
            .define("C1")
            .withRegion("westcentralus")
            .withExistingResourceGroup("test-rg")
            .withSourceResourceId(
                "/subscriptions/6d875e77-e412-4d7d-9af4-8895278b4443/resourceGroups/test-rg/providers/Microsoft.Workloads/sapVirtualInstances/X00")
            .withTags(mapOf())
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

### Connectors_Delete

```java
/** Samples for Connectors Delete. */
public final class ConnectorsDeleteSamp {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/connectors/Delete.json
     */
    /**
     * Sample code: Delete the connector resource.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void deleteTheConnectorResource(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        manager.connectors().delete("test-rg", "C1", com.azure.core.util.Context.NONE);
    }
}
```

### Connectors_GetByResourceGroup

```java
/** Samples for Connectors GetByResourceGroup. */
public final class ConnectorsGetByResou {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/connectors/Get.json
     */
    /**
     * Sample code: Get the connector resource.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void getTheConnectorResource(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        manager.connectors().getByResourceGroupWithResponse("test-rg", "C1", com.azure.core.util.Context.NONE);
    }
}
```

### Connectors_List

```java
/** Samples for Connectors List. */
public final class ConnectorsListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/connectors/ListBySubscription.json
     */
    /**
     * Sample code: List all connector resources in a subscription.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void listAllConnectorResourcesInASubscription(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        manager.connectors().list(com.azure.core.util.Context.NONE);
    }
}
```

### Connectors_ListByResourceGroup

```java
/** Samples for Connectors ListByResourceGroup. */
public final class ConnectorsListByReso {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/connectors/ListByResourceGroup.json
     */
    /**
     * Sample code: List all connector resources in a resource group.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void listAllConnectorResourcesInAResourceGroup(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        manager.connectors().listByResourceGroup("test-rg", com.azure.core.util.Context.NONE);
    }
}
```

### Connectors_Update

```java
import com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.models.Connector;
import java.util.HashMap;
import java.util.Map;

/** Samples for Connectors Update. */
public final class ConnectorsUpdateSamp {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/connectors/preview/2023-10-01-preview/examples/connectors/Update.json
     */
    /**
     * Sample code: Update the connector resource.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void updateTheConnectorResource(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        Connector resource =
            manager
                .connectors()
                .getByResourceGroupWithResponse("test-rg", "C1", com.azure.core.util.Context.NONE)
                .getValue();
        resource.update().withTags(mapOf("tag1", "value1")).apply();
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
/** Samples for Operations List. */
public final class OperationsListSamples {
    /*
     * x-ms-original-file: specification/workloads/resource-manager/Microsoft.Workloads/operations/preview/2023-10-01-preview/examples/Operations_List.json
     */
    /**
     * Sample code: Operations.
     *
     * @param manager Entry point to WorkloadsManager.
     */
    public static void operations(
        com.azure.resourcemanager.workloadsmicrosoftworkloadsconnectors.WorkloadsManager manager) {
        manager.operations().list(com.azure.core.util.Context.NONE);
    }
}
```

