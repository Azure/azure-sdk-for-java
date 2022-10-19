// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import com.azure.core.util.logging.ClientLogger;
import org.slf4j.MDC;
import reactor.util.annotation.Nullable;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.HOSTNAME_ERROR;

public class HostName {

    private static final ClientLogger logger = new ClientLogger(HostName.class);

    /**
     * Returns the hostname using {@link InetAddress#getHostName()} on {@link
     * InetAddress#getLocalHost()}. If an error is encountered, the error is logged and it returns
     * null.
     *
     * @return the local hostname, or null
     */
    @Nullable
    @SuppressWarnings("try")
    public static String get() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (UnknownHostException ex) {
            try (MDC.MDCCloseable ignored = HOSTNAME_ERROR.makeActive()) {
                logger.warning("Error resolving hostname", ex);
            }
            return null;
        }
    }

    private HostName() {
    }
}
