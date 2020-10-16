// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.implementation.guava25.base.Preconditions;

import java.util.List;

/**
 * Provides context about decryption operation.
 */
public class DecryptionContext {
    private final List<DecryptionInfo> DecryptionInfoList;

    /**
     * Gets the list of {@link DecryptionInfo} corresponding to the DataEncryptionKey(s) used.
     *
     * @return list of DecryptionInfos.
     */
    public List<DecryptionInfo> getDecryptionInfoList() {
        return DecryptionInfoList;
    }

    /**
     * Initializes a new instance of the {@link DecryptionContext} class.
     *
     * @param decryptionInfoList List of DecryptionInfo.
     */
    public DecryptionContext(List<DecryptionInfo> decryptionInfoList) {

        Preconditions.checkNotNull(decryptionInfoList, "decryptionInfoList");
        this.DecryptionInfoList = decryptionInfoList;
    }
}
