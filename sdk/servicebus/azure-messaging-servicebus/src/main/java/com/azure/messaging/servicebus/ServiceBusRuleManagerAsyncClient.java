// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.RuleFilter;
import com.azure.messaging.servicebus.administration.models.RuleProperties;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.implementation.Messages.INVALID_OPERATION_DISPOSED_RULE_MANAGER;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An <b>asynchronous</b> rule manager responsible for managing rules for a specific topic subscription. The rule manager
 * requires only Listen claims, whereas the {@link ServiceBusAdministrationAsyncClient} requires Manage claims.
 *
 * <p><strong>Create an instance of rule manager</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.instantiation -->
 * <pre>
 * &#47;&#47; The required parameters is connectionString, a way to authenticate with Service Bus using credentials.
 * &#47;&#47; The connectionString&#47;queueName must be set by the application. The 'connectionString' format is shown below.
 * &#47;&#47; &quot;Endpoint=&#123;fully-qualified-namespace&#125;;SharedAccessKeyName=&#123;policy-name&#125;;SharedAccessKey=&#123;key&#125;&quot;
 *
 * ServiceBusRuleManagerAsyncClient ruleManager = new ServiceBusClientBuilder&#40;&#41;
 *     .connectionString&#40;connectionString&#41;
 *     .ruleManager&#40;&#41;
 *     .topicName&#40;topicName&#41;
 *     .subscriptionName&#40;subscriptionName&#41;
 *     .buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.instantiation -->
 *
 * <p><strong>Create a rule to a Service Bus subscription</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.createRule -->
 * <pre>
 * RuleFilter trueRuleFilter = new TrueRuleFilter&#40;&#41;;
 * CreateRuleOptions options = new CreateRuleOptions&#40;trueRuleFilter&#41;;
 * ruleManager.createRule&#40;&quot;new-rule&quot;, options&#41;.subscribe&#40;
 *     unused -&gt; &#123; &#125;,
 *     err -&gt; System.err.println&#40;&quot;Error occurred when create a rule, err: &quot; + err&#41;,
 *     &#40;&#41; -&gt; System.out.println&#40;&quot;Create complete.&quot;&#41;
 * &#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.createRule -->
 *
 * <p><strong>Fetch all rules.</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.getRules -->
 * <pre>
 * ruleManager.getRules&#40;&#41;.subscribe&#40;
 *     ruleProperties -&gt; ruleProperties.forEach&#40;rule -&gt; System.out.println&#40;rule.getName&#40;&#41;&#41;&#41;,
 *     err -&gt; System.err.println&#40;&quot;Error occurred when get rules, err: &quot; + err&#41;,
 *     &#40;&#41; -&gt; System.out.println&#40;&quot;Get complete.&quot;&#41;
 * &#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.getRules -->
 *
 * <p><strong>Delete a rule.</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.deleteRule -->
 * <pre>
 * ruleManager.deleteRule&#40;&quot;exist-rule&quot;&#41;.subscribe&#40;
 *     unused -&gt; &#123; &#125;,
 *     err -&gt; System.err.println&#40;&quot;Error occurred when delete rule, err: &quot; + err&#41;,
 *     &#40;&#41; -&gt; System.out.println&#40;&quot;Delete complete.&quot;&#41;
 * &#41;;
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.deleteRule -->
 * @see ServiceBusClientBuilder
 */
@ServiceClient(builder = ServiceBusClientBuilder.class, isAsync = true)
public class ServiceBusRuleManagerAsyncClient implements AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusRuleManagerAsyncClient.class);

    private final String entityPath;
    private final MessagingEntityType entityType;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private final Runnable onClientClose;
    private final AtomicBoolean isDisposed = new AtomicBoolean();

    /**
     * Creates a rule manager that manage rules for a Service Bus subscription.
     *
     * @param entityPath The name of the topic and subscription.
     * @param entityType The type of the Service Bus resource.
     * @param connectionProcessor The AMQP connection to the Service Bus resource.
     * @param onClientClose Operation to run when the client completes.
     */
    ServiceBusRuleManagerAsyncClient(String entityPath, MessagingEntityType entityType,
        ServiceBusConnectionProcessor connectionProcessor, Runnable onClientClose) {
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.entityType = entityType;
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor,
            "'connectionProcessor' cannot be null.");
        this.onClientClose = onClientClose;
    }

    /**
     * Creates a rule to the current subscription to filter the messages reaching from topic to the subscription.
     *
     * @param ruleName Name of rule.
     * @param options The options for the rule to add.
     * @return A Mono that completes when the rule is created.
     *
     * @throws NullPointerException if {@code options}, {@code ruleName} is null.
     * @throws IllegalStateException if client is disposed.
     * @throws IllegalArgumentException if {@code ruleName} is empty string.
     */
    public Mono<Void> createRule(String ruleName, CreateRuleOptions options) {
        if (Objects.isNull(options)) {
            return monoError(LOGGER, new NullPointerException("'options' cannot be null."));
        }
        return createRuleInternal(ruleName, options);
    }

    /**
     * Creates a rule to the current subscription to filter the messages reaching from topic to the subscription.
     *
     * @param name Name of rule.
     * @param filter The filter expression against which messages will be matched.
     * @return A Mono that completes when the rule is created.
     *
     * @throws NullPointerException if {@code options}, {@code name} is null.
     * @throws IllegalStateException if client is disposed.
     * @throws IllegalArgumentException if name is empty string.
     */
    public Mono<Void> createRule(String name, RuleFilter filter) {
        CreateRuleOptions options = new CreateRuleOptions(filter);
        return createRuleInternal(name, options);
    }

    /**
     * Fetches all rules associated with the topic and subscription.
     *
     * @return A collection of rules associated with the topic and subscription.
     *
     * @throws IllegalStateException if client is disposed.
     */
    public Mono<Collection<RuleProperties>> getRules() {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RULE_MANAGER, "getRules")
            ));
        }

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(ServiceBusManagementNode::getRules);
    }

    /**
     * Removes the rule on the subscription identified by {@code ruleName}.
     *
     * @param ruleName Name of rule to delete.
     * @return A Mono that completes when the rule is deleted.
     *
     * @throws NullPointerException if {@code ruleName} is null.
     * @throws IllegalStateException if client is disposed.
     * @throws IllegalArgumentException if {@code ruleName} is empty string.
     */
    public Mono<Void> deleteRule(String ruleName) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RULE_MANAGER, "getRules")
            ));
        }

        if (ruleName == null) {
            return monoError(LOGGER, new NullPointerException("'ruleName' cannot be null."));
        } else if (ruleName.isEmpty()) {
            return monoError(LOGGER, new IllegalArgumentException("'ruleName' cannot be an empty string."));
        }

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(managementNode -> managementNode.deleteRule(ruleName));
    }

    /**
     * Disposes of the {@link ServiceBusRuleManagerAsyncClient}. If the client has a dedicated connection, the underlying
     * connection is also closed.
     */
    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        onClientClose.run();
    }

    private Mono<Void> createRuleInternal(String name, CreateRuleOptions options) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RULE_MANAGER, "addRule")
            ));
        }

        if (name == null) {
            return monoError(LOGGER, new NullPointerException("'name' cannot be null."));
        } else if (name.isEmpty()) {
            return monoError(LOGGER, new IllegalArgumentException("'name' cannot be an empty string."));
        }

        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(managementNode -> managementNode.createRule(name, options));
    }
}
