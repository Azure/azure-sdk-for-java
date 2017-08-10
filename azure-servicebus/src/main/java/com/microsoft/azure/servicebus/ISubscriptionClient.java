// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;
import com.microsoft.azure.servicebus.rules.Filter;
import com.microsoft.azure.servicebus.rules.RuleDescription;

/**
 * SubscriptionClient can be used for all basic interactions with a Service Bus Subscription.
 */
public interface ISubscriptionClient extends IMessageEntityClient, IMessageAndSessionPump {

    /**
     * Gets the {@link ReceiveMode} of the current receiver
     *
     * @return The receive mode.
     */
    public ReceiveMode getReceiveMode();

    /**
     * Adds a rule to the current subscription to filter the messages reaching from topic to the subscription.
     *
     * @param ruleDescription The rule description that provides the rule to add.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if add rule failed
     */
    public void addRule(RuleDescription ruleDescription) throws InterruptedException, ServiceBusException;

    /**
     * Asynchronously adds a rule to the current subscription to filter the messages reaching from topic to the subscription.
     *
     * @param ruleDescription The rule description that provides the rule to add.
     * @return a CompletableFuture representing the pending rule add operation.
     */
    public CompletableFuture<Void> addRuleAsync(RuleDescription ruleDescription);

    /**
     * Adds a rule with specified name and {@link Filter} to the current subscription to filter the messages reaching from topic to the subscription.
     *
     * @param ruleName The rule name
     * @param filter   The {@link Filter} to add.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if add rule failed
     */
    public void addRule(String ruleName, Filter filter) throws InterruptedException, ServiceBusException;

    /**
     * Asynchronously adds a rule with specified name and {@link Filter} to the current subscription to filter the messages reaching from topic to the subscription.
     *
     * @param ruleName The rule name
     * @param filter   The {@link Filter} to add.
     * @return a CompletableFuture representing the pending rule add operation.
     */
    public CompletableFuture<Void> addRuleAsync(String ruleName, Filter filter);

    /**
     * Asynchronously removes the rule on the subscription identified by ruleName
     *
     * @param ruleName he name of rule.
     * @return a CompletableFuture representing the pending rule remove operation.
     */
    public CompletableFuture<Void> removeRuleAsync(String ruleName);

    /**
     * Removes the rule on the subscription identified by ruleName
     *
     * @param ruleName The name of rule.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if remove rule failed
     */
    public void removeRule(String ruleName) throws InterruptedException, ServiceBusException;

    /**
     * Get all rules associated with the subscription.
     *
     * @return The collection fo the rules.
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ServiceBusException  if get rules failed
     */
    public Collection<RuleDescription> getRules() throws ServiceBusException, InterruptedException;

    /**
     * Get all rules associated with the subscription.
     *
     * @return a CompletableFuture representing the pending get rules operation.
     */
    public CompletableFuture<Collection<RuleDescription>> getRulesAsync();

    /**
     * Gets the name of the topic, for this subscription.
     *
     * @return the name of the topic
     */
    public String getTopicName();

    /**
     * Gets the subscription name.
     * @return The subscription name.
     */
    public String getSubscriptionName();
}
