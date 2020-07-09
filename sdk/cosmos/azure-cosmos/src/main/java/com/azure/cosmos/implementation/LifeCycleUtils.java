// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LifeCycleUtils {
    private final static Logger logger = LoggerFactory.getLogger(LifeCycleUtils.class);
    public static void closeQuietly(AutoCloseable closeable) {
        try {
            if (closeable != null) {
                logger.debug("closing an instance of {}", closeable.getClass().getCanonicalName());
                closeable.close();
            }
        } catch (Exception e) {
            logger.warn("attempting to close an instance of {} failed", closeable.getClass().getCanonicalName(), e);
        }
    }

    public static void closeQuietly(HttpClient closeable) {
        try {
            if (closeable != null) {
                logger.debug("shutting down an instance of {}", closeable.getClass().getCanonicalName());
                closeable.shutdown();
            }
        } catch (Exception e) {
            logger.warn("attempting to shutdown an instance of {} failed", closeable.getClass().getCanonicalName(), e);
        }
    }
}
