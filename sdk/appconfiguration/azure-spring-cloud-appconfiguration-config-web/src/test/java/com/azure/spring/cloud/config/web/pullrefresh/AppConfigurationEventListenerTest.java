// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.web.pullrefresh;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.azure.spring.cloud.config.AppConfigurationRefresh;

public class AppConfigurationEventListenerTest {

    @Mock
    private AppConfigurationRefresh appConfigurationRefresh;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    
    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
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
