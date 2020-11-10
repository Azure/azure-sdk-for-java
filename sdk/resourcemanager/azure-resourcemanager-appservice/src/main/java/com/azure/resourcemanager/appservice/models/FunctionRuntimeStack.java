// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import java.util.Objects;

/** Defines function app runtime for Linux operating system. */
@Fluent
public class FunctionRuntimeStack {

    /** JAVA 8. */
    public static final FunctionRuntimeStack JAVA_8 = new FunctionRuntimeStack("java", "~3",
        "java|8");

    /** JAVA 11. */
    public static final FunctionRuntimeStack JAVA_11 = new FunctionRuntimeStack("java", "~3",
        "java|11");

    private final String runtime;
    private final String version;

    private final String linuxFxVersion;

    /**
     * Creates a custom function app runtime stack.
     *
     * @param runtime the language runtime
     * @param version the language runtime version
     * @param linuxFxVersion the LinuxFxVersion property value
     */
    public FunctionRuntimeStack(
        String runtime,
        String version,
        String linuxFxVersion) {
        this.runtime = Objects.requireNonNull(runtime);
        this.version = Objects.requireNonNull(version);

        this.linuxFxVersion = Objects.requireNonNull(linuxFxVersion);
    }

    /** @return the name of the language runtime */
    public String runtime() {
        return runtime;
    }

    /** @return the version of the Language runtime */
    public String version() {
        return version;
    }

    /**
     * Gets LinuxFxVersion property value.
     *
     * @return the LinuxFxVersion property value for siteConfig
     */
    public String getLinuxFxVersion() {
        return linuxFxVersion;
    }

    @Override
    public String toString() {
        return runtime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FunctionRuntimeStack that = (FunctionRuntimeStack) o;
        return runtime.equals(that.runtime) && version.equals(that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(runtime, version);
    }
}
