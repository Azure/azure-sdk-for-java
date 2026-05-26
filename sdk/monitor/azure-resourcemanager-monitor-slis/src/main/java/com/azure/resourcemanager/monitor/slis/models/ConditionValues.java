// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.slis.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Utility helpers for the {@link ConditionOperator#IN} / {@link ConditionOperator#NOT_IN}
 * operators, which the SLI resource provider expects as a {@code ^^}-delimited string in the wire
 * {@link Condition#value()} field.
 *
 * <p>The {@link Condition} class is generated and marked {@code final}, so these helpers live in a
 * sibling utility class instead of as instance methods.</p>
 */
public final class ConditionValues {

    /**
     * Literal delimiter the SLI resource provider expects between list items for the
     * {@link ConditionOperator#IN} / {@link ConditionOperator#NOT_IN} operators.
     */
    public static final String IN_VALUE_SEPARATOR = "^^";

    private ConditionValues() {
        // Utility class; not instantiable.
    }

    /**
     * Returns the list of items encoded into {@link Condition#value()} for the
     * {@link ConditionOperator#IN} / {@link ConditionOperator#NOT_IN} operators by splitting on
     * the literal {@code ^^} separator. Returns an empty, unmodifiable list when the value is
     * {@code null}.
     *
     * @param condition the condition to read.
     * @return the list of values, or an empty list when {@link Condition#value()} is {@code null}.
     * @throws NullPointerException if {@code condition} is {@code null}.
     */
    public static List<String> values(Condition condition) {
        Objects.requireNonNull(condition, "condition");
        String raw = condition.value();
        if (raw == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(raw.split(java.util.regex.Pattern.quote(IN_VALUE_SEPARATOR), -1));
    }

    /**
     * Sets {@link Condition#value()} by joining {@code values} with the literal {@code ^^}
     * separator used on the wire for the {@link ConditionOperator#IN} / {@link ConditionOperator#NOT_IN}
     * operators. Passing {@code null} clears the value; passing an empty list sets it to an empty
     * string.
     *
     * @param condition the condition to mutate.
     * @param values the values to encode.
     * @return the same {@link Condition} instance for chaining.
     * @throws NullPointerException if {@code condition} is {@code null}.
     */
    public static Condition withValues(Condition condition, List<String> values) {
        Objects.requireNonNull(condition, "condition");
        if (values == null) {
            return condition.withValue(null);
        }
        return condition.withValue(String.join(IN_VALUE_SEPARATOR, values));
    }

    /**
     * Builds a new {@link Condition} for a list operator ({@link ConditionOperator#IN} or
     * {@link ConditionOperator#NOT_IN}) by joining {@code values} with the wire {@code ^^}
     * separator.
     *
     * @param operator must be {@link ConditionOperator#IN} or {@link ConditionOperator#NOT_IN}.
     * @param values items to encode; must contain at least one item and no item may contain the
     *               reserved {@code ^^} separator.
     * @return the populated {@link Condition}.
     * @throws NullPointerException if {@code operator} or {@code values} is {@code null}.
     * @throws IllegalArgumentException if {@code operator} is not a list operator, {@code values}
     *                                  is empty, or any item contains the reserved {@code ^^}
     *                                  separator.
     */
    public static Condition forListOperator(ConditionOperator operator, List<String> values) {
        Objects.requireNonNull(operator, "operator");
        Objects.requireNonNull(values, "values");
        if (!ConditionOperator.IN.equals(operator) && !ConditionOperator.NOT_IN.equals(operator)) {
            throw new IllegalArgumentException(
                "operator must be ConditionOperator.IN or ConditionOperator.NOT_IN; got '" + operator + "'.");
        }
        if (values.isEmpty()) {
            throw new IllegalArgumentException("At least one value is required for list operators.");
        }
        for (int i = 0; i < values.size(); i++) {
            String item = values.get(i);
            if (item != null && item.contains(IN_VALUE_SEPARATOR)) {
                throw new IllegalArgumentException(
                    "Value at index " + i + " contains the reserved '" + IN_VALUE_SEPARATOR + "' separator.");
            }
        }
        return new Condition().withOperator(operator).withValue(String.join(IN_VALUE_SEPARATOR, values));
    }
}
