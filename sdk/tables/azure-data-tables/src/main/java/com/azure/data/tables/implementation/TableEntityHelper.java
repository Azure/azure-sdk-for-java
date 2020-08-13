// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.data.tables.models.TableEntity;

import java.util.Objects;

/**
 * Used to access internal methods on models.
 */
public final class TableEntityHelper {
    private static EntityAccessor entityAccessor;

    static {
        // Force initialise this class.
        try {
            Class.forName(EntityAccessor.class.getName(), true, EntityAccessor.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new ClientLogger(TableEntityHelper.class).logThrowableAsError(new AssertionError(e));
        }
    }

    /**
     * Sets the entity accessor.
     *
     * @param accessor The entity accessor.
     * @throws IllegalStateException if the accessor has already been set.
     */
    public static void setEntityAccessor(EntityAccessor accessor) {
        Objects.requireNonNull(accessor, "'accessor' cannot be null.");

        if (TableEntityHelper.entityAccessor != null) {
            throw new ClientLogger(TableEntityHelper.class).logExceptionAsError(new IllegalStateException(
                "'entityAccessor' is already set."));
        }

        entityAccessor = accessor;
    }

    /**
     * Sets the ETag on an {@link TableEntity}.
     *
     * @param entity Entity to set the ETag on.
     * @param eTag ETag to set.
     */
    public static void setETag(TableEntity entity, String eTag) {
        if (entityAccessor == null) {
            throw new ClientLogger(TableEntityHelper.class).logExceptionAsError(
                new IllegalStateException("'entityAccessor' should not be null."));
        }

        entityAccessor.setETag(entity, eTag);
    }

    public interface EntityAccessor {
        /**
         * Sets the ETag on an {@link TableEntity}.
         *
         * @param entity Entity to set the ETag on.
         * @param eTag ETag to set.
         */
        void setETag(TableEntity entity, String eTag);
    }
}
