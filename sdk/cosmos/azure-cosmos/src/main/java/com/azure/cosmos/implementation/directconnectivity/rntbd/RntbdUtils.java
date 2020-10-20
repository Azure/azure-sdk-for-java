// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.Utils;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.SingleThreadEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RntbdUtils {

    private static final Logger logger = LoggerFactory.getLogger(RntbdUtils.class);

    public static int tryGetExecutorTaskQueueSize(EventExecutor eventLoop) {
        try {
            if (eventLoop == null) {
                return -1;
            }

            SingleThreadEventExecutor singleThreadEventExecutor = Utils.as(eventLoop,
                SingleThreadEventExecutor.class);

            if (singleThreadEventExecutor != null) {
                return singleThreadEventExecutor.pendingTasks();
            }
        } catch (RuntimeException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Unexpected failure in estimating eventloop [{}] task queue size", eventLoop.getClass().getName(), e);
            }
        }
        return -1;
    }
}
