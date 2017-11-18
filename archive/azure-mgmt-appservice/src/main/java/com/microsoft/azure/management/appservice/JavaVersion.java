/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import java.util.Collection;

import com.microsoft.azure.management.resources.fluentcore.arm.ExpandableStringEnum;

/**
 * Defines values for Java versions.
 */
public final class JavaVersion extends ExpandableStringEnum<JavaVersion> {
    /** Static value 'Off' for JavaVersion. */
    public static final JavaVersion OFF = fromString("null");

    /** Static value Java 7 newest for JavaVersion. */
    public static final JavaVersion JAVA_7_NEWEST = fromString("1.7");

    /** Static value 1.7.0_51 for JavaVersion. */
    public static final JavaVersion JAVA_1_7_0_51 = fromString("1.7.0_51");

    /** Static value 1.7.0_71 for JavaVersion. */
    public static final JavaVersion JAVA_1_7_0_71 = fromString("1.7.0_71");

    /** Static value Java 8 newest for JavaVersion. */
    public static final JavaVersion JAVA_8_NEWEST = fromString("1.8");

    /** Static value 1.8.0_25 for JavaVersion. */
    public static final JavaVersion JAVA_1_8_0_25 = fromString("1.8.0_25");

    /** Static value 1.8.0_60 for JavaVersion. */
    public static final JavaVersion JAVA_1_8_0_60 = fromString("1.8.0_60");

    /** Static value 1.8.0_73 for JavaVersion. */
    public static final JavaVersion JAVA_1_8_0_73 = fromString("1.8.0_73");

    /** Static value 1.8.0_111 for JavaVersion. */
    public static final JavaVersion JAVA_1_8_0_111 = fromString("1.8.0_111");

    /** Static value Zulu 1.8.0_92 for JavaVersion. */
    public static final JavaVersion JAVA_ZULU_1_8_0_92 = fromString("1.8.0_92");

    /** Static value Zulu 1.8.0_102 for JavaVersion. */
    public static final JavaVersion JAVA_ZULU_1_8_0_102 = fromString("1.8.0_102");

    /**
     * Finds or creates a Java version value based on the provided name.
     * @param name a name
     * @return a JavaVersion instance
     */
    public static JavaVersion fromString(String name) {
        return fromString(name, JavaVersion.class);
    }

    /**
     * @return known Java versions
     */
    public static Collection<JavaVersion> values() {
        return values(JavaVersion.class);
    }
}
