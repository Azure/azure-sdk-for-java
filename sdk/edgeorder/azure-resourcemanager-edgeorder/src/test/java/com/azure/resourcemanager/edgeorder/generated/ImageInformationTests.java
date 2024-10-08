// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.edgeorder.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.edgeorder.models.ImageInformation;

public final class ImageInformationTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        ImageInformation model = BinaryData.fromString("{\"imageType\":\"BulletImage\",\"imageUrl\":\"zrnkcqvyxlwh\"}")
            .toObject(ImageInformation.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        ImageInformation model = new ImageInformation();
        model = BinaryData.fromObject(model).toObject(ImageInformation.class);
    }
}
