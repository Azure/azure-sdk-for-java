// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx;

import com.azure.cosmos.implementation.routing.UInt128;
import org.testng.annotations.Test;

import java.nio.ByteBuffer;

import static org.assertj.core.api.Assertions.assertThat;

public class UInt128Test {

    @Test(groups = "unit")
    public void fromByteBuffer() {
        /*
        Convert a UInt128 to byteBuffer and get it back and validate.
         */
        UInt128 uInt128 = new UInt128(10L, 20L);
        ByteBuffer byteBuffer = uInt128.toByteBuffer();
        UInt128 fromBB = new UInt128(byteBuffer);
        assertThat(fromBB.getLow()).isEqualTo(10L);
        assertThat(fromBB.getHigh()).isEqualTo(20L);
    }
}
