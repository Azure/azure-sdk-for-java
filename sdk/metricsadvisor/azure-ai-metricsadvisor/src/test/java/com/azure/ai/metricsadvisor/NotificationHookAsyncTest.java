// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationAsyncClient;
import com.azure.ai.metricsadvisor.administration.models.ListHookOptions;
import com.azure.ai.metricsadvisor.administration.models.NotificationHook;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.test.TestBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.azure.ai.metricsadvisor.TestUtils.DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public final class NotificationHookAsyncTest extends NotificationHookTestBase {
    @BeforeAll
    static void beforeAll() {
        TestBase.setupClass();
        StepVerifier.setDefaultTimeout(Duration.ofSeconds(DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS));
    }

    @AfterAll
    static void afterAll() {
        StepVerifier.resetDefaultTimeout();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    void createEmailHook(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

        Mono<NotificationHook> createHookMono  = client.createHook(CreateEmailHookInput.INSTANCE.hook);
        String[] hookId = new String[1];
        Assertions.assertNotNull(createHookMono);
        StepVerifier.create(createHookMono)
            .assertNext(hook -> {
                assertCreateEmailHookOutput(hook);
                hookId[0] = hook.getId();
            })
            .verifyComplete();

        Mono<Void> deleteHookMono = client.deleteHook(hookId[0]);

        StepVerifier.create(deleteHookMono)
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    void createWebHook(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

        Mono<NotificationHook> createHookMono  = client.createHook(CreateWebHookInput.INSTANCE.hook);
        String[] hookId = new String[1];
        Assertions.assertNotNull(createHookMono);
        StepVerifier.create(createHookMono)
            .assertNext(hook -> {
                assertCreateWebHookOutput(hook);
                hookId[0] = hook.getId();
            })
            .verifyComplete();

        Mono<Void> deleteHookMono = client.deleteHook(hookId[0]);

        StepVerifier.create(deleteHookMono)
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListHook(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationAsyncClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildAsyncClient();

        String[] hookId = new String[2];
        StepVerifier.create(client.createHook(ListHookInput.INSTANCE.emailHook))
            .consumeNextWith(hook -> {
                hookId[0] = hook.getId();
            })
            .verifyComplete();
        StepVerifier.create(client.createHook(ListHookInput.INSTANCE.webHook))
            .consumeNextWith(hook -> {
                hookId[1] = hook.getId();
            })
            .verifyComplete();

        Assertions.assertNotNull(hookId[0]);
        Assertions.assertNotNull(hookId[1]);

        List<NotificationHook> notificationHookList = new ArrayList<>();
        StepVerifier.create(client.listHooks(new ListHookOptions().setHookNameFilter("java_test")))
            .thenConsumeWhile(notificationHookList::add)
            .verifyComplete();

        assertListHookOutput(notificationHookList);

        List<PagedResponse<NotificationHook>> hookPageList = new ArrayList<>();
        StepVerifier.create(client.listHooks(new ListHookOptions()
            .setHookNameFilter("java_test")
            .setMaxPageSize(ListHookInput.INSTANCE.pageSize)).byPage())
            .thenConsumeWhile(hookPageList::add)
            .verifyComplete();

        assertPagedListHookOutput(hookPageList);

        StepVerifier.create(client.deleteHook(hookId[0]))
            .verifyComplete();
        StepVerifier.create(client.deleteHook(hookId[1]))
            .verifyComplete();
    }
}
