// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import java.util.Objects;

/** Defines web app runtime stack on Windows operating system. */
@Fluent
public class WebAppRuntimeStack {

    /** .NET Core. */
    public static final WebAppRuntimeStack NETCORE = new WebAppRuntimeStack("dotnetcore");

    /** .NET Framework. */
    public static final WebAppRuntimeStack NET = new WebAppRuntimeStack("dotnet");

    /** PHP. */
    public static final WebAppRuntimeStack PHP = new WebAppRuntimeStack("php");

    /** Python. */
    public static final WebAppRuntimeStack PYTHON = new WebAppRuntimeStack("python");

    /** Java. */
    public static final WebAppRuntimeStack JAVA = new WebAppRuntimeStack("java");

    private final String runtime;

    /**
     * Creates a custom web app runtime stack on Windows operating system.
     *
     * @param runtime the language runtime
     */
    public WebAppRuntimeStack(String runtime) {
        this.runtime = Objects.requireNonNull(runtime);
    }

    /** @return the name of the language runtime */
    public String runtime() {
        return runtime;
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
        WebAppRuntimeStack that = (WebAppRuntimeStack) o;
        return runtime.equals(that.runtime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(runtime);
    }
}
