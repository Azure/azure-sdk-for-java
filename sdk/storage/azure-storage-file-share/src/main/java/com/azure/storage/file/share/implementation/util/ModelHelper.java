// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.implementation.util;

import com.azure.storage.file.share.implementation.models.DeleteSnapshotsOptionType;
import com.azure.storage.file.share.models.ShareSnapshotsDeleteOptionType;

public class ModelHelper {

    /**
     * Converts an internal type to a public type.
     *
     * @param option {@link ShareSnapshotsDeleteOptionType}
     * @return {@link DeleteSnapshotsOptionType}
     */
    public static DeleteSnapshotsOptionType toDeleteSnapshotsOptionType(ShareSnapshotsDeleteOptionType option) {
        if (option == null) {
            return null;
        }
        switch (option) {
            case INCLUDE:
                return DeleteSnapshotsOptionType.INCLUDE;
            case INCLUDE_WITH_LEASED:
                return DeleteSnapshotsOptionType.INCLUDE_LEASED;
            default:
                throw new IllegalArgumentException("Invalid " + option.getClass());
        }
    }
}
