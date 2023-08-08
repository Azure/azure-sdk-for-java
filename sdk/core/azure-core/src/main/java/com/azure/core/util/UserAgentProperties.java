// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

/**
 * Class to hold the properties used in user agent strings.
 */
public class UserAgentProperties {

    private final String name;
    private final String version;

    /**
     * Creates the user agent properties with given name and version.
     *
     * @param name The name of the client library.
     * @param version The version of the client library.
     */
    UserAgentProperties(final String name, final String version) {
        this.name = name;
        this.version = version;
    }

    /**
     * Returns the name of the client library.
     *
     * @return the name of the client library.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the version of the client library.
     *
     * @return the version of the client library.
     */
    public String getVersion() {
        return version;
    }
}
