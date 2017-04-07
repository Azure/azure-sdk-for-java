/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for Java versions.
 */
public final class JavaVersion {
    /** Static value 'Off' for JavaVersion. */
    public static final JavaVersion OFF = new JavaVersion("null");

    /** Static value Java 7 newest for JavaVersion. */
    public static final JavaVersion JAVA_7_NEWEST = new JavaVersion("1.7");

    /** Static value 1.7.0_51 for JavaVersion. */
    public static final JavaVersion JAVA_1_7_0_51 = new JavaVersion("1.7.0_51");

    /** Static value 1.7.0_71 for JavaVersion. */
    public static final JavaVersion JAVA_1_7_0_71 = new JavaVersion("1.7.0_71");

    /** Static value Java 8 newest for JavaVersion. */
    public static final JavaVersion JAVA_8_NEWEST = new JavaVersion("1.8");

    /** Static value 1.8.0_25 for JavaVersion. */
    public static final JavaVersion JAVA_1_8_0_25 = new JavaVersion("1.8.0_25");

    /** Static value 1.8.0_60 for JavaVersion. */
    public static final JavaVersion JAVA_1_8_0_60 = new JavaVersion("1.8.0_60");

    /** Static value 1.8.0_73 for JavaVersion. */
    public static final JavaVersion JAVA_1_8_0_73 = new JavaVersion("1.8.0_73");

    /** Static value 1.8.0_111 for JavaVersion. */
    public static final JavaVersion JAVA_1_8_0_111 = new JavaVersion("1.8.0_111");

    /** Static value Zulu 1.8.0_92 for JavaVersion. */
    public static final JavaVersion JAVA_ZULU_1_8_0_92 = new JavaVersion("1.8.0_92");

    /** Static value Zulu 1.8.0_102 for JavaVersion. */
    public static final JavaVersion JAVA_ZULU_1_8_0_102 = new JavaVersion("1.8.0_102");

    private String value;

    /**
     * Creates a custom value for JavaVersion.
     * @param value the custom value
     */
    public JavaVersion(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JavaVersion)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        JavaVersion rhs = (JavaVersion) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
