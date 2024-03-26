package com.azure.cosmos.kafka.connect.implementation.apachecommons.lang;

public class Validate {
    private Validate() {
        super();
    }

    /**
     * <p>Validate that the argument condition is {@code true}; otherwise
     * throwing an exception with the specified message.
     *
     * @param expression  the boolean expression to check
     * @param message  the exception message if invalid, not null
     * @param values  the optional values for the formatted exception message, null array not recommended
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isTrue(final boolean expression, final String message, final Object... values) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(message, values));
        }
    }

    /**
     * Validate that the specified primitive value falls between the two
     * inclusive values specified; otherwise, throws an exception with the
     * specified message.
     *
     * @param start the inclusive start value
     * @param end   the inclusive end value
     * @param value the value to validate
     * @param message the exception message if invalid, not null
     *
     * @throws IllegalArgumentException if the value falls outside the boundaries
     *
     */
    public static void inclusiveBetween(final long start, final long end, final long value, final String message) {
        // TODO when breaking BC, consider returning value
        if (value < start || value > end) {
            throw new IllegalArgumentException(message);
        }
    }
}
