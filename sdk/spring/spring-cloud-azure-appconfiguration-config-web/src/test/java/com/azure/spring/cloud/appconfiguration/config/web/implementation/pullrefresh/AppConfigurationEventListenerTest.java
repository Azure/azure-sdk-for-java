// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.web.implementation.pullrefresh;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.context.support.ServletRequestHandledEvent;

import com.azure.spring.cloud.appconfiguration.config.AppConfigurationRefresh;

import reactor.core.publisher.Mono;

@SuppressWarnings("deprecation")
public class AppConfigurationEventListenerTest {

    @Mock
    private AppConfigurationRefresh appConfigurationRefresh;

    @Mock
    private Mono<Boolean> refreshResult;

    @Captor
    private ArgumentCaptor<Consumer<? super Throwable>> errorHandler;

    private AutoCloseable mocks;

    @BeforeEach
    public void setup() {
        mocks = MockitoAnnotations.openMocks(this);
    }
    
    @AfterEach
    public void cleanup() throws Exception {
        mocks.close();
    }

    @Test
    public void handlesSynchronousFailure() {
        AppConfigurationEventListener listener = new AppConfigurationEventListener(appConfigurationRefresh);
        when(appConfigurationRefresh.refreshConfigurations()).thenThrow(new IllegalStateException("Refresh failed"));

        assertDoesNotThrow(() -> listener.onApplicationEvent(requestEvent("/test")));

        verify(appConfigurationRefresh).refreshConfigurations();
    }

    @Test
    public void subscribesToRefresh() {
        AtomicBoolean subscribed = new AtomicBoolean();
        AppConfigurationEventListener listener = new AppConfigurationEventListener(appConfigurationRefresh);
        when(appConfigurationRefresh.refreshConfigurations())
            .thenReturn(Mono.fromSupplier(() -> {
                subscribed.set(true);
                return false;
            }));

        listener.onApplicationEvent(requestEvent("/test"));

        assertTrue(subscribed.get());
    }

    @Test
    public void handlesAsynchronousFailure() throws Exception {
        AppConfigurationEventListener listener = new AppConfigurationEventListener(appConfigurationRefresh);
        when(appConfigurationRefresh.refreshConfigurations()).thenReturn(refreshResult);
        listener.onApplicationEvent(requestEvent("/test"));

        verify(refreshResult).subscribe(any(), errorHandler.capture());

        CompletableFuture.runAsync(() -> errorHandler.getValue()
            .accept(new IllegalStateException("Asynchronous refresh failed"))).get(5, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/actuator/appconfiguration-refresh", "/actuator/appconfiguration-refresh-bus"})
    public void skipsRefreshEndpoints(String requestUrl) {
        AppConfigurationEventListener listener = new AppConfigurationEventListener(appConfigurationRefresh);

        listener.onApplicationEvent(requestEvent(requestUrl));

        verifyNoInteractions(appConfigurationRefresh);
    }

    private ServletRequestHandledEvent requestEvent(String requestUrl) {
        return new ServletRequestHandledEvent(this, requestUrl, "127.0.0.1", "GET", "dispatcherServlet", null, null, 0);
    }
}
