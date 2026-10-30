// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.administration;

import com.azure.messaging.servicebus.administration.implementation.EntityHelper;
import com.azure.messaging.servicebus.administration.implementation.models.TopicDescription;
import com.azure.messaging.servicebus.administration.models.TopicProperties;
import com.azure.messaging.servicebus.administration.models.TopicRuntimeProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the topic filter-count runtime properties on {@link TopicRuntimeProperties}.
 */
public class TopicRuntimePropertiesTest {
    /**
     * The SqlFilterCount / CorrelationFilterCount parsed onto the description flow through to the
     * public {@link TopicRuntimeProperties}.
     */
    @Test
    void filterCountsPropagateFromDescription() {
        final TopicDescription description
            = new TopicDescription().setSubscriptionCount(2).setSqlFilterCount(7).setCorrelationFilterCount(9);

        final TopicProperties properties = EntityHelper.toModel(description);
        final TopicRuntimeProperties runtimeProperties = new TopicRuntimeProperties(properties);

        assertEquals(2, runtimeProperties.getSubscriptionCount());
        assertEquals(7, runtimeProperties.getSqlFilterCount());
        assertEquals(9, runtimeProperties.getCorrelationFilterCount());
    }

    /**
     * A service region that has not yet deployed the topic filter-count feature omits the elements
     * entirely; the counts must gracefully default to zero rather than throw.
     */
    @Test
    void filterCountsDefaultToZeroWhenAbsent() {
        final TopicDescription description = new TopicDescription().setSubscriptionCount(1);

        final TopicProperties properties = EntityHelper.toModel(description);
        final TopicRuntimeProperties runtimeProperties = new TopicRuntimeProperties(properties);

        assertEquals(1, runtimeProperties.getSubscriptionCount());
        assertEquals(0, runtimeProperties.getSqlFilterCount());
        assertEquals(0, runtimeProperties.getCorrelationFilterCount());
    }
}
