// Original file from https://github.com/FasterXML/jackson-core under Apache-2.0 license.
package com.azure.json.implementation.jackson.core.util;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value class used with some {@link com.azure.json.implementation.jackson.core.PrettyPrinter}
 * implements
 *
 * @see com.fasterxml.jackson.core.util.DefaultPrettyPrinter
 * @see com.fasterxml.jackson.core.util.MinimalPrettyPrinter
 *
 * @since 2.9
 */
public class Separators implements Serializable {
    private static final long serialVersionUID = 1;

    /**
     * Constant that specifies default "root-level" separator to use between
     * root values: a single space character.
     *
     * @since 2.16
     */
    public final static String DEFAULT_ROOT_VALUE_SEPARATOR = " ";

    /**
     * String to use in empty Object to separate start and end markers.
     * Default is single space, resulting in output of {@code { }}.
     * 
     * @since 2.17
     */
    public final static String DEFAULT_OBJECT_EMPTY_SEPARATOR = " ";

    /**
     * String to use in empty Array to separate start and end markers.
     * Default is single space, resulting in output of {@code [ ]}.
     * 
     * @since 2.17
     */
    public final static String DEFAULT_ARRAY_EMPTY_SEPARATOR = " ";

    /**
     * Define the spacing around elements like commas and colons.
     * 
     * @since 2.16
     */
    public enum Spacing {
        NONE("", ""), BEFORE(" ", ""), AFTER("", " "), BOTH(" ", " ");

        private final String spacesBefore;
        private final String spacesAfter;

        private Spacing(String spacesBefore, String spacesAfter) {
            this.spacesBefore = spacesBefore;
            this.spacesAfter = spacesAfter;
        }

        public String spacesBefore() {
            return spacesBefore;
        }

        public String spacesAfter() {
            return spacesAfter;
        }

        public String apply(char separator) {
            return spacesBefore + separator + spacesAfter;
        }
    }

    private final char objectFieldValueSeparator;
    private final Spacing objectFieldValueSpacing;
    private final char objectEntrySeparator;
    private final Spacing objectEntrySpacing;
    private final String objectEmptySeparator;
    private final char arrayValueSeparator;
    private final Spacing arrayValueSpacing;
    private final String arrayEmptySeparator;
    private final String rootSeparator;

    public static Separators createDefaultInstance() {
        return new Separators();
    }

    /**
     * Constructor for creating an instance with default settings for all
     * separators.
     */
    public Separators() {
        this(':', ',', ',');
    }

    /**
     * Create an instance with the specified separator characters. There will be spaces before and
     * after the <code>objectFieldValueSeparator</code> and none around the other two.
     */
    public Separators(char objectFieldValueSeparator, char objectEntrySeparator, char arrayValueSeparator) {
        this(DEFAULT_ROOT_VALUE_SEPARATOR, objectFieldValueSeparator, Spacing.BOTH, objectEntrySeparator, Spacing.NONE,
            DEFAULT_OBJECT_EMPTY_SEPARATOR, arrayValueSeparator, Spacing.NONE, DEFAULT_ARRAY_EMPTY_SEPARATOR);
    }

    /**
     * Create an instance with the specified separator characters and spaces around those characters.
     * 
     * @since 2.16
     *
     * @deprecated Since 2.17 use new canonical constructor
     */
    @Deprecated // since 2.17
    public Separators(String rootSeparator, char objectFieldValueSeparator, Spacing objectFieldValueSpacing,
        char objectEntrySeparator, Spacing objectEntrySpacing, char arrayValueSeparator, Spacing arrayValueSpacing) {
        this(rootSeparator, objectFieldValueSeparator, objectFieldValueSpacing, objectEntrySeparator,
            objectEntrySpacing, DEFAULT_OBJECT_EMPTY_SEPARATOR, arrayValueSeparator, arrayValueSpacing,
            DEFAULT_ARRAY_EMPTY_SEPARATOR);
    }

    /**
     * Canonical constructor for creating an instance with the specified separator
     * characters and spaces around those characters.
     *
     * @since 2.17
     */
    public Separators(String rootSeparator, char objectFieldValueSeparator, Spacing objectFieldValueSpacing,
        char objectEntrySeparator, Spacing objectEntrySpacing, String objectEmptySeparator, char arrayValueSeparator,
        Spacing arrayValueSpacing, String arrayEmptySeparator) {
        this.rootSeparator = rootSeparator;
        this.objectFieldValueSeparator = objectFieldValueSeparator;
        this.objectFieldValueSpacing = objectFieldValueSpacing;
        this.objectEntrySeparator = objectEntrySeparator;
        this.objectEntrySpacing = objectEntrySpacing;
        this.objectEmptySeparator = objectEmptySeparator;
        this.arrayValueSeparator = arrayValueSeparator;
        this.arrayValueSpacing = arrayValueSpacing;
        this.arrayEmptySeparator = arrayEmptySeparator;
    }

    public Separators withRootSeparator(String sep) {
        return Objects.equals(rootSeparator, sep)
            ? this
            : new Separators(sep, objectFieldValueSeparator, objectFieldValueSpacing, objectEntrySeparator,
                objectEntrySpacing, objectEmptySeparator, arrayValueSeparator, arrayValueSpacing, arrayEmptySeparator);
    }

    public Separators withObjectFieldValueSeparator(char sep) {
        return (objectFieldValueSeparator == sep)
            ? this
            : new Separators(rootSeparator, sep, objectFieldValueSpacing, objectEntrySeparator, objectEntrySpacing,
                objectEmptySeparator, arrayValueSeparator, arrayValueSpacing, arrayEmptySeparator);
    }

    /**
     * @return This instance (for call chaining)
     *
     * @since 2.16
     */
    public Separators withObjectFieldValueSpacing(Spacing spacing) {
        return (objectFieldValueSpacing == spacing)
            ? this
            : new Separators(rootSeparator, objectFieldValueSeparator, spacing, objectEntrySeparator,
                objectEntrySpacing, objectEmptySeparator, arrayValueSeparator, arrayValueSpacing, arrayEmptySeparator);
    }

    public Separators withObjectEntrySeparator(char sep) {
        return (objectEntrySeparator == sep)
            ? this
            : new Separators(rootSeparator, objectFieldValueSeparator, objectFieldValueSpacing, sep, objectEntrySpacing,
                objectEmptySeparator, arrayValueSeparator, arrayValueSpacing, arrayEmptySeparator);
    }

    /**
     * @return This instance (for call chaining)
     *
     * @since 2.16
     */
    public Separators withObjectEntrySpacing(Spacing spacing) {
        return (objectEntrySpacing == spacing)
            ? this
            : new Separators(rootSeparator, objectFieldValueSeparator, objectFieldValueSpacing, objectEntrySeparator,
                spacing, objectEmptySeparator, arrayValueSeparator, arrayValueSpacing, arrayEmptySeparator);
    }

    /**
     * @return This instance (for call chaining)
     *
     * @since 2.17
     */
    public Separators withObjectEmptySeparator(String sep) {
        return Objects.equals(objectEmptySeparator, sep)
            ? this
            : new Separators(rootSeparator, objectFieldValueSeparator, objectFieldValueSpacing, objectEntrySeparator,
                objectEntrySpacing, sep, arrayValueSeparator, arrayValueSpacing, arrayEmptySeparator);
    }

    public Separators withArrayValueSeparator(char sep) {
        return (arrayValueSeparator == sep)
            ? this
            : new Separators(rootSeparator, objectFieldValueSeparator, objectFieldValueSpacing, objectEntrySeparator,
                objectEntrySpacing, objectEmptySeparator, sep, arrayValueSpacing, arrayEmptySeparator);
    }

    /**
     * @return This instance (for call chaining)
     *
     * @since 2.16
     */
    public Separators withArrayValueSpacing(Spacing spacing) {
        return (arrayValueSpacing == spacing)
            ? this
            : new Separators(rootSeparator, objectFieldValueSeparator, objectFieldValueSpacing, objectEntrySeparator,
                objectEntrySpacing, objectEmptySeparator, arrayValueSeparator, spacing, arrayEmptySeparator);
    }

    /**
     * @return This instance (for call chaining)
     *
     * @since 2.17
     */
    public Separators withArrayEmptySeparator(String sep) {
        return Objects.equals(arrayEmptySeparator, sep)
            ? this
            : new Separators(rootSeparator, objectFieldValueSeparator, objectFieldValueSpacing, objectEntrySeparator,
                objectEntrySpacing, objectEmptySeparator, arrayValueSeparator, arrayValueSpacing, sep);
    }

    /**
     * @return String used as Root value separator
     *
     * @since 2.16
     */
    public String getRootSeparator() {
        return rootSeparator;
    }

    public char getObjectFieldValueSeparator() {
        return objectFieldValueSeparator;
    }

    /**
     * @return {@link Spacing} to use for Object fields
     * 
     * @since 2.16
     */
    public Spacing getObjectFieldValueSpacing() {
        return objectFieldValueSpacing;
    }

    public char getObjectEntrySeparator() {
        return objectEntrySeparator;
    }

    /**
     * @return {@link Spacing} to use for Object entries
     *
     * @since 2.16
     */
    public Spacing getObjectEntrySpacing() {
        return objectEntrySpacing;
    }

    /**
     * @return String to use in empty Object
     * 
     * @since 2.17
     */
    public String getObjectEmptySeparator() {
        return objectEmptySeparator;
    }

    public char getArrayValueSeparator() {
        return arrayValueSeparator;
    }

    /**
     * @return {@link Spacing} to use between Array values
     *
     * @since 2.16
     */
    public Spacing getArrayValueSpacing() {
        return arrayValueSpacing;
    }

    /**
     * @return String to use in empty Array
     * 
     * @since 2.17
     */
    public String getArrayEmptySeparator() {
        return arrayEmptySeparator;
    }
}
