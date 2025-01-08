// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation;

import org.apache.qpid.proton.amqp.DescribedType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.URI_SYMBOL;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link UriDescribedType}.
 */
public class UriDescribedTypeTests {

    @Test
    public void fromJavaClassToAmqp() {
        // Arrange
        final String uriString = "https://bing.com";
        final int symbolLength = URI_SYMBOL.length();
        final int uriLength = uriString.getBytes(StandardCharsets.UTF_8).length;
        final int expectedLength = symbolLength + uriLength;

        // Act
        final UriDescribedType actual = new UriDescribedType(URI.create(uriString));

        // Assert
        Assertions.assertEquals(URI_SYMBOL, actual.getDescriptor());
        Assertions.assertEquals(expectedLength, actual.size());
    }

    @Test
    public void fromAmqpToJavaClass() {
        // Arrange
        final String uriString = "https://bing.com";
        final URI uri = URI.create(uriString);
        final DescribedType describedType = mock(DescribedType.class);
        when(describedType.getDescriptor()).thenReturn(URI_SYMBOL);
        when(describedType.getDescribed()).thenReturn(uriString);

        // Act
        final URI actual = MessageUtils.describedToOrigin(describedType);

        // Assert
        Assertions.assertEquals(uri, actual);
    }
}
