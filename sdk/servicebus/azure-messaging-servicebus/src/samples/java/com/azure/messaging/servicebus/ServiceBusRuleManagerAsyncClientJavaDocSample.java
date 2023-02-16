// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.RuleFilter;
import com.azure.messaging.servicebus.administration.models.TrueRuleFilter;
import org.junit.jupiter.api.Test;

/**
 * Code snippets demonstrating various {@link ServiceBusRuleManagerAsyncClient} scenarios.
 */
public class ServiceBusRuleManagerAsyncClientJavaDocSample {
    // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
    // We are reading 'connectionString/topicName/subscriptionName' from environment variable.
    // You can configure them as it fits suitable for your application.
    // 1. "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
    // 2. "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
    // 3. "topicName" will be the name of the Service Bus queue instance you created
    //    inside the Service Bus namespace.
    // 4. "subscriptionName" will be the name of the Service Bus subscription instance you created inside
    //    the Service Bus topic.
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String topicName = System.getenv("AZURE_SERVICEBUS_TOPIC_NAME");
    String subscriptionName = System.getenv("AZURE_SERVICEBUS_SUBSCRIPTION_NAME");

    ServiceBusRuleManagerAsyncClient ruleManager = new ServiceBusClientBuilder()
        .connectionString(connectionString)
        .ruleManager()
        .topicName(topicName)
        .subscriptionName(subscriptionName)
        .buildAsyncClient();

    /**
     * Code snippet for creating a ServiceBusRuleManagerAsyncClient.
     */
    @Test
    public void initialization() {
        // BEGIN: com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.instantiation
        // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
        // The connectionString/queueName must be set by the application. The 'connectionString' format is shown below.
        // "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"

        ServiceBusRuleManagerAsyncClient ruleManager = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .ruleManager()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .buildAsyncClient();
        // END: com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.instantiation

        ruleManager.close();
    }

    /**
     * Demonstrates how to create a rule for a Service Bus subscription.
     */
    @Test
    public void createRule() {
        ServiceBusRuleManagerAsyncClient ruleManager = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .ruleManager()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.createRule
        RuleFilter trueRuleFilter = new TrueRuleFilter();
        CreateRuleOptions options = new CreateRuleOptions(trueRuleFilter);
        ruleManager.createRule("new-rule", options).subscribe(
            unused -> { },
            err -> System.err.println("Error occurred when create a rule, err: " + err),
            () -> System.out.println("Create complete.")
        );
        // END: com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.createRule

        ruleManager.close();
    }

    /**
     * Demonstrates how to fetch all rules under a Service Bus subscription.
     */
    @Test
    public void getRules() {
        // BEGIN: com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.getRules
        ruleManager.listRules().subscribe(ruleProperties -> System.out.println(ruleProperties.getName()));
        // END: com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.getRules
    }

    /**
     * Demonstrates how to delete a rule.
     */
    @Test
    public void deleteRule() {
        // BEGIN: com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.deleteRule
        ruleManager.deleteRule("exist-rule").subscribe(
            unused -> { },
            err -> System.err.println("Error occurred when delete rule, err: " + err),
            () -> System.out.println("Delete complete.")
        );
        // END: com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.deleteRule
    }
}
