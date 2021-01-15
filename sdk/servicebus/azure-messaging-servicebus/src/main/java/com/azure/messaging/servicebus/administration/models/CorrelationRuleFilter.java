// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.ServiceBusMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the correlation rule filter expression. It holds a set of conditions that are matched against one or more
 * of an arriving message's user and system properties. A common use case is to match against the message's:
 * <ul>
 *     <li>{@link ServiceBusMessage#getCorrelationId() correlation id}</li>
 *     <li>{@link ServiceBusMessage#getContentType() content type}</li>
 *     <li>{@link ServiceBusMessage#getSubject() subject}</li>
 *     <li>{@link ServiceBusMessage#getMessageId() message id}</li>
 *     <li>{@link ServiceBusMessage#getReplyTo() reply-to}</li>
 *     <li>{@link ServiceBusMessage#getReplyToSessionId() reply-to session id}</li>
 *     <li>{@link ServiceBusMessage#getSessionId() session id}</li>
 *     <li>{@link ServiceBusMessage#getTo() to}</li>
 *     <li>or, any {@link ServiceBusMessage#getApplicationProperties() user-defined properties}</li>
 * </ul>
 * <p>
 * A match exists when an arriving message's value for a property is equal to the value specified in the correlation
 * filter. For string expressions, the comparison is case-sensitive. When specifying multiple match properties, the
 * filter combines them as a logical <code>AND</code> condition, meaning all conditions must match for the filter to
 * match.
 * <p>
 * This provides an efficient shortcut for declarations of filters that deal only with correlation
 * equality. In this case the cost of the lexigraphical analysis of the expression can be avoided. Not only will
 * correlation filters be optimized at declaration time, but they will also be optimized at runtime. Correlation filter
 * matching can be reduced to a hashtable lookup, which aggregates the complexity of the set of defined correlation
 * filters to <code>O(1)</code>.
 *
 * @see CreateRuleOptions#setFilter(RuleFilter)
 * @see RuleProperties#setFilter(RuleFilter)
 */
@Fluent
public class CorrelationRuleFilter extends RuleFilter {
    private final Map<String, Object> properties = new HashMap<>();
    private String correlationId;
    private String contentType;
    private String label;
    private String messageId;
    private String replyTo;
    private String replyToSessionId;
    private String sessionId;
    private String to;

    /**
     * Initializes a new instance of {@link CorrelationRuleFilter} with default values.
     */
    public CorrelationRuleFilter() {
        this.correlationId = null;
    }

    /**
     * Initializes a new instance of {@link CorrelationRuleFilter} with default values with the specified correlation
     * identifier.
     *
     * @param correlationId The identifier for the correlation.
     *
     * @throws IllegalArgumentException If {@code correlationId} is an empty string.
     * @throws NullPointerException If {@code correlationId} is null.
     */
    public CorrelationRuleFilter(String correlationId) {
        final ClientLogger logger = new ClientLogger(CorrelationRuleFilter.class);
        if (correlationId == null) {
            throw logger.logExceptionAsError(new NullPointerException("'correlationId' cannot be null"));
        } else if (correlationId.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'correlationId' cannot be empty."));
        }

        this.correlationId = correlationId;
    }

    /**
     * Gets the content type of the message.
     *
     * @return The content type of the message.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type of the message.
     *
     * @param contentType The content type of the message.
     *
     * @return The updated {@link CorrelationRuleFilter} itself.
     */
    public CorrelationRuleFilter setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Gets the correlation identifier.
     *
     * @return The correlation identifier.
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Sets the correlation identifier.
     *
     * @param correlationId The correlation identifier.
     *
     * @return The updated {@link CorrelationRuleFilter} itself.
     */
    public CorrelationRuleFilter setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    /**
     * Gets the application specific label.
     *
     * @return The application specific label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the application specific label.
     *
     * @param label The application specific label.
     *
     * @return The updated {@link CorrelationRuleFilter} itself.
     */
    public CorrelationRuleFilter setLabel(String label) {
        this.label = label;
        return this;
    }

    /**
     * Gets the identifier for the message.
     *
     * @return The identifier for the message.
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the identifier for the message.
     *
     * @param messageId The identifier for the message.
     *
     * @return The updated {@link CorrelationRuleFilter} itself.
     */
    public CorrelationRuleFilter setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    /**
     * Gets application specific properties of the message.
     *
     * @return The application specific properties of the message.
     */
    public Map<String, Object> getProperties() {
        return properties;
    }

    /**
     * Gets the address of the queue or subscription to reply to.
     *
     * @return The address of the queue or subscription to reply to.
     */
    public String getReplyTo() {
        return replyTo;
    }

    /**
     * Sets the address of the queue or subscription to reply to.
     *
     * @param replyTo The address of the queue or subscription to reply to.
     *
     * @return The updated {@link CorrelationRuleFilter} itself.
     */
    public CorrelationRuleFilter setReplyTo(String replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    /**
     * Gets the session identifier to reply to.
     *
     * @return The session identifier to reply to.
     */
    public String getReplyToSessionId() {
        return replyToSessionId;
    }

    /**
     * Sets the session identifier to reply to. Max size of {@code replyToSessionId} is 128.
     *
     * @param replyToSessionId The session identifier to reply to.
     *
     * @return The updated {@link CorrelationRuleFilter} itself.
     */
    public CorrelationRuleFilter setReplyToSessionId(String replyToSessionId) {
        this.replyToSessionId = replyToSessionId;
        return this;
    }

    /**
     * Gets the session identifier.
     *
     * @return The session identifier.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Sets the session identifier. Max size of {@code sessionId} is 128 chars.
     *
     * @param sessionId The session identifier.
     *
     * @return The updated {@link CorrelationRuleFilter} itself.
     */
    public CorrelationRuleFilter setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    /**
     * Gets the address to send to.
     *
     * @return The address to send to.
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets the address to send to.
     *
     * @param to The address to send to.
     *
     * @return The updated {@link CorrelationRuleFilter} itself.
     */
    public CorrelationRuleFilter setTo(String to) {
        this.to = to;
        return this;
    }

    /**
     * Converts the value of the current instance to its equivalent string representation.
     *
     * @return A string representation of the current instance.
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder("CorrelationRuleFilter: ");

        boolean isFirstExpression = appendPropertyExpression(true, builder, "sys.CorrelationId",
            correlationId);
        isFirstExpression = appendPropertyExpression(isFirstExpression, builder, "sys.MessageId",
            messageId);
        isFirstExpression = appendPropertyExpression(isFirstExpression, builder, "sys.To", to);
        isFirstExpression = appendPropertyExpression(isFirstExpression, builder, "sys.ReplyTo", replyTo);
        isFirstExpression = appendPropertyExpression(isFirstExpression, builder, "sys.Label", label);
        isFirstExpression = appendPropertyExpression(isFirstExpression, builder, "sys.SessionId", sessionId);
        isFirstExpression = appendPropertyExpression(isFirstExpression, builder, "sys.ReplyToSessionId",
            replyToSessionId);
        isFirstExpression = appendPropertyExpression(isFirstExpression, builder, "sys.ContentType",
            contentType);

        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            isFirstExpression = appendPropertyExpression(isFirstExpression, builder, entry.getKey(),
                entry.getValue().toString());
        }

        return builder.toString();
    }

    private static boolean appendPropertyExpression(boolean isFirstExpression, StringBuilder builder, String display,
        String value) {

        if (value == null) {
            return true;
        }

        if (!isFirstExpression) {
            builder.append(" AND ");
        }

        builder.append(String.format("%s = '%s'", display, value));
        return false;
    }
}
