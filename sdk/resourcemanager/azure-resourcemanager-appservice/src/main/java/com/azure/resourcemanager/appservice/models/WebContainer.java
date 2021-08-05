// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/** Defines values for Java web container. */
public final class WebContainer extends ExpandableStringEnum<WebContainer> {
    /** Static value tomcat 7.0 newest for WebContainer. */
    public static final WebContainer TOMCAT_7_0_NEWEST = WebContainer.fromString("tomcat 7.0");

    /** Static value tomcat 7.0.50 for WebContainer. */
    public static final WebContainer TOMCAT_7_0_50 = WebContainer.fromString("tomcat 7.0.50");

    /** Static value tomcat 7.0.62 for WebContainer. */
    public static final WebContainer TOMCAT_7_0_62 = WebContainer.fromString("tomcat 7.0.62");

    /** Static value tomcat 8.0 newest for WebContainer. */
    public static final WebContainer TOMCAT_8_0_NEWEST = WebContainer.fromString("tomcat 8.0");

    /** Static value tomcat 8.0.23 for WebContainer. */
    public static final WebContainer TOMCAT_8_0_23 = WebContainer.fromString("tomcat 8.0.23");

    /** Static value tomcat 8.5 newest for WebContainer. */
    public static final WebContainer TOMCAT_8_5_NEWEST = WebContainer.fromString("tomcat 8.5");

    /** Static value tomcat 8.5.6 for WebContainer. */
    public static final WebContainer TOMCAT_8_5_6 = WebContainer.fromString("tomcat 8.5.6");

    /** Static value tomcat 8.5.20 for WebContainer. */
    public static final WebContainer TOMCAT_8_5_20 = WebContainer.fromString("tomcat 8.5.20");

    /** Static value tomcat 8.5.31 for WebContainer. */
    public static final WebContainer TOMCAT_8_5_31 = WebContainer.fromString("tomcat 8.5.31");

    /** Static value tomcat 8.5.34 for WebContainer. */
    public static final WebContainer TOMCAT_8_5_34 = WebContainer.fromString("tomcat 8.5.34");

    /** Static value tomcat 8.5.37 for WebContainer. */
    public static final WebContainer TOMCAT_8_5_37 = WebContainer.fromString("tomcat 8.5.37");

    /** Static value tomcat 9.0 newest for WebContainer. */
    public static final WebContainer TOMCAT_9_0_NEWEST = WebContainer.fromString("tomcat 9.0");

    /** Static value tomcat 9_0_0 for WebContainer. */
    public static final WebContainer TOMCAT_9_0_0 = WebContainer.fromString("tomcat 9.0.0");

    /** Static value tomcat 9_0_8 for WebContainer. */
    public static final WebContainer TOMCAT_9_0_8 = WebContainer.fromString("tomcat 9.0.8");

    /** Static value tomcat 9_0_12 for WebContainer. */
    public static final WebContainer TOMCAT_9_0_12 = WebContainer.fromString("tomcat 9.0.12");

    /** Static value tomcat 9_0_14 for WebContainer. */
    public static final WebContainer TOMCAT_9_0_14 = WebContainer.fromString("tomcat 9.0.14");

    /** Static value jetty 9.1 for WebContainer. */
    public static final WebContainer JETTY_9_1_NEWEST = WebContainer.fromString("jetty 9.1");

    /** Static value jetty 9.1.0 v20131115 for WebContainer. */
    public static final WebContainer JETTY_9_1_V20131115 = WebContainer.fromString("jetty 9.1.0.20131115");

    /** Static value jetty 9.3 for WebContainer. */
    public static final WebContainer JETTY_9_3_NEWEST = WebContainer.fromString("jetty 9.3");

    /** Static value jetty 9.3.13 v20161014 for WebContainer. */
    public static final WebContainer JETTY_9_3_V20161014 = WebContainer.fromString("jetty 9.3.13.20161014");

    /** Static value java 8 for WebContainer. */
    public static final WebContainer JAVA_8 = WebContainer.fromString("java 8");

    /**
     * Finds or creates a Web container based on the specified name.
     *
     * @param name a name
     * @return a WebContainer instance
     */
    public static WebContainer fromString(String name) {
        return fromString(name, WebContainer.class);
    }

    /** @return known Web container types */
    public static Collection<WebContainer> values() {
        return values(WebContainer.class);
    }
}
