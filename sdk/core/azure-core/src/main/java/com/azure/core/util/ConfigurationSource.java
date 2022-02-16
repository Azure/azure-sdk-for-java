package com.azure.core.util;

import java.util.Map;

/**
 * Configuration property source which provides configuration values from a specific place. Samples may include
 * properties file supported by frameworks or other source.
 *
 * Note that environment configuration (environment variables and system properties) are supported by default and
 * don't need a source implementation.
 */
public interface ConfigurationSource {
    /**
     * Returns all properties (name and value) which names start with given path.
     * Null (or empty) path indicate that all properties should be returned.
     *
     * For example, if following properties are defined:
     *   azure.sdk.foo = 1
     *   azure.sdk.bar.baz = 2
     *
     * source implementation should support following behavior:
     *  - {@code getProperties("azure.sdk")} must return both properties
     *  - {@code getProperties("azure.sdk.foo")} must return {"azure.sdk.foo", "1"}
     *  - {@code getProperties("azure.sdk.ba")} must return empty map
     *
     * @param path propery name
     * @return
     */
    Map<String, String> getProperties(String path);
}
