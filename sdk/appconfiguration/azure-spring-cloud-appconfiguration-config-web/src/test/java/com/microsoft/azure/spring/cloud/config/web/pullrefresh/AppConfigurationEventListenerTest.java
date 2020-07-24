// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.spring.cloud.config.web.pullrefresh;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.microsoft.azure.spring.cloud.config.AppConfigurationRefresh;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AppConfigurationEventListenerTest {

    @Mock
    private AppConfigurationRefresh appConfigurationRefresh;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void throwException() {
        AppConfigurationEventListener listener = new AppConfigurationEventListener(appConfigurationRefresh);
        doThrow(new RuntimeException("The listener should swallow all exceptions.")).when(appConfigurationRefresh)
            .refreshConfigurations();
        listener.onApplicationEvent(null);
    }

    @Test
    public void watchEnabledNotConfiguredShouldNotCreateWatch() {
        AppConfigurationEventListener listener = new AppConfigurationEventListener(appConfigurationRefresh);
        when(appConfigurationRefresh.refreshConfigurations()).thenReturn(null);
        listener.onApplicationEvent(null);
    }
}
