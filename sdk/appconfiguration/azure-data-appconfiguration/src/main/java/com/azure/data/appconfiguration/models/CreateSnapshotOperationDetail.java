// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Immutable;
import com.azure.data.appconfiguration.implementation.CreateSnapshotOperationDetailPropertiesHelper;

/**
 * The {@link CreateSnapshotOperationDetail} model.
 */
@Immutable
public final class CreateSnapshotOperationDetail {
    private String operationId;

    static {
        CreateSnapshotOperationDetailPropertiesHelper.setAccessor(
            new CreateSnapshotOperationDetailPropertiesHelper.CreateSnapshotOperationDetailAccessor() {
                @Override
                public void setOperationId(CreateSnapshotOperationDetail operationDetail, String operationId) {
                    operationDetail.setOperationId(operationId);
                }
            });
    }

    /**
     * Gets the operationId property of the {@link CreateSnapshotOperationDetail}.
     *
     * @return The operationId property of the {@link CreateSnapshotOperationDetail}.
     */
    public String getOperationId() {
        return operationId;
    }

    private void setOperationId(String operationId) {
        this.operationId = operationId;
    }
}
