/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Defines values for Java web container.
 */
public final class WebContainer {
    /** Static value tomcat 7.0 newest for WebContainer. */
    public static final WebContainer TOMCAT_7_0_NEWEST = new WebContainer("tomcat 7.0");

    /** Static value tomcat 7.0.50 for WebContainer. */
    public static final WebContainer TOMCAT_7_0_50 = new WebContainer("tomcat 7.0.50");

    /** Static value tomcat 7.0.62 for WebContainer. */
    public static final WebContainer TOMCAT_7_0_62 = new WebContainer("tomcat 7.0.62");

    /** Static value tomcat 8.0 newest for WebContainer. */
    public static final WebContainer TOMCAT_8_0_NEWEST = new WebContainer("tomcat 8.0");

    /** Static value tomcat 8.0.23 for WebContainer. */
    public static final WebContainer TOMCAT_8_0_23 = new WebContainer("tomcat 8.0.23");

    /** Static value tomcat 8.0 newest for WebContainer. */
    public static final WebContainer TOMCAT_8_5_NEWEST = new WebContainer("tomcat 8.5");

    /** Static value tomcat 8.0.23 for WebContainer. */
    public static final WebContainer TOMCAT_8_5_6 = new WebContainer("tomcat 8.5.6");

    /** Static value jetty 9.1 for WebContainer. */
    public static final WebContainer JETTY_9_1_NEWEST = new WebContainer("jetty 9.1");

    /** Static value jetty 9.1.0 v20131115 for WebContainer. */
    public static final WebContainer JETTY_9_1_V20131115 = new WebContainer("jetty 9.1.0.20131115");

    /** Static value jetty 9.3 for WebContainer. */
    public static final WebContainer JETTY_9_3_NEWEST = new WebContainer("jetty 9.3");

    /** Static value jetty 9.3.13 v20161014 for WebContainer. */
    public static final WebContainer JETTY_9_3_V20161014 = new WebContainer("jetty 9.3.13.20161014");

    private String value;

    /**
     * Creates a custom value for WebContainer.
     * @param value the custom value
     */
    public WebContainer(String value) {
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
        if (!(obj instanceof WebContainer)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        WebContainer rhs = (WebContainer) obj;
        if (value == null) {
            return rhs.value == null;
        } else {
            return value.equals(rhs.value);
        }
    }
}
