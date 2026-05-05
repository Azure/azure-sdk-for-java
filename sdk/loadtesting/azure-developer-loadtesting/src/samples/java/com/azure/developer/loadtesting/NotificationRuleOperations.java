// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.developer.loadtesting.models.NotificationRule;
import com.azure.developer.loadtesting.models.PassFailTestResult;
import com.azure.developer.loadtesting.models.TestRunEndedEventCondition;
import com.azure.developer.loadtesting.models.TestRunEndedNotificationEventFilter;
import com.azure.developer.loadtesting.models.TestRunStatus;
import com.azure.developer.loadtesting.models.TestsNotificationEventFilter;
import com.azure.developer.loadtesting.models.TestsNotificationRule;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrates how to create, get, list and delete notification rules.
 */
public final class NotificationRuleOperations {
    /**
     * Authenticates with the load testing resource and shows how to manage
     * notification rules.
     *
     * @param args Unused. Arguments to the program.
     *
     * @throws ClientAuthenticationException - when the credentials have
     *                                       insufficient permissions for load test
     *                                       resource.
     * @throws ResourceNotFoundException     - when the specified resource does not
     *                                       exist.
     */
    public static void main(String[] args) {
        createOrUpdateNotificationRule();
        getNotificationRule();
        listNotificationRules();
        deleteNotificationRule();
    }

    public static void createOrUpdateNotificationRule() {
        LoadTestAdministrationClient client = new LoadTestAdministrationClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("<endpoint>")
                .buildClient();

        String notificationRuleId = "sample-notification-rule-id";
        String testId = "sample-test-id";
        String actionGroupId = "/subscriptions/<subscription-id>/resourceGroups/<resource-group>"
                + "/providers/Microsoft.Insights/actionGroups/<action-group-name>";

        // Build the event filter condition for test run completion
        TestRunEndedEventCondition condition = new TestRunEndedEventCondition()
                .setTestRunStatuses(Arrays.asList(TestRunStatus.DONE, TestRunStatus.FAILED))
                .setTestRunResults(Arrays.asList(PassFailTestResult.PASSED, PassFailTestResult.FAILED));

        // Build the event filter
        TestRunEndedNotificationEventFilter eventFilter = new TestRunEndedNotificationEventFilter()
                .setCondition(condition);

        // Build the event filters map
        Map<String, TestsNotificationEventFilter> eventFilters = new HashMap<>();
        eventFilters.put("testRunEnded", eventFilter);

        // Create the notification rule
        TestsNotificationRule rule = new TestsNotificationRule()
                .setDisplayName("Sample Notification Rule")
                .setTestIds(Arrays.asList(testId))
                .setActionGroupIds(Arrays.asList(actionGroupId))
                .setEventFilters(eventFilters);

        NotificationRule response = client.createOrUpdateNotificationRule(notificationRuleId, rule);

        System.out.println("Notification Rule created: " + response.getNotificationRuleId());
        System.out.println("Display Name: " + response.getDisplayName());
    }

    public static void getNotificationRule() {
        LoadTestAdministrationClient client = new LoadTestAdministrationClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("<endpoint>")
                .buildClient();

        String notificationRuleId = "sample-notification-rule-id";

        NotificationRule rule = client.getNotificationRule(notificationRuleId);

        System.out.println("Notification Rule ID: " + rule.getNotificationRuleId());
        System.out.println("Display Name: " + rule.getDisplayName());
    }

    public static void listNotificationRules() {
        LoadTestAdministrationClient client = new LoadTestAdministrationClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("<endpoint>")
                .buildClient();

        PagedIterable<NotificationRule> notificationRules = client.listNotificationRules();

        notificationRules.forEach(rule -> {
            String notificationRuleId = rule.getNotificationRuleId();
            String displayName = rule.getDisplayName();

            System.out.println(String.format("%s\t%s", notificationRuleId, displayName));
        });
    }

    public static void deleteNotificationRule() {
        LoadTestAdministrationClient client = new LoadTestAdministrationClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("<endpoint>")
                .buildClient();

        String notificationRuleId = "sample-notification-rule-id";

        client.deleteNotificationRule(notificationRuleId);

        System.out.println("Notification Rule deleted: " + notificationRuleId);
    }
}
