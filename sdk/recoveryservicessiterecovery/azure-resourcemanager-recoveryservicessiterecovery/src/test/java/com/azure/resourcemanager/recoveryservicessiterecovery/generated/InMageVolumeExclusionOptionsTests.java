// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.recoveryservicessiterecovery.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.recoveryservicessiterecovery.models.InMageVolumeExclusionOptions;
import org.junit.jupiter.api.Assertions;

public final class InMageVolumeExclusionOptionsTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        InMageVolumeExclusionOptions model
            = BinaryData.fromString("{\"volumeLabel\":\"nsm\",\"onlyExcludeIfSingleVolume\":\"fiwjbctvbp\"}")
                .toObject(InMageVolumeExclusionOptions.class);
        Assertions.assertEquals("nsm", model.volumeLabel());
        Assertions.assertEquals("fiwjbctvbp", model.onlyExcludeIfSingleVolume());
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        InMageVolumeExclusionOptions model
            = new InMageVolumeExclusionOptions().withVolumeLabel("nsm").withOnlyExcludeIfSingleVolume("fiwjbctvbp");
        model = BinaryData.fromObject(model).toObject(InMageVolumeExclusionOptions.class);
        Assertions.assertEquals("nsm", model.volumeLabel());
        Assertions.assertEquals("fiwjbctvbp", model.onlyExcludeIfSingleVolume());
    }
}
