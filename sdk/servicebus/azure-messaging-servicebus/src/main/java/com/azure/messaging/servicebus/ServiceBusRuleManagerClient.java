// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.IterableStream;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient;
import com.azure.messaging.servicebus.administration.models.CorrelationRuleFilter;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.RuleProperties;
import com.azure.messaging.servicebus.administration.models.SqlRuleAction;
import com.azure.messaging.servicebus.administration.models.SqlRuleFilter;

import java.time.Duration;
import java.util.Objects;

/**
 * A <b>synchronous</b> rule manager responsible for managing rules for a specific topic subscription. The rule manager
 * requires only Listen claims, whereas the {@link ServiceBusAdministrationAsyncClient} requires Manage claims.
 *
 * <p><strong>Create a rule to a Service Bus subscription</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusrulemanagerclient.createRule -->
 * <pre>
 * RuleFilter trueRuleFilter = new TrueRuleFilter&#40;&#41;;
 * CreateRuleOptions options = new CreateRuleOptions&#40;trueRuleFilter&#41;;
 * ruleManager.createRule&#40;&quot;new-rule&quot;, options&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusrulemanagerclient.createRule -->
 */
@ServiceClient(builder = ServiceBusClientBuilder.class)
public class ServiceBusRuleManagerClient implements AutoCloseable {
    private final ServiceBusRuleManagerAsyncClient asyncClient;
    private final Duration operationTimeout;

    /**
     * Creates a new instance of {@link ServiceBusRuleManagerClient} that manages rules for a Service Bus subscription.
     *
     * @param asyncClient Asynchronous rule manager client.
     * @param operationTimeout Timeout to wait for operation to complete.
     *
     * @throws NullPointerException if {@code asyncClient} or {@code operationTimeout} is null.
     */
    ServiceBusRuleManagerClient(ServiceBusRuleManagerAsyncClient asyncClient,
                                Duration operationTimeout) {
        this.asyncClient = Objects.requireNonNull(asyncClient, "'asyncClient' cannot be null.");
        this.operationTimeout = Objects.requireNonNull(operationTimeout, "'operationTimeout' cannot be null.");
    }

    /**
     * Gets the fully qualified namespace.
     *
     * @return The fully qualified namespace.
     */
    public String getFullyQualifiedNamespace() {
        return asyncClient.getFullyQualifiedNamespace();
    }

    /**
     * Gets the name of the Service Bus resource.
     *
     * @return The name of the Service Bus resource.
     */
    public String getEntityPath() {
        return asyncClient.getEntityPath();
    }

    /**
     * Creates a rule to the current subscription to filter the messages reaching from topic to the subscription.
     *
     * @param ruleName Name of rule.
     * @param options The options for the rule to add.
     *
     * @throws NullPointerException if {@code options}, {@code ruleName} is null.
     * @throws IllegalStateException if client is disposed.
     * @throws IllegalArgumentException if {@code ruleName} is empty string, action of {@code options} is not null and not
     * instanceof {@link SqlRuleAction}, filter of {@code options} is not instanceof {@link SqlRuleFilter} or
     * {@link CorrelationRuleFilter}.
     * @throws ServiceBusException if filter matches {@code ruleName} is already created in subscription.
     */
    public void createRule(String ruleName, CreateRuleOptions options) {
        asyncClient.createRule(ruleName, options).block(operationTimeout);
    }

    /**
     * Fetches all rules associated with the topic and subscription.
     *
     * @return A list of rules associated with the topic and subscription.
     *
     * @throws IllegalStateException if client is disposed.
     * @throws UnsupportedOperationException if client cannot support filter with descriptor in message body.
     */
    public IterableStream<RuleProperties> listRules() {
        return new IterableStream<>(asyncClient.listRules());
    }

    /**
     * Removes the rule on the subscription identified by {@code ruleName}.
     *
     * @param ruleName Name of rule to delete.
     *
     * @throws NullPointerException if {@code ruleName} is null.
     * @throws IllegalStateException if client is disposed.
     * @throws IllegalArgumentException if {@code ruleName} is empty string.
     * @throws ServiceBusException if cannot find filter matches {@code ruleName} in subscription.
     */
    public void deleteRule(String ruleName) {
        asyncClient.deleteRule(ruleName).block(operationTimeout);
    }

    /**
     * Disposes of the {@link ServiceBusRuleManagerClient}. If the client has a dedicated connection, the underlying
     * connection is also closed.
     */
    @Override
    public void close() {
        asyncClient.close();
    }
}
