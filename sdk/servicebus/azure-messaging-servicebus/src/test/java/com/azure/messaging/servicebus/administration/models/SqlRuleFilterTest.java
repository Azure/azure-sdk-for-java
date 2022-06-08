// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link SqlRuleFilter}.
 */
class SqlRuleFilterTest {
    @Test
    void testConstructor() {
        // Arrange
        final String expectedToString = "SqlRuleFilter: some-expression";
        final String expectedSqlExpression = "some-expression";

        // Act
        final SqlRuleFilter actual = new SqlRuleFilter("some-expression");

        // Assert
        assertEquals(expectedSqlExpression, actual.getSqlExpression());
        assert (actual.getParameters().isEmpty());
        assertNull(actual.getCompatibilityLevel());
        assertNull(actual.isPreprocessingRequired());

        final String toString = actual.toString();
        assertNotNull(toString);
        assertEquals(expectedToString, toString);
    }

    @Test
    void testEqualsAndHashCode() {
        final SqlRuleFilter someFilter = new SqlRuleFilter("some-expression");
        final SqlRuleFilter otherFilter = new SqlRuleFilter("other-expression");
        assertNotEquals(someFilter, otherFilter);
        assertNotEquals(someFilter.hashCode(), otherFilter.hashCode());
        final SqlRuleFilter similarFilter = new SqlRuleFilter("some-expression");
        assertEquals(someFilter, similarFilter);
        assertEquals(someFilter.hashCode(), similarFilter.hashCode());

        final SqlRuleFilter filter = new SqlRuleFilter("some-expression",
            "some-compatibility-level", true);

        assertNotEquals(filter, someFilter);
        assertEquals(filter.hashCode(), someFilter.hashCode());

        final SqlRuleFilter differentCompatibilityFilter = new SqlRuleFilter("some-expression",
            "other-compatibility-level", true);
        assertNotEquals(filter, differentCompatibilityFilter);
        assertEquals(filter.hashCode(), differentCompatibilityFilter.hashCode());

        final SqlRuleFilter differentPreprocessingFilter = new SqlRuleFilter("some-expression",
            "some-compatibility-level", false);
        assertNotEquals(filter, differentPreprocessingFilter);
        assertEquals(filter.hashCode(), differentPreprocessingFilter.hashCode());

        final SqlRuleFilter allSameFilter = new SqlRuleFilter("some-expression",
            "some-compatibility-level", true);
        assertEquals(filter, allSameFilter);
    }

    @Test
    void testEqualsAndHashCodeTrueAndFalseFilters() {
        final TrueRuleFilter trueRuleFilter = new TrueRuleFilter();
        final TrueRuleFilter otherTrueRuleFilter = new TrueRuleFilter();
        assertEquals(trueRuleFilter, otherTrueRuleFilter);
        assertEquals(trueRuleFilter.hashCode(), otherTrueRuleFilter.hashCode());

        final FalseRuleFilter falseRuleFilter = new FalseRuleFilter();
        final FalseRuleFilter otherFalseRuleFilter = new FalseRuleFilter();
        assertEquals(falseRuleFilter, otherFalseRuleFilter);
        assertEquals(falseRuleFilter.hashCode(), otherFalseRuleFilter.hashCode());

        assertNotEquals(trueRuleFilter.hashCode(), falseRuleFilter.hashCode());
    }
}
