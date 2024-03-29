// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.hybridnetwork.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.hybridnetwork.models.ImageArtifactProfile;
import org.junit.jupiter.api.Assertions;

public final class ImageArtifactProfileTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        ImageArtifactProfile model
            = BinaryData.fromString("{\"imageName\":\"avmqfoudor\",\"imageVersion\":\"gyyprotwy\"}")
                .toObject(ImageArtifactProfile.class);
        Assertions.assertEquals("avmqfoudor", model.imageName());
        Assertions.assertEquals("gyyprotwy", model.imageVersion());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        ImageArtifactProfile model
            = new ImageArtifactProfile().withImageName("avmqfoudor").withImageVersion("gyyprotwy");
        model = BinaryData.fromObject(model).toObject(ImageArtifactProfile.class);
        Assertions.assertEquals("avmqfoudor", model.imageName());
        Assertions.assertEquals("gyyprotwy", model.imageVersion());
    }
}
