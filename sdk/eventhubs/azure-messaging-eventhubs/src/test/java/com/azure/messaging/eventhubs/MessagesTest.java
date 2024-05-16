// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MessagesTest {

    static Stream<String> keys() {
        return Stream.of(Messages.class.getFields()).map(Field::getName);
    }

    @ParameterizedTest
    @MethodSource("keys")
    void getMessage(String messageKey) {
        assertNotEquals(messageKey, Messages.getMessage(messageKey));
    }

    @ParameterizedTest
    @MethodSource("keys")
    void messageField(String messageKey) throws NoSuchFieldException, IllegalAccessException {
        Field field = Messages.class.getField(messageKey);
        field.setAccessible(true);
        assertEquals(Messages.getMessage(messageKey), Objects.toString(field.get(Messages.class)));
    }
}
