package com.azure.messaging.servicebus.administration.models;

import com.azure.messaging.servicebus.ServiceBusMessage;

/**
 * Describes a filter expression that is evaluated against a {@link ServiceBusMessage}. Filter is an abstract class with
 * the following concrete implementations:
 * <ul>
 *     <li>{@link SqlRuleFilter} that represents a filter using SQL syntax.</li>
 *     <li>{@link CorrelationRuleFilter} that provides an optimisation for correlation equality expressions.</li>
 * </ul>
 */
public class RuleFilter {
}
