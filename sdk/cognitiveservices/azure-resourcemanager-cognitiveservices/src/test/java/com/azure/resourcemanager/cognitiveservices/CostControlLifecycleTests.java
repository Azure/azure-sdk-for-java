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
import com.azure.resourcemanager.cognitiveservices.models.CostControl;
import com.azure.resourcemanager.cognitiveservices.models.CostControlDimension;
import com.azure.resourcemanager.cognitiveservices.models.CostControlDimensionType;
import com.azure.resourcemanager.cognitiveservices.models.CostControlPatchProperties;
import com.azure.resourcemanager.cognitiveservices.models.CostControlPeriod;
import com.azure.resourcemanager.cognitiveservices.models.CostControlProperties;
import com.azure.resourcemanager.cognitiveservices.models.CostControlRule;
import com.azure.resourcemanager.cognitiveservices.models.CostControlThreshold;
import com.azure.resourcemanager.cognitiveservices.models.CostControlThresholdAction;
import com.azure.resourcemanager.cognitiveservices.models.CostControlThresholdType;
import com.azure.resourcemanager.cognitiveservices.models.CostControlUnit;
import com.azure.resourcemanager.test.utils.TestUtilities;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class CostControlLifecycleTests extends TestProxyTestBase {
    private static final String SUBSCRIPTION_PLACEHOLDER = "00000000-0000-0000-0000-000000000000";
    private static final String RESOURCE_GROUP_PLACEHOLDER = "sanitized-resource-group";
    private static final String ACCOUNT_NAME_PLACEHOLDER = "sanitized-cognitive-account";
    private static final String SANITIZED_ETAG = "sanitized-etag";
    private static final String RECORDED_API_VERSION = "2026-09-15-preview";
    private static final String COST_CONTROL_NAME = "sdk-cost-control-lifecycle";

    private CognitiveServicesManager manager;
    private String resourceGroupName;
    private String accountName;

    @Override
    protected void beforeTest() {
        String subscriptionId = valueForTest("AZURE_SUBSCRIPTION_ID", SUBSCRIPTION_PLACEHOLDER);
        resourceGroupName = valueForTest("AZURE_RESOURCE_GROUP", RESOURCE_GROUP_PLACEHOLDER);
        accountName = valueForTest("AZURE_COGNITIVE_SERVICES_ACCOUNT", ACCOUNT_NAME_PLACEHOLDER);

        if (getTestMode() != TestMode.LIVE) {
            interceptorManager.removeSanitizers("AZSDK3493", "AZSDK3430");
            if (getTestMode() == TestMode.RECORD) {
                addValueSanitizers(subscriptionId, SUBSCRIPTION_PLACEHOLDER);
                addValueSanitizers(resourceGroupName, RESOURCE_GROUP_PLACEHOLDER);
                addValueSanitizers(accountName, ACCOUNT_NAME_PLACEHOLDER);
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
            CostControl createdCostControl = manager.costControls()
                .define(COST_CONTROL_NAME)
                .withExistingAccount(resourceGroupName, accountName)
                .withProperties(createProperties("SDK Cost Control lifecycle", 25))
                .withIfNoneMatch("*")
                .create();
            created = true;
            Assertions.assertEquals(COST_CONTROL_NAME, createdCostControl.name());
            Assertions.assertEquals(CostControlThresholdAction.AUDIT,
                createdCostControl.properties().rules().get(0).thresholds().get(0).action());

            CostControl fetched = manager.costControls().get(resourceGroupName, accountName, COST_CONTROL_NAME);
            Assertions.assertNotNull(fetched.etag());
            Assertions.assertEquals(25.0, fetched.properties().rules().get(0).amount());

            List<String> names = manager.costControls()
                .list(resourceGroupName, accountName)
                .stream()
                .map(CostControl::name)
                .collect(Collectors.toList());
            Assertions.assertTrue(names.contains(COST_CONTROL_NAME));

            CostControl updated = createdCostControl.update()
                .withProperties(new CostControlPatchProperties().withDisplayName("SDK Cost Control lifecycle updated")
                    .withRules(Collections.singletonList(createRule(50))))
                .withIfMatch(fetched.etag())
                .apply();
            Assertions.assertEquals("SDK Cost Control lifecycle updated", updated.properties().displayName());
            Assertions.assertEquals(50.0, updated.properties().rules().get(0).amount());

            CostControl fetchedAfterUpdate
                = manager.costControls().get(resourceGroupName, accountName, COST_CONTROL_NAME);
            Assertions.assertNotNull(fetchedAfterUpdate.etag());
            Assertions.assertEquals(50.0, fetchedAfterUpdate.properties().rules().get(0).amount());

            manager.costControls()
                .deleteWithResponse(resourceGroupName, accountName, COST_CONTROL_NAME, fetchedAfterUpdate.etag(),
                    com.azure.core.util.Context.NONE);
            created = false;

            ManagementException exception = Assertions.assertThrows(ManagementException.class,
                () -> manager.costControls().get(resourceGroupName, accountName, COST_CONTROL_NAME));
            Assertions.assertEquals(404, exception.getResponse().getStatusCode());
        } catch (RuntimeException exception) {
            testFailure = exception;
            throw exception;
        } finally {
            if (created) {
                try {
                    manager.costControls().delete(resourceGroupName, accountName, COST_CONTROL_NAME);
                } catch (ManagementException exception) {
                    if (!isNotFound(exception) && testFailure == null) {
                        throw exception;
                    }
                }
            }
        }
    }

    private void addValueSanitizers(String value, String replacement) {
        interceptorManager.addSanitizers(new TestProxySanitizer(value, replacement, TestProxySanitizerType.URL),
            new TestProxySanitizer(value, replacement, TestProxySanitizerType.BODY_REGEX));
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

    private static CostControlProperties createProperties(String displayName, double amount) {
        return new CostControlProperties().withDisplayName(displayName)
            .withRules(Collections.singletonList(createRule(amount)));
    }

    private static CostControlRule createRule(double amount) {
        return new CostControlRule().withName("account-budget")
            .withCounterKey(
                Collections.singletonList(new CostControlDimension().withType(CostControlDimensionType.ACCOUNT)))
            .withUnit(CostControlUnit.USD)
            .withAmount(amount)
            .withPeriod(CostControlPeriod.MONTH)
            .withRecurring(true)
            .withThresholds(
                Collections.singletonList(new CostControlThreshold().withType(CostControlThresholdType.PERCENTAGE)
                    .withValue(0)
                    .withAction(CostControlThresholdAction.AUDIT)));
    }

    private static boolean isNotFound(ManagementException exception) {
        return exception.getResponse() != null && exception.getResponse().getStatusCode() == 404;
    }
}
