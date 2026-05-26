// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.monitor.slis;

import com.azure.resourcemanager.monitor.slis.models.Condition;
import com.azure.resourcemanager.monitor.slis.models.ConditionOperator;
import com.azure.resourcemanager.monitor.slis.models.ConditionValues;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class ConditionValuesTests {

    @Test
    public void valuesRoundTripThroughWireValue() {
        Condition c = new Condition().withOperator(ConditionOperator.IN).withValue("east^^west^^north");
        Assertions.assertEquals(Arrays.asList("east", "west", "north"), ConditionValues.values(c));
    }

    @Test
    public void withValuesJoinsWithSeparator() {
        Condition c = new Condition().withOperator(ConditionOperator.IN);
        ConditionValues.withValues(c, Arrays.asList("east", "west", "north"));
        Assertions.assertEquals("east^^west^^north", c.value());
    }

    @Test
    public void withValuesNullClearsValue() {
        Condition c = new Condition().withOperator(ConditionOperator.IN).withValue("east^^west");
        ConditionValues.withValues(c, null);
        Assertions.assertNull(c.value());
    }

    @Test
    public void valuesEmptyWhenValueNull() {
        Condition c = new Condition().withOperator(ConditionOperator.IN);
        Assertions.assertEquals(Collections.emptyList(), ConditionValues.values(c));
    }

    @Test
    public void forListOperatorIn() {
        Condition c = ConditionValues.forListOperator(ConditionOperator.IN, Arrays.asList("east", "west"));
        Assertions.assertEquals(ConditionOperator.IN, c.operator());
        Assertions.assertEquals("east^^west", c.value());
    }

    @Test
    public void forListOperatorNotIn() {
        Condition c = ConditionValues.forListOperator(ConditionOperator.NOT_IN, Collections.singletonList("only"));
        Assertions.assertEquals(ConditionOperator.NOT_IN, c.operator());
        Assertions.assertEquals("only", c.value());
    }

    @Test
    public void forListOperatorRejectsWrongOperator() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> ConditionValues.forListOperator(ConditionOperator.EQUAL, Arrays.asList("east")));
    }

    @Test
    public void forListOperatorRejectsEmpty() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> ConditionValues.forListOperator(ConditionOperator.IN, Collections.emptyList()));
    }

    @Test
    public void forListOperatorRejectsItemContainingSeparator() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> ConditionValues.forListOperator(ConditionOperator.IN, Arrays.asList("ok", "bad^^value")));
    }
}
