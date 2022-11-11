// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.storage.common.StorageChecksumAlgorithm;

public class DownloadChecksumRequest {
    private final Boolean md5;
    private final Boolean crc64;

    public DownloadChecksumRequest(StorageChecksumAlgorithm algorithm) {
        algorithm = ChecksumUtils.resolveAuto(algorithm);
        if (algorithm == null) {
            md5 = null;
            crc64 = null;
        } else {
            switch (algorithm) {
                case MD5:
                    md5 = true;
                    crc64 = null;
                    break;
                case StorageCrc64:
                    crc64 = true;
                    md5 = null;
                    break;
                case None:
                    crc64 = null;
                    md5 = null;
                    break;
                default:
                    throw new IllegalArgumentException(String.format(
                        "DownloadChecksumRequest does not support the given algorithm %s.", algorithm.name()));
            }
        }
    }

    public Boolean requestMd5() {
        return md5;
    }

    public Boolean requestCrc64() {
        return crc64;
    }
}
