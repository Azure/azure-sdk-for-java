// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

public enum LeaseVersion {

    PARTITION_KEY_BASED_LEASE(0),
    EPK_RANGE_BASED_LEASE(1);

    /**
     * NOTE: versionId will get stored in lease document.
     * DO NOT CHANGE IT.
     */
    private final int versionId;

    LeaseVersion(int versionId) {
        this.versionId = versionId;
    }

    public int getVersionId() {
        return versionId;
    }

    public static LeaseVersion fromVersionId(int version) {
        switch(version) {
            case 0:
                return PARTITION_KEY_BASED_LEASE;
            case 1:
                return EPK_RANGE_BASED_LEASE;
            default:
                throw new UnsupportedOperationException("Unsupported lease version {" + version + "}");
        }
    }
}
