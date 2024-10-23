// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import com.azure.monitor.opentelemetry.exporter.implementation.utils.Constant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class NetworkStatsbeatTest {

    private NetworkStatsbeat networkStatsbeat;
    private static final String IKEY = "00000000-0000-0000-0000-0FEEDDADBEEF";
    private static final String FAKE_HOST = "fake-host";

    @BeforeEach
    public void init() {
        networkStatsbeat = new NetworkStatsbeat();
    }

    @Test
    public void testIncrementRequestSuccessCount() {
        assertThat(networkStatsbeat.getRequestSuccessCount(IKEY, FAKE_HOST)).isEqualTo(0);
        assertThat(networkStatsbeat.getRequestDurationAvg(IKEY, FAKE_HOST)).isEqualTo(0);
        networkStatsbeat.incrementRequestSuccessCount(1000, IKEY, FAKE_HOST);
        networkStatsbeat.incrementRequestSuccessCount(3000, IKEY, FAKE_HOST);
        assertThat(networkStatsbeat.getRequestSuccessCount(IKEY, FAKE_HOST)).isEqualTo(2);
        assertThat(networkStatsbeat.getRequestDurationAvg(IKEY, FAKE_HOST)).isEqualTo(2000.0);
    }

    @Test
    public void testIncrementRequestFailureCount() {
        int statusCode = 400;
        assertThat(networkStatsbeat.getRequestFailureCount(IKEY, FAKE_HOST, statusCode)).isEqualTo(0);
        networkStatsbeat.incrementRequestFailureCount(IKEY, FAKE_HOST, Constant.STATUS_CODE, statusCode);
        networkStatsbeat.incrementRequestFailureCount(IKEY, FAKE_HOST, Constant.STATUS_CODE, statusCode);
        assertThat(networkStatsbeat.getRequestFailureCount(IKEY, FAKE_HOST, statusCode)).isEqualTo(2);
    }

    @Test
    public void testIncrementRetryCount() {
        int statusCode = 500;
        assertThat(networkStatsbeat.getRetryCount(IKEY, FAKE_HOST, statusCode)).isEqualTo(0);
        networkStatsbeat.incrementRetryCount(IKEY, FAKE_HOST, Constant.STATUS_CODE, statusCode);
        networkStatsbeat.incrementRetryCount(IKEY, FAKE_HOST, Constant.STATUS_CODE, statusCode);
        assertThat(networkStatsbeat.getRetryCount(IKEY, FAKE_HOST, statusCode)).isEqualTo(2);
    }

    @Test
    public void testIncrementThrottlingCount() {
        int statusCode = 402;
        assertThat(networkStatsbeat.getThrottlingCount(IKEY, FAKE_HOST, statusCode)).isEqualTo(0);
        networkStatsbeat.incrementThrottlingCount(IKEY, FAKE_HOST, Constant.STATUS_CODE, statusCode);
        networkStatsbeat.incrementThrottlingCount(IKEY, FAKE_HOST, Constant.STATUS_CODE, statusCode);
        assertThat(networkStatsbeat.getThrottlingCount(IKEY, FAKE_HOST, statusCode)).isEqualTo(2);
    }

    @Test
    public void testIncrementExceptionCount() {
        String exceptionType = NullPointerException.class.getName();
        assertThat(networkStatsbeat.getExceptionCount(IKEY, FAKE_HOST, exceptionType)).isEqualTo(0);
        networkStatsbeat.incrementExceptionCount(IKEY, FAKE_HOST, Constant.EXCEPTION_TYPE, exceptionType);
        networkStatsbeat.incrementExceptionCount(IKEY, FAKE_HOST, Constant.EXCEPTION_TYPE, exceptionType);
        assertThat(networkStatsbeat.getExceptionCount(IKEY, FAKE_HOST, exceptionType)).isEqualTo(2);
    }

    @Test
    public void testRaceCondition() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 100; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j < 1000; j++) {
                        networkStatsbeat.incrementRequestSuccessCount(j % 2 == 0 ? 5 : 10, IKEY, FAKE_HOST);
                        networkStatsbeat.incrementRequestFailureCount(IKEY, FAKE_HOST, Constant.STATUS_CODE, 400);
                        networkStatsbeat.incrementRetryCount(IKEY, FAKE_HOST, Constant.STATUS_CODE, 500);
                        networkStatsbeat.incrementThrottlingCount(IKEY, FAKE_HOST, Constant.STATUS_CODE, 402);
                        networkStatsbeat.incrementExceptionCount(IKEY, FAKE_HOST, Constant.EXCEPTION_TYPE,
                            NullPointerException.class.getName());
                    }
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);
        assertThat(networkStatsbeat.getRequestSuccessCount(IKEY, FAKE_HOST)).isEqualTo(100000);
        assertThat(networkStatsbeat.getRequestFailureCount(IKEY, FAKE_HOST, 400)).isEqualTo(100000);
        assertThat(networkStatsbeat.getRetryCount(IKEY, FAKE_HOST, 500)).isEqualTo(100000);
        assertThat(networkStatsbeat.getThrottlingCount(IKEY, FAKE_HOST, 402)).isEqualTo(100000);
        assertThat(networkStatsbeat.getExceptionCount(IKEY, FAKE_HOST, NullPointerException.class.getName()))
            .isEqualTo(100000);
        assertThat(networkStatsbeat.getRequestDurationAvg(IKEY, FAKE_HOST)).isEqualTo(7.5);
    }

    @Test
    public void testGetHost() {
        String url = "https://fakehost-1.example.com/";
        assertThat(NetworkStatsbeat.shorten(url)).isEqualTo("fakehost-1");

        url = "https://fakehost-2.example.com/";
        assertThat(NetworkStatsbeat.shorten(url)).isEqualTo("fakehost-2");

        url = "http://www.fakehost-3.example.com/";
        assertThat(NetworkStatsbeat.shorten(url)).isEqualTo("fakehost-3");

        url = "http://www.fakehost.com/v2/track";
        assertThat(NetworkStatsbeat.shorten(url)).isEqualTo("fakehost");

        url = "https://www.fakehost0-4.com/";
        assertThat(NetworkStatsbeat.shorten(url)).isEqualTo("fakehost0-4");

        url = "https://www.fakehost-5.com";
        assertThat(NetworkStatsbeat.shorten(url)).isEqualTo("fakehost-5");

        url = "https://fakehost.com";
        assertThat(NetworkStatsbeat.shorten(url)).isEqualTo("fakehost");

        url = "http://fakehost-5/";
        assertThat(NetworkStatsbeat.shorten(url)).isEqualTo("fakehost-5");
    }
}
