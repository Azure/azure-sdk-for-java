// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.administration.MetricsAdvisorAdministrationClient;
import com.azure.ai.metricsadvisor.administration.models.ListHookOptions;
import com.azure.ai.metricsadvisor.administration.models.NotificationHook;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.ai.metricsadvisor.TestUtils.DEFAULT_SUBSCRIBER_TIMEOUT_SECONDS;
import static com.azure.ai.metricsadvisor.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public final class NotificationHookTest extends NotificationHookTestBase {
    @BeforeAll
    static void beforeAll() {
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
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        NotificationHook createdNotificationHook = client.createHook(CreateEmailHookInput.INSTANCE.hook);
        Assertions.assertNotNull(createdNotificationHook);
        assertCreateEmailHookOutput(createdNotificationHook);
        client.deleteHook(createdNotificationHook.getId());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    @Override
    void createWebHook(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        NotificationHook createdNotificationHook = client.createHook(CreateWebHookInput.INSTANCE.hook);
        Assertions.assertNotNull(createdNotificationHook);
        assertCreateWebHookOutput(createdNotificationHook);
        client.deleteHook(createdNotificationHook.getId());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.metricsadvisor.TestUtils#getTestParameters")
    void testListHook(HttpClient httpClient, MetricsAdvisorServiceVersion serviceVersion) {
        MetricsAdvisorAdministrationClient client
            = getMetricsAdvisorAdministrationBuilder(httpClient, serviceVersion).buildClient();

        String[] hookId = new String[2];
        NotificationHook notificationHook1 = client.createHook(ListHookInput.INSTANCE.emailHook);
        hookId[0] = notificationHook1.getId();

        NotificationHook notificationHook2 = client.createHook(ListHookInput.INSTANCE.webHook);
        hookId[1] = notificationHook2.getId();

        Assertions.assertNotNull(hookId[0]);
        Assertions.assertNotNull(hookId[1]);

        List<NotificationHook> notificationHookList = client.listHooks(new ListHookOptions()
            .setHookNameFilter("java_test"), Context.NONE)
            .stream()
            .collect(Collectors.toList());

        assertListHookOutput(notificationHookList);

        List<PagedResponse<NotificationHook>> hookPageList
            = client.listHooks(new ListHookOptions()
            .setHookNameFilter("java_test")
            .setMaxPageSize(ListHookInput.INSTANCE.pageSize), Context.NONE)
            .streamByPage()
            .collect(Collectors.toList());

        assertPagedListHookOutput(hookPageList);

        client.deleteHook(hookId[0]);
        client.deleteHook(hookId[1]);
    }
}
