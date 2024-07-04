// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.recoveryservicesbackup;

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
import com.azure.resourcemanager.recoveryservices.RecoveryServicesManager;
import com.azure.resourcemanager.recoveryservices.models.CrossSubscriptionRestoreSettings;
import com.azure.resourcemanager.recoveryservices.models.CrossSubscriptionRestoreState;
import com.azure.resourcemanager.recoveryservices.models.PublicNetworkAccess;
import com.azure.resourcemanager.recoveryservices.models.RestoreSettings;
import com.azure.resourcemanager.recoveryservices.models.Sku;
import com.azure.resourcemanager.recoveryservices.models.SkuName;
import com.azure.resourcemanager.recoveryservices.models.Vault;
import com.azure.resourcemanager.recoveryservices.models.VaultProperties;
import com.azure.resourcemanager.recoveryservicesbackup.models.AzureVmWorkloadProtectionPolicy;
import com.azure.resourcemanager.recoveryservicesbackup.models.DayOfWeek;
import com.azure.resourcemanager.recoveryservicesbackup.models.LogSchedulePolicy;
import com.azure.resourcemanager.recoveryservicesbackup.models.LongTermRetentionPolicy;
import com.azure.resourcemanager.recoveryservicesbackup.models.MonthOfYear;
import com.azure.resourcemanager.recoveryservicesbackup.models.MonthlyRetentionSchedule;
import com.azure.resourcemanager.recoveryservicesbackup.models.PolicyType;
import com.azure.resourcemanager.recoveryservicesbackup.models.ProtectionPolicyResource;
import com.azure.resourcemanager.recoveryservicesbackup.models.RetentionDuration;
import com.azure.resourcemanager.recoveryservicesbackup.models.RetentionDurationType;
import com.azure.resourcemanager.recoveryservicesbackup.models.RetentionScheduleFormat;
import com.azure.resourcemanager.recoveryservicesbackup.models.ScheduleRunType;
import com.azure.resourcemanager.recoveryservicesbackup.models.Settings;
import com.azure.resourcemanager.recoveryservicesbackup.models.SimpleRetentionPolicy;
import com.azure.resourcemanager.recoveryservicesbackup.models.SimpleSchedulePolicy;
import com.azure.resourcemanager.recoveryservicesbackup.models.SubProtectionPolicy;
import com.azure.resourcemanager.recoveryservicesbackup.models.WeekOfMonth;
import com.azure.resourcemanager.recoveryservicesbackup.models.WeeklyRetentionFormat;
import com.azure.resourcemanager.recoveryservicesbackup.models.WeeklyRetentionSchedule;
import com.azure.resourcemanager.recoveryservicesbackup.models.WorkloadType;
import com.azure.resourcemanager.recoveryservicesbackup.models.YearlyRetentionSchedule;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RecoveryServicesBackupManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_WEST2;
    private String resourceGroupName = "rg" + randomPadding();
    private RecoveryServicesBackupManager recoveryServicesBackupManager;
    private RecoveryServicesManager recoveryServicesManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        recoveryServicesManager = RecoveryServicesManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);

        recoveryServicesBackupManager = RecoveryServicesBackupManager
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
    public void testCreateProtectionPolicy() {
        Vault vault = null;
        ProtectionPolicyResource protectionPolicyResource = null;
        String randomPadding = randomPadding();
        try {
            String vaultName = "vault" + randomPadding;
            String policyName = "policy" + randomPadding;

            // @embedmeStart
            OffsetDateTime scheduleDateTime = OffsetDateTime.parse(
                OffsetDateTime.now(Clock.systemUTC())
                    .withNano(0).withMinute(0).withSecond(0)
                    .plusDays(1).format(DateTimeFormatter.ISO_INSTANT));

            List<SubProtectionPolicy> lstSubProtectionPolicy = Arrays.asList(
                new SubProtectionPolicy()
                    .withPolicyType(PolicyType.FULL)
                    .withSchedulePolicy(
                        new SimpleSchedulePolicy()
                            .withScheduleRunFrequency(ScheduleRunType.WEEKLY)
                            .withScheduleRunDays(Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY))
                            .withScheduleRunTimes(Arrays.asList(scheduleDateTime)))
                    .withRetentionPolicy(
                        new LongTermRetentionPolicy()
                            .withWeeklySchedule(
                                new WeeklyRetentionSchedule()
                                    .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY))
                                    .withRetentionTimes(Arrays.asList(scheduleDateTime))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(2)
                                            .withDurationType(RetentionDurationType.WEEKS)))
                            .withMonthlySchedule(
                                new MonthlyRetentionSchedule()
                                    .withRetentionScheduleFormatType(RetentionScheduleFormat.WEEKLY)
                                    .withRetentionScheduleWeekly(
                                        new WeeklyRetentionFormat()
                                            .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                            .withWeeksOfTheMonth(Arrays.asList(WeekOfMonth.SECOND)))
                                    .withRetentionTimes(Arrays.asList(scheduleDateTime))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(1)
                                            .withDurationType(RetentionDurationType.MONTHS)))
                            .withYearlySchedule(
                                new YearlyRetentionSchedule()
                                    .withRetentionScheduleFormatType(RetentionScheduleFormat.WEEKLY)
                                    .withMonthsOfYear(Arrays.asList(MonthOfYear.JANUARY, MonthOfYear.JUNE, MonthOfYear.DECEMBER))
                                    .withRetentionScheduleWeekly(
                                        new WeeklyRetentionFormat()
                                            .withDaysOfTheWeek(Arrays.asList(DayOfWeek.SUNDAY))
                                            .withWeeksOfTheMonth(Arrays.asList(WeekOfMonth.LAST)))
                                    .withRetentionTimes(Arrays.asList(scheduleDateTime))
                                    .withRetentionDuration(
                                        new RetentionDuration()
                                            .withCount(1)
                                            .withDurationType(RetentionDurationType.YEARS)))),
                new SubProtectionPolicy()
                    .withPolicyType(PolicyType.DIFFERENTIAL)
                    .withSchedulePolicy(
                        new SimpleSchedulePolicy()
                            .withScheduleRunFrequency(ScheduleRunType.WEEKLY)
                            .withScheduleRunDays(Arrays.asList(DayOfWeek.FRIDAY))
                            .withScheduleRunTimes(Arrays.asList(scheduleDateTime)))
                    .withRetentionPolicy(
                        new SimpleRetentionPolicy()
                            .withRetentionDuration(
                                new RetentionDuration()
                                    .withCount(8)
                                    .withDurationType(RetentionDurationType.DAYS))),
                new SubProtectionPolicy()
                    .withPolicyType(PolicyType.LOG)
                    .withSchedulePolicy(new LogSchedulePolicy().withScheduleFrequencyInMins(60))
                    .withRetentionPolicy(
                        new SimpleRetentionPolicy()
                            .withRetentionDuration(
                                new RetentionDuration()
                                    .withCount(7)
                                    .withDurationType(RetentionDurationType.DAYS))));

            vault = recoveryServicesManager.vaults()
                .define(vaultName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withSku(new Sku().withName(SkuName.RS0).withTier("Standard"))
                .withProperties(new VaultProperties()
                    .withPublicNetworkAccess(PublicNetworkAccess.ENABLED)
                    .withRestoreSettings(new RestoreSettings()
                        .withCrossSubscriptionRestoreSettings(
                            new CrossSubscriptionRestoreSettings()
                                .withCrossSubscriptionRestoreState(CrossSubscriptionRestoreState.ENABLED))))
                .create();

            protectionPolicyResource = recoveryServicesBackupManager.protectionPolicies()
                .define(policyName)
                .withRegion(REGION)
                .withExistingVault(vaultName, resourceGroupName)
                .withProperties(
                    new AzureVmWorkloadProtectionPolicy()
                        .withWorkLoadType(WorkloadType.SQLDATA_BASE)
                        .withSettings(new Settings().withTimeZone("Pacific Standard Time").withIssqlcompression(false))
                        .withSubProtectionPolicy(lstSubProtectionPolicy)
                )
                .create();
            // @embedmeEnd
            protectionPolicyResource.refresh();
            Assertions.assertEquals(protectionPolicyResource.name(), policyName);
            Assertions.assertEquals(protectionPolicyResource.name(), recoveryServicesBackupManager.protectionPolicies().getById(protectionPolicyResource.id()).name());
        } finally {
            if (protectionPolicyResource != null) {
                recoveryServicesBackupManager.protectionPolicies().deleteById(protectionPolicyResource.id());
            }
            if (vault != null) {
                recoveryServicesManager.vaults().deleteById(vault.id());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
