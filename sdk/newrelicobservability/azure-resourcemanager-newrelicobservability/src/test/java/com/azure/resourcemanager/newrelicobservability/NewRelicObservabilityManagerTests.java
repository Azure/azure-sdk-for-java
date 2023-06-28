// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.newrelicobservability;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.DoNotRecord;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.resourcemanager.newrelicobservability.models.AccountCreationSource;
import com.azure.resourcemanager.newrelicobservability.models.BillingCycle;
import com.azure.resourcemanager.newrelicobservability.models.NewRelicMonitorResource;
import com.azure.resourcemanager.newrelicobservability.models.OrgCreationSource;
import com.azure.resourcemanager.newrelicobservability.models.PlanData;
import com.azure.resourcemanager.newrelicobservability.models.UsageType;
import com.azure.resourcemanager.newrelicobservability.models.UserInfo;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Random;

public class NewRelicObservabilityManagerTests extends TestBase {
    private static final Random RANDOM = new Random();
    private static final Region REGION = Region.US_EAST;
    private String resourceGroupName = "rg" + randomPadding();
    private NewRelicObservabilityManager newRelicObservabilityManager;
    private ResourceManager resourceManager;
    private boolean testEnv;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        newRelicObservabilityManager = NewRelicObservabilityManager
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
    @DoNotRecord(skipInPlayback = true)
    public void testCreateNewRelicMonitor() {
        NewRelicMonitorResource resource = null;
        String randomPadding = randomPadding();
        String userEmail = "email" + randomPadding + "@microsoft.com";
        try {
            String resourceName = "resource" + randomPadding;
            String firstName = "first" + randomPadding;
            String lastName = "last" + randomPadding;
            // @embedmeStart
            resource = newRelicObservabilityManager.monitors()
                .define(resourceName)
                .withRegion(REGION)
                .withExistingResourceGroup(resourceGroupName)
                .withPlanData(new PlanData()
                    .withUsageType(UsageType.PAYG)
                    .withBillingCycle(BillingCycle.MONTHLY)
                    .withEffectiveDate(OffsetDateTime.now())
                    .withPlanDetails("newrelic-pay-as-you-go-free-live@TIDgmz7xq9ge3py@PUBIDnewrelicinc1635200720692.newrelic_liftr_payg"))
                .withUserInfo(new UserInfo()
                    .withFirstName(firstName)
                    .withLastName(lastName)
                    .withEmailAddress(userEmail))
                .withOrgCreationSource(OrgCreationSource.NEWRELIC)
                .withAccountCreationSource(AccountCreationSource.NEWRELIC)
                .create();
            // @embedmeEnd
            resource.refresh();
            Assertions.assertEquals(resource.name(), resourceName);
            Assertions.assertEquals(resource.name(), newRelicObservabilityManager.monitors().getById(resource.id()).name());
            Assertions.assertTrue(newRelicObservabilityManager.monitors().list().stream().count() > 0);
        } finally {
            if (resource != null) {
                newRelicObservabilityManager.monitors().delete(resourceGroupName, userEmail, resource.name());
            }
        }
    }

    private static String randomPadding() {
        return String.format("%05d", Math.abs(RANDOM.nextInt() % 100000));
    }
}
