// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.implementation;

import com.azure.data.appconfiguration.models.CreateSnapshotOperationDetail;

/**
 * The helper class to set the non-public properties of an {@link CreateSnapshotOperationDetail} instance.
 */
public final class CreateSnapshotOperationDetailPropertiesHelper {
    private static CreateSnapshotOperationDetailAccessor accessor;

    private CreateSnapshotOperationDetailPropertiesHelper() { }
    /**
     * Type defining the methods to set the non-public properties of an {@link CreateSnapshotOperationDetail}
     * instance.
     */
    public interface CreateSnapshotOperationDetailAccessor {
        void setOperationId(CreateSnapshotOperationDetail operationDetail, String operationId);
    }

    /**
     * The method called from {@link CreateSnapshotOperationDetail} to set it's accessor.
     *
     * @param createSnapshotOperationDetailAccessor The accessor.
     */
    public static void setAccessor(
        final CreateSnapshotOperationDetailAccessor createSnapshotOperationDetailAccessor) {
        accessor = createSnapshotOperationDetailAccessor;
    }

    public static void setOperationId(CreateSnapshotOperationDetail operationDetail, String operationId) {
        accessor.setOperationId(operationDetail, operationId);
    }

}
