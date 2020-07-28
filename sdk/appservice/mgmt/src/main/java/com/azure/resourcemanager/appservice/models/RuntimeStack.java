// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import java.util.Collection;

/** Defines App service pricing tiers. */
@Fluent
public class RuntimeStack {
    private static final AttributeCollection<RuntimeStack> COLLECTION = new AttributeCollection<>();

    /** .NET Core v1.0. */
    public static final RuntimeStack NETCORE_V1_0 = COLLECTION.addValue(new RuntimeStack("DOTNETCORE", "1.0"));

    /** .NET Core v1.1. */
    public static final RuntimeStack NETCORE_V1_1 = COLLECTION.addValue(new RuntimeStack("DOTNETCORE", "1.1"));

    /** .NET Core v2.0. */
    public static final RuntimeStack NETCORE_V2_0 = COLLECTION.addValue(new RuntimeStack("DOTNETCORE", "2.0"));

    /** .NET Core v2.1. */
    public static final RuntimeStack NETCORE_V2_1 = COLLECTION.addValue(new RuntimeStack("DOTNETCORE", "2.1"));

    /** .NET Core v2.2. */
    public static final RuntimeStack NETCORE_V2_2 = COLLECTION.addValue(new RuntimeStack("DOTNETCORE", "2.2"));

    /** JAVA JRE 8. */
    public static final RuntimeStack JAVA_8_JRE8 = COLLECTION.addValue(new RuntimeStack("JAVA", "8-jre8"));

    /** JAVA JAVA 11. */
    public static final RuntimeStack JAVA_11_JAVA11 = COLLECTION.addValue(new RuntimeStack("JAVA", "11-java11"));

    /** Node.JS 4.4. */
    public static final RuntimeStack NODEJS_4_4 = COLLECTION.addValue(new RuntimeStack("NODE", "4.4"));

    /** Node.JS 4.5. */
    public static final RuntimeStack NODEJS_4_5 = COLLECTION.addValue(new RuntimeStack("NODE", "4.5"));

    /** Node.JS 4.8. */
    public static final RuntimeStack NODEJS_4_8 = COLLECTION.addValue(new RuntimeStack("NODE", "4.8"));

    /** Node.JS 6.2. */
    public static final RuntimeStack NODEJS_6_2 = COLLECTION.addValue(new RuntimeStack("NODE", "6.2"));

    /** Node.JS 6.6. */
    public static final RuntimeStack NODEJS_6_6 = COLLECTION.addValue(new RuntimeStack("NODE", "6.6"));

    /** Node.JS 6.9. */
    public static final RuntimeStack NODEJS_6_9 = COLLECTION.addValue(new RuntimeStack("NODE", "6.9"));

    /** Node.JS 6.10. */
    public static final RuntimeStack NODEJS_6_10 = COLLECTION.addValue(new RuntimeStack("NODE", "6.10"));

    /** Node.JS 6.11. */
    public static final RuntimeStack NODEJS_6_11 = COLLECTION.addValue(new RuntimeStack("NODE", "6.11"));

    /** Node.JS 6 LTS. */
    public static final RuntimeStack NODEJS_6_LTS = COLLECTION.addValue(new RuntimeStack("NODE", "6-lts"));

    /** Node.JS 8.0. */
    public static final RuntimeStack NODEJS_8_0 = COLLECTION.addValue(new RuntimeStack("NODE", "8.0"));

    /** Node.JS 8.1. */
    public static final RuntimeStack NODEJS_8_1 = COLLECTION.addValue(new RuntimeStack("NODE", "8.1"));

    /** Node.JS 8.2. */
    public static final RuntimeStack NODEJS_8_2 = COLLECTION.addValue(new RuntimeStack("NODE", "8.2"));

    /** Node.JS 8.8. */
    public static final RuntimeStack NODEJS_8_8 = COLLECTION.addValue(new RuntimeStack("NODE", "8.8"));

    /** Node.JS 8.9. */
    public static final RuntimeStack NODEJS_8_9 = COLLECTION.addValue(new RuntimeStack("NODE", "8.9"));

    /** Node.JS 8.11. */
    public static final RuntimeStack NODEJS_8_11 = COLLECTION.addValue(new RuntimeStack("NODE", "8.11"));

    /** Node.JS 8.12. */
    public static final RuntimeStack NODEJS_8_12 = COLLECTION.addValue(new RuntimeStack("NODE", "8.12"));

    /** Node.JS 8 LTS. */
    public static final RuntimeStack NODEJS_8_LTS = COLLECTION.addValue(new RuntimeStack("NODE", "8-lts"));

    /** Node.JS 9.4. */
    public static final RuntimeStack NODEJS_9_4 = COLLECTION.addValue(new RuntimeStack("NODE", "9.4"));

    /** Node.JS 10.1. */
    public static final RuntimeStack NODEJS_10_1 = COLLECTION.addValue(new RuntimeStack("NODE", "10.1"));

    /** Node.JS 10.10. */
    public static final RuntimeStack NODEJS_10_10 = COLLECTION.addValue(new RuntimeStack("NODE", "10.10"));

    /** Node.JS 10.12. */
    public static final RuntimeStack NODEJS_10_12 = COLLECTION.addValue(new RuntimeStack("NODE", "10.12"));

    /** Node.JS 10.14. */
    public static final RuntimeStack NODEJS_10_14 = COLLECTION.addValue(new RuntimeStack("NODE", "10.14"));

    /** Node.JS 10 LTS. */
    public static final RuntimeStack NODEJS_10_LTS = COLLECTION.addValue(new RuntimeStack("NODE", "10-lts"));

    /** Node.JS lts. */
    public static final RuntimeStack NODEJS_LTS = COLLECTION.addValue(new RuntimeStack("NODE", "lts"));

    /** PHP 5.6. */
    public static final RuntimeStack PHP_5_6 = COLLECTION.addValue(new RuntimeStack("PHP", "5.6"));

    /** PHP 7.0. */
    public static final RuntimeStack PHP_7_0 = COLLECTION.addValue(new RuntimeStack("PHP", "7.0"));

    /** PHP 7.2. */
    public static final RuntimeStack PHP_7_2 = COLLECTION.addValue(new RuntimeStack("PHP", "7.2"));

    /** PHP 7.3. */
    public static final RuntimeStack PHP_7_3 = COLLECTION.addValue(new RuntimeStack("PHP", "7.3"));

    /** PYTHON 2.7. */
    public static final RuntimeStack PYTHON_2_7 = COLLECTION.addValue(new RuntimeStack("PYTHON", "2.7"));

    /** PYTHON 3.6. */
    public static final RuntimeStack PYTHON_3_6 = COLLECTION.addValue(new RuntimeStack("PYTHON", "3.6"));

    /** PYTHON 3.7. */
    public static final RuntimeStack PYTHON_3_7 = COLLECTION.addValue(new RuntimeStack("PYTHON", "3.7"));

    /** RUBY 2.3. */
    public static final RuntimeStack RUBY_2_3 = COLLECTION.addValue(new RuntimeStack("RUBY", "2.3"));

    /** RUBY 2.4. */
    public static final RuntimeStack RUBY_2_4 = COLLECTION.addValue(new RuntimeStack("RUBY", "2.4"));

    /** RUBY 2.5. */
    public static final RuntimeStack RUBY_2_5 = COLLECTION.addValue(new RuntimeStack("RUBY", "2.5"));

    /** RUBY 2.6. */
    public static final RuntimeStack RUBY_2_6 = COLLECTION.addValue(new RuntimeStack("RUBY", "2.6"));

    /** Tomcat 8.5-java11 image with catalina root set to Azure wwwroot. */
    public static final RuntimeStack TOMCAT_8_5_JAVA11 = COLLECTION.addValue(new RuntimeStack("TOMCAT", "8.5-java11"));

    /** Tomcat 8.5-jre8 image with catalina root set to Azure wwwroot. */
    public static final RuntimeStack TOMCAT_8_5_JRE8 = COLLECTION.addValue(new RuntimeStack("TOMCAT", "8.5-jre8"));

    /** Tomcat 9.0-java11 image with catalina root set to Azure wwwroot. */
    public static final RuntimeStack TOMCAT_9_0_JAVA11 = COLLECTION.addValue(new RuntimeStack("TOMCAT", "9.0-java11"));

    /** Tomcat 9.0-jre8 image with catalina root set to Azure wwwroot. */
    public static final RuntimeStack TOMCAT_9_0_JRE8 = COLLECTION.addValue(new RuntimeStack("TOMCAT", "9.0-jre8"));

    /** The name of the language runtime stack. */
    private final String stack;
    /** The version of the runtime. */
    private final String version;

    /**
     * Creates a custom app service runtime stack on Linux operating system.
     *
     * @param stack the name of the language stack
     * @param version the version of the runtime
     */
    public RuntimeStack(String stack, String version) {
        this.stack = stack;
        this.version = version;
    }

    /** @return the name of the language runtime stack */
    public String stack() {
        return stack;
    }

    /** @return the version of the runtime stack */
    public String version() {
        return version;
    }

    /**
     * Lists the pre-defined app service runtime stacks.
     *
     * @return immutable collection of the pre-defined app service runtime stacks
     */
    public static Collection<RuntimeStack> getAll() {
        return COLLECTION.getAllValues();
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
