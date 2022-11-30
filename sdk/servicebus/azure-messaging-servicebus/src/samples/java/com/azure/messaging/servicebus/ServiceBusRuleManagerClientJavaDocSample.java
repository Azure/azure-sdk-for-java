// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.RuleFilter;
import com.azure.messaging.servicebus.administration.models.TrueRuleFilter;
import org.junit.jupiter.api.Test;

/**
 * Code snippets demonstrating various {@link ServiceBusRuleManagerClient} scenarios.
 */
public class ServiceBusRuleManagerClientJavaDocSample {
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

    ServiceBusRuleManagerClient ruleManager = new ServiceBusClientBuilder()
        .connectionString(connectionString)
        .ruleManager()
        .topicName(topicName)
        .subscriptionName(subscriptionName)
        .buildClient();

    /**
     * Demonstrates how to create a rule for a Service Bus subscription.
     */
    @Test
    public void createRule() {
        // BEGIN: com.azure.messaging.servicebus.servicebusrulemanagerclient.createRule
        RuleFilter trueRuleFilter = new TrueRuleFilter();
        CreateRuleOptions options = new CreateRuleOptions(trueRuleFilter);
        ruleManager.createRule("new-rule", options);
        // END: com.azure.messaging.servicebus.servicebusrulemanagerclient.createRule

        ruleManager.close();
    }

}
