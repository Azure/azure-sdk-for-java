/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import java.util.Collection;

import com.microsoft.azure.management.resources.fluentcore.arm.ExpandableStringEnum;

/**
 * Defines values for Java web container.
 */
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

    /** Static value tomcat 8.0 newest for WebContainer. */
    public static final WebContainer TOMCAT_8_5_NEWEST = WebContainer.fromString("tomcat 8.5");

    /** Static value tomcat 8.0.23 for WebContainer. */
    public static final WebContainer TOMCAT_8_5_6 = WebContainer.fromString("tomcat 8.5.6");

    /** Static value jetty 9.1 for WebContainer. */
    public static final WebContainer JETTY_9_1_NEWEST = WebContainer.fromString("jetty 9.1");

    /** Static value jetty 9.1.0 v20131115 for WebContainer. */
    public static final WebContainer JETTY_9_1_V20131115 = WebContainer.fromString("jetty 9.1.0.20131115");

    /** Static value jetty 9.3 for WebContainer. */
    public static final WebContainer JETTY_9_3_NEWEST = WebContainer.fromString("jetty 9.3");

    /** Static value jetty 9.3.13 v20161014 for WebContainer. */
    public static final WebContainer JETTY_9_3_V20161014 = WebContainer.fromString("jetty 9.3.13.20161014");

    /**
     * Finds or creates a Web container based on the specified name.
     * @param name a name
     * @return a WebContainer instance
     */
    public static WebContainer fromString(String name) {
        return fromString(name, WebContainer.class);
    }

    /**
     * @return known Web container types
     */
    public static Collection<WebContainer> values() {
        return values(WebContainer.class);
    }
}
