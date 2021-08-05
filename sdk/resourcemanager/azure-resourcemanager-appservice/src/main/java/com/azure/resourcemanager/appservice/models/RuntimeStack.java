// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import java.util.Collection;

/** Defines App service pricing tiers. */
@Fluent
public class RuntimeStack {
    private static final AttributeCollection<RuntimeStack> COLLECTION = new AttributeCollection<>();

    /** .NET Core v2.1. */
    public static final RuntimeStack NETCORE_V2_1 = COLLECTION.addValue(new RuntimeStack("DOTNETCORE", "2.1"));

    /** .NET Core v3.1. */
    public static final RuntimeStack NETCORE_V3_1 = COLLECTION.addValue(new RuntimeStack("DOTNETCORE", "3.1"));

    /** JAVA JRE 8. */
    public static final RuntimeStack JAVA_8_JRE8 = COLLECTION.addValue(new RuntimeStack("JAVA", "8-jre8"));

    /** JAVA JAVA 11. */
    public static final RuntimeStack JAVA_11_JAVA11 = COLLECTION.addValue(new RuntimeStack("JAVA", "11-java11"));

    /** Node.JS 10.1. */
    public static final RuntimeStack NODEJS_10_1 = COLLECTION.addValue(new RuntimeStack("NODE", "10.1"));

    /** Node.JS 10.6. */
    public static final RuntimeStack NODEJS_10_6 = COLLECTION.addValue(new RuntimeStack("NODE", "10.6"));

    /** Node.JS 10.14. */
    public static final RuntimeStack NODEJS_10_14 = COLLECTION.addValue(new RuntimeStack("NODE", "10.14"));

    /** Node.JS 10 LTS. */
    public static final RuntimeStack NODEJS_10_LTS = COLLECTION.addValue(new RuntimeStack("NODE", "10-lts"));

    /** Node.JS 12 LTS. */
    public static final RuntimeStack NODEJS_12_LTS = COLLECTION.addValue(new RuntimeStack("NODE", "12-lts"));

    /** PHP 7.2. */
    public static final RuntimeStack PHP_7_2 = COLLECTION.addValue(new RuntimeStack("PHP", "7.2"));

    /** PHP 7.3. */
    public static final RuntimeStack PHP_7_3 = COLLECTION.addValue(new RuntimeStack("PHP", "7.3"));

    /** PYTHON 3.6. */
    public static final RuntimeStack PYTHON_3_6 = COLLECTION.addValue(new RuntimeStack("PYTHON", "3.6"));

    /** PYTHON 3.7. */
    public static final RuntimeStack PYTHON_3_7 = COLLECTION.addValue(new RuntimeStack("PYTHON", "3.7"));

    /** PYTHON 3.8. */
    public static final RuntimeStack PYTHON_3_8 = COLLECTION.addValue(new RuntimeStack("PYTHON", "3.8"));

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

    /** JBOSS EAP 7.2-java8. */
    public static final RuntimeStack JBOSS_EAP_7_2_JAVA8 =
        COLLECTION.addValue(new RuntimeStack("JBOSSEAP", "7.2-java8"));

    /** JBOSS EAP 7-java8. */
    public static final RuntimeStack JBOSS_EAP_7_JAVA8 =
        COLLECTION.addValue(new RuntimeStack("JBOSSEAP", "7-java8"));

    /** JBOSS EAP 7-java11. */
    public static final RuntimeStack JBOSS_EAP_7_JAVA11 =
        COLLECTION.addValue(new RuntimeStack("JBOSSEAP", "7-java11"));

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
