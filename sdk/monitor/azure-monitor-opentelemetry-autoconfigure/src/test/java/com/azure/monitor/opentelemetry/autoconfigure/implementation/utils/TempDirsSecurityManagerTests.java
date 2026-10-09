// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.util.logging.ClientLogger;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.MockHttpResponse;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.localstorage.LocalStorageStats;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.pipeline.TelemetryItemExporter;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.statsbeat.StatsbeatModule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Path;
import java.security.Permission;
import java.util.PropertyPermission;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@Isolated("Changes the SecurityManager and the temporary directory")
@EnabledForJreRange(max = JRE.JAVA_17)
@SuppressWarnings("removal")
class TempDirsSecurityManagerTests {

    private static final ClientLogger LOGGER = new ClientLogger(TempDirsSecurityManagerTests.class);
    private static final String CONNECTION_STRING
        = "InstrumentationKey=00000000-0000-0000-0000-000000000000;IngestionEndpoint=https://example.invalid/";

    @TempDir
    Path tempDir;

    private String previousTempDir;
    private SecurityManager previousSecurityManager;

    @BeforeEach
    void setUp() {
        previousTempDir = System.getProperty("java.io.tmpdir");
        previousSecurityManager = System.getSecurityManager();
        System.setProperty("java.io.tmpdir", tempDir.toString());
    }

    @AfterEach
    void tearDown() {
        System.setSecurityManager(previousSecurityManager);
        System.setProperty("java.io.tmpdir", previousTempDir);
    }

    @ParameterizedTest
    @CsvSource({
        "getFileStoreAttributes,root",
        "accessUserInformation,root",
        "getFileStoreAttributes,telemetry",
        "accessUserInformation,telemetry",
        "getFileStoreAttributes,statsbeat",
        "accessUserInformation,statsbeat" })
    void shouldKeepExportingWhenStoragePermissionIsDenied(String deniedPermission, String stage) {
        File applicationDir = TempDirs.getApplicationInsightsTempDir(LOGGER, "test");
        assertThat(applicationDir).isDirectory();
        AtomicInteger requests = new AtomicInteger();
        AtomicInteger responseCode = new AtomicInteger(200);
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            requests.incrementAndGet();
            return Mono.just(new MockHttpResponse(request, responseCode.get()));
        }).build();
        StatsbeatModule statsbeat = mock(StatsbeatModule.class);
        AtomicInteger denials = new AtomicInteger();
        TelemetryItemExporter exporter = null;
        try {
            System.setSecurityManager(new SecurityManager() {
                @Override
                public void checkPermission(Permission permission) {
                    if (permission instanceof RuntimePermission && deniedPermission.equals(permission.getName())) {
                        denials.incrementAndGet();
                        throw new SecurityException("Denied " + deniedPermission);
                    }
                    if (previousSecurityManager != null) {
                        previousSecurityManager.checkPermission(permission);
                    }
                }
            });

            if ("root".equals(stage)) {
                applicationDir = TempDirs.getApplicationInsightsTempDir(LOGGER, "test");
                assertThat(applicationDir).isNull();
            }
            exporter = "statsbeat".equals(stage)
                ? AzureMonitorHelper.createStatsbeatTelemetryItemExporter(pipeline, statsbeat, applicationDir)
                : AzureMonitorHelper.createTelemetryItemExporter(pipeline, statsbeat, applicationDir,
                    LocalStorageStats.noop(), null);
            assertThat(denials.get()).isPositive();
            System.setSecurityManager(previousSecurityManager);

            assertThat(exporter
                .sendWithoutTracking(singletonList(TestUtils.createMetricTelemetry("test", 1, CONNECTION_STRING)))
                .join(10, SECONDS)
                .isSuccess()).isTrue();
            responseCode.set(503);
            assertThat(exporter
                .sendWithoutTracking(singletonList(TestUtils.createMetricTelemetry("test", 1, CONNECTION_STRING)))
                .join(10, SECONDS)
                .isSuccess()).isFalse();
            assertThat(requests.get()).isEqualTo(2);
            assertThat(tempDir.resolve("applicationinsights").resolve("telemetry")).doesNotExist();
            assertThat(tempDir.resolve("applicationinsights").resolve("statsbeat")).doesNotExist();
        } finally {
            System.setSecurityManager(previousSecurityManager);
            if (exporter != null) {
                exporter.shutdown().join(10, SECONDS);
            }
        }
    }

    @Test
    void shouldDisableStorageWhenTempPropertyAccessIsDenied() {
        System.setSecurityManager(new SecurityManager() {
            @Override
            public void checkPermission(Permission permission) {
                if (permission instanceof PropertyPermission && "java.io.tmpdir".equals(permission.getName())) {
                    throw new SecurityException("Denied temporary directory property access");
                }
                if (previousSecurityManager != null) {
                    previousSecurityManager.checkPermission(permission);
                }
            }
        });

        assertThat(TempDirs.getApplicationInsightsTempDir(LOGGER, "test")).isNull();
    }
}
