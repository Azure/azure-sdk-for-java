// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake.implementation.util;

import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.file.datalake.DataLakeServiceVersion;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TransformUtilsTests {
    @ParameterizedTest
    @EnumSource(DataLakeServiceVersion.class)
    public void canTransformAllServiceVersions(DataLakeServiceVersion dataLakeServiceVersion) {
        BlobServiceVersion blobServiceVersion = TransformUtils.toBlobServiceVersion(dataLakeServiceVersion);

        assertNotNull(blobServiceVersion);
        assertEquals(dataLakeServiceVersion.getVersion(), blobServiceVersion.getVersion());
    }
}
