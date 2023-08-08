// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.RuleFilter;
import com.azure.messaging.servicebus.administration.models.TrueRuleFilter;
import org.junit.jupiter.api.Test;

/**
 * Sample demonstrates how to manage rules to an Azure Service Bus subscription.
 */
public class ManageRulesAsyncSample {
    String connectionString = System.getenv("AZURE_SERVICEBUS_NAMESPACE_CONNECTION_STRING");
    String topicName = System.getenv("AZURE_SERVICEBUS_TOPIC_NAME");
    String subscriptionName = System.getenv("AZURE_SERVICEBUS_SUBSCRIPTION_NAME");

    /**
     * Main method to invoke this demo on how to manage rules to an Azure Service Bus.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        ManageRulesAsyncSample sample = new ManageRulesAsyncSample();
        sample.run();
    }

    /**
     * This method to invoke this demo on how to manage rules to an Azure Service Bus subscription.
     */
    @Test
    public void run() {
        // The connection string value can be obtained by:
        // 1. Going to your Service Bus namespace in Azure Portal.
        // 2. Go to "Shared access policies"
        // 3. Copy the connection string for the "RootManageSharedAccessKey" policy.
        // The 'connectionString' format is shown below.
        // 1. "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        // 2. "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // 3. "topicName" will be the name of the Service Bus topic instance you created
        //    inside the Service Bus namespace.
        // 4. "subscriptionName" will be the name of the Service Bus subscription instance you created inside
        //    the Service Bus topic.

        // Instantiate a client that will be used to call the service.
        ServiceBusRuleManagerAsyncClient ruleManager = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .ruleManager()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .buildAsyncClient();

        // Create a rule.
        RuleFilter trueRuleFilter = new TrueRuleFilter();
        CreateRuleOptions options = new CreateRuleOptions(trueRuleFilter);
        ruleManager.createRule("new-rule", options).subscribe(
            unused -> { },
            err -> System.err.println("Error occurred when create a rule, err: " + err),
            () -> System.out.println("Create complete.")
        );

        // Fetch all rules.
        ruleManager.listRules().subscribe(ruleProperties -> System.out.println(ruleProperties.getName()));

        // Delete rule.
        ruleManager.deleteRule("exist-rule").subscribe(
            unused -> { },
            err -> System.err.println("Error occurred when delete rule, err: " + err),
            () -> System.out.println("Delete complete.")
        );

    }
}
