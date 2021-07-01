// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import java.time.Duration;

public final class ClientConstants {
    public static final String NOT_APPLICABLE = "n/a";
    public static final String PLATFORM_INFO = getOSInformation();
    public static final String FRAMEWORK_INFO = getFrameworkInfo();
    // Base sleep wait time.
    public static final Duration SERVER_BUSY_WAIT_TIME = Duration.ofSeconds(4);

    /**
     * The default maximum allowable size, in bytes, for a batch to be sent.
     */
    static final int MAX_MESSAGE_LENGTH_BYTES = 256 * 1024;
    static final int MAX_AMQP_HEADER_SIZE_BYTES = 512;
    static final int SERVER_BUSY_BASE_SLEEP_TIME_IN_SECS = 4;

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
