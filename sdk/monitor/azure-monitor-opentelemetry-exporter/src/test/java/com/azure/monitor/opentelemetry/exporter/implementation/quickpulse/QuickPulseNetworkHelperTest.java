// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.quickpulse;

import com.azure.core.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class QuickPulseNetworkHelperTest {
    @Test
    void testIsSuccessWith200() {
        HttpResponse response = mock(HttpResponse.class);
        Mockito.doReturn(200).when(response).getStatusCode();

        boolean result = new QuickPulseNetworkHelper().isSuccess(response);
        assertThat(result).isTrue();
    }

    @Test
    void testIsSuccessWith500() {
        HttpResponse response = mock(HttpResponse.class);
        Mockito.doReturn(500).when(response).getStatusCode();

        boolean result = new QuickPulseNetworkHelper().isSuccess(response);
        assertThat(result).isFalse();
    }
}
