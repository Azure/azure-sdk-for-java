// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

/**
 * Matches all the messages arriving to be selected for the subscription.
 *
 * <p><strong>Sample: Create rule for all messages</strong></p>
 *
 * <p>The following code sample demonstrates how to create a rule.  The {@code "all-messages-subscription"} subscription
 * is associated with the {@link TrueRuleFilter}. So all messages sent to the topic are also received from the
 * subscription.</p>
 *
 * <!-- src_embed com.azure.messaging.servicebus.administration.servicebusadministrationclient.createRule -->
 * <pre>
 * String topicName = &quot;my-existing-topic&quot;;
 * String subscriptionName = &quot;all-messages-subscription&quot;;
 * String ruleName = &quot;true-filter&quot;;
 *
 * RuleFilter alwaysTrueRule = new TrueRuleFilter&#40;&#41;;
 * CreateRuleOptions createRuleOptions = new CreateRuleOptions&#40;&#41;
 *     .setFilter&#40;alwaysTrueRule&#41;;
 *
 * RuleProperties rule = client.createRule&#40;topicName, ruleName, subscriptionName, createRuleOptions&#41;;
 *
 * System.out.printf&#40;&quot;Rule '%s' created for topic %s, subscription %s. Filter: %s%n&quot;, rule.getName&#40;&#41;, topicName,
 *     subscriptionName, rule.getFilter&#40;&#41;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.administration.servicebusadministrationclient.createRule -->
 *
 * @see CreateRuleOptions#setFilter(RuleFilter)
 * @see RuleProperties#setFilter(RuleFilter)
 * @see <a href="https://learn.microsoft.com/azure/service-bus-messaging/topic-filters">Service Bus: Topic filters</a>
 */
public final class TrueRuleFilter extends SqlRuleFilter {
    private static final TrueRuleFilter INSTANCE = new TrueRuleFilter();

    /**
     * Gets an instance of the {@link TrueRuleFilter}.
     *
     * @return an instance of the {@link TrueRuleFilter}.
     */
    static TrueRuleFilter getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new instance.
     */
    public TrueRuleFilter() {
        super("1=1");
    }

    /**
     * Converts the current instance to its string representation.
     *
     * @return The string representation.
     */
    @Override
    public String toString() {
        return "TrueRuleFilter";
    }
}
