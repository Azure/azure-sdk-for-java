// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.models;

import com.azure.core.annotation.Immutable;

/**
 * Defines an action to be included as part of a transactional operation.
 */
@Immutable
public final class TableTransactionAction {
    private final TableTransactionActionType actionType;
    private final TableEntity entity;
    private final boolean ifUnchanged;

    /**
     * Initializes a new instance of the {@link TableTransactionAction}.
     *
     * @param actionType The operation type to be applied to the {@code entity}.
     * @param entity The table entity to which the {@code actionType} will be applied.
     */
    public TableTransactionAction(TableTransactionActionType actionType, TableEntity entity) {
        this(actionType, entity, false);
    }

    /**
     * Initializes a new instance of the {@link TableTransactionAction}.
     *
     * @param actionType The operation type to be applied to the {@code entity}.
     * @param entity The table entity to which the {@code actionType} will be applied.
     * @param ifUnchanged When {@code true}, the ETag of the provided entity must match the ETag of the entity in the
     * Table service. If the values do not match, the action will not be performed and an exception will be thrown.
     * This value is only applied for update and delete actions.
     */
    public TableTransactionAction(TableTransactionActionType actionType, TableEntity entity, boolean ifUnchanged) {
        this.actionType = actionType;
        this.entity = entity;
        this.ifUnchanged = ifUnchanged;
    }

    /**
     * Get the {@link TableTransactionActionType operation type} to be applied to the {@link TableEntity entity}.
     *
     * @return The {@link TableTransactionActionType operation type}.
     */
    public TableTransactionActionType getActionType() {
        return actionType;
    }

    /**
     * Get the {@link TableEntity table entity} to which the {@code actionType} will be applied.
     *
     * @return The {@link TableEntity table entity} to which the {@code actionType} will be applied.
     */
    public TableEntity getEntity() {
        return entity;
    }

    /**
     * Get the {@code ifUnchanged} value of this action. When {@code true}, the ETag of the provided entity must match
     * the ETag of the entity in the Table service. If the values do not match, the action will not be performed
     * and an exception will be thrown. This value is only applied for update and delete actions.
     *
     * @return The {@code ifUnchanged} value of this action.
     */
    public boolean getIfUnchanged() {
        return ifUnchanged;
    }
}
