// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.logging;

/**
 * Fluent logging API interface.
 */
public interface ServiceLoggerAPI {
    ServiceLoggerAPI withStackTrace(Throwable throwable);
    void log(String message);
    void log(String message, Object val);

    /**
     * Noop logger.
     */
    class NoopServiceLogger implements ServiceLoggerAPI {
        public ServiceLoggerAPI withStackTrace(Throwable throwable) {
            return this;
        }

        public void log(String message) {
        }

        public void log(String message, Object val) {
        }
    }
}
