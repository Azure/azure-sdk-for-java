// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils.configuration;

/**
 * Configuration property source which provides configuration values from a specific place. Samples may include
 * properties file supported by frameworks or other source.
 */
public interface ConfigurationSource {
    /**
     * Gets the property with the given {@code name}. If the property doesn't exist this will return null.
     * <p>
     * Example:
     * <p>
     * With following configuration properties:
     * <ul>
     *   <li>foo = 1</li>
     *   <li>bar = 2</li>
     * </ul>
     * <p>
     * {@link ConfigurationSource} implementation must the following behavior:
     * <ul>
     *   <li>{@code getProperty(null} throws {@link NullPointerException}</li>
     *   <li>{@code getProperties("foo")} must return {@code 1}</li>
     *   <li>{@code getProperties("bar")} must return {@code 2}</li>
     *   <li>{@code getProperties("baz")} must return null</li>
     * </ul>
     *
     * @param name Name of the property.
     * @return The value of the property or null if the property doesn't exist.
     * @throws NullPointerException If {@code name} is null.
     */
    String getProperty(String name);

    /**
     * Flag indicating whether the configuration source is mutable.
     * <p>
     * If the configuration source is mutable, {@link Configuration} instances using this source won't cache the values
     * and will always call {@link #getProperty(String)} to get the value.
     *
     * @return Whether the configuration source is mutable.
     */
    boolean isMutable();
}
