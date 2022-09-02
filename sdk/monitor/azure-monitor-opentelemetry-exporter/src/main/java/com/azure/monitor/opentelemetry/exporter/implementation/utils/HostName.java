/*
 * ApplicationInsights-Java
 * Copyright (c) Microsoft Corporation
 * All rights reserved.
 *
 * MIT License
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the ""Software""), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package com.azure.monitor.opentelemetry.exporter.implementation.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.annotation.Nullable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.HOSTNAME_ERROR;

public class HostName {

    private static final Logger logger = LoggerFactory.getLogger(HostName.class);

    private HostName() {
    }

    /**
     * Returns the hostname using {@link InetAddress#getHostName()} on {@link
     * InetAddress#getLocalHost()}. If an error is encountered, the error is logged and it returns
     * null.
     *
     * @return the local hostname, or null
     */
    @SuppressWarnings("try")
    @Nullable
    public static String get() {
        try {
            InetAddress addr = InetAddress.getLocalHost();
            return addr.getHostName();
        } catch (UnknownHostException ex) {
            try (MDC.MDCCloseable ignored = HOSTNAME_ERROR.makeActive()) {
                logger.warn("Error resolving hostname", ex);
            }
            return null;
        }
    }
}
