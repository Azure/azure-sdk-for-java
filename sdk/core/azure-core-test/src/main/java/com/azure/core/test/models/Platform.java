// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.models;

import com.azure.core.util.CoreUtils;

import java.util.Locale;
import java.util.Objects;

/**
 * The class provides platform information which run on pipeline matrix.
 */
public class Platform {
    private final String os;
    private final String jdk;

    /**
     * Constructor of {@link Platform}.
     *
     * @param os Operation system.
     * @param jdk JDK version.
     */
    public Platform(final String os, final String jdk) {
        Objects.requireNonNull(os);
        Objects.requireNonNull(jdk);
        this.os = os;
        this.jdk = jdk;
    }

    /**
     * Get operating system information.
     *
     * @return The operating system.
     */
    public String getOs() {
        return this.os;
    }

    /**
     * Get JDK version information.
     *
     * @return The JDK version.
     */
    public String getJdk() {
        return this.jdk;
    }

    public boolean equals(Platform other) {
        if (other == null) {
            return false;
        }
        if (other.getJdk() == null || other.getOs() == null) {
            return false;
        }
        return other.getJdk().trim().toLowerCase(Locale.ROOT).contains(this.getJdk().toLowerCase(Locale.ROOT))
            && other.getOs().trim().toLowerCase(Locale.ROOT).contains(this.getOs().toLowerCase(Locale.ROOT));
    }
}

