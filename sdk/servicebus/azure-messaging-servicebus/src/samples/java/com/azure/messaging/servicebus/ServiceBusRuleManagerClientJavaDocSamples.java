// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.RuleFilter;
import com.azure.messaging.servicebus.administration.models.TrueRuleFilter;
import org.junit.jupiter.api.Test;

/**
 * Code snippets demonstrating various {@link ServiceBusRuleManagerClient} scenarios.
 */
public class ServiceBusRuleManagerClientJavaDocSamples {
    /**
     * Fully qualified namespace is the host name of the Service Bus resource.  It can be found by navigating to the
     * Service Bus namespace and looking in the "Essentials" panel.
     */
    private final String fullyQualifiedNamespace = System.getenv("AZURE_SERVICEBUS_FULLY_QUALIFIED_DOMAIN_NAME");
    private final String topicName = System.getenv("AZURE_SERVICEBUS_TOPIC_NAME");
    private final String subscriptionName = System.getenv("AZURE_SERVICEBUS_SUBSCRIPTION_NAME");

    @Test
    public void initializationAsync() {
        // BEGIN: com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.instantiation
        // The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
        // The connectionString/queueName must be set by the application. The 'connectionString' format is shown below.
        // "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}"
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusRuleManagerAsyncClient ruleManager = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, credential)
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
        // BEGIN: com.azure.messaging.servicebus.servicebusrulemanagerclient.createRule
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusRuleManagerClient ruleManager = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, credential)
            .ruleManager()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .buildClient();

        RuleFilter trueRuleFilter = new TrueRuleFilter();
        CreateRuleOptions options = new CreateRuleOptions(trueRuleFilter);
        ruleManager.createRule("new-rule", options);

        // Dispose of the ruleManager when finished using it.
        ruleManager.close();
        // END: com.azure.messaging.servicebus.servicebusrulemanagerclient.createRule
    }

    /**
     * Demonstrates how to create a rule for a Service Bus subscription.
     */
    @Test
    public void createRuleAsync() {

        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusRuleManagerAsyncClient ruleManager = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .ruleManager()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.createRule
        RuleFilter trueRuleFilter = new TrueRuleFilter();
        CreateRuleOptions options = new CreateRuleOptions(trueRuleFilter);

        // `subscribe` is a non-blocking call. After setting up the create rule operation, it will move onto the next
        // line of code to execute.
        // Consider using Mono.usingWhen to scope the creation, usage, and cleanup of the rule manager.
        ruleManager.createRule("new-rule", options).subscribe(
            unused -> {
            },
            err -> System.err.println("Error occurred when create a rule, err: " + err),
            () -> System.out.println("Create complete.")
        );

        // Finally dispose of the rule manager when done using it.
        ruleManager.close();
        // END: com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.createRule
    }

    /**
     * Demonstrates how to fetch all rules under a Service Bus subscription.
     */
    @Test
    public void getRulesAsync() {
        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusRuleManagerAsyncClient ruleManager = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .ruleManager()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.getRules
        // `subscribe` is a non-blocking call. After setting up the list rules operation, it will move onto the next
        // line of code to execute.
        ruleManager.listRules().subscribe(ruleProperties -> System.out.println(ruleProperties.getName()));
        // END: com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.getRules

        // Finally dispose of the rule manager when done using it.
        ruleManager.close();
    }

    /**
     * Demonstrates how to delete a rule.
     */
    @Test
    public void deleteRuleAsync() {
        // 'fullyQualifiedNamespace' will look similar to "{your-namespace}.servicebus.windows.net"
        ServiceBusRuleManagerAsyncClient ruleManager = new ServiceBusClientBuilder()
            .credential(fullyQualifiedNamespace, new DefaultAzureCredentialBuilder().build())
            .ruleManager()
            .topicName(topicName)
            .subscriptionName(subscriptionName)
            .buildAsyncClient();

        // BEGIN: com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.deleteRule
        // `subscribe` is a non-blocking call. After setting up the delete rule operation, it will move onto the next
        // line of code to execute.
        ruleManager.deleteRule("exist-rule").subscribe(
            unused -> { },
            err -> System.err.println("Error occurred when delete rule, err: " + err),
            () -> System.out.println("Delete complete.")
        );
        // END: com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.deleteRule

        // Finally dispose of the rule manager when done using it.
        ruleManager.close();
    }
}
