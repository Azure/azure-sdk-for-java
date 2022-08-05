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
 * &#47;&#47; The connectionString&#47;topicName&#47;subscriptionName must be set by the application. The 'connectionString' format is shown below.
 * &#47;&#47; &quot;Endpoint=&#123;fully-qualified-namespace&#125;;SharedAccessKeyName=&#123;policy-name&#125;;SharedAccessKey=&#123;key&#125;&quot;
 *
 * ServiceBusRuleManagerAsyncClient ruleManager = new ServiceBusClientBuilder()
 *            .connectionString(connectionString)
 *            .ruleManager()
 *            .topicName(topicName)
 *            .subscriptionName(subscriptionName)
 *            .buildAsyncClient();
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.instantiation -->
 *
 * <p><strong>Create a rule to a Service Bus subscription</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.createRule -->
 * <pre>
 * RuleFilter trueRuleFilter = new TrueRuleFilter();
 * CreateRuleOptions options = new CreateRuleOptions(trueRuleFilter);
 * ruleManager.createRule("new-rule", options).subscribe(
 *     unused -> {},
 *     err -> System.err.println("Error occurred when create a rule, err: " + err),
 *     () -> System.out.println("Create complete.")
 * );
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.createRule -->
 *
 * <p><strong>Fetch all rules.</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.getRules -->
 * <pre>
 * ruleManager.getRules().subscribe(
 *     ruleProperties -> ruleProperties.forEach(rule -> System.out.println(rule.getName())),
 *     err -> System.err.println("Error occurred when get rules, err: " + err),
 *     () -> System.out.println("Get complete.")
 * );
 * </pre>
 * <!-- end com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.getRules -->
 *
 * <p><strong>Delete a rule.</strong></p>
 * <!-- src_embed com.azure.messaging.servicebus.servicebusrulemanagerasyncclient.deleteRule -->
 * <pre>
 * ruleManager.deleteRule("exist-rule").subscribe(
 *     unused -> {},
 *     err -> System.err.println("Error occurred when delete rule, err: " + err),
 *     () -> System.out.println("Delete complete.")
 * );
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

    public ServiceBusRuleManagerAsyncClient(String entityPath, MessagingEntityType entityType,
        ServiceBusConnectionProcessor connectionProcessor, Runnable onClientClose) {
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.entityType = entityType;
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor,
            "'connectionProcessor' cannot be null.");
        this.onClientClose = onClientClose;
    }

    /**
     * Creates a rule under the given topic and subscription.
     *
     * @param name Rule name.
     * @param options Options for the rule to create.
     * @return A Mono that completes when the rule is created.
     */
    public Mono<Void> createRule(String name, CreateRuleOptions options) {
        if (Objects.isNull(options)) {
            return monoError(LOGGER, new NullPointerException("'options' cannot be null."));
        }
        return createRuleInternal(name, options);
    }

    /**
     * Creates a rule under the given topic and subscription.
     *
     * @param name Name of rule.
     * @param filter Filter for the rule to create.
     * @return A Mono that completes when the rule is created.
     */
    public Mono<Void> createRule(String name, RuleFilter filter) {
        CreateRuleOptions options = new CreateRuleOptions(filter);
        return createRuleInternal(name, options);
    }

    /**
     * Fetches all rules under the given topic and subscription.
     *
     * @return A collection of rules under the given topic and subscription.
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
     * Deletes a rule the matching {@code ruleName}.
     *
     * @param ruleName Name of rule to delete.
     * @return A Mono that completes when the rule is deleted.
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
