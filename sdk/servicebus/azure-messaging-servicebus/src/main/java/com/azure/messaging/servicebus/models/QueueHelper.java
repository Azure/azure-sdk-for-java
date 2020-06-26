// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.models;

import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Used to access internal methods on {@link QueueDescription}.
 */
public final class QueueHelper {
    private static QueueAccessor accessor;

    static {
        try {
            Class.forName(QueueAccessor.class.getName(), true, QueueAccessor.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ClientLogger(QueueHelper.class).logThrowableAsError(new AssertionError(e));
        }
    }

    /**
     * Sets the queue name on a {@link QueueDescription}.
     *
     * @param queueDescription Queue to set name on.
     * @param name Name of the queue.
     */
    public static void setName(QueueDescription queueDescription, String name) {
        if (accessor == null) {
            throw new ClientLogger(QueueHelper.class).logExceptionAsError(
                new IllegalStateException("'QueueAccessor.accessor' should not be null."));
        }

        queueDescription.setName(name);
    }

    /**
     * Sets the queue accessor.
     *
     * @param accessor The queue accessor to set on the queue helper.
     */
    public static void setQueueAccessor(QueueAccessor accessor) {
        Objects.requireNonNull(accessor, "'accessor' cannot be null.");

        if (QueueHelper.accessor != null) {
            throw new ClientLogger(QueueHelper.class).logExceptionAsError(new IllegalStateException(
                "'accessor' is already set."));
        }

        QueueHelper.accessor = accessor;
    }

    /**
     * Interface for accessing methods on a queue.
     */
    public interface QueueAccessor {
        /**
         * Sets the queue helper.
         *
         * @param queueDescription The queue description.
         * @param entityHelper The entity helper that accesses methods.
         */
        void setHelper(QueueDescription queueDescription, QueueHelper entityHelper);

        /**
         * Sets the name on a queueDescription.
         *
         * @param queueDescription Queue to set name on.
         * @param name Name of the queue.
         */
        void setName(QueueDescription queueDescription, String name);
    }
}
