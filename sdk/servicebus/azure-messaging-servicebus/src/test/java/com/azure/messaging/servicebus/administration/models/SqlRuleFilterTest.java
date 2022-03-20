package com.azure.messaging.servicebus.administration.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SqlRuleFilterTest {

    @Test
    void testEqualsAndHashCode() {
        SqlRuleFilter filter1 = new SqlRuleFilter("key=value");
        SqlRuleFilter filter2 = new SqlRuleFilter("key=value");
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

        filter1 = new SqlRuleFilter("key=value2");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2 = new SqlRuleFilter("key=value3");
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2 = new SqlRuleFilter("key=value2");
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

        filter1 = new SqlRuleFilter("key=value2", "myCompat", false);
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2 = new SqlRuleFilter("key=value2", "theirCompat", false);
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2 = new SqlRuleFilter("key=value2", "myCompat", false);
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

        filter1 = new SqlRuleFilter("key=value2", "myCompat", true);
        assertNotEquals(filter1, filter2);
        assertNotEquals(filter1.hashCode(), filter2.hashCode());
        filter2 = new SqlRuleFilter("key=value2", "myCompat", true);
        assertEquals(filter1, filter2);
        assertEquals(filter1.hashCode(), filter2.hashCode());

    }

    @Test
    void testEqualsAndHashCodeTrueAndFalseFilters() {
        TrueRuleFilter trueRuleFilter = new TrueRuleFilter();
        TrueRuleFilter anotherTrueRuleFilter = new TrueRuleFilter();
        assertEquals(trueRuleFilter, anotherTrueRuleFilter);
        assertEquals(trueRuleFilter.hashCode(), anotherTrueRuleFilter.hashCode());
        FalseRuleFilter falseRuleFilter = new FalseRuleFilter();
        FalseRuleFilter anotherFalseRuleFilter = new FalseRuleFilter();
        assertEquals(falseRuleFilter, anotherFalseRuleFilter);
        assertEquals(falseRuleFilter.hashCode(), anotherFalseRuleFilter.hashCode());

        assertNotEquals(trueRuleFilter, falseRuleFilter);
        assertNotEquals(trueRuleFilter.hashCode(), falseRuleFilter.hashCode());
    }
}
