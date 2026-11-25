// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cognitiveservices;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.exception.ManagementException;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.util.Configuration;
import com.azure.resourcemanager.cognitiveservices.models.AccountCostControl;
import com.azure.resourcemanager.cognitiveservices.models.CostControlCategory;
import com.azure.resourcemanager.cognitiveservices.models.CostControlPatchProperties;
import com.azure.resourcemanager.cognitiveservices.models.CostControlProperties;
import com.azure.resourcemanager.cognitiveservices.models.CostControlTimeGrain;
import com.azure.resourcemanager.cognitiveservices.models.CostControlTimePeriod;
import com.azure.resourcemanager.test.utils.TestUtilities;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public final class CostControlLifecycleTests extends TestProxyTestBase {
    private static final String SUBSCRIPTION_PLACEHOLDER = "00000000-0000-0000-0000-000000000000";
    private static final String RESOURCE_GROUP_PLACEHOLDER = "sanitized-resource-group";
    private static final String ACCOUNT_NAME_PLACEHOLDER = "sanitized-cognitive-account";
    private static final String SANITIZED_ETAG = "sanitized-etag";
    private static final String RECORDED_API_VERSION = "2026-11-15-preview";
    private static final String COST_CONTROL_NAME = "sdk-cost-control-lifecycle";

    private CognitiveServicesManager manager;
    private String resourceGroupName;
    private String accountName;

    @Override
    protected void beforeTest() {
        if (getTestMode() == TestMode.LIVE) {
            String configuredAccount = Configuration.getGlobalConfiguration().get("AZURE_COGNITIVE_SERVICES_ACCOUNT");
            Assumptions.assumeTrue(configuredAccount != null && !configuredAccount.trim().isEmpty(),
                "Set AZURE_COGNITIVE_SERVICES_ACCOUNT to an existing cost-control-enabled account to run live tests.");
        }

        String subscriptionId = valueForTest("AZURE_SUBSCRIPTION_ID", SUBSCRIPTION_PLACEHOLDER);
        resourceGroupName = valueForTest("AZURE_RESOURCE_GROUP", RESOURCE_GROUP_PLACEHOLDER);
        accountName = valueForTest("AZURE_COGNITIVE_SERVICES_ACCOUNT", ACCOUNT_NAME_PLACEHOLDER);

        if (getTestMode() != TestMode.LIVE) {
            interceptorManager.removeSanitizers("AZSDK3493", "AZSDK3430");
            if (getTestMode() == TestMode.RECORD) {
                interceptorManager
                    .addSanitizers(createValueSanitizers("subscriptions", subscriptionId, SUBSCRIPTION_PLACEHOLDER));
                interceptorManager.addSanitizers(
                    createValueSanitizers("resourceGroups", resourceGroupName, RESOURCE_GROUP_PLACEHOLDER));
                interceptorManager
                    .addSanitizers(createValueSanitizers("accounts", accountName, ACCOUNT_NAME_PLACEHOLDER));
            }
            interceptorManager.addSanitizers(
                new TestProxySanitizer("api-version=[^&]+", "api-version=" + RECORDED_API_VERSION,
                    TestProxySanitizerType.URL),
                new TestProxySanitizer("$..etag", null, SANITIZED_ETAG, TestProxySanitizerType.BODY_KEY),
                new TestProxySanitizer("etag", ".*", SANITIZED_ETAG, TestProxySanitizerType.HEADER),
                new TestProxySanitizer("if-match", ".*", SANITIZED_ETAG, TestProxySanitizerType.HEADER),
                new TestProxySanitizer("x-ms-correlation-request-id", ".*", "sanitized-correlation-request-id",
                    TestProxySanitizerType.HEADER),
                new TestProxySanitizer("x-ms-operation-identifier", ".*", "sanitized-operation-identifier",
                    TestProxySanitizerType.HEADER),
                new TestProxySanitizer("x-ms-routing-request-id", ".*", "sanitized-routing-request-id",
                    TestProxySanitizerType.HEADER));
        }

        TokenCredential credential = TestUtilities.getTokenCredentialForTest(getTestMode());
        CognitiveServicesManager.Configurable configurable = CognitiveServicesManager.configure();
        if (getTestMode() == TestMode.PLAYBACK) {
            configurable.withHttpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            configurable.withPolicy(interceptorManager.getRecordPolicy());
        }
        manager = configurable.authenticate(credential, new AzureProfile("", subscriptionId, AzureEnvironment.AZURE));
    }

    @Test
    public void createsGetsListsUpdatesDeletesAndConfirmsDeletion() {
        boolean created = false;
        RuntimeException testFailure = null;

        try {
            AccountCostControl createdCostControl = manager.accountCostControls()
                .define(COST_CONTROL_NAME)
                .withExistingAccount(resourceGroupName, accountName)
                .withProperties(createProperties(25))
                .withIfNoneMatch("*")
                .create();
            created = true;
            Assertions.assertEquals(COST_CONTROL_NAME, createdCostControl.name());
            Assertions.assertEquals(CostControlCategory.COST, createdCostControl.properties().category());
            Assertions.assertEquals(CostControlTimeGrain.MONTHLY, createdCostControl.properties().timeGrain());
            Assertions.assertEquals(25.0, createdCostControl.properties().amount());

            manager.accountCostControls().checkExistence(resourceGroupName, accountName, COST_CONTROL_NAME);
            AccountCostControl fetched
                = manager.accountCostControls().get(resourceGroupName, accountName, COST_CONTROL_NAME);
            Assertions.assertNotNull(fetched.etag());
            Assertions.assertEquals(25.0, fetched.properties().amount());

            List<String> names = manager.accountCostControls()
                .list(resourceGroupName, accountName)
                .stream()
                .map(AccountCostControl::name)
                .collect(Collectors.toList());
            Assertions.assertTrue(names.contains(COST_CONTROL_NAME));

            AccountCostControl updated = createdCostControl.update()
                .withProperties(new CostControlPatchProperties().withAmount(50.0))
                .withIfMatch(fetched.etag())
                .apply();
            Assertions.assertEquals(50.0, updated.properties().amount());
            Assertions.assertEquals(CostControlCategory.COST, updated.properties().category());

            AccountCostControl fetchedAfterUpdate
                = manager.accountCostControls().get(resourceGroupName, accountName, COST_CONTROL_NAME);
            Assertions.assertNotNull(fetchedAfterUpdate.etag());
            Assertions.assertEquals(50.0, fetchedAfterUpdate.properties().amount());

            manager.accountCostControls()
                .deleteWithResponse(resourceGroupName, accountName, COST_CONTROL_NAME, fetchedAfterUpdate.etag(),
                    com.azure.core.util.Context.NONE);
            created = false;

            ManagementException exception = Assertions.assertThrows(ManagementException.class,
                () -> manager.accountCostControls().get(resourceGroupName, accountName, COST_CONTROL_NAME));
            Assertions.assertEquals(404, exception.getResponse().getStatusCode());
        } catch (RuntimeException exception) {
            testFailure = exception;
            throw exception;
        } finally {
            if (created) {
                try {
                    manager.accountCostControls().delete(resourceGroupName, accountName, COST_CONTROL_NAME);
                } catch (ManagementException exception) {
                    if (!isNotFound(exception) && testFailure == null) {
                        throw exception;
                    }
                }
            }
        }
    }

    static TestProxySanitizer[] createValueSanitizers(String segment, String value, String replacement) {
        // Character classes also survive the test utility's unescaped JSON registration payload.
        String regex = value.replace(".", "[.]").replace("(", "[(]").replace(")", "[)]");
        regex = "(?<=/" + segment + "/)" + regex + "(?![A-Za-z0-9_.()-])";
        return new TestProxySanitizer[] {
            new TestProxySanitizer(regex, replacement, TestProxySanitizerType.URL),
            new TestProxySanitizer(regex, replacement, TestProxySanitizerType.BODY_REGEX) };
    }

    private String valueForTest(String name, String playbackValue) {
        if (getTestMode() == TestMode.PLAYBACK) {
            return playbackValue;
        }
        String value = Configuration.getGlobalConfiguration().get(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                "Set the " + name + " environment variable before recording or running live tests.");
        }
        return value;
    }

    private static CostControlProperties createProperties(double amount) {
        return new CostControlProperties().withCategory(CostControlCategory.COST)
            .withAmount(amount)
            .withTimeGrain(CostControlTimeGrain.MONTHLY)
            .withTimePeriod(new CostControlTimePeriod().withStartDate(OffsetDateTime.parse("2026-11-01T00:00:00Z"))
                .withEndDate(OffsetDateTime.parse("2027-11-01T00:00:00Z")));
    }

    private static boolean isNotFound(ManagementException exception) {
        return exception.getResponse() != null && exception.getResponse().getStatusCode() == 404;
    }
}
