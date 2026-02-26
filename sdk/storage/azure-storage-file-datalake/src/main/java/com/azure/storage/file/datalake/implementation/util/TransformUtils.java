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

        switch (version) {
            case V2019_02_02:
                return BlobServiceVersion.V2019_02_02;

            case V2019_07_07:
                return BlobServiceVersion.V2019_07_07;

            case V2019_12_12:
                return BlobServiceVersion.V2019_12_12;

            case V2020_02_10:
                return BlobServiceVersion.V2020_02_10;

            case V2020_04_08:
                return BlobServiceVersion.V2020_04_08;

            case V2020_06_12:
                return BlobServiceVersion.V2020_06_12;

            case V2020_08_04:
                return BlobServiceVersion.V2020_08_04;

            case V2020_10_02:
                return BlobServiceVersion.V2020_10_02;

            case V2020_12_06:
                return BlobServiceVersion.V2020_12_06;

            case V2021_02_12:
                return BlobServiceVersion.V2021_02_12;

            case V2021_04_10:
                return BlobServiceVersion.V2021_04_10;

            case V2021_06_08:
                return BlobServiceVersion.V2021_06_08;

            case V2021_08_06:
                return BlobServiceVersion.V2021_08_06;

            case V2021_10_04:
                return BlobServiceVersion.V2021_10_04;

            case V2021_12_02:
                return BlobServiceVersion.V2021_12_02;

            case V2022_11_02:
                return BlobServiceVersion.V2022_11_02;

            case V2023_01_03:
                return BlobServiceVersion.V2023_01_03;

            case V2023_05_03:
                return BlobServiceVersion.V2023_05_03;

            case V2023_08_03:
                return BlobServiceVersion.V2023_08_03;

            case V2023_11_03:
                return BlobServiceVersion.V2023_11_03;

            case V2024_02_04:
                return BlobServiceVersion.V2024_02_04;

            case V2024_05_04:
                return BlobServiceVersion.V2024_05_04;

            case V2024_08_04:
                return BlobServiceVersion.V2024_08_04;

            case V2024_11_04:
                return BlobServiceVersion.V2024_11_04;

            case V2025_01_05:
                return BlobServiceVersion.V2025_01_05;

            case V2025_05_05:
                return BlobServiceVersion.V2025_05_05;

            case V2025_07_05:
                return BlobServiceVersion.V2025_07_05;

            case V2025_11_05:
                return BlobServiceVersion.V2025_11_05;

            case V2026_02_06:
                return BlobServiceVersion.V2026_02_06;

            case V2026_04_06:
                return BlobServiceVersion.V2026_04_06;

            case V2026_06_06:
                return BlobServiceVersion.V2026_06_06;

            default:
                return null;
        }
    }
}
