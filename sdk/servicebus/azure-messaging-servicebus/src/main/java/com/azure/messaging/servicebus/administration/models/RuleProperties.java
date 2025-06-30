// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import com.azure.core.annotation.Fluent;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationAsyncClient;
import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient;
import com.azure.messaging.servicebus.administration.implementation.EntityHelper;
import com.azure.messaging.servicebus.administration.implementation.models.CorrelationFilter;
import com.azure.messaging.servicebus.administration.implementation.models.FalseFilter;
import com.azure.messaging.servicebus.administration.implementation.models.KeyValue;
import com.azure.messaging.servicebus.administration.implementation.models.RuleDescription;
import com.azure.messaging.servicebus.administration.implementation.models.SqlFilter;
import com.azure.messaging.servicebus.administration.implementation.models.TrueFilter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Properties on a rule.
 *
 * @see ServiceBusAdministrationAsyncClient#getRule(String, String, String)
 * @see ServiceBusAdministrationClient#getRule(String, String, String)
 */
@Fluent
public class RuleProperties {
    private final String name;
    private RuleFilter filter;
    private RuleAction action;

    static {
        EntityHelper.setRuleAccessor(new EntityHelper.RuleAccessor() {
            private final com.azure.messaging.servicebus.administration.implementation.models.EmptyRuleAction emptyRuleAction
                = new com.azure.messaging.servicebus.administration.implementation.models.EmptyRuleAction();
            private final SqlFilter trueFilter = new TrueFilter().setSqlExpression("1=1");
            private final SqlFilter falseFilter = new FalseFilter().setSqlExpression("1=0");

            @Override
            public RuleProperties toModel(RuleDescription description) {
                final RuleFilter filter = description.getFilter() != null ? toModel(description.getFilter()) : null;
                final RuleAction action = description.getAction() != null ? toModel(description.getAction()) : null;

                return new RuleProperties(description.getName(), filter, action);
            }

            @Override
            public RuleAction
                toModel(com.azure.messaging.servicebus.administration.implementation.models.RuleAction implementation) {
                if (implementation instanceof com.azure.messaging.servicebus.administration.implementation.models.EmptyRuleAction) {
                    return EmptyRuleAction.getInstance();
                } else if (implementation instanceof com.azure.messaging.servicebus.administration.implementation.models.SqlRuleAction) {
                    final com.azure.messaging.servicebus.administration.implementation.models.SqlRuleAction action
                        = (com.azure.messaging.servicebus.administration.implementation.models.SqlRuleAction) implementation;
                    final SqlRuleAction returned = new SqlRuleAction(action.getSqlExpression(),
                        action.getCompatibilityLevel(), action.isRequiresPreprocessing());

                    if (action.getParameters() != null) {
                        for (KeyValue parameter : action.getParameters()) {
                            returned.getParameters().put(parameter.getKey(), parameter.getValue());
                        }
                    }

                    return returned;
                } else {
                    return null;
                }
            }

            @Override
            public RuleFilter
                toModel(com.azure.messaging.servicebus.administration.implementation.models.RuleFilter implementation) {
                if (implementation instanceof TrueFilter) {
                    return TrueRuleFilter.getInstance();
                } else if (implementation instanceof FalseFilter) {
                    return FalseRuleFilter.getInstance();
                } else if (implementation instanceof CorrelationFilter) {
                    final CorrelationFilter filter = (CorrelationFilter) implementation;
                    final CorrelationRuleFilter returned
                        = new CorrelationRuleFilter().setContentType(filter.getContentType())
                            .setCorrelationId(filter.getCorrelationId())
                            .setLabel(filter.getLabel())
                            .setMessageId(filter.getMessageId())
                            .setTo(filter.getTo())
                            .setSessionId(filter.getSessionId())
                            .setReplyTo(filter.getReplyTo())
                            .setReplyToSessionId(filter.getReplyToSessionId());

                    if (filter.getProperties() != null) {
                        filter.getProperties()
                            .forEach(keyValue -> returned.getProperties().put(keyValue.getKey(), keyValue.getValue()));
                    }

                    return returned;
                } else if (implementation instanceof SqlFilter) {
                    final SqlFilter filter = (SqlFilter) implementation;
                    final SqlRuleFilter returned = new SqlRuleFilter(filter.getSqlExpression(),
                        filter.getCompatibilityLevel(), filter.isRequiresPreprocessing());

                    if (filter.getParameters() != null) {
                        filter.getParameters()
                            .forEach(keyValue -> returned.getParameters().put(keyValue.getKey(), keyValue.getValue()));
                    }

                    return returned;
                } else {
                    return null;
                }
            }

            @Override
            public RuleDescription toImplementation(RuleProperties ruleProperties) {
                final com.azure.messaging.servicebus.administration.implementation.models.RuleFilter filter
                    = ruleProperties.getFilter() != null ? toImplementation(ruleProperties.getFilter()) : null;
                final com.azure.messaging.servicebus.administration.implementation.models.RuleAction action
                    = ruleProperties.getAction() != null ? toImplementation(ruleProperties.getAction()) : null;

                return new RuleDescription().setName(ruleProperties.getName()).setAction(action).setFilter(filter);
            }

            @Override
            public com.azure.messaging.servicebus.administration.implementation.models.RuleAction
                toImplementation(RuleAction model) {
                if (model instanceof EmptyRuleAction) {
                    return emptyRuleAction;
                } else if (model instanceof SqlRuleAction) {
                    final SqlRuleAction action = (SqlRuleAction) model;
                    final com.azure.messaging.servicebus.administration.implementation.models.SqlRuleAction returned
                        = new com.azure.messaging.servicebus.administration.implementation.models.SqlRuleAction()
                            .setSqlExpression(action.getSqlExpression())
                            .setCompatibilityLevel(action.getCompatibilityLevel())
                            .setRequiresPreprocessing(action.isPreprocessingRequired());

                    if (!action.getParameters().isEmpty()) {
                        final List<KeyValue> parameters = action.getParameters()
                            .entrySet()
                            .stream()
                            .map(entry -> new KeyValue().setKey(entry.getKey()).setValue(entry.getValue().toString()))
                            .collect(Collectors.toList());

                        returned.setParameters(parameters);
                    }

                    return returned;
                } else {
                    return null;
                }
            }

            @Override
            public com.azure.messaging.servicebus.administration.implementation.models.RuleFilter
                toImplementation(RuleFilter model) {
                if (model instanceof TrueRuleFilter) {
                    return trueFilter;
                } else if (model instanceof FalseRuleFilter) {
                    return falseFilter;
                } else if (model instanceof CorrelationRuleFilter) {
                    final CorrelationRuleFilter filter = (CorrelationRuleFilter) model;
                    final CorrelationFilter returned = new CorrelationFilter().setContentType(filter.getContentType())
                        .setCorrelationId(filter.getCorrelationId())
                        .setLabel(filter.getLabel())
                        .setMessageId(filter.getMessageId())
                        .setTo(filter.getTo())
                        .setSessionId(filter.getSessionId())
                        .setReplyTo(filter.getReplyTo())
                        .setReplyToSessionId(filter.getReplyToSessionId());

                    if (!filter.getProperties().isEmpty()) {
                        final List<KeyValue> parameters = filter.getProperties()
                            .entrySet()
                            .stream()
                            .map(entry -> new KeyValue().setKey(entry.getKey()).setValue(entry.getValue().toString()))
                            .collect(Collectors.toList());

                        returned.setProperties(parameters);
                    }

                    return returned;
                } else if (model instanceof SqlRuleFilter) {
                    final SqlRuleFilter filter = (SqlRuleFilter) model;
                    final SqlFilter returned = new SqlFilter().setSqlExpression(filter.getSqlExpression())
                        .setCompatibilityLevel(filter.getCompatibilityLevel())
                        .setRequiresPreprocessing(filter.isPreprocessingRequired());

                    if (!filter.getParameters().isEmpty()) {
                        final List<KeyValue> parameters = filter.getParameters()
                            .entrySet()
                            .stream()
                            .map(entry -> new KeyValue().setKey(entry.getKey()).setValue(entry.getValue().toString()))
                            .collect(Collectors.toList());

                        returned.setParameters(parameters);
                    }

                    return returned;
                } else {
                    return null;
                }
            }
        });
    }

    /**
     * Initializes a new instance with the given rule {@code name}, {@code filter}, and {@code action}.
     *
     * @param name Name of the rule.
     * @param filter Filter for the rule.
     * @param action Action for the rule.
     */
    RuleProperties(String name, RuleFilter filter, RuleAction action) {
        this.name = name;
        this.filter = filter;
        this.action = action;
    }

    /**
     * Gets the filter expression used to match messages.
     *
     * @return The filter expression used to match messages.
     */
    public RuleFilter getFilter() {
        return filter;
    }

    /**
     * Sets the filter expression used to match messages.
     *
     * @param filter the filter expression used to match messages.
     *
     * @return The updated {@link RuleProperties} object itself.
     * @throws NullPointerException if {@code filter} is null.
     */
    public RuleProperties setFilter(RuleFilter filter) {
        this.filter = Objects.requireNonNull(filter, "'filter' cannot be null.");
        return this;
    }

    /**
     * Gets the name of the rule.
     *
     * @return The name of the rule.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the action to perform if the message satisfies the filtering expression.
     *
     * @return The action to perform if the message satisfies the filtering expression.
     */
    public RuleAction getAction() {
        return action;
    }

    /**
     * Sets the action to perform if the message satisfies the filtering expression.
     *
     * @param action The action to perform if the message satisfies the filtering expression.
     *
     * @return The updated {@link RuleProperties} object itself.
     */
    public RuleProperties setAction(RuleAction action) {
        this.action = action;
        return this;
    }
}
