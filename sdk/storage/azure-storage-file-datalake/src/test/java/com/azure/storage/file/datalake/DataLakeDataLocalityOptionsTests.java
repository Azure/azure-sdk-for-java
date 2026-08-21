// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake;

import com.azure.core.util.Context;
import com.azure.storage.common.policy.DataLocalityPolicy;
import com.azure.storage.file.datalake.options.DataLakeFileInputStreamOptions;
import com.azure.storage.file.datalake.options.ReadToFileOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataLakeDataLocalityOptionsTests {
    @Test
    public void readToFileOptionsCarryDataLocalityEndpoint() {
        String endpoint = "https://layout.example.net:443";
        ReadToFileOptions options = new ReadToFileOptions("/path/to/file.txt").setDataLocalityEndpoint(endpoint);

        assertEquals(endpoint, options.getDataLocalityEndpoint());

        Context context = Transforms.addDataLocalityEndpoint(Context.NONE, options.getDataLocalityEndpoint());
        assertTrue(context.getData(DataLocalityPolicy.LAYOUT_ENDPOINT_KEY).isPresent());
        assertEquals(endpoint, context.getData(DataLocalityPolicy.LAYOUT_ENDPOINT_KEY).get());
    }

    @Test
    public void inputStreamOptionsCarryDataLocalityEndpoint() {
        String endpoint = "https://layout.example.net:443";
        DataLakeFileInputStreamOptions options = new DataLakeFileInputStreamOptions().setDataLocalityEndpoint(endpoint);

        assertEquals(endpoint, options.getDataLocalityEndpoint());

        Context context = Transforms.addDataLocalityEndpoint(Context.NONE, options.getDataLocalityEndpoint());
        assertTrue(context.getData(DataLocalityPolicy.LAYOUT_ENDPOINT_KEY).isPresent());
        assertEquals(endpoint, context.getData(DataLocalityPolicy.LAYOUT_ENDPOINT_KEY).get());
    }
}
