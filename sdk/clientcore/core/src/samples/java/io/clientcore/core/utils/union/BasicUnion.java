// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils.union;

import io.clientcore.core.utils.Union;

// This is a simple example of how to use the Union type. It allows for multiple types to be stored in a single
// property, and provides methods to consume the value based on its type.
public class BasicUnion {
    public static void main(String[] args) {
        Union union = Union.ofTypes(String.class, Integer.class, Double.class);

        // This union allows for String, Integer, and Double types
        union.setValue("Hello");

        // we can (attempt to) exhaustively consume the union using a switch statement...
        handleUnion(union);

        // ... or we can pass in lambda expressions to consume the union for the types we care about
        union.tryConsume(v -> System.out.println("String value from lambda: " + v), String.class);

        // ... or we can just get the value to the type we expect it to be using the getValue methods
        String value = union.getValue();
        System.out.println("Value (from getValue()): " + value);

        value = union.getValue(String.class);
        System.out.println("Value (from getValue(Class<?> cls)): " + value);

        // Of course, this union supports Integer and Double types as well:
        union.setValue(123);
        handleUnion(union);
        union.setValue(3.14);
        handleUnion(union);

        // This will throw an IllegalArgumentException, as the union does not support the type Long
        try {
            union.setValue(123L);
        } catch (IllegalArgumentException e) {
            System.out.println("Caught exception: " + e.getMessage());
        }
    }

    private static void handleUnion(Union union) {
        // we can write an if-else block to consume the value in Java 8+, or switch pattern match in Java 17+
        Object value = union.getValue();
        if (value instanceof String) {
            String s = (String) value;
            System.out.println("String value from if-else: " + s);
        } else if (value instanceof Integer) {
            Integer i = (Integer) value;
            System.out.println("Integer value from if-else: " + i);
        } else if (value instanceof Double) {
            Double d = (Double) value;
            System.out.println("Double value from if-else: " + d);
        } else {
            throw new IllegalArgumentException("Unknown type: " + union.getCurrentType().getTypeName());
        }
    }
}
