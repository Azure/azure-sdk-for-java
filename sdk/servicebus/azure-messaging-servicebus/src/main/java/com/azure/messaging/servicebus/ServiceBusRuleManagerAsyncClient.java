// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.implementation.RecoverableReactorConnection;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient;
import com.azure.messaging.servicebus.administration.models.CorrelationRuleFilter;
import com.azure.messaging.servicebus.administration.models.CreateRuleOptions;
import com.azure.messaging.servicebus.administration.models.RuleProperties;
import com.azure.messaging.servicebus.administration.models.SqlRuleAction;
import com.azure.messaging.servicebus.administration.models.SqlRuleFilter;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusManagementNode;
import com.azure.messaging.servicebus.implementation.ServiceBusReactorAmqpConnection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.messaging.servicebus.implementation.Messages.INVALID_OPERATION_DISPOSED_RULE_MANAGER;

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
 * ruleManager.listRules&#40;&#41;.subscribe&#40;ruleProperties -&gt; System.out.println&#40;ruleProperties.getName&#40;&#41;&#41;&#41;;
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
    private final RecoverableReactorConnection<ServiceBusReactorAmqpConnection> recoverableConnection;
    private final Runnable onClientClose;
    private final AtomicBoolean isDisposed = new AtomicBoolean();

    /**
     * Creates a rule manager that manages rules for a Service Bus subscription.
     *
     * @param entityPath The name of the topic and subscription.
     * @param entityType The type of the Service Bus resource.
     * @param recoverableConnection The AMQP connection to the Service Bus resource.
     * @param onClientClose Operation to run when the client completes.
     */
    ServiceBusRuleManagerAsyncClient(String entityPath, MessagingEntityType entityType,
        RecoverableReactorConnection<ServiceBusReactorAmqpConnection> recoverableConnection, Runnable onClientClose) {
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.entityType = Objects.requireNonNull(entityType, "'entityType' cannot be null.");
        this.recoverableConnection = Objects.requireNonNull(recoverableConnection,
            "'recoverableConnection' cannot be null.");
        this.onClientClose = onClientClose;
    }

    /**
     * Gets the fully qualified namespace.
     *
     * @return The fully qualified namespace.
     */
    public String getFullyQualifiedNamespace() {
        return recoverableConnection.getFullyQualifiedNamespace();
    }

    /**
     * Gets the name of the Service Bus resource.
     *
     * @return The name of the Service Bus resource.
     */
    public String getEntityPath() {
        return entityPath;
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
     * @throws IllegalArgumentException if {@code ruleName} is empty string, action of {@code options} is not null and not
     * instanceof {@link SqlRuleAction}, filter of {@code options} is not instanceof {@link SqlRuleFilter} or
     * {@link CorrelationRuleFilter}.
     * @throws ServiceBusException if filter matches {@code ruleName} is already created in subscription.
     */
    public Mono<Void> createRule(String ruleName, CreateRuleOptions options) {
        if (Objects.isNull(options)) {
            return monoError(LOGGER, new NullPointerException("'options' cannot be null."));
        }
        return createRuleInternal(ruleName, options);
    }

    /**
     * Fetches all rules associated with the topic and subscription.
     *
     * @return A list of rules associated with the topic and subscription.
     *
     * @throws IllegalStateException if client is disposed.
     * @throws UnsupportedOperationException if client cannot support filter with descriptor in message body.
     */
    public Flux<RuleProperties> listRules() {
        if (isDisposed.get()) {
            return fluxError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RULE_MANAGER, "getRules")
            ));
        }

        return recoverableConnection
            .get()
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMapMany(ServiceBusManagementNode::listRules);
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
     * @throws ServiceBusException if cannot find filter matches {@code ruleName} in subscription.
     */
    public Mono<Void> deleteRule(String ruleName) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RULE_MANAGER, "deleteRule")
            ));
        }

        if (ruleName == null) {
            return monoError(LOGGER, new NullPointerException("'ruleName' cannot be null."));
        } else if (ruleName.isEmpty()) {
            return monoError(LOGGER, new IllegalArgumentException("'ruleName' cannot be an empty string."));
        }

        return recoverableConnection
            .get()
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

    private Mono<Void> createRuleInternal(String ruleName, CreateRuleOptions options) {
        if (isDisposed.get()) {
            return monoError(LOGGER, new IllegalStateException(
                String.format(INVALID_OPERATION_DISPOSED_RULE_MANAGER, "createRule")
            ));
        }

        if (ruleName == null) {
            return monoError(LOGGER, new NullPointerException("'ruleName' cannot be null."));
        } else if (ruleName.isEmpty()) {
            return monoError(LOGGER, new IllegalArgumentException("'ruleName' cannot be an empty string."));
        }

        return recoverableConnection
            .get()
            .flatMap(connection -> connection.getManagementNode(entityPath, entityType))
            .flatMap(managementNode -> managementNode.createRule(ruleName, options));
    }
}
