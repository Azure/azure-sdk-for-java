// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.implementation.util;

import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.file.datalake.DataLakeServiceVersion;

public class TransformUtils {

    public static BlobServiceVersion toBlobServiceVersion(DataLakeServiceVersion version) {
        if (version == null) {
            return null;
        }
        if (DataLakeServiceVersion.V2019_12_12.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2019_12_12;
        } else if (DataLakeServiceVersion.V2019_07_07.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2019_07_07;
        } else if (DataLakeServiceVersion.V2019_02_02.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2019_02_02;
        } else if (DataLakeServiceVersion.V2020_02_10.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2020_02_10;
        } else if (DataLakeServiceVersion.V2020_04_08.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2020_04_08;
        } else if (DataLakeServiceVersion.V2020_06_12.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2020_06_12;
        } else if (DataLakeServiceVersion.V2020_08_04.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2020_08_04;
        } else if (DataLakeServiceVersion.V2020_10_02.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2020_10_02;
        }
        return null;
    }
}
