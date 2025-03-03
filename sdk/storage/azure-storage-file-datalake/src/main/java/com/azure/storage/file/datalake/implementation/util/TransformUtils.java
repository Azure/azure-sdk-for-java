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
        } else if (DataLakeServiceVersion.V2020_12_06.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2020_12_06;
        } else if (DataLakeServiceVersion.V2021_02_12.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2021_02_12;
        } else if (DataLakeServiceVersion.V2021_04_10.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2021_04_10;
        } else if (DataLakeServiceVersion.V2021_06_08.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2021_06_08;
        } else if (DataLakeServiceVersion.V2021_08_06.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2021_08_06;
        } else if (DataLakeServiceVersion.V2021_10_04.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2021_10_04;
        } else if (DataLakeServiceVersion.V2021_12_02.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2021_12_02;
        } else if (DataLakeServiceVersion.V2022_11_02.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2022_11_02;
        } else if (DataLakeServiceVersion.V2023_01_03.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2023_01_03;
        } else if (DataLakeServiceVersion.V2023_05_03.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2023_05_03;
        } else if (DataLakeServiceVersion.V2023_08_03.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2023_08_03;
        } else if (DataLakeServiceVersion.V2023_11_03.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2023_11_03;
        } else if (DataLakeServiceVersion.V2024_02_04.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2024_02_04;
        } else if (DataLakeServiceVersion.V2024_05_04.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2024_05_04;
        } else if (DataLakeServiceVersion.V2024_08_04.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2024_08_04;
        } else if (DataLakeServiceVersion.V2024_11_04.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2024_11_04;
        } else if (DataLakeServiceVersion.V2025_01_05.ordinal() == version.ordinal()) {
            return BlobServiceVersion.V2025_01_05;
        }

        return null;
    }
}
