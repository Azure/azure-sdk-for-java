// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import java.time.Duration;

/**
 * Constants used in Azure AMQP client.
 */
public final class ClientConstants {
    public static final String NOT_APPLICABLE = "n/a";
    public static final String PLATFORM_INFO = getOSInformation();
    public static final String FRAMEWORK_INFO = getFrameworkInfo();
    // Base sleep wait time.
    public static final Duration SERVER_BUSY_WAIT_TIME = Duration.ofSeconds(4);

    // Logging context keys
    public static final String CONNECTION_ID_KEY = "connectionId";
    public static final String LINK_NAME_KEY = "linkName";
    public static final String ENTITY_PATH_KEY = "entityPath";
    public static final String ENTITY_NAME_KEY = "entityName";
    public static final String UPDATED_LINK_CREDIT_KEY = "updatedLinkCredit";
    public static final String REMOTE_CREDIT_KEY = "remoteCredit";
    public static final String IS_PARTIAL_DELIVERY_KEY = "delivery.isPartial";
    public static final String IS_SETTLED_DELIVERY_KEY = "delivery.isSettled";
    public static final String SESSION_NAME_KEY = "sessionName";
    public static final String SESSION_ID_KEY = "sessionId";
    public static final String FULLY_QUALIFIED_NAMESPACE_KEY = "namespace";
    public static final String OPERATION_NAME_KEY = "amqpOperation";
    public static final String DELIVERY_KEY = "delivery";
    public static final String DELIVERY_STATE_KEY = "deliveryState";
    public static final String DELIVERY_TAG_KEY = "lockToken";
    public static final String ERROR_CONDITION_KEY = "errorCondition";
    public static final String ERROR_DESCRIPTION_KEY = "errorDescription";
    public static final String EMIT_RESULT_KEY = "emitResult";
    public static final String SIGNAL_TYPE_KEY = "signalType";
    public static final String HOSTNAME_KEY = "hostName";
    public static final String INTERVAL_KEY = "intervalMs";
    public static final String SUBSCRIBER_ID_KEY = "subscriberId";
    public static final String PUMP_ID_KEY = "pumpId";
    public static final String CALL_SITE_KEY = "callSite";

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

        return "jre:" + javaRuntimeClassPkg.getImplementationVersion() + ";vendor:"
            + javaRuntimeClassPkg.getImplementationVendor() + ";jvm" + System.getProperty("java.vm.version");
    }
}
