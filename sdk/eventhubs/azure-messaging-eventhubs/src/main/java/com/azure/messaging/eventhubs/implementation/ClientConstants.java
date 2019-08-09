// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation;

import java.time.Duration;

public final class ClientConstants {
    public static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(60);
    public static final String NOT_APPLICABLE = "n/a";
    public static final int MAX_EVENTHUB_AMQP_HEADER_SIZE_BYTES = 512;
    public static final Duration TOKEN_VALIDITY = Duration.ofMinutes(20);
    public static final int SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS = 4;

    public static final String PRODUCT_NAME = "azsdk-java-eventhubs";
    public static final String CURRENT_JAVA_CLIENT_VERSION = "1.0.0-preview.2";
    public static final String PLATFORM_INFO = getOSInformation();
    public static final String FRAMEWORK_INFO = getFrameworkInfo();

    /**
     * Gets the USER AGENT string as defined in:
     * $/core/azure-core/src/main/java/com/azure/core/http/policy/UserAgentPolicy.java
     * TODO (conniey): Extract logic from UserAgentPolicy into something we can use here.
     */
    public static final String USER_AGENT = String.format("%s/%s %s;%s",
        PRODUCT_NAME, CURRENT_JAVA_CLIENT_VERSION, System.getProperty("java.version"), PLATFORM_INFO);
    public static final String ENDPOINT_FORMAT = "sb://%s.%s";

    private ClientConstants() {
    }

    private static String getOSInformation() {
        return String.join(" ", System.getProperty("os.name"), System.getProperty("os.version"));
    }

    private static String getFrameworkInfo() {
        final Package javaRuntimeClassPkg = Runtime.class.getPackage();

        return "jre:" + javaRuntimeClassPkg.getImplementationVersion()
            + ";vendor:" + javaRuntimeClassPkg.getImplementationVendor()
            + ";jvm" + System.getProperty("java.vm.version");
    }
}
