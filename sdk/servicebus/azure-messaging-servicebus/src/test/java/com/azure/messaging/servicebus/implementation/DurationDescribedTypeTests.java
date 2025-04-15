// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus.implementation;

import org.apache.qpid.proton.amqp.DescribedType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.DURATION_SYMBOL;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DurationDescribedType}
 */
public class DurationDescribedTypeTests {

    @Test
    public void fromJavaClassToAmqp() {
        // Arrange
        // var timeSpan = new TimeSpan(0, 25, 10, 12, 450);
        final Duration duration = Duration.ofHours(25).plusMinutes(10).plusSeconds(12).plusMillis(450);
        final Long expectedTicks = 906124500000L;
        final int expectedLength = DURATION_SYMBOL.length() + Long.BYTES;

        // Act
        final DurationDescribedType actual = new DurationDescribedType(duration);

        // Assert
        Assertions.assertEquals(DURATION_SYMBOL, actual.getDescriptor());

        final Long actualTick = (Long) actual.getDescribed();
        Assertions.assertEquals(expectedTicks, actualTick);

        Assertions.assertEquals(expectedLength, actual.size());
    }

    @Test
    public void fromAmqpToJavaClass() {
        // Arrange
        // var timeSpan = new TimeSpan(0, 3, 20, 35, 600);
        // 3 hours, 20 minutes, 35 seconds, 600 milliseconds.
        final long amqpValueTicks = 120356000000L;
        final Duration expectedDuration = Duration.ofHours(3).plusMinutes(20).plusSeconds(35).plusMillis(600);
        final DescribedType describedType = mock(DescribedType.class);
        when(describedType.getDescriptor()).thenReturn(DURATION_SYMBOL);
        when(describedType.getDescribed()).thenReturn(amqpValueTicks);

        // Act
        final Duration actual = MessageUtils.describedToOrigin(describedType);

        // Assert
        Assertions.assertEquals(expectedDuration, actual);
    }
}
