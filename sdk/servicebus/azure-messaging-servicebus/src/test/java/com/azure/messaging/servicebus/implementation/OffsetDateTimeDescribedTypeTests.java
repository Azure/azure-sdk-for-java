// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import org.apache.qpid.proton.amqp.DescribedType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.OFFSETDATETIME_SYMBOL;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for OffsetDateTimeDescribedType
 */
public class OffsetDateTimeDescribedTypeTests {
    @Test
    public void fromJavaClassToAmqp() {
        // Arrange
        // DateTimeOffset represented 2024-11-15T23:26:02.5931825+00:00
        final long expectedTicks = 638673099625931825L;
        final OffsetDateTime input = OffsetDateTime.of(2024, 11, 15, 23, 26, 2, 593182500, ZoneOffset.UTC);
        final int expectedLength = OFFSETDATETIME_SYMBOL.length() + Long.BYTES;

        // Act
        final OffsetDateTimeDescribedType actualType = new OffsetDateTimeDescribedType(input);

        // Assert
        Assertions.assertEquals(OFFSETDATETIME_SYMBOL, actualType.getDescriptor());

        Assertions.assertInstanceOf(Long.class, actualType.getDescribed());

        final Long actual = (Long) actualType.getDescribed();
        Assertions.assertEquals(expectedTicks, actual);

        Assertions.assertEquals(expectedLength, actualType.size());
    }

    @Test
    public void fromAmqpToJavaClass() {
        // Arrange
        // DateTimeOffset represented is: 2024-11-16T00:06:39.6292674+00:00
        // Number of ticks for this .NET object is: 638673123996292674
        final Long amqpValue = 638673123996292674L;
        final OffsetDateTime expectedValue = OffsetDateTime.of(2024, 11, 16, 0, 6, 39, 629267400, ZoneOffset.UTC);

        final DescribedType describedType = mock(DescribedType.class);
        when(describedType.getDescriptor()).thenReturn(OFFSETDATETIME_SYMBOL);
        when(describedType.getDescribed()).thenReturn(amqpValue);

        // Act
        final OffsetDateTime actual = MessageUtils.describedToOrigin(describedType);

        // Assert
        Assertions.assertEquals(expectedValue, actual);
    }
}
