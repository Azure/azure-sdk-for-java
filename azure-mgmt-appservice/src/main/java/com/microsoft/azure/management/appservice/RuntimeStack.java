/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Fluent;

/**
 * Defines App service pricing tiers.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
public class RuntimeStack {
    /** Node.JS 6.9.3. */
    public static final RuntimeStack NODEJS_6_9_3 = new RuntimeStack("NODE", "6.9.3");

    /** Node.JS 6.6.0. */
    public static final RuntimeStack NODEJS_6_6_0 = new RuntimeStack("NODE", "6.6.0");

    /** Node.JS 6.2.2. */
    public static final RuntimeStack NODEJS_6_2_2 = new RuntimeStack("NODE", "6.2.2");

    /** Node.JS 4.5.0. */
    public static final RuntimeStack NODEJS_4_5_0 = new RuntimeStack("NODE", "4.5.0");

    /** Node.JS 4.4.7. */
    public static final RuntimeStack NODEJS_4_4_7 = new RuntimeStack("NODE", "4.4.7");

    /** PHP 5.6.23. */
    public static final RuntimeStack PHP_5_6_23 = new RuntimeStack("PHP", "5.6.23");

    /** PHP 7.0.6. */
    public static final RuntimeStack PHP_7_0_6 = new RuntimeStack("PHP", "7.0.6");

    /** .NET Core v1.0. */
    public static final RuntimeStack NETCORE_V1_0 = new RuntimeStack("DOTNETCORE", "v1.0");

    /** Ruby 2.3. */
    public static final RuntimeStack RUBY_2_3 = new RuntimeStack("RUBY", "2.3");

    /** The name of the language runtime stack. */
    private String stack;
    /** The version of the runtime. */
    private String version;

    /**
     * Creates a custom app service pricing tier.
     * @param stack the name of the language stack
     * @param version the version of the runtime
     */
    public RuntimeStack(String stack, String version) {
        this.stack = stack;
        this.version = version;
    }

    /**
     * @return the name of the language runtime stack
     */
    public String stack() {
        return stack;
    }

    /**
     * @return the version of the runtime stack
     */
    public String version() {
        return version;
    }

    @Override
    public String toString() {
        return stack + " " + version;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RuntimeStack)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        RuntimeStack rhs = (RuntimeStack) obj;
        return toString().equalsIgnoreCase(rhs.toString());
    }
}