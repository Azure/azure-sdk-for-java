// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.gremlin.common;

import com.azure.spring.data.gremlin.telemetry.MacAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MacAddressUnitTest {

    @Test
    public void testGetHashMacNormal() {
        Assertions.assertNotNull(MacAddress.getHashMac());
        Assertions.assertFalse(MacAddress.getHashMac().isEmpty());
        Assertions.assertFalse(MacAddress.isValidHashMacFormat(""));
        Assertions.assertTrue(MacAddress.isValidHashMacFormat(MacAddress.getHashMac()));
    }
}
