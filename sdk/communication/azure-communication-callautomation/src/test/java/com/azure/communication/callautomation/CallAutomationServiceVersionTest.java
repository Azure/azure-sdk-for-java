// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import org.junit.jupiter.api.Test;

import static com.azure.communication.callautomation.CallAutomationServiceVersion.V2023_03_06;
import static com.azure.communication.callautomation.CallAutomationServiceVersion.V2023_10_15;
import static org.junit.jupiter.api.Assertions.*;

class CallAutomationServiceVersionTest {

    @Test
    void getVersion() {
        CallAutomationServiceVersion callAutomationServiceVersion = V2023_03_06;
        assertEquals("2023-03-06", callAutomationServiceVersion.getVersion());
        callAutomationServiceVersion = V2023_10_15;
        assertEquals("2023-10-15", callAutomationServiceVersion.getVersion());
    }

    @Test
    void getLatest() {
        assertEquals(V2023_10_15, CallAutomationServiceVersion.getLatest());
    }
}
