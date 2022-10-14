// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MessagesTest {

    public static Stream<Field> keys() {
        return Arrays.stream(Messages.class.getDeclaredFields()).filter(field -> {
            final int modifiers = field.getModifiers();
            if (Modifier.isPrivate(modifiers)) {
                return false;
            }

            return Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers);
        });
    }

    @ParameterizedTest
    @MethodSource("keys")
    public void getMessage(Field field) {
        final String fieldName = field.getName();
        assertNotEquals(fieldName, Messages.getMessage(fieldName));
    }

    @ParameterizedTest
    @MethodSource("keys")
    public void messageField(Field field) throws IllegalAccessException {
        final String fieldName = field.getName();
        assertEquals(Messages.getMessage(fieldName), Objects.toString(field.get(Messages.class)));
    }
}
