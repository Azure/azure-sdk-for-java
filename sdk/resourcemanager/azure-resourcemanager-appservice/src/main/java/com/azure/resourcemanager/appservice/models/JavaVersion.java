// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for Java versions. */
public final class JavaVersion extends ExpandableStringEnum<JavaVersion> {
    /** Static value 'Off' for JavaVersion. */
    public static final JavaVersion OFF = fromString("null");

    /** Static value Java 7 newest for JavaVersion. */
    public static final JavaVersion JAVA_7_NEWEST = fromString("1.7");

    /** Static value 1.7.0_51 for JavaVersion. */
    public static final JavaVersion JAVA_1_7_0_51 = fromString("1.7.0_51");

    /** Static value 1.7.0_71 for JavaVersion. */
    public static final JavaVersion JAVA_1_7_0_71 = fromString("1.7.0_71");

    /** Static value 1.7.0_80 for JavaVersion. */
    public static final JavaVersion JAVA_1_7_0_80 = fromString("1.7.0_80");

    /** Static value 1.7.0_191_ZULU for JavaVersion. */
    public static final JavaVersion JAVA_ZULU_1_7_0_191 = fromString("1.7.0_191_ZULU");

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

    /** Static value 1.8.0_144 for JavaVersion. */
    public static final JavaVersion JAVA_1_8_0_144 = fromString("1.8.0_144");

    /** Static value 1.8.0_172 for JavaVersion. */
    public static final JavaVersion JAVA_1_8_0_172 = fromString("1.8.0_172");

    /** Static value 1.8.0_172_ZULU for JavaVersion. */
    public static final JavaVersion JAVA_ZULU_1_8_0_172 = fromString("1.8.0_172_ZULU");

    /** Static value Zulu 1.8.0_92 for JavaVersion. */
    public static final JavaVersion JAVA_ZULU_1_8_0_92 = fromString("1.8.0_92");

    /** Static value Zulu 1.8.0_102 for JavaVersion. */
    public static final JavaVersion JAVA_ZULU_1_8_0_102 = fromString("1.8.0_102");

    /** Static value Zulu 1.8.0_181 for JavaVersion. */
    public static final JavaVersion JAVA_1_8_0_181 = fromString("1.8.0_181");

    /** Static value Zulu 1.8.0_181_ZULU for JavaVersion. */
    public static final JavaVersion JAVA_ZULU_1_8_0_181 = fromString("1.8.0_181_ZULU");

    /** Static value Zulu 1.8.0_202 for JavaVersion. */
    public static final JavaVersion JAVA_1_8_0_202 = fromString("1.8.0_202");

    /** Static value Zulu 1.8.0_202_ZULU for JavaVersion. */
    public static final JavaVersion JAVA_ZULU_1_8_0_202 = fromString("1.8.0_202_ZULU");

    /** Static value Zulu 11 for JavaVersion. */
    public static final JavaVersion JAVA_11 = fromString("11");

    /** Static value Zulu 11.0.2_ZULU for JavaVersion. */
    public static final JavaVersion JAVA_ZULU_11_0_2 = fromString("11.0.2_ZULU");

    /**
     * Finds or creates a Java version value based on the provided name.
     *
     * @param name a name
     * @return a JavaVersion instance
     */
    public static JavaVersion fromString(String name) {
        return fromString(name, JavaVersion.class);
    }

    /** @return known Java versions */
    public static Collection<JavaVersion> values() {
        return values(JavaVersion.class);
    }
}
