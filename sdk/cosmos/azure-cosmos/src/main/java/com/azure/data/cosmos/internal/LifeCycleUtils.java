// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

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
}
